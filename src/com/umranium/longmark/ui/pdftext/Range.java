/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.ui.pdftext;

import com.umranium.longmark.common.Constants;
import com.umranium.longmark.common.Log;
import java.io.PrintWriter;

/**
 *
 * @author Umran
 */
public class Range {
    
    private static long COUNT = 0;
    private static boolean DISPLAY_ID = true;
    
    public static final Range NAN_RANGE = new Range(Double.NaN,false);
    public static final Range ZERO_RANGE = new Range(0.0,false);
    
    protected long _id;
    private double start, end;
    private boolean adjustable;

    public Range(double p) {
        this(p, p, true);
    }
    
    public Range(double p, boolean adjustable) {
        this(p, p, adjustable);
    }
    
    public Range(double p1, double p2) {
        this(p1, p2, true);
    }
    
    public Range(Range range) {
        this(range.getStart(), range.getEnd(), true);
    }
    
    public Range(Range range, boolean adjustable) {
        this(range.getStart(), range.getEnd(), adjustable);
    }
    
    public Range(double p1, double p2, boolean adjustable) {
        this._id = ++COUNT;
        this.start = p1;
        this.end = p2;
        this.adjustable = adjustable;
        //this.start = Math.min(p1, p2);
        //this.end = Math.max(p1, p2);
    }
    
    public void set(double start, double end) {
        setStart(start);
        setEnd(end);
    }
    
    public void set(double value) {
        set(value,value);
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }
    
    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }
    
    public boolean contains(double value) {
        return (getStart()<=value && getEnd()>=value)
                || (Math.abs(getStart()-value)<=Constants.ONE_MM_IN_PDF_UNITS)
                || (Math.abs(getEnd()-value)<=Constants.ONE_MM_IN_PDF_UNITS);
    }
    
    public boolean isDynamic() {
        return false;
    }
    
    public boolean isAdjustable() {
        return adjustable;
    }
    
    public boolean isIn(Range container) {
        return isIn(getStart(), getEnd(), container.getStart(), container.getEnd());
    }
    
    public boolean isOut(Range container) {
        return isOut(getStart(), getEnd(), container.getStart(), container.getEnd());
    }
    
    /**
     * @return True if the range is a point, the point could be NaN or infinite.
     */
    public boolean isFixedPoint() {
        if (Double.isNaN(getStart()) && Double.isNaN(getEnd()))
            return true;
        if (Double.isInfinite(getStart()) && Double.isInfinite(getEnd()))
            return true;
        return getStart()==getEnd() || Math.abs(getStart()-getEnd())<0.01;
    }
    
    /**
     * @return True if the range is a point that isn't NaN or infinite.
     */
    public boolean isDefinedPoint() {
        if (Double.isNaN(getStart()) || Double.isNaN(getEnd()))
            return false;
        if (Double.isInfinite(getStart()) || Double.isInfinite(getEnd()))
            return false;
        return getStart()==getEnd() || Math.abs(getStart()-getEnd())<0.01;
    }
    
    public double getMiddlePoint() {
        return (getStart()+getEnd())/2.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Range) {
            Range r = (Range)obj;
            return Math.abs(r.getStart()-this.getStart())<0.0001 &&
                    Math.abs(r.getEnd()-this.getEnd())<0.0001;
        }
        return super.equals(obj);
    }
    
    public Range createCopy() {
        return new Range(this.getStart(), this.getEnd());
    }
    
    public void restoreCopy(Range range) {
        this.setStart(range.getStart());
        this.setEnd(range.getEnd());
    }
    

    @Override
    public String toString() {
        if (DISPLAY_ID) {
            if (isFixedPoint())
                return String.format("{"+_id+"}%.2f", getStart());
            else
                return String.format("{"+_id+"}[%.2f-%.2f]", getStart(), getEnd());
        } else {
            if (isFixedPoint())
                return String.format("%.2f", getStart());
            else
                return String.format("[%.2f-%.2f]", getStart(), getEnd());
        }
    }
    
    public String expression() {
        return toString();
    }
    
    private static class DynamicRange extends Range {
        protected Range a, b;

        public DynamicRange(Range a, Range b) {
            super(Double.NaN);
            if (a==null)
                throw new IllegalArgumentException("Argument a can't be null");
            if (b==null)
                throw new IllegalArgumentException("Argument b can't be null");
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

    }
    
    public static class DynamicCopy extends Range {
        public Range a;

        public DynamicCopy(Range a) {
            super(Double.NaN);
        }

        @Override
        public double getStart() {
            return a.getStart();
        }

        @Override
        public double getEnd() {
            return a.getEnd();
        }

        @Override
        public void setStart(double start) {
            if (isAdjustable()) {
                a.setStart(start);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public void setEnd(double end) {
            if (isAdjustable()) {
                a.setEnd(end);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public boolean isAdjustable() {
            return a.isAdjustable();
        }

        @Override
        public String expression() {
            if (DISPLAY_ID)
                return "{"+_id+"}Copy("+a.expression()+")";
            else
                return "Copy("+a.expression()+")";
        }
        
        
        
    }
    
    private static class DistanceRange extends DynamicRange {
        
        public DistanceRange(Range a, Range b) {
            super(a, b);
        }

        @Override
        public double getStart() {
            double d1 = b.getEnd()-a.getEnd();
            double d2 = b.getStart()-a.getStart();
            double min = Math.abs(Math.min(d1, d2));
            return min;
        }

        @Override
        public double getEnd() {
            double d1 = b.getEnd()-a.getEnd();
            double d2 = b.getStart()-a.getStart();
            double max = Math.abs(Math.max(d1, d2));
            return max;
        }

        @Override
        public void setStart(double start) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setEnd(double end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAdjustable() {
            return false;
        }

        @Override
        public String expression() {
            if (DISPLAY_ID)
                return "{"+_id+"}Distance("+a.expression()+","+b.expression()+")";
            else
                return "Distance("+a.expression()+","+b.expression()+")";
        }
        
        
        
    }
    
    private static class SingleFixedPointRange extends DynamicRange {
        
        public SingleFixedPointRange(Range a, Range b) {
            super(a, b);
        }

        @Override
        public void setStart(double start) {
            if (a.isAdjustable()) {
                double diff = getStart()-start;
                a.setStart(a.getStart()-diff);
                if (Math.abs(getStart()-start)>=1.0) {
                    throw new RuntimeException("Unable to set start, value="+start+", final value="+getStart());
                }
            } else {
                throw new UnsupportedOperationException("expression="+this.expression()+", value="+this);
            }
        }

        @Override
        public void setEnd(double end) {
            if (a.isAdjustable()) {
                double diff = getEnd()-end;
                a.setEnd(a.getEnd()-diff);
                if (Math.abs(getEnd()-end)>=1.0) {
                    throw new RuntimeException("Unable to set end, value="+end+", final value="+getEnd());
                }
            } else {
                throw new UnsupportedOperationException("expression="+this.expression()+", value="+this);
            }
        }
        
        @Override
        public boolean isAdjustable() {
            return a.isAdjustable();
        }
    }
    
    private static class AddRange extends SingleFixedPointRange {
        
        public AddRange(Range a, Range b) {
            super(a, b);
        }

        @Override
        public double getStart() {
            return a.getStart()+b.getStart();
        }

        @Override
        public double getEnd() {
            return a.getEnd()+b.getEnd();
        }

        @Override
        public String expression() {
            //PrintWriter out = Log.out();
            //out.println("about to get expression of "+a+" and "+b);
            if (DISPLAY_ID)
                return "{"+_id+"}Add("+a.expression()+","+b.expression()+")";
            else
                return "Add("+a.expression()+","+b.expression()+")";
        }
        
        
    }
    
    private static class SubtractRange extends SingleFixedPointRange {
        
        public SubtractRange(Range a, Range b) {
            super(a, b);
        }

        @Override
        public double getStart() {
            return a.getStart()-b.getStart();
        }

        @Override
        public double getEnd() {
            return a.getEnd()-b.getEnd();
        }

        @Override
        public String expression() {
            if (DISPLAY_ID)
                return "{"+_id+"}Subtract("+a.expression()+","+b.expression()+")";
            else
                return "Subtract("+a.expression()+","+b.expression()+")";
                
        }
    }
    
    
//    public static Range union(Range a, Range b) {
//        double min = Math.min(a.getStart(), b.getStart());
//        double max = Math.max(a.getEnd(), b.getEnd());
//        return new Range(min, max);
//    }
//    
//    public static boolean intersect(Range a, Range b) {
//        return (a.getStart()>b.getStart() && a.getStart()<b.getEnd()) || (b.getStart()>a.getStart() && b.getStart()<a.getEnd());
//    }
//    
//    public static Range intersection(Range a, Range b) {
//        if (intersect(a, b)) {
//            return new Range(Math.max(a.getStart(), b.getStart()), Math.min(a.getEnd(), b.getEnd()));
//        } else
//            return null;
//    }
    
    public static Range distanceStatic(Range a, Range b) {
        double d1 = Math.abs(a.getEnd()-b.getStart());
        double d2 = Math.abs(b.getEnd()-a.getStart());
        double min = Math.min(d1, d2);
        double max = Math.max(d1, d2);
        return new Range(min, max);
    }
    
    public static Range addStatic(Range point, Range distance) {
        return new Range(point.getStart()+distance.getStart(), point.getEnd()+distance.getEnd());
    }
    
    public static Range subtractStatic(Range point, Range distance) {
        return new Range(point.getStart()-distance.getStart(), point.getEnd()-distance.getEnd());
    }
    
    public static Range distanceDynamic(Range a, Range b) {
        return new DistanceRange(a, b);
    }
    
    public static Range addDynamic(Range point, Range distance) {
        return new AddRange(point, distance);
    }
    
    public static Range subtractDynamic(Range point, Range distance) {
        return new SubtractRange(point, distance);
    }
    
    public static boolean isIn(double testStart, double testEnd, double containerStart, double containerEnd) {
        return testStart>=containerStart &&
                testEnd<=containerEnd;
    }
    
    public static boolean isOut(double testStart, double testEnd, double containerStart, double containerEnd) {
        return testEnd<containerStart ||
                testStart>containerEnd;
    }
    
    public static boolean isOverlapping(double aStart, double aEnd, double bStart, double bEnd) {
        return aStart<=bEnd && aEnd>=bStart;
    }
    
    public static boolean isOverlapping(Range a, Range b) {
        return isOverlapping(a.getStart(), a.getEnd(), b.getStart(), b.getEnd());
    }
    
    public static boolean isOverlapping(Range a, double b) {
        return isOverlapping(a.getStart(), a.getEnd(), b, b);
    }
    
    public static Range getOverlap(double aStart, double aEnd, double bStart, double bEnd) {
        return new Range(Math.max(aStart, bStart), Math.min(aEnd, bEnd));
    }
    
    public static Range getMerger(double aStart, double aEnd, double bStart, double bEnd) {
        return new Range(Math.min(aStart, bStart), Math.max(aEnd, bEnd));
    }
    
    public static Range getOverlap(Range a, Range b) {
        return getOverlap(a.getStart(), a.getEnd(), b.getStart(), b.getEnd());
    }
    
    public static void main(String[] args) {
        PrintWriter out = Log.out();
        
        Range a = new Range(387.11);
        Range b = new Range(94.00,401.00);
        out.println(a.isIn(b));
    }
}
