/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui;

import com.umranium.longmark.common.BackgroundProcessor;
import com.umranium.longmark.common.Constants;
import com.umranium.longmark.model.DocumentSection;
import com.umranium.longmark.ui.doc.DocSecPanel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;

/**
 *
 * @author umran
 */
public class SingleSectionMarker extends javax.swing.JFrame {
    
    private DocumentSection docSection;
    private BackgroundProcessor bgImageLoader;
    private DocSecPanel docSecPanel;
    private BindingGroup bindingGroup;

    /**
     * Creates new form SingleSectionMarker
     */
    public SingleSectionMarker(DocumentSection documentSection, BackgroundProcessor bgImageLoader, double scale) {
        docSecPanel = new DocSecPanel(documentSection, bgImageLoader);
        docSecPanel.setScale(scale);
        this.bgImageLoader = bgImageLoader;
        initComponents();
        sldrMagnification.setValue((int)(scale*100));
        
        bindingGroup = new BindingGroup();
        Binding markBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                documentSection,
                BeanProperty.create(DocumentSection.PROP_MARK),
                marksSpinner,
                BeanProperty.create("value"));
        Binding commentsBinding = Bindings.createAutoBinding(
                AutoBinding.UpdateStrategy.READ_WRITE,
                documentSection,
                BeanProperty.create(DocumentSection.PROP_COMMENTS),
                commentsTxt,
                BeanProperty.create("text"));
        bindingGroup.addBinding(markBinding);
        bindingGroup.addBinding(commentsBinding);
        bindingGroup.bind();
        
    }
    
    private void updateCurrentScaleTxt() {
        lblCurrentScale.setText(String.format("x%.2f", docSecPanel.getScale()));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        docSecPanelContainer = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = docSecPanel;
        javax.swing.JPanel commentsPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JPanel magnificationPanel = new javax.swing.JPanel();
        sldrMagnification = new javax.swing.JSlider();
        lblCurrentScale = new javax.swing.JLabel();
        marksSpinner = new javax.swing.JSpinner();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        commentsTxt = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        docSecPanelContainer.setLayout(new java.awt.GridBagLayout());
        docSecPanelContainer.add(jPanel1, new java.awt.GridBagConstraints());

        jScrollPane2.setViewportView(docSecPanelContainer);

        getContentPane().add(jScrollPane2, java.awt.BorderLayout.CENTER);

        commentsPanel.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        magnificationPanel.setLayout(new java.awt.BorderLayout());

        sldrMagnification.setMajorTickSpacing(10);
        sldrMagnification.setMaximum((int)(Constants.MAX_SCALE*100));
        sldrMagnification.setMinimum((int)(Constants.MIN_SCALE*100));
        sldrMagnification.setMinorTickSpacing(5);
        sldrMagnification.setPaintTicks(true);
        sldrMagnification.setSnapToTicks(true);
        sldrMagnification.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldrMagnificationStateChanged(evt);
            }
        });
        magnificationPanel.add(sldrMagnification, java.awt.BorderLayout.CENTER);

        lblCurrentScale.setPreferredSize(new java.awt.Dimension(50, 20));
        magnificationPanel.add(lblCurrentScale, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(magnificationPanel, gridBagConstraints);

        marksSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(0.5d)));
        marksSpinner.setPreferredSize(new java.awt.Dimension(100, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(marksSpinner, gridBagConstraints);

        commentsPanel.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        commentsTxt.setColumns(20);
        commentsTxt.setRows(5);
        jScrollPane1.setViewportView(commentsTxt);

        commentsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(commentsPanel, java.awt.BorderLayout.EAST);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sldrMagnificationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldrMagnificationStateChanged

        int sldrVal = sldrMagnification.getValue();

        double newScale = sldrVal / 100.0;

        if (newScale>Constants.MAX_SCALE) {
            return;
        }
        if (newScale<Constants.MIN_SCALE) {
            return;
        }

        docSecPanel.setScale(newScale);
        updateCurrentScaleTxt();
        docSecPanel.revalidate();

    }//GEN-LAST:event_sldrMagnificationStateChanged

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea commentsTxt;
    private javax.swing.JPanel docSecPanelContainer;
    private javax.swing.JLabel lblCurrentScale;
    private javax.swing.JSpinner marksSpinner;
    private javax.swing.JSlider sldrMagnification;
    // End of variables declaration//GEN-END:variables

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws FileNotFoundException, IOException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MultiColumnTextTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MultiColumnTextTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MultiColumnTextTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MultiColumnTextTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DocumentSection documentSection = new DocumentSection(
                        "id", null,
                        Collections.EMPTY_LIST, 1.0);
                SingleSectionMarker marker = new SingleSectionMarker(
                        documentSection,
                        BackgroundProcessor.INSTANCE,
                        1.0);
                marker.setVisible(true);
            }
        });
    }
}