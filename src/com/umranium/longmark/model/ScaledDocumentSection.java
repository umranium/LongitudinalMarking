/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import com.umranium.longmark.common.AsyncResultsReceiver;
import com.umranium.longmark.common.BackgroundProcessor;
import com.umranium.longmark.common.DimensionsChangedHandler;
import static com.umranium.longmark.ui.doc.DocSecPanel.DIVIDER_WIDTH;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * A representation of a scaled document section. Caches the scaled images so
 * as to avoid filling up the memory.
 * 
 * @author umran
 */
public class ScaledDocumentSection {
    
    public static final int FOOTER_WHITESPACE_HEIGHT = 20;
    
    private static List<BufferedImage> removeWhitespace(List<BufferedImage> imgs) {
        List<BufferedImage> newImgs = new ArrayList<BufferedImage>(imgs.size());
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
 
    public static final String PROP_SIZE = "size";
    private transient final PropertyChangeSupport propertyChangeSupport = 
            new PropertyChangeSupport(this);

    private DocumentSection documentSection;
    private int imageCount;
    private double imgScale;
    private double scale;
    private Dimension[] scaledImgSizes;
    private ScalingImageCache imageCache;
    private Semaphore cachedImagesGlobalSemaphore;
//    private Map<Integer,BufferedImage> cachedOrigImages;
    private Dimension size;

    public ScaledDocumentSection(DocumentSection documentSection) {
        this.documentSection = documentSection;
        this.imageCount = documentSection.getImageCount();
        this.imgScale = documentSection.getImgScale();
        this.scale = 1.0;
        this.scaledImgSizes = new Dimension[imageCount];
        this.imageCache = new ScalingImageCache(imageCount);
        cachedImagesGlobalSemaphore = new Semaphore(imageCount, true);
        this.computeDimensions();
    }
    
    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        try {
            // acquire all permits in order to wait for all
            //      threads to finish working, while
            //      also blocking any threads from working while
            //      this task is processing.
            cachedImagesGlobalSemaphore.acquire(imageCount);
        } catch (InterruptedException ex) {
            return;
        }
        try {
            imageCache.clear();
            this.scale = scale;
            computeDimensions();
        } finally {
            //  release all permits
            cachedImagesGlobalSemaphore.release(imageCount);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
    

    /**
     * Get the value of size
     *
     * @return the value of size
     */
    public Dimension getSize() {
        return size;
    }
    
    public int getImageCount() {
        return imageCount;
    }

    public Dimension[] getScaledImgSizes() {
        return scaledImgSizes;
    }
    
    public Future asyncLoadScaledImageAsync(int index,
            BackgroundProcessor processor, AsyncResultsReceiver<Image> results) {
        return imageCache.getImageAsync(index, processor, results);
    }
    
    public Map<Integer,Image> getScaledImages() {
        return imageCache.getImages(false);
    }
    
    private Image createScaledImage(int index) {
        try {
            //  acquire a permit, hence blocking any overall tasks from
            //      being done, and also blocking this task from being done
            //      if an overall task is being done (e.g. re-scaling)
            cachedImagesGlobalSemaphore.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            BufferedImage origImg = documentSection.getImage(index);//cachedOrigImages.get(index);
            //  scale down original
            Dimension scaledSize = scaledImgSizes[index];
            return origImg.getScaledInstance(
                    scaledSize.width,
                    scaledSize.height,
                    BufferedImage.SCALE_SMOOTH);
        } finally {
            cachedImagesGlobalSemaphore.release();
        }
        
    }
    
    private void computeDimensions() {
        boolean first = true;
        int totalWidth = 0;
        int totalHeight = 0;
        for (int i=0; i<imageCount; ++i) {
            if (first) {
                first = false;
            } else {
                totalHeight += DIVIDER_WIDTH;
            }
            
            Dimension scaledSize = computeScaledImgSize(i);
            
            if (totalWidth<scaledSize.width) {
                totalWidth = scaledSize.width;
            }
            
            totalHeight += scaledSize.height;
            
            scaledImgSizes[i] = scaledSize;
        }
        
//        System.out.println(this.getName()+": current width="+width+", height="+height);
        
        Dimension oldSize = this.size;
        this.size = new Dimension(totalWidth, totalHeight);
        
////        this.setMinimumSize(size);
////        this.setMaximumSize(size);
//        this.setPreferredSize(size);
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, size);
    }
    
    private Dimension computeScaledImgSize(int index) {
        Dimension origImgSize = documentSection.getImgSize(index);
        int imgWid = (int)Math.ceil(origImgSize.width/imgScale*scale);
        int imgHei = (int)Math.ceil(origImgSize.height/imgScale*scale);
        return new Dimension(imgWid, imgHei);
    }
    
    private class ScalingImageCache extends ImageCache<Image> {

        public ScalingImageCache(int imageCount) {
            super(imageCount);
        }
        
        @Override
        protected Image loadImage(int index) {
            return createScaledImage(index);
        }
        
    }

}
