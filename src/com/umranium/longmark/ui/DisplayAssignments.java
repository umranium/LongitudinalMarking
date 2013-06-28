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
public class DisplayAssignments {
    
    private static final File ROOT_FOLDER = new File("/Users/umran/ADFA-stuff/CPS/marking-ass2/Group1");
    
    private static final Splitter[] SPLITTERS = new Splitter[] {
        new Splitter("Q1:Answer","<<[TAG:Question1]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q2:Answer","<<[TAG:Question2]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q3:Answer","<<[TAG:Question3]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q4:Answer","<<[TAG:Question4]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q5:Answer","<<[TAG:Question5]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q6:Answer","<<[TAG:Question6]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q7:Answer","<<[TAG:Question7]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
        new Splitter("Q8:Answer","<<[TAG:Question8]>>", 0.5, null, null, 2380.0, 16.0, 80.0, 3368.0),
    };
    
    public static void main(String[] args) {

        List<File> markingSheets = new ArrayList<>();
        
        for (File dir:ROOT_FOLDER.listFiles()) {
            if (!dir.isDirectory()) {
                continue;
            }
            
            File[] pdfFiles = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (!pathname.isFile()) {
                        return false;
                    }
                    
                    String filename = pathname.getName();
                    
                    if (!filename.equals("tagged_assignment.pdf")) {
                        return false;
                    }
                    
                    return true;
                }
            });
            
            for (File f:pdfFiles) {
                markingSheets.add(f);
            }
        }
        
//        while (markingSheets.size()>5) {
//            markingSheets.remove(markingSheets.size()-1);
//        }
        
        System.out.println("Found "+markingSheets.size()+" PDF files.");

        final List<Document> documents = new ArrayList<>();
        
        for (int i=0; i<markingSheets.size(); ++i) {
            File pdf = markingSheets.get(i);
            
            try {
                DocumentExtractor extractor = new DocumentExtractor(
                        pdf.getParentFile().getName(), pdf, SPLITTERS);
                Document document = extractor.extract();
                documents.add(document);
                System.gc();
            } catch (IOException ex) {
                System.out.println("Error while processing: "+pdf);
                ex.printStackTrace(System.out);
            } catch (MissingSplitter ex) {
                System.out.println("Error while processing: "+pdf);
                ex.printStackTrace(System.out);
            }
            
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
