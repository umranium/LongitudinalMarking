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
public class DisplayMarkingSheets {
    
    private static final File[] ROOT_FOLDERS = new File[]{
        new File("/Users/umran/ADFA-stuff/CPS/marking-ass2/Group1"),
        //new File("/Users/umran/ADFA-stuff/CPS/marking-ass2/Group1"),
    };
    
    private static class MarkingSheetSplitter extends Splitter {

        public MarkingSheetSplitter(String id, String text, Double requiredAccuracy) {
            super(id, text, requiredAccuracy, null, null, 2448.0, 16.0, 82.0, 3168.0);
        }
        
    }
    
    private static final MarkingSheetSplitter[] SPLITTERS = new MarkingSheetSplitter[] {
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
    
    private static final MarkingSheetSplitter[] SPLITTERS_OLD = new MarkingSheetSplitter[] {
        new MarkingSheetSplitter("Q1:Mark-Old","Two-step Colour Gradient", 0.75),
        new MarkingSheetSplitter("Q2:Mark-Old","Pixellate an Image", 0.75),
        new MarkingSheetSplitter("Q3:Mark-Old","Pixellate a Face in a Video", 0.75),
        new MarkingSheetSplitter("Q4:Mark-Old","Book Ended Zeroes", 0.75),
        new MarkingSheetSplitter("Q5:Mark-Old","Alphabet Soup", 0.75),
        new MarkingSheetSplitter("Q6:Mark-Old","Roughly Same Number of 0s and 1s", 0.75),
        new MarkingSheetSplitter("Q7:Mark-Old","Parity Check", 0.75),
        new MarkingSheetSplitter("Q8:Mark-Old","Divisible by 4, Even Symbols", 0.75),
        new MarkingSheetSplitter("Q9:Comments-Old","Other/General Feedback & Comments", 0.75),
    };
    
    private static class FileEntry {
        final String source;
        final File markSheet;
        final File oldMarksheet;

        public FileEntry(String source, File markSheet, File oldMarksheet) {
            this.source = source;
            this.markSheet = markSheet;
            this.oldMarksheet = oldMarksheet;
        }
        
        
    }
    
    public static void main(String[] args) {

        List<FileEntry> foundFiles = new ArrayList<FileEntry>();
        
        for (File rootDir:ROOT_FOLDERS) {
            for (File dir:rootDir.listFiles()) {
                if (!dir.isDirectory()) {
                    continue;
                }

                File markSheet = new File(dir, "markingSheet.pdf");
                if (!markSheet.exists()) {
                    throw new RuntimeException("Mark Sheet does not exist in folder: "+dir);
                }
                
                File oldMarkSheet = new File(dir, "markingSheet-old.pdf");
                if (!oldMarkSheet.exists()) {
                    throw new RuntimeException("Old Mark Sheet does not exist in folder: "+dir);
                }
                
                foundFiles.add(new FileEntry(
                        rootDir.getName()+":"+dir.getName(),
                        markSheet, oldMarkSheet));
            }
        }
        
//        while (foundFiles.size()>5) {
//            foundFiles.remove(foundFiles.size()-1);
//        }
        
        System.out.println("Found "+foundFiles.size()+" PDF files.");

        final List<Document> documents = new ArrayList<Document>();
        
        for (int i=0; i<foundFiles.size(); ++i) {
            FileEntry fileEntry = foundFiles.get(i);
            
            Document document = null;
            
            try {
                DocumentExtractor extractor = new DocumentExtractor(
                        fileEntry.source, fileEntry.markSheet, SPLITTERS);
                document = extractor.extract();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
                continue;
            } catch (MissingSplitter ex) {
                ex.printStackTrace(System.out);
                continue;
            }
            
            try {
                DocumentExtractor extractor = new DocumentExtractor(
                        fileEntry.source, fileEntry.oldMarksheet, SPLITTERS_OLD);
                Document doc2 = extractor.extract();
                document = Document.merge(fileEntry.source, document, doc2);
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
                continue;
            } catch (    MissingSplitter ex) {
                ex.printStackTrace(System.out);
                continue;
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
