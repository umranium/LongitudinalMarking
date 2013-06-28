/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui.pdftext;

import com.umranium.longmark.common.Constants;
import com.umranium.longmark.common.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
 *
 * @author Umran
 */
public class PdfText {

    private File pdfFile;
    private int numberOfPages;
    private Map<Integer, TextLocationMap> pageTextLocMaps;
    private Map<Integer, PageSizing> pageSizingMap;
    private PDDocument document;
    private List<PDPage> pages;

    public PdfText(File pdf) {
        this.pdfFile = pdf;
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public List<PDPage> getPages() {
        return pages;
    }
    
    public void initExtraction() throws IOException {
        PrintWriter out = Log.out();

        if (Constants.DEBUG_DATA) {
            out.println("Extracting From: " + pdfFile);
        }
        this.document = PDDocument.load(pdfFile.getAbsoluteFile());
        this.numberOfPages = document.getNumberOfPages();
        this.pageTextLocMaps = new HashMap<Integer, TextLocationMap>(numberOfPages);
        this.pageSizingMap = new HashMap<Integer, PageSizing>(numberOfPages);
        if (document.isEncrypted()) {
            try {
                document.decrypt("");
            } catch (InvalidPasswordException e) {
                out.println("Error: Document is encrypted with a password.");
                e.printStackTrace(out);
            } catch (CryptographyException ex) {
                out.println("Error: Document is encrypted with a password.");
                ex.printStackTrace(out);
            }
        }
        this.pages = document.getDocumentCatalog().getAllPages();
    }

    public PDDocument getDocument() {
        return document;
    }
    
    public void finalizeExtraction() {
        if (document != null) {
            pages = null;
            try {
                document.close();
            } catch (IOException ex) {
            }
            document = null;
        }
    }

    public void extract(int pageIndex) throws IOException {
        final PrintWriter out = Log.out();
        
        if (Constants.DEBUG_DATA) {
            out.println("\tExtracting Page: " + pageIndex);
        }

        PDPage page = pages.get(pageIndex);

        PDRectangle mediaBox = page.findMediaBox();
        float pdfX1 = mediaBox.getLowerLeftX();
        float pdfY1 = mediaBox.getHeight() - mediaBox.getUpperRightY();
        float pdfCX = mediaBox.getWidth();
        float pdfCY = mediaBox.getHeight();
        
        Integer rotation = page.getRotation();
        if (rotation!=null) {
            switch (rotation) {
                case 0:
                    break;
                case 90:
                {
                    float tmp = pdfCX;
                    pdfCX = pdfCY;
                    pdfCY = tmp;
                    break;
                }
                default:
                    throw new RuntimeException("Found PDF with rotation = "+
                            rotation+", currently unable to deal with these PDFs");
            }
        }

        //System.out.println("pdf.mediaBox="+pdfX1+"\t"+pdfY1+"\t"+pdfCX+"\t"+pdfCY+"\t"+rotation);
        
        PDStream contents = page.getContents();
        final TextLocationMap locationMap = new TextLocationMap(false, false, true);
        if (contents != null) {
            PDFTextStripper textStripper = new PDFTextStripper() {
                @Override
                protected void processTextPosition(TextPosition text) {
                    locationMap.place(this.getGraphicsState(), text);
                }
            };
            textStripper.setSortByPosition(true);
            try {
                textStripper.processStream(page, page.findResources(), page.getContents().getStream());
            } catch (NullPointerException e) {
                e.printStackTrace(out);
            }
        }
        //out.println(page.getMediaBox());

        pageTextLocMaps.put(pageIndex, locationMap);


        pageSizingMap.put(pageIndex,
                new PageSizing(
                pageIndex,
                pdfX1, pdfY1, pdfCX, pdfCY
                ));
    }
    
    public static class MatchedText {

        public TextLocation textLocation;
        public double matchWeight;

        public MatchedText(TextLocation textLocation, double matchWeight) {
            this.textLocation = textLocation;
            this.matchWeight = matchWeight;
        }
    }
    
    public static final Comparator<PdfText.MatchedText> MATCHED_TEXT_WEIGHT_COMPARATOR = new Comparator<PdfText.MatchedText>() {

        @Override
        public int compare(MatchedText o1, MatchedText o2) {
            return -Double.compare(o1.matchWeight, o2.matchWeight);
        }
    };
    public static final Comparator<PdfText.MatchedText> INV_MATCHED_TEXT_WEIGHT_COMPARATOR = new Comparator<PdfText.MatchedText>() {

        @Override
        public int compare(MatchedText o1, MatchedText o2) {
            return Double.compare(o1.matchWeight, o2.matchWeight);
        }
    };
    
    public List<TextLocation> getAllWords(int page, double minX, double minY, double maxX, double maxY) {
        TextLocationMap textLocationMap = pageTextLocMaps.get(page);
        textLocationMap.processWords(minX, minY, maxX, maxY, true);
        List<TextLocation> wordLocs = textLocationMap.getTextLocs();
        for (int i=0; i<wordLocs.size(); ++i) {
            TextLocation tl = wordLocs.get(i);
            tl.page = page;
        }
        
        return wordLocs;
    }
    
    public List<MatchedText> findTextIn(List<TextLocation> wordLocs, String text) {
        char[] textChars = text.toCharArray();
        List<MatchedText> matchedText = new ArrayList<>(wordLocs.size());
        for (TextLocation tl:wordLocs) {
            String foundText = tl.text;
            
            int maxSkippedChars = 0;
            boolean missedChar = false;
            int currentIndex = 0;
            boolean firstChar = true;
            for (char ch:textChars) {
                int foundAt = foundText.indexOf(ch, currentIndex);
                if (foundAt<0) {
                    missedChar = true;
                    continue;
                } else {
                    if (!firstChar) {
                        int skippedChars = foundAt-currentIndex;
                        if (skippedChars>maxSkippedChars) {
                            maxSkippedChars = skippedChars;
                        }
                    }
                    currentIndex = foundAt+1;
                }
                if (firstChar) {
                    firstChar = false;
                }
            }
            
            if (missedChar) {
                continue;
            }
            
            matchedText.add(new MatchedText(tl, maxSkippedChars));
        }
        
        Collections.sort(matchedText, INV_MATCHED_TEXT_WEIGHT_COMPARATOR);
        
        return matchedText;
    }
    
    public List<TextLocation> findAllText(int page,
            double minX, double minY, double maxX, double maxY) {
        TextLocationMap textLocationMap = pageTextLocMaps.get(page);
        textLocationMap.processWords(minX, minY, maxX, maxY, true);
        List<TextLocation> wordLocs = textLocationMap.getTextLocs();
        if (wordLocs.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return wordLocs;
    }


    public PageSizing getPageSizing(Integer page) {
        return pageSizingMap.get(page);
    }

}
