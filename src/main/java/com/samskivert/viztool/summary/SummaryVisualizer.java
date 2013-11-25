//
// viztool - a tool for visualizing collections of java classes
// Copyright (c) 2001-2013, Michael Bayne - All rights reserved.
// http://github.com/samskivert/viztool/blob/master/LICENSE

package com.samskivert.viztool.summary;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.viztool.Log;
import com.samskivert.viztool.Visualizer;
import com.samskivert.viztool.layout.PackedColumnElementLayout;

/**
 * The summary visualizer displays summaries of the enumerated classes.
 */
public class SummaryVisualizer implements Visualizer
{
    // documentation inherited
    public void setPackageRoot (String pkgroot)
    {
        _pkgroot = pkgroot;
    }

    public void setClasses (Iterator<Class<?>> iter)
    {
        // remove any old summaries
        _summaries.clear();

        // create the new summaries
        while (iter.hasNext()) {
            _summaries.add(new ClassSummary(iter.next(), this));
        }
    }

    /**
     * Configures the visualizer to prefix classnames with their package
     * names (when they are outside of the visualized package root) or
     * not.
     */
    public void setDisplayPackageNames (boolean displayPackageNames)
    {
        _displayPackageNames = displayPackageNames;
    }

    /**
     * Lays out the class summary visualizations into the specified page
     * dimensions.
     */
    public void layout (Graphics2D gfx, double x, double y,
                        double width, double height)
    {
        // first layout all of our summaries (giving them dimensions)
        for (int i = 0; i < _summaries.size(); i++) {
            ClassSummary sum = _summaries.get(i);
            sum.layout(gfx);
        }

        // now arrange our summaries onto pages
        _pages = new ArrayList<ArrayList<ClassSummary>>();
        ArrayList<ClassSummary> list = new ArrayList<ClassSummary>(_summaries);
        ArrayList<ClassSummary> next = new ArrayList<ClassSummary>();
        PackedColumnElementLayout elay = new PackedColumnElementLayout();
        elay.setSortByHeight(false);

        while (list.size() > 0) {
            // lay out the elements that fit on this page
            elay.layout(list, width, height, next);

            // remove the overflowed elements from the list for this page
            list.removeAll(next);

            // append this page to the pages list
            _pages.add(list);

            // move to the next page
            list = next;
            next = new ArrayList<ClassSummary>();
        }

        // finally adjust all of the bounds of the class summaries by the
        // x and y offset of the page
        for (int i = 0; i < _summaries.size(); i++) {
            ClassSummary sum = _summaries.get(i);
            Rectangle2D b = sum.getBounds();
            sum.setBounds(b.getX()+x, b.getY()+y, b.getWidth(), b.getHeight());
        }
    }

    /**
     * Lays out and renders each of the classes that make up this package
     * summary visualization.
     */
    public int print (Graphics g, PageFormat pf, int pageIndex)
        throws PrinterException
    {
        Graphics2D gfx = (Graphics2D)g;

        // relay things out if the page format has changed or if we've
        // never been laid out
        if (!pf.equals(_format) || _pages == null) {
            // keep this around
            _format = pf;

            // and do the layout
            layout(gfx, pf.getImageableX(), pf.getImageableY(),
                   pf.getImageableWidth(), pf.getImageableHeight());
        }

        // adjust the stroke
        gfx.setStroke(new BasicStroke(0.1f));

        // make sure we're rendering a page that we have
        if (pageIndex < 0 || pageIndex >= _pages.size()) {
            return NO_SUCH_PAGE;
        }

        // render the summaries on the requested page
        ArrayList<?> list = _pages.get(pageIndex);
        for (int i = 0; i < list.size(); i++) {
            ((ClassSummary)list.get(i)).render(gfx);
        }
        return PAGE_EXISTS;
    }

    /**
     * Renders the specified page to the supplied graphics context.
     */
    public void paint (Graphics2D gfx, int pageIndex)
    {
        // ignore them if we haven't been laid out or if we have
        // absolutely nothing to display
        if (_pages == null || _pages.size() == 0) {
            return;
        }

        // sanity check
        if (pageIndex >= _pages.size()) {
            Log.info("Requested to render non-existent page " +
                     "[index=" + pageIndex + "].");
            return;
        }

        // adjust the stroke
        gfx.setStroke(new BasicStroke(0.1f));

        // render the summaries on the requested page
        ArrayList<?> list = _pages.get(pageIndex);
        for (int i = 0; i < list.size(); i++) {
            ((ClassSummary)list.get(i)).render(gfx);
        }
    }

    /**
     * Returns the number of pages occupied by this visualization. This is
     * only valid after a call to {@link #layout}.
     *
     * @return the page count or -1 if we've not yet been laid out.
     */
    public int getPageCount ()
    {
        return (_pages == null) ? -1 : _pages.size();
    }

    /**
     * Cleans up a fully qualified class name according to our
     * configuration and the package root.
     */
    public String name (Class<?> clazz)
    {
        if (clazz.isArray()) {
            return name(clazz.getComponentType()) + "[]";
        }

        String name = clazz.getName();
        if (_displayPackageNames) {
            if (name.startsWith(_pkgroot)) {
                return "." + name.substring(_pkgroot.length());
            } else if (name.startsWith("java.lang")) {
                return name.substring(10);
            } else {
                return name;
            }

        } else {
            int ldidx = name.lastIndexOf(".");
            return (ldidx == -1) ? name : name.substring(ldidx+1);
        }
    }

    protected String _pkgroot = "";
    protected ArrayList<ClassSummary> _summaries = new ArrayList<ClassSummary>();
    protected ArrayList<ArrayList<ClassSummary>> _pages;
    protected PageFormat _format;
    protected boolean _displayPackageNames = false;

    protected static final int GAP = 72/4;
}
