/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

/**
 *
 * @author umran
 */
public class Splitter {
    
    public String id;
    public String text;
    public Double requiredAccuracy;
    public Double minX;
    public Double maxX;
    public Double templateWidth;
    public Double topPadding;
    public Double height;
    public Double templateHeight;

    public Splitter() {
    }

    public Splitter(String id, String text, Double requiredAccuracy, Double minX, Double maxX, Double templateWidth, Double topPadding, Double height, Double templateHeight) {
        this.id = id;
        this.text = text;
        this.requiredAccuracy = requiredAccuracy;
        this.minX = minX;
        this.maxX = maxX;
        this.templateWidth = templateWidth;
        this.topPadding = topPadding;
        this.height = height;
        this.templateHeight = templateHeight;
    }
    
}
