/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.storage;

/**
 *
 * @author umran
 */
public interface ColumnDefinition<KeyType, RecordType> {

    boolean isKey();

    String getTitle();
    
    //  for keys
    KeyType parseKey(String keyString);
    
    String keyToStr(KeyType valueString);

    //  for values
    String getOutputValue(KeyType key, RecordType record);

    void readAndSetInputValue(String value, KeyType key, RecordType record);
    
    
    public static class StringKeyColumn extends AbstractKeyColumn<String> {

        public StringKeyColumn(String title) {
            super(title);
        }
        
        @Override
        public String parseKey(String keyString) {
            return keyString;
        }

        @Override
        public String keyToStr(String valueString) {
            return valueString;
        }
        
    }

    public static class LongKeyColumn extends AbstractKeyColumn<Long> {

        public LongKeyColumn(String title) {
            super(title);
        }
        
        @Override
        public Long parseKey(String keyString) {
            return Long.parseLong(keyString);
        }

        @Override
        public String keyToStr(Long valueString) {
            return Long.toString(valueString);
        }
        
    }
}
