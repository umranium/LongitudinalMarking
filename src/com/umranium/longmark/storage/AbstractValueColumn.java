/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.storage;

/**
 *
 * @author umran
 */
public abstract class AbstractValueColumn<KeyType, RecordType> implements ColumnDefinition<KeyType, RecordType> {
    
    private String title;

    public AbstractValueColumn(String title) {
        this.title = title;
    }
    
    @Override
    public boolean isKey() {
        return false;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public final KeyType parseKey(String keyString) {
        throw new UnsupportedOperationException("This should not have been called.");
    }

    @Override
    public final String keyToStr(KeyType valueString) {
        throw new UnsupportedOperationException("This should not have been called.");
    }

    @Override
    public abstract String getOutputValue(KeyType key, RecordType record);

    @Override
    public abstract void readAndSetInputValue(String value, KeyType key, RecordType record);
    
    
}
