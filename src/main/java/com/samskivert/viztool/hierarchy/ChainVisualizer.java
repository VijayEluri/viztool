//
// viztool - a tool for visualizing collections of java classes
// Copyright (c) 2001-2013, Michael Bayne - All rights reserved.
// http://github.com/samskivert/viztool/blob/master/LICENSE

package com.samskivert.viztool.hierarchy;

import java.awt.Graphics2D;

/**
 * The chain visualizer is used to compute the dimensions of chains and their children in
 * preparation for rendering and then to perform said rendering.
 */
public interface ChainVisualizer
{
    /**
     * Assigns positions to the children of the supplied chain based on the layout policies desired
     * by the implementation and assigns dimensions to the specified chain based on the dimensions
     * of its children and the aforementioned layout policies. The children of the provided chain
     * instance are guaranteed to have been layed out (meaning they have dimensions but no
     * position) prior to this call.
     *
     * @param chain the chain to be layed out.
     * @param gfx the graphics context to use when computing dimensions.
     */
    public void layoutChain (Chain chain, Graphics2D gfx);

    /**
     * Renders the specified chain (and its subchains) based on the layout information (dimensions)
     * already computed for this chain.
     *
     * @param chain the chain to be rendered.
     * @param gfx the graphics context in which to render the chain.
     */
    public void renderChain (Chain chain, Graphics2D gfx);
}
