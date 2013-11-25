//
// viztool - a tool for visualizing collections of java classes
// Copyright (c) 2001-2013, Michael Bayne - All rights reserved.
// http://github.com/samskivert/viztool/blob/master/LICENSE

package com.samskivert.viztool.hierarchy;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextLayout;
import java.util.*;

import com.samskivert.viztool.layout.ElementLayout;
import com.samskivert.viztool.layout.PackedColumnElementLayout;
import com.samskivert.viztool.util.FontPicker;

/**
 * A chain group is used to group together all of the classes from a
 * particular package.
 */
public class ChainGroup
{
    /**
     * Constructs a chain group for a particular package with the specified package root and an
     * iterator that is configured only to return classes from the specified package.
     */
    public ChainGroup (String pkgroot, String pkg, Iterator<Class<?>> iter)
    {
        // keep track of the package
        _pkg = pkg;

        // process the classes provided by our enumerator
        _roots = ChainUtil.buildChains(pkgroot, pkg, iter);

        // sort our roots' children
        for (Chain root : _roots) {
            root.sortChildren(NAME_COMP);
        }

        // System.err.println(_roots.size() + " chains for " + pkg + ".");
    }

    protected ChainGroup (String pkg, List<Chain> roots)
    {
        _pkg = pkg;
        _roots = roots;
    }

    /**
     * Returns the dimensions of this chain group. This value is only
     * valid after <code>layout</code> has been called.
     */
    public Rectangle2D getBounds ()
    {
        return _size;
    }

    /**
     * Sets the upper left coordinate of this group. The group itself
     * never looks at this information, but it will be made available as
     * the x and y coordinates of the rectangle returned by
     * <code>getBounds</code>.
     */
    public void setPosition (double x, double y)
    {
        _size.setRect(x, y, _size.getWidth(), _size.getHeight());
    }

    /**
     * Returns the page on which this group should be rendered.
     */
    public int getPage ()
    {
        return _page;
    }

    /**
     * Sets the page on which this group should be rendered.
     */
    public void setPage (int page)
    {
        _page = page;
    }

    /**
     * Lays out the chains in this group and returns the total size. If
     * the layout process requires that this chain group be split across
     * multiple pages, a new chain group containing the overflow chains
     * will be returned. If the group fits in the allotted space, null
     * will be returned.
     */
    public ChainGroup layout (Graphics2D gfx, double pageWidth, double pageHeight)
    {
        // we'll need room to incorporate our title
        TextLayout layout = new TextLayout(_pkg, FontPicker.getTitleFont(),
                                           gfx.getFontRenderContext());

        // we let the title stick halfway up out of our rectangular
        // bounding box
        Rectangle2D tbounds = layout.getBounds();
        double titleAscent = tbounds.getHeight()/2;

        // keep room for our border and title
        pageWidth -= 2*BORDER;
        pageHeight -= (2*BORDER + titleAscent);

        // lay out the internal structure of our chains
        ChainVisualizer clay = new CascadingChainVisualizer();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = _roots.get(i);
            Chain oflow = chain.layout(gfx, clay, pageWidth, pageHeight);
            // if this chain overflowed when being laid out, add the newly
            // created root to our list
            if (oflow != null) {
                _roots.add(i+1, oflow);
            }
        }

        // arrange them on the page
        ElementLayout elay = new PackedColumnElementLayout();
        List<Chain> overflow = new ArrayList<Chain>();
        _size = elay.layout(_roots, pageWidth, pageHeight, overflow);

        // make sure we're wide enough for our title
        double width = Math.max(_size.getWidth(), layout.getAdvance() + 4);

        // adjust for our border and title
        double height = _size.getHeight() + titleAscent;
        _size.setRect(0, 0, width + 2*BORDER, height + 2*BORDER);

        // if we have overflow elements, create a new chain group with
        // these elements, remove them from our roots list and be on our
        // way
        if (overflow.size() > 0) {
            // remove the overflow roots from our list
            for (Chain oflow : overflow) {
                _roots.remove(oflow);
            }
            return new ChainGroup(_pkg, overflow);
        }

        return null;
    }

    /**
     * Renders the chains in this group to the supplied graphics object.
     * This function requires that <code>layout</code> has previously been
     * called to lay out the group's chains.
     *
     * @see #layout
     */
    public void render (Graphics2D gfx, double x, double y)
    {
        TextLayout layout = new TextLayout(_pkg, FontPicker.getTitleFont(),
                                           gfx.getFontRenderContext());

        // we let the title stick halfway up out of our rectangular
        // bounding box
        Rectangle2D tbounds = layout.getBounds();
        double titleAscent = tbounds.getHeight()/2;
        double dy = -tbounds.getY();

        // print our title
        layout.draw(gfx, (float)(x + BORDER + 2), (float)(y + dy));

        // shift everything down by the ascent of the title
        y += titleAscent;

        // translate to our rendering area
        double cx = x + BORDER;
        double cy = y + BORDER;
        gfx.translate(cx, cy);

        // render our chains
        ChainVisualizer renderer = new CascadingChainVisualizer();
        for (Chain chain : _roots) {
            renderer.renderChain(chain, gfx);
        }

        // undo the translation
        gfx.translate(-cx, -cy);

        // print our border box
        double height = _size.getHeight() - titleAscent;
        GeneralPath path = new GeneralPath();
        path.moveTo((float)(x + BORDER), (float)y);
        path.lineTo((float)x, (float)y);
        path.lineTo((float)x, (float)(y + height));
        path.lineTo((float)(x + _size.getWidth()),
                    (float)(y + height));
        path.lineTo((float)(x + _size.getWidth()), (float)y);
        path.lineTo((float)(x + BORDER + layout.getAdvance() + 4), (float)y);
        gfx.draw(path);
    }

    public Chain getRoot (int index)
    {
        return _roots.get(index);
    }

    public String toString ()
    {
        return "[pkg=" + _pkg + ", roots=" + _roots.size() +
            ", size=" + _size + ", page=" + _page + "]";
    }

    protected String _pkg;
    protected List<Chain> _roots;

    protected Rectangle2D _size;
    protected int _page;

    protected static final double BORDER = 72/8;

    /**
     * Compares the names of two chains. Used to sort them into
     * alphabetical order.
     */
    protected static class NameComparator implements Comparator<Chain>
    {
        public int compare (Chain c1, Chain c2) {
            return c1.getName().compareTo(c2.getName());
        }
    }

    protected static final Comparator<Chain> NAME_COMP = new NameComparator();
}
