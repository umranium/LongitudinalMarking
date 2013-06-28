/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

/**
 *
 * @author umran
 */
public class SectionAnnotation {
    
    private String comments;
    private double mark;

    public SectionAnnotation() {
        this("", 0.0);
    }
    
    public SectionAnnotation(SectionAnnotation other) {
        this(other.comments, other.mark);
    }
    
    public SectionAnnotation(String comments, double mark) {
        this.comments = comments;
        this.mark = mark;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }
    
}
