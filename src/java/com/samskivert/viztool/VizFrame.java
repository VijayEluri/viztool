//
// $Id$

package com.samskivert.viztool;

import java.awt.BorderLayout;
import javax.swing.*;

import com.samskivert.viztool.viz.HierarchyVisualizer;

public class TestFrame extends JFrame
{
    public TestFrame (HierarchyVisualizer viz)
    {
        super("Test Frame");

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        TestPanel panel = new TestPanel(viz);
        getContentPane().add(panel, BorderLayout.CENTER);
    }
}
