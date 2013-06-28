/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.common;

/**
 *
 * @author umran
 */
public interface AsyncResultsReceiver<T> {
    
    boolean stillValid();
    void onResults(T result);
    void onCancelled();
    void onException(Exception e);
    
}
