/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.umranium.longmark.common;

/**
 *
 * @author Umran
 */
public class Pair<FirstType,SecondType> {

    private FirstType first;
    private SecondType second;

    public Pair( FirstType first, SecondType second ) {
        this.first = first;
        this.second = second;
    }

    public FirstType getFirst() {
        return first;
    }

    public void setFirst( FirstType first ) {
        this.first = first;
    }

    public SecondType getSecond() {
        return second;
    }

    public void setSecond( SecondType second ) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "["+first+","+second+"]";
    }



    
}
