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
 * Deals with coordinate conversions between: 1) The template: pixel coordinates
 * relative to the dimensions of the template. 2) The input PDF: PDF coordinates
 * in PDF-units. 3) The input PDF Image: the coordinates of the input PDF, when
 * it has been rendered to an image of particular dimensions.
 *
 * @author Umran
 */
public class PageSizing {

    private int templatePageNo;
    private double pdfOffsetX, pdfOffsetY;
    private double pdfWidth, pdfHeight;

    public PageSizing(int templatePageNo, float boxOffsetX, float boxOffsetY, float boxWidth, float boxHeight) {
        PrintWriter out = Log.out();

        this.templatePageNo = templatePageNo;
        this.pdfOffsetX = 0;//boxOffsetX;
        this.pdfOffsetY = 0;//boxOffsetY;
        this.pdfWidth = boxWidth;
        this.pdfHeight = boxHeight;
        if (Constants.DEBUG_DATA) {
            out.println("Page Sizing (" + boxOffsetX + "," + boxOffsetY + "," + boxWidth + "," + boxHeight + ")");
        }
    }

    public double getBoxWidth() {
        return pdfWidth;
    }

    public double getBoxHeight() {
        return pdfHeight;
    }

    /**
     *
     * @param templateX Template coordinate X in pixels
     * @return PDF X coordinates in PDF units
     */
    public double toPdfXCoord(double templateX, double templateWidth) {
        return templateX * pdfWidth / templateWidth + pdfOffsetX;
    }

    /**
     *
     * @param templateY Template coordinate Y in pixels
     * @return PDF Y coordinates in PDF units
     */
    public double toPdfYCoord(double templateY, double templateHeight) {
        return templateY * pdfHeight / templateHeight + pdfOffsetY;
    }

    /**
     *
     * @param pdfX PDF X coordinates in PDF units
     * @return Template coordinate X in pixels
     */
    public double toTemplateXCoord(double pdfX, double templateWidth) {
        return (pdfX - pdfOffsetX) * templateWidth / pdfWidth;
    }

    /**
     *
     * @param pdfY PDF Y coordinates in PDF units
     * @return Template coordinate Y in pixels
     */
    public double toTemplateYCoord(double pdfY, double templateHeight) {
        return (pdfY - pdfOffsetY) * templateHeight / pdfHeight;
    }


    @Override
    public String toString() {
        return "Page Sizing (" + templatePageNo + ")";
    }
}
