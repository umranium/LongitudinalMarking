/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.umranium.longmark.common;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Umran
 */
public class ExtendedTreeMap<KeyType,ValueType> extends TreeMap<KeyType,ValueType> {

    private MappedInstanceCreator<KeyType,ValueType> valueInstanceCreator;

    public ExtendedTreeMap( SortedMap<KeyType, ? extends ValueType> m, MappedInstanceCreator<KeyType,ValueType> valueInstanceCreator ) {
        super(m);
        this.valueInstanceCreator = valueInstanceCreator;
    }

    public ExtendedTreeMap( Map<? extends KeyType, ? extends ValueType> m, MappedInstanceCreator<KeyType,ValueType> valueInstanceCreator ) {
        super(m);
        this.valueInstanceCreator = valueInstanceCreator;
    }

    public ExtendedTreeMap( Comparator<? super KeyType> comparator, MappedInstanceCreator<KeyType,ValueType> valueInstanceCreator ) {
        super(comparator);
        this.valueInstanceCreator = valueInstanceCreator;
    }

    public ExtendedTreeMap( MappedInstanceCreator<KeyType,ValueType> valueInstanceCreator ) {
        this.valueInstanceCreator = valueInstanceCreator;
    }

    @Override
    public ValueType get( Object key ) {
        if (this.containsKey((KeyType)key)) {
            return super.get((KeyType)key);
        } else {
            ValueType value = valueInstanceCreator.newInstance((KeyType)key);
            this.put((KeyType)key, value);
            return value;
        }
    }




}
