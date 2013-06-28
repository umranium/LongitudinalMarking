/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.common;

import java.io.File;

/**
 *
 * @author umran
 */
public class FileNameHelper {
    
    private static final char DOT = '.';
    
    public static File replaceExtension(File file, String nameAppenditure, String newExt) {
        String filename = file.getName();
        StringBuffer buffer = new StringBuffer(filename.length()+
                (nameAppenditure!=null?nameAppenditure.length():0));
        buffer.append(filename.substring(0, filename.lastIndexOf(DOT)));
        if (nameAppenditure!=null) {
            buffer.append(nameAppenditure);
        }
        buffer.append(DOT);
        buffer.append(newExt);
        return new File(file.getParentFile(), buffer.toString());
    }
    
}
