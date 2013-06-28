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
    private List<DocumentSection> documentSections;
    private Map<String,DocumentSection> documentSectionMap;

    public Document(String source, List<DocumentSection> documentSections) {
        this.source = source;
        this.documentSections = documentSections;
        this.documentSectionMap = new TreeMap<>();
        
        for (DocumentSection section:documentSections) {
            if (section.getId()!=null) {
                documentSectionMap.put(section.getId(), section);
            }
            section.setDocument(this);
        }
    }
    
    public static Document merge(String source, Document... docs) {
        List<DocumentSection> sections = new ArrayList<>();
        for (Document doc:docs) {
            sections.addAll(doc.getSections());
        }
        return new Document(source, sections);
    }

    public String getSource() {
        return source;
    }
    
    public List<DocumentSection> getSections() {
        return documentSections;
    }
    
    public DocumentSection getSection(String splitId) {
        return documentSectionMap.get(splitId);
    }
    
    public Set<String> getSectionIds() {
        return documentSectionMap.keySet();
    }
    
    
}
