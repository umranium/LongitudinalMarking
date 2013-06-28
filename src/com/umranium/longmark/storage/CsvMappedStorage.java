/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.storage;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author umran
 */
public class CsvMappedStorage<KeyType, RecordType> {

    private ColumnDefinition<KeyType, RecordType>[] columnDefinitions;
    private int keyColumn;
    
    public CsvMappedStorage(ColumnDefinition<KeyType, RecordType>[] columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
        
        keyColumn = -1;
        for (int i=0; i<columnDefinitions.length; ++i) {
            if (columnDefinitions[i].isKey()==true) {
                keyColumn = i;
                break;
            }
        }
        
        if (keyColumn<0) {
            throw new RuntimeException("Key column definition NOT found.");
        }
    }
    
    public void load(File file, Map<KeyType,RecordType> outputMap, RecordGenerator<KeyType, RecordType> recGen) throws FileNotFoundException, MalformedDataFileException, IOException {
        outputMap.clear();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            final int columnCount = columnDefinitions.length;
            
            String[] headers = reader.readNext();
            if (headers==null) {
                headers = new String[0];
            }
            
            Integer[] defToCsvColIndex = new Integer[columnCount];
            
            for (int c=0; c<columnCount; ++c) {
                ColumnDefinition<KeyType, RecordType> columnDefinition = columnDefinitions[c];
                for (int h=0; h<headers.length; ++h) {
                    if (columnDefinition.getTitle().equals(headers[h])) {
                        defToCsvColIndex[c] = h;
                        break;
                    }
                }
            }
            
            String[] values;
            while ((values = reader.readNext())!=null) {
                String keyString = values[defToCsvColIndex[keyColumn]];
                KeyType key = columnDefinitions[defToCsvColIndex[keyColumn]].parseKey(keyString);
                RecordType record = recGen.createNew(key);
                
                for (int c=0; c<columnCount; ++c)
                    if (c!=keyColumn) {
                        ColumnDefinition<KeyType, RecordType> columnDefinition = columnDefinitions[c];
                        Integer csvColIndex = defToCsvColIndex[c];
                        if (csvColIndex==null) {
                            continue;
                        }
                        columnDefinition.readAndSetInputValue(
                                values[defToCsvColIndex[c]], key, record);
                    }
                
                outputMap.put(key, record);
            }
            
        }
    }
    
    public void save(File file, Map<KeyType,RecordType> inputMap) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            final int columnCount = columnDefinitions.length;
            String[] values = new String[columnCount];
            
            for (int c=0; c<columnCount; ++c) {
                ColumnDefinition<KeyType,RecordType> columnDefinition =
                        columnDefinitions[c];
                values[c] = columnDefinition.getTitle();
            }
            writer.writeNext(values);
            
            for (Map.Entry<KeyType,RecordType> entry:inputMap.entrySet()) {
                Arrays.fill(values, null);
                KeyType key = entry.getKey();
                RecordType record = entry.getValue();
                
                for (int c=0; c<columnCount; ++c) {
                    ColumnDefinition<KeyType,RecordType> columnDefinition =
                            columnDefinitions[c];
                    if (columnDefinition.isKey()) {
                        values[c] = columnDefinition.keyToStr(key);
                    } else {
                        values[c] = columnDefinition.getOutputValue(key, record);
                    }
                }
                
                writer.writeNext(values);
            }
        }
    }
    
    
}
