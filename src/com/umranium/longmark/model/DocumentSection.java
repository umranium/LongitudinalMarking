/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import com.umranium.longmark.ui.pdftext.PdfText;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * A portion of a document. Each section has an ID, and a number of images.
 * 
 * @author umran
 */
public class DocumentSection {
    
    public static final int FOOTER_WHITESPACE_HEIGHT = 20;
    
    public static final String PROP_COMMENTS = "comments";
    public static final String PROP_MARK = "mark";

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    private static List<BufferedImage> removeWhitespace(List<BufferedImage> imgs) {
        List<BufferedImage> newImgs = new ArrayList<>(imgs.size());
        for (BufferedImage img:imgs) {
            int height = img.getHeight();
            int width = img.getWidth();
            int lastRow = height-1;
            while (lastRow>=0) {
                boolean differenceFound = false;
                int firstPixel = img.getRGB(0, lastRow);
                for (int i=1; i<width; ++i) {
                    int pix = img.getRGB(i, lastRow);
                    if (pix!=firstPixel) {
                        differenceFound = true;
                        break;
                    }
                }
                if (!differenceFound) {
                    --lastRow;
                } else {
                    break;
                }
            }
            
            if (lastRow<0) {
                continue;
            }
            
            //  leave some whitespace at the end.
            int newHeight = lastRow + 1 + FOOTER_WHITESPACE_HEIGHT;
            
            if (newHeight>height) {
                newImgs.add(img);
                continue;
            }
            
            BufferedImage newImg = img.getSubimage(0, 0, width, lastRow+1);
            newImgs.add(newImg);
        }
        return newImgs;
    }
 
    private Document document;
    private String id;
//    private PdfText pdfText;
    private int imageCount;
    private double imgScale;
    private Dimension[] origImgSizes;
    private File[] tempImgFiles;
    private ImageCache<BufferedImage> imgs;

    private String comments = "No comments";
    private double mark = 1.0;
    
    
    public DocumentSection(String id, PdfText pdfText, List<BufferedImage> imgs, double imgScale) {
        imgs = removeWhitespace(imgs);
        
        this.document = null;
        this.id = id;
//        this.pdfText = pdfText;
        this.imageCount = imgs.size();
        this.imgScale = imgScale;
        this.origImgSizes = new Dimension[imageCount];
        this.tempImgFiles = new File[imageCount];
        
        for (int i=0; i<imageCount; ++i) {
            BufferedImage img = imgs.get(i);
            try {
                File tempFile = File.createTempFile("temp-img-", ".bmp");
                ImageIO.write(img, "BMP", tempFile);
                this.tempImgFiles[i] = tempFile;
                tempFile.deleteOnExit();
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Error while writing temporary image to file.",
                        ex);
            }
            
            this.origImgSizes[i] = new Dimension(img.getWidth(), img.getHeight());
        }
        
        this.imgs = new TempFileLoadingImageCache(imageCount);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
    
    public String getId() {
        return id;
    }
    
    public int getImageCount() {
        return imageCount;
    }

    public double getImgScale() {
        return imgScale;
    }
    
    public BufferedImage getImage(int index) {
        return imgs.getImage(index);
    }

    public Dimension getImgSize(int index) {
        return origImgSizes[index];
    }
    
    /**
     * Get the value of comments
     *
     * @return the value of comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Set the value of comments
     *
     * @param comments new value of comments
     */
    public void setComments(String comments) {
        String oldComments = this.comments;
        this.comments = comments;
        propertyChangeSupport.firePropertyChange(PROP_COMMENTS, oldComments, comments);
    }

    /**
     * Get the value of mark
     *
     * @return the value of mark
     */
    public double getMark() {
        return mark;
    }

    /**
     * Set the value of mark
     *
     * @param mark new value of mark
     */
    public void setMark(double mark) {
        double oldMark = this.mark;
        this.mark = mark;
        propertyChangeSupport.firePropertyChange(PROP_MARK, oldMark, mark);
    }


    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    private class TempFileLoadingImageCache extends ImageCache<BufferedImage> {

        public TempFileLoadingImageCache(int imageCount) {
            super(imageCount);
        }
        
        @Override
        protected BufferedImage loadImage(int index) {
            try {
                System.out.println("Loading from temp image file:"+tempImgFiles[index]);
                return ImageIO.read(tempImgFiles[index]);
            } catch (IOException ex) {
                throw new RuntimeException("Error while loading temporary image "+
                        tempImgFiles[index]+". Temporary files probably cleared.",
                        ex);
            }
        }
        
    }

}
