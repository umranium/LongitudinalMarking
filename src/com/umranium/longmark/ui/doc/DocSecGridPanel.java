/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui.doc;

import com.umranium.longmark.common.BackgroundProcessor;
import com.umranium.longmark.common.DimensionsChangedHandler;
import com.umranium.longmark.model.DocumentSection;
import com.umranium.longmark.model.Document;
import com.umranium.longmark.ui.MarkingPanel;
import com.umranium.longmark.ui.MultiColumnText;
import com.umranium.longmark.ui.SectionVisibilityManager;
import com.umranium.longmark.ui.SingleSectionMarker;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author umran
 */
public class DocSecGridPanel extends javax.swing.JPanel {
    
    public static final int NOTE_TEXT_COLS = 40;
    public static final int NOTE_TEXT_ROWS = 10;
    
    private class GridSection {
        DocSecPanel docSecPanel;
        MarkingPanel markingPanel;

        public GridSection(DocSecPanel docSecPanel, MarkingPanel markingPanel) {
            this.docSecPanel = docSecPanel;
            this.markingPanel = markingPanel;
        }
    }
    
    private BackgroundProcessor bgImageLoader;
    private SectionVisibilityManager sectionVisibilityManager;
    private double scale = 0.5;
    private Map<Document,Integer> documentColumns;
    private Map<String,Integer> sectionRows;
    private List<List<GridSection>> documentSectionPanelGrid;
    
    /**
     * Creates new form DocSecGridPanel
     */
    public DocSecGridPanel(BackgroundProcessor bgImageLoader, SectionVisibilityManager sectionVisibilityManager) {
        this.bgImageLoader = bgImageLoader;
        this.sectionVisibilityManager = sectionVisibilityManager;
        
        initComponents();
        
        documentColumns = new TreeMap<Document,Integer>(new Comparator<Document>(){
            @Override
            public int compare(Document o1, Document o2) {
                return o1.getSource().compareTo(o2.getSource());
            }
        });
        sectionRows = new TreeMap<String,Integer>(new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        documentSectionPanelGrid = new ArrayList<List<GridSection>>();
    }

    public double getScale() {
        return scale;
    }
    
    public void setScale(double scale) {
        this.scale = scale;
        
        for (Component c:getComponents()) {
            if (c instanceof DocSecPanel) {
                DocSecPanel item = (DocSecPanel)c;
                item.setScale(scale);
            }
        }
        
        revalidate();
    }
    
    public int getDocumentColumn(Document document) {
        return documentColumns.get(document);
    }
    
    public int getColumnCount() {
        return documentColumns.size();
    }
    
    public Set<Document> getDocuments() {
        return documentColumns.keySet();
    }
    
    public void addDocument(Document document) {
        List<DocumentSection> sections = document.getSections();
        List<GridSection> docSectionsColumn = new ArrayList<GridSection>();
        
        int column = documentColumns.size();
        
        for (final DocumentSection section:sections) {
            int row = sectionRows.size();
            if (sectionRows.containsKey(section.getId())) {
                row = sectionRows.get(section.getId());
            } else {
                sectionRows.put(section.getId(), row);
            }
            
            final DocSecPanel panel = new DocSecPanel(section, bgImageLoader);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = column;
            gbc.gridy = row*2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(5, 5, 5, 5);
            this.add(panel, gbc);
            
            //Dimension panelSize = panel.getPreferredSize();
            
            final MarkingPanel markingPanel = new MarkingPanel(
                    section, NOTE_TEXT_COLS, NOTE_TEXT_ROWS);
            gbc = new GridBagConstraints();
            gbc.gridx = column;
            gbc.gridy = row*2 + 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(5, 5, 5, 5);
            this.add(markingPanel, gbc);
            
            panel.setDimensionsChangeHandler(new DimensionsChangedHandler() {
                @Override
                public void onDimensionsChanged(Dimension newSize) {
                    Dimension sz = markingPanel.getPreferredSize();
                    markingPanel.setPreferredSize(new Dimension(
                            (int)newSize.width, (int)sz.height));
                }
            });
            
            final GridSection gridSection = new GridSection(panel, markingPanel);
            
            markingPanel.getEditCommentsBtn().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onMarkingCommentsClicked(section, gridSection);
                }
            });

            panel.setScale(scale);
            docSectionsColumn.add(gridSection);
        }
        
        documentColumns.put(document, column);
        documentSectionPanelGrid.add(docSectionsColumn);
    }

    public BackgroundProcessor getBgImageLoader() {
        return bgImageLoader;
    }
    
    public boolean updateSectionVisibility(boolean displayAll, boolean displayMarkingPanels) {
        
        boolean atLeastOneChanged = false;
        for (List<GridSection> sections:documentSectionPanelGrid) {
            for (GridSection gridSection:sections) {
                DocSecPanel docSecPanel = gridSection.docSecPanel;
                MarkingPanel markingPanel = gridSection.markingPanel;
                
                DocumentSection section = docSecPanel.getDocumentSection();
                boolean vis = displayAll;
                if (!vis) {
                    String sectionId = section.getId();
                    if (sectionId!=null) {
                        vis = sectionVisibilityManager.isVisible(sectionId);
                    }
                }
                boolean markingPanelVis = vis && displayMarkingPanels;
                
                if (vis!=docSecPanel.isVisible()) {
                    docSecPanel.setVisible(vis);
                    atLeastOneChanged = true;
                }
                if (markingPanelVis!=markingPanel.isVisible()) {
                    markingPanel.setVisible(markingPanelVis);
                    atLeastOneChanged = true;
                }
            }
        }
        
        return atLeastOneChanged;
    }
    
    public DocSecPanel getPanel(DocumentSection section) {
        Document doc = section.getDocument();
        int column = documentColumns.get(doc);
        int row = sectionRows.get(section.getId());
        return documentSectionPanelGrid.get(column).get(row).docSecPanel;
    }
    
    private void onMarkingCommentsClicked(final DocumentSection section, final GridSection gridSection) {
        System.out.println("comments clicked: "+section.getId());
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                SingleSectionMarker marker = new SingleSectionMarker(
                        section,
                        bgImageLoader,
                        scale);
                marker.setVisible(true);
            }
        });
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
