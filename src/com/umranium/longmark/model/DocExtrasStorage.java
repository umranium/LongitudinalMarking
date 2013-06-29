/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import au.com.bytecode.opencsv.CSVReader;
import com.umranium.longmark.storage.AbstractValueColumn;
import com.umranium.longmark.storage.ColumnDefinition;
import com.umranium.longmark.storage.CsvMappedStorage;
import com.umranium.longmark.storage.MalformedDataFileException;
import com.umranium.longmark.storage.RecordGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author umran
 */
public class DocExtrasStorage {
    
    private static ColumnDefinition<String,SectionAnnotation>[] COL_DEFS =
            new ColumnDefinition[] {
        new ColumnDefinition.StringKeyColumn("Section Id"),
        new AbstractValueColumn<String,SectionAnnotation>("Comment") {
            @Override
            public String getOutputValue(String key, SectionAnnotation record) {
                return record.getComments();
            }

            @Override
            public void readAndSetInputValue(String value, String key, SectionAnnotation record) {
                record.setComments(value);
            }
        },
        new AbstractValueColumn<String,SectionAnnotation>("Mark") {
            @Override
            public String getOutputValue(String key, SectionAnnotation record) {
                return Double.toString(record.getMark());
            }

            @Override
            public void readAndSetInputValue(String value, String key, SectionAnnotation record) {
                record.setMark(Double.parseDouble(value));
            }
        },
    };
    
    private static RecordGenerator<String,SectionAnnotation> REC_GEN =
            new RecordGenerator<String, SectionAnnotation>() {
        @Override
        public SectionAnnotation createNew(String key) {
            return new SectionAnnotation();
        }
    };
    
    private File file;
    private Map<String,SectionAnnotation> secAnnotations;
    private CsvMappedStorage csvStorage = new CsvMappedStorage(COL_DEFS);

    public DocExtrasStorage(File csvFile) {
        this.file = csvFile;
        secAnnotations = new HashMap<String,SectionAnnotation>();
    }
    
    synchronized 
    public void load() throws FileNotFoundException, IOException, MalformedDataFileException {
        csvStorage.load(file, secAnnotations, REC_GEN);
    }
    
    synchronized 
    public void save() throws IOException {
        csvStorage.save(file, secAnnotations);
    }
    
    synchronized 
    public void set(String sectionId, SectionAnnotation annotation) {
        secAnnotations.put(sectionId, annotation);
    }

    public boolean hasSection(String sectionId) {
        return secAnnotations.containsKey(sectionId);
    }
    
    public SectionAnnotation get(String sectionId) {
        return secAnnotations.get(sectionId);
    }
    
}
