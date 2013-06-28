/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.storage;

/**
 *
 * @author umran
 */
public class MalformedDataFileException extends Exception {

    public MalformedDataFileException() {
    }

    public MalformedDataFileException(String message) {
        super(message);
    }

    public MalformedDataFileException(Throwable cause) {
        super(cause);
    }

    public MalformedDataFileException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
