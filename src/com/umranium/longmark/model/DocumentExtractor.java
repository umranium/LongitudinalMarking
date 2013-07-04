/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import com.umranium.longmark.common.Constants;
import com.umranium.longmark.common.FileNameHelper;
import com.umranium.longmark.json.JsonCommon;
import com.umranium.longmark.storage.MalformedDataFileException;
import com.umranium.longmark.ui.pdftext.PageSizing;
import com.umranium.longmark.ui.pdftext.PdfText;
import com.umranium.longmark.ui.pdftext.PdfText.MatchedText;
import com.umranium.longmark.ui.pdftext.TextLocation;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFImageWriter;

/**
 *
 * @author umran
 */
public class DocumentExtractor {
    
    private static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;
    
    private String source;
    private File pdfFile;
    private File extrasCsvFile;
    private Splitter[] splitters;
    
    private List<DocumentSection> documentSections;
    private DocExtrasStorage extrasStorage;

    public DocumentExtractor(String source, File pdfFile, Splitter[] splitters) {
        this.source = source;
        this.pdfFile = pdfFile;
        this.extrasCsvFile = new File(pdfFile.getParentFile(),
                pdfFile.getName()
                        .replaceAll("\\..*", String.format("-extras.csv"))
                );
        this.splitters = splitters;
    }

    private String woWs(String text) {
        return text.replaceAll("\\s+", "");
    }

    private void extractSection(PdfText pdfText, BufferedImage[] pageImages,
            double imgScale, String splitId,
            int startPage, double startY,
            int endPage, Double endY) {
        PrintStream out = System.out;
        out.println("Extract section '"+splitId+"' from [page=" +
                startPage + ", y=" + startY
                + "] to [page=" + endPage + ", y=" + endY + "]");
        List<BufferedImage> images = new ArrayList<BufferedImage>();
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
                splitId, pdfText, extrasStorage, images, imgScale);
        documentSections.add(documentSection);
    }
    
    public Document extract() throws IOException, MissingSplitter {
        this.documentSections = new ArrayList<DocumentSection>();
        this.extrasStorage = new DocExtrasStorage(extrasCsvFile);
        if (extrasCsvFile.exists()) {
            try {
                extrasStorage.load();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DocumentExtractor.class.getName()).log(
                        Level.WARNING, "Error while loading extras", ex);
            } catch (MalformedDataFileException ex) {
                Logger.getLogger(DocumentExtractor.class.getName()).log(
                        Level.WARNING, "Error while loading extras", ex);
            }
        }
        
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
                
                if (imageFile.exists() && imageFile.lastModified()>pdfFile.lastModified()) {
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
        
        Map<TextLocation,Integer> splitterBestMatches = new TreeMap<TextLocation,Integer>(COMP_BY_LOC);
        
        for (int sp=0; sp<splitters.length; ++sp) {
            Splitter splitter = splitters[sp];
            
            List<TextLocation> txtLocs = new ArrayList<TextLocation>();
            for (int page=0; page<pageCount; ++page) {
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
                
                minY = Double.NEGATIVE_INFINITY;
                maxY = Double.POSITIVE_INFINITY;
                
                //System.out.println("finding all text on page "+page);
                List<TextLocation> tmpLocs = pdfText.findAllText(
                         page, minX, minY, maxX, maxY);
                txtLocs.addAll(tmpLocs);
                
                for (TextLocation loc:tmpLocs) {
                    loc.page = page;
                }
            }
            
            boolean bestMatchFound = false;
            if (!txtLocs.isEmpty()) {
                List<MatchedText> matches = pdfText.findTextIn(txtLocs, woWs(splitter.text));
                if (!matches.isEmpty() && matches.get(0).matchWeight<1.0) {
                    bestMatchFound = true;
                    TextLocation bestMatch = matches.get(0).textLocation;
                    
                    
                    /*
                    System.out.println("Best match of '"+splitter.id+
                            "' was found as '"+JsonCommon.quoteText(bestMatch.text)+
                            "' on page "+bestMatch.page);
                    */
                    PageSizing pageSizing = pdfText.getPageSizing(bestMatch.page);
                    if (splitter.height!=null) {
                        bestMatch.y = bestMatch.y2 - 
                                pageSizing.toPdfYCoord(splitter.height,
                                splitter.templateHeight);
                    }
                    if (splitter.topPadding!=null) {
                        double padding = pageSizing.toPdfYCoord(splitter.topPadding,
                                splitter.templateHeight);
                        bestMatch.y += padding;
                        bestMatch.y2 += padding;
                    }
                
                    splitterBestMatches.put(bestMatch, sp);
                }
            }
            
            if (!bestMatchFound) {
                Logger.getLogger(DocumentExtractor.class.getName()).log(
                        Level.WARNING,
                        "Splitter {0} (''{1}'') could not be located in document.",
                        new Object[]{splitter.id, splitter.text});                
            }
        }
        
        TextLocation lastMatch = null;
        String lastSplitterId = null;
        for (Map.Entry<TextLocation,Integer> entry:splitterBestMatches.entrySet()) {
            Splitter currentSplitter = splitters[entry.getValue()];
            TextLocation currentMatch = entry.getKey();
            
            int startPage = 0;
            double startY = 0.0;
            String splitterId = Constants.FIRST_SEGMENT_ID;
            if (lastMatch!=null) {
                startPage = lastMatch.page;
                startY = lastMatch.y;
                splitterId = lastSplitterId;
            }
            extractSection(pdfText, pageImages,
                    scale, splitterId,
                    startPage, startY,
                    currentMatch.page, currentMatch.y);
            lastMatch = currentMatch;
            lastSplitterId = currentSplitter.id;
        }
        
        {
            int startPage = 0;
            double startY = 0.0;
            int lastPage = pageCount-1;
            String splitterId = Constants.FIRST_SEGMENT_ID;
            if (lastMatch!=null) {
                startPage = lastMatch.page;
                startY = lastMatch.y;
                splitterId = lastSplitterId;
            }
            extractSection(pdfText, pageImages,
                    scale, splitterId,
                    startPage, startY,
                    lastPage, null);
        }
        
        //  find and add all the sections that weren't found
        Set<String> foundSections = new TreeSet<String>();
        for (DocumentSection section:documentSections) {
            foundSections.add(section.getId());
        }
        for (Splitter splitter:splitters) {
            if (!foundSections.contains(splitter.id)) {
                DocumentSection section = new DocumentSection(
                        splitter.id, pdfText, extrasStorage,
                        Collections.EMPTY_LIST, scale);
                documentSections.add(section);
            }
        }
        
        return new Document(source,
                Collections.singletonList(extrasStorage),
                documentSections);
    }
    
    
    private Comparator<TextLocation> COMP_BY_LOC = new Comparator<TextLocation>() {
        @Override
        public int compare(TextLocation o1, TextLocation o2) {
            int res = Integer.compare(o1.page, o2.page);
            if (res!=0) {
                return res;
            }
            res = Double.compare(o1.y2, o2.y2);
            if (res!=0) {
                return res;
            }
            res = Double.compare(o1.y, o2.y);
            if (res!=0) {
                return res;
            }
            return 0;
        }
    };
}
