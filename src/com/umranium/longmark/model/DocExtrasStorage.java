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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
import org.apache.poi.hwpf.usermodel.Bookmarks;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Field;
import org.apache.poi.hwpf.usermodel.Fields;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;

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
    
    private static final Pattern TAG_PATTERN = Pattern.compile("\\{\\{.*\\}\\}");
    
    public void generateMarkSheet(File template, Map<String,String> studentIds) throws FileNotFoundException, IOException {
        InputStream templateInput = new BufferedInputStream(
                new FileInputStream(template),
                (int)template.length());
        
        try {
            HWPFDocument inputSheet = new HWPFDocument(
                    templateInput
                    );
            
            double totalMark = 0.0;
            Map<String,String> tagToValueMap = new TreeMap<String, String>();
            for (Map.Entry<String,SectionAnnotation> entry:secAnnotations.entrySet()) {
                SectionAnnotation ann = entry.getValue();

                String sectionId = entry.getKey();
                
                //  TODO: No need for this in future
                sectionId = sectionId.substring(0, sectionId.indexOf(':'));

                String commentTag = "{{"+sectionId+":comment}}";
                tagToValueMap.put(commentTag, ann.getComments());

                String markTag = "{{"+sectionId+":mark}}";;
                tagToValueMap.put(markTag, Double.toString(ann.getMark()));
                
                totalMark += ann.getMark();
            }
            
            String name = file.getParentFile().getName();
            
            //  TODO: No need of this in future
            name = name.substring(name.indexOf('-')+1);
            
            tagToValueMap.put("{{name}}", name);
            tagToValueMap.put("{{total}}", Double.toString(totalMark));
            if (studentIds!=null && studentIds.containsKey(name)) {
                tagToValueMap.put("{{studentID}}", studentIds.get(name));
            } else {
                System.out.println(">>ID of "+name+" not found.");
            }
            
            Range range = inputSheet.getRange();
            for (int p=0; p<range.numParagraphs(); ++p) {
                Paragraph paragraph = range.getParagraph(p);
                
                String text = paragraph.text();
                
                Matcher matcher = TAG_PATTERN.matcher(text);
                while (matcher.find()) {
                    String match = matcher.group();
                    
                    String value = "";
                    if (tagToValueMap.containsKey(match)) {
                        value = tagToValueMap.get(match);
                    }
                    
                    paragraph.replaceText(match, value);
                }
            }
            
            File outMarksheetFile = new File(file.getParent(), name+".doc");
            inputSheet.write(new FileOutputStream(outMarksheetFile));
        } finally {
            templateInput.close();
        }
    }
    
}
