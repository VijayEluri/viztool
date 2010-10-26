//
// $Id$
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.layout;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * The element layout is used to lay a collection of elements out on a
 * page. It computes the desired position of each element and sets it via
 * <code>setBounds()</code> with the expectation that the location of the
 * elements will be used later in the rendering process.
 */
public interface ElementLayout
{
    /**
     * Lay out the supplied list of elements. Any elements that do not fit
     * into the allotted space should be added to the overflow list. The
     * supplied elements list should not be modified.
     *
     * @return the bounding dimensions of the collection of elements that
     * were laid out.
     */
    public <E extends Element> Rectangle2D layout (
        List<E> elements, double pageWidth, double pageHeight, List<E> overflow);
}
