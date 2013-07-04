/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.common;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author umran
 */
public class FileExtFilter extends FileFilter {
    
    private String ext;
    private String descr;

    public FileExtFilter(String ext, String descr) {
        if (ext==null || ext.isEmpty()) {
            throw new RuntimeException("Extention not given.");
        }
        this.ext = ext;
        if (this.ext.charAt(0)!='.') {
            this.ext = "." + this.ext;
        }
        this.descr = descr;
    }
    
    @Override
    public boolean accept(File f) {
        return f.getName().endsWith(ext);
    }

    @Override
    public String getDescription() {
        return descr;
    }
    
}
