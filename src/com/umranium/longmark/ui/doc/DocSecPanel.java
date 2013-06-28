/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui.doc;

import com.umranium.longmark.model.DocumentSection;
import com.umranium.longmark.common.AsyncResultsReceiver;
import com.umranium.longmark.common.BackgroundProcessor;
import com.umranium.longmark.common.DimensionsChangedHandler;
import com.umranium.longmark.model.ScaledDocumentSection;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author umran
 */
public class DocSecPanel extends javax.swing.JPanel {
    
    public static final int DIVIDER_WIDTH = 10;

    private BackgroundProcessor backgroundProcessor;
    private DocumentSection documentSection;
    private ScaledDocumentSection scaledDocumentSection;
    private String sourceName = "Unknown Source";
    private DimensionsChangedHandler dimensionsChangeHandler;
    
    /**
     * Creates new form DocSecPanel
     */
    public DocSecPanel(DocumentSection documentSection, BackgroundProcessor backgroundProcessor) {
        this.backgroundProcessor = backgroundProcessor;
        this.documentSection = documentSection;
        this.scaledDocumentSection = new ScaledDocumentSection(documentSection);
        initComponents();
        if (documentSection.getDocument()!=null) {
            sourceName = documentSection.getDocument().getSource();
        }
        this.setToolTipText(sourceName);
        scaledDocumentSection.addPropertyChangeListener(ScaledDocumentSection.PROP_SIZE,
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Dimension newSize = (Dimension)evt.getNewValue();
                setPreferredSize(newSize);

                if (dimensionsChangeHandler!=null) {
                    dimensionsChangeHandler.onDimensionsChanged(newSize);
                }
            }
                });
    }
    
    public double getScale() {
        return scaledDocumentSection.getScale();
    }

    public void setScale(double scale) {
        this.scaledDocumentSection.setScale(scale);
    }

    public DimensionsChangedHandler getDimensionsChangeHandler() {
        return dimensionsChangeHandler;
    }

    public void setDimensionsChangeHandler(DimensionsChangedHandler dimensionsChangeHandler) {
        this.dimensionsChangeHandler = dimensionsChangeHandler;
    }
    
    public DocumentSection getDocumentSection() {
        return documentSection;
    }

    private AtomicLong callNo = new AtomicLong(0L);
    private final Object drawMutex = new Object();
    private AtomicInteger pendingAsyncLoadImageCount = new AtomicInteger(0);
    
    
    @Override
    protected void paintComponent(Graphics g) {
        
        //  if there is a pending image load, return
        if (pendingAsyncLoadImageCount.get()>0) {
            return;
        }
        
        final String name = sourceName + 
                ":" + documentSection.getId()+":"+callNo.incrementAndGet();
        System.out.println("Painting "+name);
        
        final int imgCount = documentSection.getImageCount();
        
        Map<Integer,Image> scaledImages = scaledDocumentSection.getScaledImages();
        
        if (scaledImages.size()<imgCount) {
            for (int i=0; i<imgCount; ++i) {
                //  if already available, skip
                if (scaledImages.containsKey(i)) {
                    continue;
                }
                final int index = i;
                //  otherwise, asynchronously load image
                System.out.println("\treloading "+name+" image "+index);
                //  increase the number of pending images
                pendingAsyncLoadImageCount.incrementAndGet();
                scaledDocumentSection.asyncLoadScaledImageAsync(
                        i, backgroundProcessor,
                        new AsyncResultsReceiver<Image>() {

                    @Override
                    public boolean stillValid() {
                        boolean valid = isValid();
                        System.out.println("\tcheck "+name+" image "+index+" valid="+valid);
                        return valid;
                    }
                    
                    private boolean isValid() {
                        return !DocSecPanel.this.getVisibleRect().isEmpty();
                    }

                    @Override
                    public void onResults(Image result) {
                        System.out.println("\tloaded "+name+" image "+index);
                        checkRePaint();
                    }

                    @Override
                    public void onCancelled() {
                        System.out.println("\tcancelled load "+name+" image "+index);
                        checkRePaint();
                    }

                    @Override
                    public void onException(Exception e) {
                        e.printStackTrace(System.out);
                        checkRePaint();
                    }

                    private void checkRePaint() {
                        synchronized (drawMutex) {
                            //  decrease the number of pending images
                            int tmpCount = pendingAsyncLoadImageCount.decrementAndGet();
                            //  if no images left to load
                            if (tmpCount==0) {
                                //  check and request a repaint
                                if (isValid()) {
                                    System.out.println("\trepaint "+name);
                                    java.awt.EventQueue.invokeLater(repaintRunnable);
                                }
                            }
                        }
                    }
                });
            }
        } else {
            System.out.println("\tdrawing "+name);
            synchronized (drawMutex) {
                int width = getWidth();
                Dimension[] scaledImgSizes = scaledDocumentSection.getScaledImgSizes();

                boolean first = true;
                int top = 0;
                for (int i=0; i<imgCount; ++i) {
                    Dimension imgSize = scaledImgSizes[i];

                    if (first) {
                        first = false;
                    } else {
                        g.setColor(getBackground());
                        g.fillRect(0, top, width, DIVIDER_WIDTH);
                        top += DIVIDER_WIDTH;
                    }

                    int x1 = (width - imgSize.width) / 2;
                    int height = imgSize.height;

                    Image img = scaledImages.get(i);
                    if (img!=null) {
                        g.drawImage(img, x1, top, null);
                    } else {
                        g.setColor(Color.MAGENTA);
                        g.fillRect(x1, top, width, height);
                    }

                    top += height;
                }
            }
        }
        
    }
    
    private final Runnable repaintRunnable = new Runnable() {
        @Override
        public void run() {
            if (!getVisibleRect().isEmpty()) {
                repaint();
            }
        }
    };
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(102, 102, 102));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 398, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 298, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
