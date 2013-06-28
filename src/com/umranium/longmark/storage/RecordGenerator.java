/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.storage;

/**
 *
 * @author umran
 */
public interface RecordGenerator<KeyType, RecordType> {
    
    public RecordType createNew(KeyType key);
    
}
