/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui;

import com.umranium.longmark.ui.doc.DocSecGridHeader;
import com.umranium.longmark.ui.doc.DocSecGridPanel;
import com.umranium.longmark.common.BackgroundProcessor;
import com.umranium.longmark.model.Document;
import com.umranium.longmark.common.Constants;
import com.umranium.longmark.common.FileExtFilter;
import com.umranium.longmark.model.DocExtrasStorage;
import com.umranium.longmark.model.DocumentSection;
import com.umranium.longmark.model.Splitter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author umran
 */
public class MainFrame extends javax.swing.JFrame {
    
    private static final String ACT_KEY_TOGGLE_TOOL_PANEL_VIS = "Toggle Tool Panel Vis";
    private static final String ACT_KEY_GENERATE_MARKSHEETS = "Generate Marksheets";
    private static final String ACT_KEY_LOAD_MARKS = "Load Marks";
    
    private Map<String,String> studentIds;
    
    /**
     * Hides or shows the tool panel.
     */
    private final Action ACTION_TOGGLE_TOOL_PANEL_VISIBILITY = 
            new AbstractAction(ACT_KEY_TOGGLE_TOOL_PANEL_VIS) {
        {
            updateUiState();
        }
        
        private void updateUiState() {
            boolean vis = true;
            if (toolPanelInternalFrame!=null) {
                vis = toolPanelInternalFrame.isVisible();
            }
            
            if (vis) {
                this.putValue(Action.NAME, "Hide Tools");
                this.putValue(Action.SHORT_DESCRIPTION, "Hide the tool panel.");
                this.putValue(Action.LONG_DESCRIPTION, "Hide the tool panel.");
            } else {
                this.putValue(Action.NAME, "Show Tools");
                this.putValue(Action.SHORT_DESCRIPTION, "Show the tool panel.");
                this.putValue(Action.LONG_DESCRIPTION, "Show the tool panel.");
            }
            
            if (showToolPanelBtn!=null) {
                showToolPanelBtn.setSelected(toolPanelInternalFrame.isVisible());
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Performed Action: "+this.getValue(Action.NAME));
            
            boolean vis = toolPanelInternalFrame.isVisible();
            if (vis) {
                Component inFocus = MainFrame.this.getFocusOwner();
                while (inFocus!=null && inFocus!=toolPanelInternalFrame) {
                    inFocus = inFocus.getParent();
                }
                if (inFocus==toolPanelInternalFrame) {
                    showToolPanelBtn.requestFocus();
                }
            }
            
            toolPanelInternalFrame.setVisible(!vis);
            
            if (!vis) {
                if (showToolPanelBtn.isFocusOwner()) {
                    toolPanelInternalFrame.requestFocus();
                }
            }
            
            updateUiState();
        }
    };
    
    /**
     * Generates mark-sheets from comments and marks.
     */
    private final Action ACTION_GENERATE_MARKSHEETS =
            new AbstractAction(ACT_KEY_GENERATE_MARKSHEETS) {
        {
            this.putValue(Action.NAME, "Gen. Marksheets");
            this.putValue(Action.SHORT_DESCRIPTION, "Generate Marksheets.");
            this.putValue(Action.LONG_DESCRIPTION, "Generate Marksheets.");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Performed Action: "+this.getValue(Action.NAME));
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select marksheet template");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.addChoosableFileFilter(new FileExtFilter("doc", "Microsoft Word '97(-2007) Document"));
//            fileChooser.addChoosableFileFilter(new FileExtFilter("docx", "Microsoft Word 2007 Document"));
            
            int res = fileChooser.showOpenDialog(MainFrame.this);
            if (res==JFileChooser.APPROVE_OPTION) {
                File template = fileChooser.getSelectedFile();
                Set<Document> docs = docSectionGrid.getDocuments();
                for (Document doc:docs) {
                    for (DocExtrasStorage extrasStorage:doc.getExtrasStorage()) {
                        try {
                            extrasStorage.generateMarkSheet(template, studentIds);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(
                                    Level.SEVERE, "Error while generating marksheet", ex);
                        } catch (IOException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(
                                    Level.SEVERE, "Error while generating marksheet", ex);
                        }
                    }
                }
            }
        }
    };
    
    /**
     * Loads marks from an excel file.
     */
    private final Action ACTION_LOAD_MARKS =
            new AbstractAction(ACT_KEY_LOAD_MARKS) {
        {
            this.putValue(Action.NAME, "Load Marks");
            this.putValue(Action.SHORT_DESCRIPTION, "Load marks from excel worksheet.");
            this.putValue(Action.LONG_DESCRIPTION, "Load marks from excel worksheet.");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Performed Action: "+this.getValue(Action.NAME));
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select worksheet with marks.");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
//            fileChooser.addChoosableFileFilter(new FileExtFilter("xls", "Microsoft Excel '97(-2007) Worksheet"));
            fileChooser.addChoosableFileFilter(new FileExtFilter("xlsx", "Microsoft Excel 2007 Worksheet"));
            
            int res = fileChooser.showOpenDialog(MainFrame.this);
            if (res==JFileChooser.APPROVE_OPTION) {
                File worksheet = fileChooser.getSelectedFile();
                try {
                    XSSFWorkbook workbook = new XSSFWorkbook(
                            new BufferedInputStream(new FileInputStream(worksheet)));
                    studentIds = loadStudentIds(workbook);
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                            "Error processing marks worksheet", ex);
                }
            }
        }
        
    };
    
    private class ScrollAction extends AbstractAction {
        
        int xScroll, yScroll;

        public ScrollAction(int xScroll, int yScroll, String name) {
            super(name);
            this.xScroll = xScroll;
            this.yScroll = yScroll;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JViewport viewport = containerScrollPane.getViewport();
            Point pos = viewport.getViewPosition();
            int x = pos.x + xScroll;
            int y = pos.y + yScroll;
            if (x<0) {
                x = 0;
            }
            if (y<0) {
                y = 0;
            }
            int maxX = viewport.getView().getWidth() - viewport.getWidth();
            if (x>=maxX) {
                x = maxX;
            }
            int maxY = viewport.getView().getHeight() - viewport.getHeight();
            if (y>=maxY) {
                y = maxY;
            }
            viewport.setViewPosition(new Point(x, y));
        }
        
    }
    
    private BackgroundProcessor bgImageLoader = new BackgroundProcessor(1);
    private SectionVisibilityManager sectionVisibilityManager;
    private DocSecGridPanel docSectionGrid;
    private DocSecGridHeader docSectionHeader;
    private SectionVisibilityPanelManager sectionVisibilityPanelManager;
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        sectionVisibilityManager = new SectionVisibilityManager();
        docSectionGrid = new DocSecGridPanel(bgImageLoader, sectionVisibilityManager);
        docSectionHeader = new DocSecGridHeader(docSectionGrid);
        sectionVisibilityPanelManager = new SectionVisibilityPanelManager();
        
        initComponents();
        
        containerScrollPane.setColumnHeaderView(docSectionHeader);
        containerScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        containerScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        
        if (isMacOSX()) {
            if (enableFullScreenMode(this)) {
                btnFullscreen.setVisible(false);
            }
        } else {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            btnFullscreen.setEnabled(gd.isDisplayChangeSupported());
        }
        
        setScale(1.25);
        
        initShortcuts();
        
        addSections(Collections.singletonList(Constants.FIRST_SEGMENT_ID));
    }
    
    public void addSections(Splitter[] splitters) {
        List<String> ids = new ArrayList<String>(splitters.length);
        for (Splitter splitter:splitters) {
            ids.add(splitter.id);
        }
        addSections(ids);
    }
    
    public void addSections(List<String> sectionIds) {
        sectionVisibilityPanelManager.addSections(sectionIds);
        docSectionGrid.addSections(sectionIds);
    }
    
    private void initShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK),
                ACT_KEY_TOGGLE_TOOL_PANEL_VIS);
        getRootPane().getActionMap().put(
                ACT_KEY_TOGGLE_TOOL_PANEL_VIS,
                ACTION_TOGGLE_TOOL_PANEL_VISIBILITY);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK),
                ACT_KEY_GENERATE_MARKSHEETS);
        getRootPane().getActionMap().put(
                ACT_KEY_GENERATE_MARKSHEETS,
                ACTION_GENERATE_MARKSHEETS);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                ACT_KEY_LOAD_MARKS);
        getRootPane().getActionMap().put(
                ACT_KEY_LOAD_MARKS,
                ACTION_LOAD_MARKS);
        
        String[] dirStr = {"Left", "Right", "Up", "Down"};
        int[][] dirSign = {{-1,0},{1,0},{0,-1},{0,1}};
        int[] dirKeys = {KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,
                           KeyEvent.VK_UP,KeyEvent.VK_DOWN};
        String[] magStr = {"", "Slow", "Fast", "Super Fast"};
        int[] magAmount = {10, 1, 50, 500};
        int[] magModifiers = {0,
            InputEvent.SHIFT_DOWN_MASK,
            InputEvent.ALT_DOWN_MASK, 
            InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK};
        
        for (int dir=0; dir<dirStr.length; ++dir) {
            for (int mag=0; mag<magStr.length; ++mag) {
                String key = "Scroll "+dirStr[dir]+" "+magStr[mag];
                KeyStroke keyStroke = KeyStroke.getKeyStroke(dirKeys[dir], magModifiers[mag]);
                int x = dirSign[dir][0]*magAmount[mag];
                int y = dirSign[dir][1]*magAmount[mag];
                
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        keyStroke, key);
                getRootPane().getActionMap().put(
                        key, new ScrollAction(x, y, key));
            }
        }
    }
    
    public void addDocuments(List<Document> newDocs) {
        for (int i=0; i<newDocs.size(); ++i) {
            Document document = newDocs.get(i);
            docSectionGrid.addDocument(document);
            sectionVisibilityPanelManager.addSections(document.getSectionIds());
            docSectionHeader.addDocument(document);
        }
        this.pack();
    }
    
    private void setScale(double scale) {
        this.sldrMagnification.setValue((int)(scale*100));
        sldrMagnificationStateChanged(null);
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

        toolBarPanel = new javax.swing.JPanel();
        showToolPanelBtn = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        layeredPane = new javax.swing.JLayeredPane();
        containerScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel containerPanel = docSectionGrid;
        toolPanelInternalFrame = new javax.swing.JInternalFrame();
        toolPanel = new javax.swing.JPanel();
        btnFullscreen = new javax.swing.JToggleButton();
        javax.swing.JPanel magnificationPanel = new javax.swing.JPanel();
        sldrMagnification = new javax.swing.JSlider();
        lblCurrentScale = new javax.swing.JLabel();
        chkDisplayAll = new javax.swing.JCheckBox();
        chkDisplayMarking = new javax.swing.JCheckBox();
        sectionVisibilityPanelScroller = new javax.swing.JScrollPane();
        sectionVisibilityPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        showToolPanelBtn.setAction(ACTION_TOGGLE_TOOL_PANEL_VISIBILITY);
        showToolPanelBtn.setSelected(true);
        toolBarPanel.add(showToolPanelBtn);

        jButton1.setAction(ACTION_GENERATE_MARKSHEETS);
        toolBarPanel.add(jButton1);

        jButton2.setAction(ACTION_LOAD_MARKS);
        toolBarPanel.add(jButton2);

        getContentPane().add(toolBarPanel, java.awt.BorderLayout.NORTH);

        layeredPane.setPreferredSize(new java.awt.Dimension(800, 600));
        layeredPane.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
            }
            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
                layeredPaneAncestorResized(evt);
            }
        });

        containerScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        containerScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        containerScrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
        containerScrollPane.setViewportView(containerPanel);

        containerScrollPane.setBounds(0, 0, 400, 300);
        layeredPane.add(containerScrollPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        toolPanelInternalFrame.setVisible(true);

        toolPanel.setPreferredSize(new java.awt.Dimension(331, 30));
        toolPanel.setLayout(new java.awt.GridBagLayout());

        btnFullscreen.setText("Fullscreen");
        btnFullscreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFullscreenActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        toolPanel.add(btnFullscreen, gridBagConstraints);

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
        toolPanel.add(magnificationPanel, gridBagConstraints);

        chkDisplayAll.setSelected(true);
        chkDisplayAll.setText("Display All");
        chkDisplayAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisplayAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        toolPanel.add(chkDisplayAll, gridBagConstraints);

        chkDisplayMarking.setSelected(true);
        chkDisplayMarking.setText("Display Marking Panels");
        chkDisplayMarking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisplayMarkingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        toolPanel.add(chkDisplayMarking, gridBagConstraints);

        sectionVisibilityPanelScroller.setPreferredSize(new java.awt.Dimension(250, 250));

        sectionVisibilityPanel.setLayout(new java.awt.GridLayout(1, 0));
        sectionVisibilityPanelScroller.setViewportView(sectionVisibilityPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        toolPanel.add(sectionVisibilityPanelScroller, gridBagConstraints);

        toolPanelInternalFrame.getContentPane().add(toolPanel, java.awt.BorderLayout.CENTER);

        toolPanelInternalFrame.setBounds(0, 0, 310, 450);
        layeredPane.add(toolPanelInternalFrame, javax.swing.JLayeredPane.MODAL_LAYER);

        getContentPane().add(layeredPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFullscreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFullscreenActionPerformed
        
        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (btnFullscreen.isSelected()) {
            gd.setFullScreenWindow(this);
        } else {
            gd.setFullScreenWindow(null);
        }        
        
    }//GEN-LAST:event_btnFullscreenActionPerformed

    private void sldrMagnificationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldrMagnificationStateChanged
        
        int sldrVal = sldrMagnification.getValue();
        
        double newScale = sldrVal / 100.0;

        if (newScale>Constants.MAX_SCALE) {
            return;
        }
        if (newScale<Constants.MIN_SCALE) {
            return;
        }

        lblCurrentScale.setText(String.format("x%.2f", newScale));
        docSectionGrid.setScale(newScale);
        
    }//GEN-LAST:event_sldrMagnificationStateChanged

    private void layeredPaneAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_layeredPaneAncestorResized
        
        Dimension sz = layeredPane.getSize();
        
        containerScrollPane.setSize(sz);
        
        int toolPanelX = toolPanelInternalFrame.getX();
        int toolPanelY = toolPanelInternalFrame.getY();
        if (toolPanelX+toolPanelInternalFrame.getWidth()>sz.width) {
            toolPanelX = sz.width - toolPanelInternalFrame.getWidth();
        }
        if (toolPanelY+toolPanelInternalFrame.getHeight()>sz.height) {
            toolPanelY = sz.height - toolPanelInternalFrame.getHeight();
        }
        toolPanelInternalFrame.setLocation(toolPanelX, toolPanelY);
        
        layeredPane.revalidate();
        
    }//GEN-LAST:event_layeredPaneAncestorResized

    private void chkDisplayAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisplayAllActionPerformed
        
        updateSectionVisibility();
        
    }//GEN-LAST:event_chkDisplayAllActionPerformed

    private void chkDisplayMarkingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisplayMarkingActionPerformed
        
        updateSectionVisibility();
        
    }//GEN-LAST:event_chkDisplayMarkingActionPerformed

    
    private void updateSectionVisibility() {
        if (docSectionGrid.updateSectionVisibility(chkDisplayAll.isSelected(),
                chkDisplayMarking.isSelected())) {
            docSectionHeader.revalidate();
            containerScrollPane.revalidate();
        }
    }
    
    private static Map<String,String> loadStudentIds(XSSFWorkbook workbook) {
        //  TODO: Generelize this code. This code is too particular to
        //          the current worksheet.

        Map<String,String> studentIds = new TreeMap<String, String>();
        XSSFSheet sheet = workbook.getSheetAt(0);
        for (int i=0; i<78; ++i) {
            int rowIndex = 2+i;
            XSSFRow row = sheet.getRow(rowIndex);

            if (row==null) {
                System.out.println("row "+rowIndex+" is NULL");
                continue;
            }

            String id = row.getCell(1).getStringCellValue();
            String name = row.getCell(2).getStringCellValue();

            System.out.println(">> "+name+":"+id);

            studentIds.put(name, id);
        }

        return studentIds;
    }

    private void loadMarks(XSSFWorkbook workbook) {
        //  TODO: Generelize this code. This code is too particular to
        //          the current worksheet.
        XSSFSheet sheet = workbook.getSheetAt(0);
        for (int i=0; i<17; ++i) {
            XSSFRow row = sheet.getRow(3+i);

            String name = row.getCell(0).getStringCellValue();
            double q1Mark = row.getCell(4).getNumericCellValue();
            double q2Mark = row.getCell(7).getNumericCellValue();
            double q3Mark = row.getCell(11).getNumericCellValue();
            double q4Mark = row.getCell(15).getNumericCellValue();
            double q5Mark = row.getCell(18).getNumericCellValue();
            double q6Mark = row.getCell(21).getNumericCellValue();
            double q7Mark = row.getCell(27).getNumericCellValue();
            double q8Mark = row.getCell(40).getNumericCellValue();

            Set<Document> docs = docSectionGrid.getDocuments();
            for (Document doc:docs) {
                String source = doc.getSource();
                source = source.substring(source.indexOf('-')+1);
                if (!source.startsWith(name)) {
                    continue;
                }
                System.out.println(">> found: "+source+" ("+name+")");

                for (String section:doc.getSectionIds()) {
                    DocumentSection documentSection = doc.getSection(section);
                    if (section.startsWith("Q1:")) {
                        documentSection.setMark(q1Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q2:")) {
                        documentSection.setMark(q2Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q3:")) {
                        documentSection.setMark(q3Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q4:")) {
                        documentSection.setMark(q4Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q5:")) {
                        documentSection.setMark(q5Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q6:")) {
                        documentSection.setMark(q6Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q7:")) {
                        documentSection.setMark(q7Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                    if (section.startsWith("Q8:")) {
                        documentSection.setMark(q8Mark);
                        System.out.println(">> set: "+section+"="+documentSection.getMark());
                        continue;
                    }
                }
            }
        }

    }
    
    public static boolean enableFullScreenMode(Window window) {
        String className = "com.apple.eawt.FullScreenUtilities";
        String methodName = "setWindowCanFullScreen";
 
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, new Class<?>[] {
                    Window.class, boolean.class });
            method.invoke(null, window, true);
            return true;
        } catch (Throwable t) {
            System.err.println("Full screen mode is not supported");
            t.printStackTrace(System.err);
            return false;
        }
    }
    
    private static boolean isMacOSX() {
        return System.getProperty("os.name").indexOf("Mac OS X") >= 0;
    }    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>
        
        File worksheet = new File("/Users/umran/ADFA-stuff/CPS/marking-ass4/classMarks2013.xlsx");
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(
                    new BufferedInputStream(new FileInputStream(worksheet)));
            loadStudentIds(workbook);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE,
                    "Error processing marks worksheet", ex);
        }
        
        
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                MainFrame mainFrame = new MainFrame();
//                mainFrame.setVisible(true);
//            }
//        });
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnFullscreen;
    private javax.swing.JCheckBox chkDisplayAll;
    private javax.swing.JCheckBox chkDisplayMarking;
    private javax.swing.JScrollPane containerScrollPane;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLayeredPane layeredPane;
    private javax.swing.JLabel lblCurrentScale;
    private javax.swing.JPanel sectionVisibilityPanel;
    private javax.swing.JScrollPane sectionVisibilityPanelScroller;
    private javax.swing.JToggleButton showToolPanelBtn;
    private javax.swing.JSlider sldrMagnification;
    private javax.swing.JPanel toolBarPanel;
    private javax.swing.JPanel toolPanel;
    private javax.swing.JInternalFrame toolPanelInternalFrame;
    // End of variables declaration//GEN-END:variables

    
    private class SectionVisibilityPanelManager {
        Map<String, JCheckBox> sectionMap;

        public SectionVisibilityPanelManager() {
            sectionMap = new TreeMap<String, JCheckBox>();
        }
        
        public void addSections(Collection<String> sections) {
            for (final String section:sections) {
                if (!this.sectionMap.containsKey(section)) {
                    final JCheckBox chkBox = new JCheckBox(section);
                    chkBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            sectionVisibilityManager.setVibility(section, chkBox.isSelected());
                            updateSectionVisibility();
                        }
                    });
                    
                    Dimension sz = new Dimension(200, 30);
                    chkBox.setPreferredSize(sz);
                    chkBox.setMinimumSize(sz);
                    
                    GridLayout grid = (GridLayout)sectionVisibilityPanel.getLayout();
                    grid.setRows(sectionVisibilityPanel.getComponentCount()+1);
                    
                    sectionVisibilityManager.setVibility(section, false);
                    sectionVisibilityPanel.add(chkBox);
                    sectionMap.put(section, chkBox);
                }
            }
            
            sectionVisibilityPanelScroller.revalidate();
        }
        
        public JCheckBox getCheckBox(String section) {
            return sectionMap.get(section);
        }
        
    }

}
