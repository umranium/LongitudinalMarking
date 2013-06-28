/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.umranium.longmark.common;

/**
 *
 * @author Umran
 */
public interface MappedInstanceCreator<KeyType,ValueType> {

    ValueType newInstance(KeyType key);

}
