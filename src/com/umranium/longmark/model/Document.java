/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Represents an input document. A document consists of named sections.
 * 
 * @author umran
 */
public class Document {
    
    private String source;
    private List<DocExtrasStorage> extrasStorage;
    private List<DocumentSection> documentSections;
    private Map<String,DocumentSection> documentSectionMap;

    public Document(String source, List<DocExtrasStorage> extrasStorage,
            List<DocumentSection> documentSections) {
        this.source = source;
        this.extrasStorage = extrasStorage;
        this.documentSections = documentSections;
        this.documentSectionMap = new TreeMap<String,DocumentSection>();
        
        initSectionMap();
    }
    
    private void initSectionMap() {
        for (DocumentSection section:this.documentSections) {
            if (section.getId()!=null) {
                documentSectionMap.put(section.getId(), section);
            }
            section.setDocument(this);
        }
    }
    
    public static Document merge(String source, Document... docs) {
        List<DocumentSection> sections = new ArrayList<DocumentSection>();
        List<DocExtrasStorage> extraStorage = new ArrayList<DocExtrasStorage>();
        for (Document doc:docs) {
            sections.addAll(doc.getSections());
            extraStorage.addAll(doc.getExtrasStorage());
        }
        return new Document(source, extraStorage, sections);
    }
    
    public String getSource() {
        return source;
    }

    public List<DocumentSection> getSections() {
        return documentSections;
    }

    public List<DocExtrasStorage> getExtrasStorage() {
        return extrasStorage;
    }
    
    public DocumentSection getSection(String splitId) {
        return documentSectionMap.get(splitId);
    }
    
    public Set<String> getSectionIds() {
        return documentSectionMap.keySet();
    }
    
    
}
