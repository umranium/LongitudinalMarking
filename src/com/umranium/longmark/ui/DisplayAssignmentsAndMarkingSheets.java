/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui;

import com.umranium.longmark.model.Document;
import com.umranium.longmark.model.DocumentExtractor;
import com.umranium.longmark.model.MissingSplitter;
import com.umranium.longmark.model.Splitter;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author umran
 */
public class DisplayAssignmentsAndMarkingSheets {
    
    private static final File[] ROOT_FOLDER = new File[]{
        new File("/Users/umran/ADFA-stuff/CPS/marking-ass2/Group1"),
        new File("/Users/umran/ADFA-stuff/CPS/marking-ass2/Group2"),
    };
    
    private static class MarkingSheetSplitter extends Splitter {

        public MarkingSheetSplitter(String id, String text, Double requiredAccuracy) {
            super(id, text, requiredAccuracy, null, null, 2448.0, 16.0, 82.0, 3168.0);
        }
        
    }
    
    private static final Splitter[] ASSIGNMENT_SPLITTERS = new Splitter[] {
        new Splitter("Q1:Answer","<<[TAG:Question1]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q2:Answer","<<[TAG:Question2]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q3:Answer","<<[TAG:Question3]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q4:Answer","<<[TAG:Question4]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q5:Answer","<<[TAG:Question5]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q6:Answer","<<[TAG:Question6]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q7:Answer","<<[TAG:Question7]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
        new Splitter("Q8:Answer","<<[TAG:Question8]>>", 0.5, null, null, 2380.0, 20.0, 100.0, 3368.0),
    };
    
    private static final MarkingSheetSplitter[] MARK_SHEET_SPLITTERS = new MarkingSheetSplitter[] {
        new MarkingSheetSplitter("Q1:Mark","Two-step Colour Gradient", 0.75),
        new MarkingSheetSplitter("Q2:Mark","Pixellate an Image", 0.75),
        new MarkingSheetSplitter("Q3:Mark","Pixellate a Face in a Video", 0.75),
        new MarkingSheetSplitter("Q4:Mark","Book Ended Zeroes", 0.75),
        new MarkingSheetSplitter("Q5:Mark","Alphabet Soup", 0.75),
        new MarkingSheetSplitter("Q6:Mark","Roughly Same Number of 0s and 1s", 0.75),
        new MarkingSheetSplitter("Q7:Mark","Parity Check", 0.75),
        new MarkingSheetSplitter("Q8:Mark","Divisible by 4, Even Symbols", 0.75),
        new MarkingSheetSplitter("Q9:Comments","Other/General Feedback & Comments", 0.75),
    };
    
    private static final String ASSIGNMENT_FILE_NAME = "tagged_assignment.pdf";
    private static final String MARKSHEET_FILE_NAME = "markingSheet.pdf";
    
    private static class AssignmentAndMarkSheet {
        final String source;
        final File assignment;
        final File markSheet;

        public AssignmentAndMarkSheet(String source, File assignment, File markSheet) {
            this.source = source;
            this.assignment = assignment;
            this.markSheet = markSheet;
        }
    }
    
    public static void main(String[] args) {

        List<AssignmentAndMarkSheet> foundFiles = new ArrayList<>();

        for (File rootDir:ROOT_FOLDER) {
            for (File dir:rootDir.listFiles()) {
                if (!dir.isDirectory()) {
                    continue;
                }
                
                if (dir.getName().equals("John Wilbraham_21475_assignsubmission_file_Ass2")) {
                    continue;
                }

                File assignment = new File(dir, ASSIGNMENT_FILE_NAME);

                if (!assignment.exists()) {
                    throw new RuntimeException("No assignmnet found in folder: "+dir);
                }

                File markSheet = new File(dir, MARKSHEET_FILE_NAME);

                if (!markSheet.exists()) {
                    //throw new RuntimeException("No mark sheet found in folder: "+dir);
                    markSheet = null;
                }


                foundFiles.add(new AssignmentAndMarkSheet(
                        dir.getParentFile().getName()+":"+dir.getName(), 
                        assignment, markSheet));
            }
        }
        
//        while (foundFiles.size()>3) {
//            foundFiles.remove(foundFiles.size()-1);
//        }
        
        System.out.println("Found "+foundFiles.size()+" student entries.");

        final List<Document> documents = new ArrayList<>();
        
        for (int i=0; i<foundFiles.size(); ++i) {
            AssignmentAndMarkSheet aams = foundFiles.get(i);
            
//            if (!aams.source.contains("Brenden"))
//                continue;
            
            Document document = null;
            try {
                DocumentExtractor extractor = new DocumentExtractor(
                        aams.source, aams.assignment, ASSIGNMENT_SPLITTERS);
                document = extractor.extract();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
                continue;
            } catch (    MissingSplitter ex) {
                ex.printStackTrace(System.out);
                continue;
            }
            
            if (aams.markSheet!=null) {
                try {
                    DocumentExtractor extractor = new DocumentExtractor(
                            aams.source, aams.markSheet, MARK_SHEET_SPLITTERS);
                    Document sheetDocument = extractor.extract();

                    document = Document.merge(aams.source, document, sheetDocument);
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                    continue;
                } catch (        MissingSplitter ex) {
                    ex.printStackTrace(System.out);
                    continue;
                }
            }
            
            documents.add(document);
        }
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                mainFrame.addDocuments(documents);
            }
        });
    }
}
