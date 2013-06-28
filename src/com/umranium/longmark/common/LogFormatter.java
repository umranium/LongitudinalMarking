/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Umran
 */
public class LogFormatter extends Formatter {
    
    @Override
    public String format(LogRecord record) {
        if (record.getThrown()!=null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            printWriter.println(record.getMessage());
            record.getThrown().printStackTrace(printWriter);
            printWriter.close();
            return stringWriter.getBuffer().toString();
        } else {
            return record.getMessage();
        }
    }
    
}
