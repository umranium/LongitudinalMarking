/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import com.umranium.longmark.common.Constants;
import com.umranium.longmark.common.FileNameHelper;
import com.umranium.longmark.json.JsonCommon;
import com.umranium.longmark.ui.pdftext.PageSizing;
import com.umranium.longmark.ui.pdftext.PdfText;
import com.umranium.longmark.ui.pdftext.PdfText.MatchedText;
import com.umranium.longmark.ui.pdftext.TextLocation;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFImageWriter;

/**
 *
 * @author umran
 */
public class DocumentExtractor {
    
    private static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;
    private static final String FIRST_SEGMENT_ID = "First Segment";
    private String source;
    private File pdfFile;
    private Splitter[] splitters;
    private List<DocumentSection> documentSections;

    public DocumentExtractor(String source, File pdfFile, Splitter[] splitters) {
        this.source = source;
        this.pdfFile = pdfFile;
        this.splitters = splitters;
        this.documentSections = new ArrayList<>();
    }

    private String woWs(String text) {
        return text.replaceAll("\\s+", "");
    }

    private void extractSection(PdfText pdfText, BufferedImage[] pageImages,
            double imgScale, String splitId,
            int startPage, double startY,
            int endPage, Double endY) {
        PrintStream out = System.out;
        out.println("Extract section from [page=" + startPage + ", y=" + startY
                + "] to [page=" + endPage + ", y=" + endY + "]");
        List<BufferedImage> images = new ArrayList<>();
        for (int pg = startPage; pg <= endPage; ++pg) {
            BufferedImage pageImg = pageImages[pg];
            int x1 = 0;
            int y1 = 0;
            int x2 = pageImg.getWidth();
            int y2 = pageImg.getHeight();
            if (pg == startPage) {
                PageSizing pageSizing = pdfText.getPageSizing(pg);
                y1 = (int) Math.floor(pageSizing.toTemplateYCoord(
                        startY, pageImg.getHeight()));
            }
            if (pg == endPage && endY != null) {
                PageSizing pageSizing = pdfText.getPageSizing(pg);
                y2 = (int) Math.ceil(pageSizing.toTemplateYCoord(
                        endY, pageImg.getHeight()));
            }
            images.add(pageImg.getSubimage(x1, y1, x2 - x1, y2 - y1));
        }
        DocumentSection documentSection = new DocumentSection(
                splitId, pdfText, images, imgScale);
        documentSections.add(documentSection);
    }
    
    public Document extract() throws IOException, MissingSplitter {
        PdfText pdfText = new PdfText(pdfFile);
        pdfText.initExtraction();
        int pageCount = pdfText.getNumberOfPages();
        BufferedImage[] pageImages = new BufferedImage[pageCount];
        int resDpi = (int)Math.ceil(Constants.MAX_SCALE*DEFAULT_USER_SPACE_UNIT_DPI);
        double scale = (double)resDpi / (double)DEFAULT_USER_SPACE_UNIT_DPI;
        try {
            for (int i=0; i<pageCount; ++i) {
                String imageName = pdfFile.getName()
                        .replaceAll("\\..*", String.format("-%02d.png", i+1));
                File imageFile = new File(pdfFile.getParentFile(), imageName);
                
                if (imageFile.exists()) {
                    pageImages[i] = ImageIO.read(imageFile);
                } else {
                    PDPage page = pdfText.getPages().get(i);
                    pageImages[i] = page.convertToImage(
                            BufferedImage.TYPE_INT_RGB,
                            resDpi
                            );
                    ImageIO.write(pageImages[i], "PNG", imageFile);
                }
                
                pdfText.extract(i);
            }
        } finally {
            pdfText.finalizeExtraction();
        }
        
        int currentSplitter = 0;
        TextLocation lastMatch = null;
        
        while (currentSplitter<splitters.length) {
            Splitter splitter = splitters[currentSplitter];
            
            //System.out.println("Looking for '"+splitter.text+"'");
            
            List<TextLocation> txtLocs = new ArrayList<>();
            int startSearchPage = 0;
            if (lastMatch!=null) {
                startSearchPage = lastMatch.page;
            }
            for (int page=startSearchPage; page<pageCount; ++page) {
                double minX, maxX;
                double minY, maxY;
                
                PageSizing pageSizing = pdfText.getPageSizing(page);
                
                if (splitter.minX!=null) {
                    minX = pageSizing.toPdfXCoord(splitter.minX, splitter.templateWidth);
                } else {
                    minX = Double.NEGATIVE_INFINITY;
                }

                if (splitter.maxX!=null) {
                    maxX = pageSizing.toPdfXCoord(splitter.maxX, splitter.templateWidth);
                } else {
                    maxX = Double.POSITIVE_INFINITY;
                }
                
                maxY = Double.POSITIVE_INFINITY;
                if (lastMatch!=null && page==lastMatch.page) {
                    minY = lastMatch.y2;
                } else {
                    minY = Double.NEGATIVE_INFINITY;
                }
                
                List<TextLocation> tmpLocs = pdfText.findAllText(
                         page, minX, minY, maxX, maxY);
                txtLocs.addAll(tmpLocs);
                
                //System.out.println("Page: "+page);
                for (TextLocation loc:tmpLocs) {
                    loc.page = page;
//                    System.out.println("\t"+loc.page);
//                    System.out.println("\t'"+loc.text+"'");
                }
            }
            
            if (txtLocs.isEmpty()) {
                throw new MissingSplitter(splitter);
            }
            List<MatchedText> matches = pdfText.findTextIn(txtLocs, woWs(splitter.text));
            if (matches.isEmpty()) {
                throw new MissingSplitter(splitter);
            }
            
            
            TextLocation currentMatch = matches.get(0).textLocation;
            {
                PageSizing pageSizing = pdfText.getPageSizing(currentMatch.page);
                if (splitter.height!=null) {
                    currentMatch.y = currentMatch.y2 - 
                            pageSizing.toPdfYCoord(splitter.height,
                            splitter.templateHeight);
                }
                if (splitter.topPadding!=null) {
                    double padding = pageSizing.toPdfYCoord(splitter.topPadding,
                            splitter.templateHeight);
                    currentMatch.y += padding;
                    currentMatch.y2 += padding;
                }
            }
            //System.out.println("Match for splitter "+splitter.id+": "+splitter.text);
            //System.out.println("\t"+currentMatch.text);
            
            int startPage = 0;
            double startY = 0.0;
            String splitterId = FIRST_SEGMENT_ID;
            if (lastMatch!=null) {
                startPage = lastMatch.page;
                startY = lastMatch.y;
                splitterId = splitters[currentSplitter-1].id;
            }
            extractSection(pdfText, pageImages,
                    scale, splitterId,
                    startPage, startY,
                    currentMatch.page, currentMatch.y);
            lastMatch = currentMatch;
            ++currentSplitter;
        }
        
        {
            int startPage = 0;
            double startY = 0.0;
            int lastPage = pageCount-1;
            String splitterId = FIRST_SEGMENT_ID;
            if (lastMatch!=null) {
                startPage = lastMatch.page;
                startY = lastMatch.y;
                splitterId = splitters[currentSplitter-1].id;
            }
            extractSection(pdfText, pageImages,
                    scale, splitterId,
                    startPage, startY,
                    lastPage, null);
        }
        
        return new Document(source, documentSections);
    }
    
}
