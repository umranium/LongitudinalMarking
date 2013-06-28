/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui.pdftext;

import com.umranium.longmark.common.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Umran
 */
public class TextLocation
{
    public String text;
    public double x, y, cx, cy, x2, y2;
    public double spaceWidth; // width of a space
    public int page;
    public double pageWidth;
    public double pageHeight;
    public List<TextLocation> mergedLocations;
    public String interTextBreak;
    public boolean diacritic;

    public TextLocation( String text, double x, double y, double x2, double y2, double cx, double cy, double spaceWidth ) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.cx = cx;
        this.cy = cy;
        this.x2 = x2;
        this.y2 = y2;
        this.spaceWidth = spaceWidth;
        this.mergedLocations = Collections.EMPTY_LIST;
        this.interTextBreak = null;
    }

    public TextLocation(TextLocation tl) {
        this.text = tl.text;
        this.x = tl.x;
        this.y = tl.y;
        this.cx = tl.cx;
        this.cy = tl.cy;
        this.x2 = tl.x2;
        this.y2 = tl.y2;
        this.mergedLocations = new ArrayList<TextLocation>(tl.mergedLocations);
        this.interTextBreak = tl.interTextBreak;
    }
    
    public TextLocation(List<TextLocation> locations) {
        this(locations,null);
    }
    
    public TextLocation(List<TextLocation> locations, String interTextBreak) {
        this.interTextBreak = interTextBreak;
        
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        double maxSpaceWidth = Double.NaN;
        
        StringBuilder sb = new StringBuilder(locations.size());

        boolean first = true;
        for ( TextLocation loc : locations ) {
            if ( !Double.isNaN(loc.x) && loc.x < minX ) {
                minX = loc.x;
            }
            if ( !Double.isNaN(loc.y) && loc.y < minY ) {
                minY = loc.y;
            }
            if ( !Double.isNaN(loc.x2) && loc.x2 > maxX ) {
                maxX = loc.x2;
            }
            if ( !Double.isNaN(loc.y2) && loc.y2 > maxY ) {
                maxY = loc.y2;
            }
            
            if ( !Double.isNaN(loc.spaceWidth) && (Double.isNaN(maxSpaceWidth) ||
                loc.spaceWidth > maxSpaceWidth)) {
                maxSpaceWidth = loc.spaceWidth;
            }
            
            if (first)
                first = false;
            else {
                if (interTextBreak!=null) {
                    sb.append(interTextBreak);
                }
            }
            
            sb.append(loc.text);
        }
        
        this.text = sb.toString();
        this.x = minX;
        this.y = minY;
        this.x2 = maxX;
        this.y2 = maxY;
        this.cx = maxX-minX;
        this.cy = maxY-minY;
        this.spaceWidth = maxSpaceWidth;
        this.mergedLocations = new ArrayList<TextLocation>(locations);
    }
    
    public List<TextLocation> generateFlattenedMergedLocations() {
        List<TextLocation> locs = new ArrayList<TextLocation>();
        flatten(this, locs);
        return locs;
    }
    
    /**
     * Returns a copy of the text without leading and trailing whitespaces
     */
    public TextLocation copyWithoutWhitespaces() {
        return copyWithoutWhitespaces(this);
    }

    public double getSpaceWidthEst() {
        if (Double.isNaN(spaceWidth))
            return cx;
        else
            return spaceWidth;
    }
    
    @Override
    public String toString() {
        return "('"+text+"':"+x+","+y+","+x2+","+y2+","+cx+","+cy+")";
    }
    
    private static void flatten(TextLocation tl, List<TextLocation> results) {
        if (tl.mergedLocations.isEmpty()) {
            String text = tl.text;
            if (text.length()>1) {
                for (int i=0; i<text.length(); ++i) {
                    results.add(new TextLocation(Character.toString(text.charAt(i)), tl.x, tl.y, tl.x2, tl.y2, tl.cx, tl.cy, tl.spaceWidth ));
                }
            } else {
                results.add(tl);
            }
        } else {
            for (TextLocation t:tl.mergedLocations) {
                flatten(t, results);
            }
        }
    }
    
    /**
     * Returns a copy of the text without leading and trailing whitespaces
     */
    private static TextLocation copyWithoutWhitespaces(TextLocation tl) {
        TextLocation copy;
        if (tl.mergedLocations.isEmpty()) {
            String text = tl.text.trim();
            if (text.isEmpty()) {
                copy = null;
            } else {
                copy = new TextLocation(tl);
                copy.text = text;
            }
        } else {
            List<TextLocation> locs = new ArrayList<TextLocation>(tl.mergedLocations.size());
            for (TextLocation tl2:tl.mergedLocations) {
                TextLocation tl2Copy = copyWithoutWhitespaces(tl2);
                if (tl2Copy!=null) {
                    locs.add(tl2Copy);
                }
            }
            if (locs.isEmpty()) {
                copy = null;
            } else {
                copy = new TextLocation(locs, tl.interTextBreak);
            }
        }
        return copy;
    }
    
    
}
