/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.storage;

/**
 *
 * @author umran
 */
public abstract class AbstractKeyColumn<KeyType> implements ColumnDefinition<KeyType, Object> {
    
    private String title;

    public AbstractKeyColumn(String title) {
        this.title = title;
    }

    @Override
    public boolean isKey() {
        return true;
    }

    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public abstract KeyType parseKey(String keyString);

    @Override
    public abstract String keyToStr(KeyType valueString);

    @Override
    public final String getOutputValue(KeyType key, Object record) {
        throw new UnsupportedOperationException("This should not have been called.");
    }

    @Override
    public final void readAndSetInputValue(String value, KeyType key, Object record) {
        throw new UnsupportedOperationException("This should not have been called.");
    }
    
}
