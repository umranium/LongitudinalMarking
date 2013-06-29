/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author umran
 */
public class SectionVisibilityManager {
    
    private Map<String,Boolean> sectionVibility;

    public SectionVisibilityManager() {
        sectionVibility = new TreeMap<String,Boolean>();
    }
    
    public boolean hasSection(String section) {
        return sectionVibility.containsKey(section);
    }
    
    public boolean isVisible(String section) {
        if (!sectionVibility.containsKey(section)) {
            return false;
        }
        return sectionVibility.get(section);
    }

    public void setVibility(String section, boolean vis) {
        this.sectionVibility.put(section, vis);
    }
    
    
    
}
