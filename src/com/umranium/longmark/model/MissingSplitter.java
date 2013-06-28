/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

/**
 *
 * @author umran
 */
public class MissingSplitter extends Exception {

    public MissingSplitter(Splitter splitter) {
        this("Splitter not found: "+splitter.id+" ("+splitter.text+")");
    }

    public MissingSplitter(String message) {
        super(message);
    }

    public MissingSplitter(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingSplitter(Throwable cause) {
        super(cause);
    }

    public MissingSplitter(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
