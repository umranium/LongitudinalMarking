/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui.pdftext;

import com.umranium.longmark.common.ExtendedTreeMap;
import com.umranium.longmark.common.Log;
import com.umranium.longmark.common.MappedInstanceCreator;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.util.TextPosition;

/**
 *
 * @author Umran
 */
public class TextLocationMap {

    private static int compareCoord(double a, double b) {
        int res;
        if (Math.abs(a - b) < 1) {
            res = 0;
        } else {
            if (a < b) {
                res = -1;
            } else {
                res = 1;
            }
        }
        return res;
    }
    
    private static final Comparator<Double> COORD_COMP = new Comparator<Double>() {

        @Override
        public int compare(Double o1, Double o2) {
            return compareCoord(o1, o2);
        }
    };
    
    
    private static final Comparator<TextLocation> X_ACC_LOCATION_COMPARE = new Comparator<TextLocation>() {
        @Override
        public int compare(TextLocation o1, TextLocation o2) {
            return Double.compare(o1.x, o2.x);
        }
    };
    
    private static final Comparator<TextLocation> LOCATION_COMPARE = new Comparator<TextLocation>() {

        @Override
        public int compare(TextLocation o1, TextLocation o2) {
            int v = 0;
            if (v == 0) {
                v = compareCoord(o1.x, o2.x);
            }
            if (v == 0) {
                v = compareCoord(o1.y, o2.y);
            }
            if (v == 0) {
                v = compareCoord(o1.x2, o2.x2);
            }
            if (v == 0) {
                v = compareCoord(o1.y2, o2.y2);
            }
            return v;
        }
    };
    private static final Comparator<TextLocation> TEXT_LOCATION_COMPARE = new Comparator<TextLocation>() {

        @Override
        public int compare(TextLocation o1, TextLocation o2) {
//            out.println("Compare "+o1+" to "+o2);
            int v = o1.text.compareTo(o2.text);
            if (v == 0) {
                v = LOCATION_COMPARE.compare(o1, o2);
            }
            return v;
        }
    };
    private static final MappedInstanceCreator<Double, Set<TextLocation>> STRING_LIST_CREATOR = new MappedInstanceCreator<Double, Set<TextLocation>>() {

        @Override
        public Set<TextLocation> newInstance(Double key) {
            return new TreeSet<TextLocation>(TEXT_LOCATION_COMPARE);
        }
    };
    private static final MappedInstanceCreator<Double, Map<Double, Set<TextLocation>>> MAP_FLOAT_STRING_CREATOR = new MappedInstanceCreator<Double, Map<Double, Set<TextLocation>>>() {

        @Override
        public Map<Double, Set<TextLocation>> newInstance(Double key) {
            return new ExtendedTreeMap<Double, Set<TextLocation>>(COORD_COMP, STRING_LIST_CREATOR);
        }
    };
    
    private Map<Double, Map<Double, Set<TextLocation>>> charLocs = new ExtendedTreeMap<Double, Map<Double, Set<TextLocation>>>(
            COORD_COMP, MAP_FLOAT_STRING_CREATOR);
    private List<TextLocation> textLocs = new ArrayList<TextLocation>();
    private List<TextLocation> currentLine = new ArrayList<TextLocation>();
    private List<TextLocation> currentWord = new ArrayList<TextLocation>();
    private boolean splitByWhitespace;
    private boolean cleanWords;
    private boolean concateWordsToLines;

    public TextLocationMap(boolean splitByWhitespace, boolean cleanWords, boolean concateWordsToLines) {
        this.splitByWhitespace = splitByWhitespace;
        this.cleanWords = cleanWords;
        this.concateWordsToLines = concateWordsToLines;
    }
    
    public void place(PDGraphicsState graphicsState, TextPosition text) {
        //PrintWriter out = Log.out();
        
        double x = text.getX();
        double y = text.getY();
        double cx = text.getWidth();
        double cy = text.getHeight();

        String characters = text.getCharacter();
        
        double spaceWidth = text.getWidthOfSpace();
        if (spaceWidth==0.0) {
            spaceWidth = cx;
        }
        
        //out.println(characters + " " + ((int) characters.charAt(0)) + " (" + x + "," + (y - cy) + "," + (x + cx) + "," + y + ") space-width="+spaceWidth);
        this.place(x, y - cy, x + cx, y, cx, cy, spaceWidth, characters, text.isDiacritic());
    }

    public void place(double x, double y, double x2, double y2, double cx, double cy, double spaceWidth, String text, boolean diacritic) {
        //double yMM = (double)Math.round(y2 / Constants.ONE_MM_IN_PDF_UNITS)*Constants.ONE_MM_IN_PDF_UNITS;
        TextLocation tl = new TextLocation(text, x, y, x2, y2, cx, cy, spaceWidth);
        tl.diacritic = diacritic;
        charLocs.get(y).get(x).add(tl);
    }

    private void resetWords() {
        textLocs.clear();
        currentLine.clear();
        currentWord.clear();
    }

    private void finishCurrentWord() {
        if (currentWord.isEmpty())
            return;
        
        //out.println("Word finished.");

        //  remove initial empty spaces
        while (!currentWord.isEmpty() && isWhitespace(currentWord.get(0).text)) {
            currentWord.remove(0);
        }

        //  remove final empty spaces
        while (!currentWord.isEmpty() && isWhitespace(currentWord.get(currentWord.size() - 1).text)) {
            currentWord.remove(currentWord.size() - 1);
        }

        if (!currentWord.isEmpty()) {
            //Collections.sort(currentWord, LOCATION_COMPARE);
            TextLocation word = new TextLocation(new ArrayList<TextLocation>(currentWord));
            currentWord.clear();

            if (cleanWords) {
                word.text = trimWhitespace(word.text);
            }

            if (!word.text.isEmpty() && (!cleanWords || shouldKeep(word.text))) {
                word.text = processWord(word.text);
                currentLine.add(word);
            }
        }
    }
    
    private void finishCurrentLine() {
        if (currentLine.isEmpty())
            return;
        
        if (concateWordsToLines) {
            TextLocation line = new TextLocation(new ArrayList<TextLocation>(currentLine), " ");
            currentLine.clear();
            textLocs.add(line);
        } else {
            textLocs.addAll(currentLine);
            currentLine.clear();
        }
    }

    synchronized public void processWords(double minX, double minY, double maxX, double maxY) {
        processWords(minX, minY, maxX, maxY, false);
    }

    synchronized public void processWords(double minX, double minY, double maxX, double maxY, boolean addPartials) {
        PrintWriter out = Log.out();
        
        double tMinX = Math.min(minX, maxX);
        double tMinY = Math.min(minY, maxY);
        double tMaxX = Math.max(minX, maxX);
        double tMaxY = Math.max(minY, maxY);

        //out.println("tMinX = " + tMinX);
        //out.println("tMinY = " + tMinY);
        //out.println("tMaxX = " + tMaxX);
        //out.println("tMaxY = " + tMaxY);

        List<TextLocation> foundLocs = new ArrayList<TextLocation>();

        List<Double> yVals = new ArrayList<Double>(charLocs.keySet());
        for (int iY = 0; iY < yVals.size(); ++iY) {
            Double y = yVals.get(iY);
            for (Set<TextLocation> lineCharLocs : charLocs.get(y).values()) {
                for (TextLocation charLoc : lineCharLocs) {
                    //out.println(charLoc.text + "\t" + charLoc.x + "\t" + charLoc.y + "\t" + charLoc.x2 + "\t" + charLoc.y2);

                    if ((!addPartials && charLoc.x >= tMinX && charLoc.x2 <= tMaxX && charLoc.y >= tMinY && charLoc.y2 <= tMaxY)
                            || (addPartials && charLoc.x <= tMaxX && charLoc.x2 >= tMinX
                            && charLoc.y <= tMaxY && charLoc.y2 >= tMinY)) {
                        foundLocs.add(charLoc);
                    }
                }
            }
        }

//        out.println("Found Locations:");
//        for (TextLocation tl : foundLocs) {
//            out.println("\t" + tl);
//        }

        Collections.sort(foundLocs, X_ACC_LOCATION_COMPARE);

        List<List<TextLocation>> lines = new ArrayList<List<TextLocation>>();

        for (TextLocation tl : foundLocs) {
            List<TextLocation> foundLine = null;

            for (List<TextLocation> testLine : lines) {
                TextLocation lastItem = testLine.get(testLine.size() - 1);
                boolean yOverlap = Range.isOverlapping(
                        lastItem.y,
                        lastItem.y2,
                        tl.y,
                        tl.y2);
                if (yOverlap) {
                    foundLine = testLine;
                    break;
                }
            }

            if (foundLine == null) {
                foundLine = new ArrayList<TextLocation>();
                lines.add(foundLine);
            }

            foundLine.add(tl);
        }

//        out.println("Found Lines:");
//        for (int i = 0; i < lines.size(); ++i) {
//            List<TextLocation> line = lines.get(i);
//            out.println("\tLine: " + (i + 1));
//            for (TextLocation tl : line) {
//                out.println("\t\t" + tl);
//            }
//        }

        resetWords();

        for (List<TextLocation> line : lines) {
//            out.print("Processing Line: ");
//            for (TextLocation l:line) {
//                out.print("'"+l.text+"'");
//            }
//            out.println();
            
            TextLocation lastCharLoc = null;

            for (TextLocation charLoc : line) {
//                out.println("Processing char: "+charLoc);
                
                if (splitByWhitespace && lastCharLoc != null) {
                    double lastCharLocSp = lastCharLoc.getSpaceWidthEst();
                    double charLocSp = charLoc.getSpaceWidthEst();
                    
//                    out.println("lastCharLocSp = " + lastCharLocSp+", charLocSp = " + charLocSp);
                    
                    boolean charactersAreOverlapping = false;
                    
                    if (Range.isOverlapping(lastCharLoc.x, lastCharLoc.x2, charLoc.x, charLoc.x2)) {
                        Range merger = Range.getMerger(lastCharLoc.x, lastCharLoc.x2, charLoc.x, charLoc.x2);
                        Range overlap = Range.getOverlap(lastCharLoc.x, lastCharLoc.x2, charLoc.x, charLoc.x2);
                        
                        double mergerWidth = merger.getEnd()-merger.getStart();
                        double overlapWidth = overlap.getEnd()-overlap.getStart();
                        
                        if (overlapWidth>0.9*mergerWidth) { // overlap is 90% of merger
                            charactersAreOverlapping = true;
                        }
                    }
                    
                    if (charactersAreOverlapping) {
                        if (charLoc.diacritic) {
                            // not sure what to do here....
                        }
                        // ignore all overlaps
                        continue;
                    }
                    
                    double lastX1 = lastCharLoc.x - lastCharLocSp;
                    double lastX2 = lastCharLoc.x2 + lastCharLocSp;
                    double currX1 = charLoc.x - charLocSp;
                    double currX2 = charLoc.x2 + charLocSp;
                    
//                    out.println("lastX1="+lastX1+", lastX2="+lastX2+", currX1="+currX1+", currX2="+currX2);

                    boolean xOverlap = false;
                    
                    if (Range.isOverlapping(lastX1, lastX2, charLoc.x, charLoc.x2)) {
                        Range overlap = Range.getOverlap(lastX1, lastX2, charLoc.x, charLoc.x2);
//                        out.println("\toverlap = "+overlap);
                        if (Math.abs(overlap.getStart()-overlap.getEnd())>1.0) {
                            xOverlap = true;
                        }
                    }
                    
                    if (!xOverlap) {
                        if (Range.isOverlapping(lastCharLoc.x, lastCharLoc.x2, currX1, currX2)) {
                            Range overlap = Range.getOverlap(lastCharLoc.x, lastCharLoc.x2, currX1, currX2);
//                            out.println("\toverlap = "+overlap);
                            if (Math.abs(overlap.getStart()-overlap.getEnd())>1.0) {
                                xOverlap = true;
                            }
                        }
                    }
                    
                    if (!xOverlap || isWhitespace(charLoc.text)) {
//                        out.println("(space found) finish word end of word");
                        finishCurrentWord();
                    }
                    
                }
                
                currentWord.add(new TextLocation(charLoc));
                lastCharLoc = charLoc;
            }

            finishCurrentWord();
            
//            out.println("\tFound words:");
//            for (TextLocation tl:currentLine) {
//                out.println("\t\t'"+tl.text+"'");
//            }
            
            finishCurrentLine();
        }
    }
    
    private boolean isWhitespace(char v) {
        switch (Character.getType(v)) {
            case Character.COMBINING_SPACING_MARK:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.CONTROL:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.SPACE_SEPARATOR:
                return true;
            default:
                return false;
        }
    }
    
    private boolean isWhitespace(String v) {
        char[] chars = v.toCharArray();
        for (int i=0; i<chars.length; ++i)
            if (!isWhitespace(chars[i])) {
                return false;
            }
        return true;
    }
    
    private String trimWhitespace(String v) {
        StringBuilder v2 = new StringBuilder(v.length());
        char[] chars = v.toCharArray();
        int i = 0;
        while (i<chars.length && isWhitespace(chars[i])) {
            ++i;
        }
        int j = chars.length-1;
        while (j>=0 && isWhitespace(chars[j])) {
            --j;
        }
        v2.append(chars, i, j-i+1);
        return v2.toString();
    }

//    private void printCurrentWord() {
//        out.print("Current Word: '");
//        for (TextLocation tl:currentWord)
//            out.print(tl.text);
//        out.println("'");
//    }
    private static boolean shouldKeep(String wordText) {
        boolean hasSignificantLength = wordText.length() >= 4;
        boolean hasOkeyishLength = wordText.length() >= 3;
        boolean hasNoSmallLetters = wordText.toUpperCase().equals(wordText);
        boolean isNumber = wordText.matches("[0-9.]+");

        return hasSignificantLength
                || isNumber
                || (hasNoSmallLetters && hasOkeyishLength);
    }

    private String processWord(String wordText) {
        if (cleanWords) {
            if (wordText.toUpperCase().equals(wordText)) {
                return wordText;
            } else {
                return wordText.toLowerCase();
            }
        } else {
            return wordText;
        }
    }

    public Map<Double, Map<Double, Set<TextLocation>>> getCharLocs() {
        return new HashMap<Double, Map<Double, Set<TextLocation>>>(charLocs);
    }

    public List<TextLocation> getTextLocs() {
        return new ArrayList<TextLocation>(textLocs);
    }

    public static void main(String[] args) {
        PrintWriter out = Log.out();
        
        String text = "5";
        out.println(text + ": " + shouldKeep(text));
    }
}
