//
// $Id$

package com.samskivert.viztool.viz;

import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.Point;

/**
 * A chain is used by the hierarchy visualizer to represent inheritance
 * chains.
 */
public class Chain implements Element
{
    /**
     * Constructs a chain with the specified class as its root.
     */
    public Chain (String pkgroot, Class root)
    {
        _pkgroot = pkgroot;
        _root = root;
        _name = _root.getName();
        if (_name.startsWith(pkgroot)) {
            _name = _name.substring(_pkgroot.length()+1);
        }
    }

    /**
     * Returns the name of this chain (which should be used when
     * displaying the chain).
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the class that forms the root of this chain.
     */
    public Class getRoot ()
    {
        return _root;
    }

    /**
     * Returns the name of the class that forms the root of this chain.
     */
    public String getRootName ()
    {
        return _root.getName();
    }

    /**
     * Returns a <code>Dimension</code> instance representing the size of
     * this chain (and all contained subchains). All coordinates are in
     * points.
     */
    public Dimension getSize ()
    {
        return _size;
    }

    /**
     * Returns the location of this chain relative to its parent chain.
     * All coordinates are in points.
     */
    public Point getLocation ()
    {
        return _location;
    }

    /**
     * Sets the size of this chain. All coordinates are in points.
     *
     * @see #getSize
     */
    public void setSize (int width, int height)
    {
        _size = new Dimension(width, height);
    }

    /**
     * Sets the location of this chain relative to its parent. All
     * coordinates are in points.
     *
     * @see #getLocation
     */
    public void setLocation (int x, int y)
    {
        _location = new Point(x, y);
    }

    // inherited from interface
    public void setPage (int pageno)
    {
        _pageno = pageno;
    }

    // inherited from interface
    public int getPage ()
    {
        return _pageno;
    }

    /**
     * Returns an array list containing the children chains of this chain.
     * If this chain has no children the list will be of zero length but
     * will not be null. This list should <em>not</em> be modified. Oh,
     * for a <code>const</code> keyword.
     */
    public ArrayList getChildren ()
    {
        return _children;
    }

    /**
     * Lays out all of the children of this chain and then requests that
     * the supplied layout manager arrange those children and compute the
     * dimensions of this chain based on all of that information.
     */
    public void layout (int pointSize, ChainLayout clay)
    {
        // first layout our children
        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);
            child.layout(pointSize, clay);
        }

        // now lay ourselves out
        clay.layoutChain(this, pointSize);
    }

    /**
     * Adds a child to this chain. The specified class is assumed to
     * directly inherit from the class that is the root of this chain.
     */
    public void addClass (Class child)
    {
        Chain chain = new Chain(_pkgroot, child);
        if (!_children.contains(chain)) {
            _children.add(chain);
        }
    }

    /**
     * Locates the chain for the specified target class and returns it if
     * it is a registered child of this chain. Returns null if no child
     * chain of this chain contains the specified target class.
     */
    public Chain getChain (Class target)
    {
        if (_root.equals(target)) {
            return this;
        }

        // just do a depth first search because it's fun
        for (int i = 0; i < _children.size(); i++) {
            Chain chain = ((Chain)_children.get(i)).getChain(target);
            if (chain != null) {
                return chain;
            }
        }

        return null;
    }

    public boolean equals (Object other)
    {
        if (other == null) {
            return false;
        } else if (other instanceof Chain) {
            return ((Chain)other)._root.equals(_root);
        } else {
            return false;
        }
    }

    public String toString ()
    {
        StringBuffer out = new StringBuffer();
        toString("", out);
        return out.toString();
    }

    protected void toString (String indent, StringBuffer out)
    {
        out.append(indent).append(_name).append("\n");
        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);
            child.toString(indent + "  ", out);
        }
    }

    protected String _pkgroot;
    protected Class _root;
    protected String _name;

    protected ArrayList _children = new ArrayList();
    protected Dimension _size = new Dimension(0, 0);
    protected Point _location = new Point(0, 0);
    protected int _pageno;
}
