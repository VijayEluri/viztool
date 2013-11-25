//
// viztool - a tool for visualizing collections of java classes
// Copyright (c) 2001-2013, Michael Bayne - All rights reserved.
// http://github.com/samskivert/viztool/blob/master/LICENSE

package com.samskivert.viztool;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;

import com.samskivert.swing.util.SwingUtil;

import com.samskivert.viztool.clenum.ClassEnumerator;
import com.samskivert.viztool.clenum.FilterEnumerator;
import com.samskivert.viztool.clenum.RegexpEnumerator;
import com.samskivert.viztool.util.FontPicker;

/**
 * The viztool ant task. It takes the following arguments:
 *
 * <pre>
 * pkgroot = the base package from which names will be shortened
 * classes = a regular expression matching the classes to be visualized
 * visualizer = the classname of the visualizer to be used
 * </pre>
 *
 * The task should contain an embedded &lt;classpath&gt; element to
 * provide the classpath over which we will iterate, looking for matching
 * classes.
 */
public class DriverTask extends Task
{
    public void setVisualizer (String vizclass)
    {
        _vizclass = vizclass;
    }

    public void setPkgroot (String pkgroot)
    {
        _pkgroot = pkgroot;
    }

    public void setClasses (String classes)
    {
        _classes = classes;
    }

    public void setExclude (String exclude)
    {
        _exclude = exclude;
    }

    public void setOutput (File output)
    {
        _output = output;
    }

    public Path createClasspath ()
    {
        return _cmdline.createClasspath(getProject()).createPath();
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // make sure everything was set up properly
        ensureSet(_vizclass, "Must specify the visualizer class via the 'visualizer' attribute.");
        ensureSet(_pkgroot, "Must specify the package root via the 'pkgroot' attribute.");
        ensureSet(_pkgroot, "Must specify the class regexp via the 'classes' attribute.");
        Path classpath = _cmdline.getClasspath();
        ensureSet(classpath, "Must provide a <classpath> subelement " +
                  "describing the classpath to be searched for classes.");

        // initialize the font picker
        FontPicker.init(_output != null);

        // create the classloader we'll use to load the visualized classes
        ClassLoader cl = new AntClassLoader(null, getProject(), classpath, false);

        // scan the classpath and determine which classes will be visualized
        ClassEnumerator clenum = new ClassEnumerator(classpath.toString());
        FilterEnumerator fenum = null;
        try {
            fenum = new RegexpEnumerator(_classes, _exclude, clenum);
        } catch  (Exception e) {
            throw new BuildException("Invalid package regular expression [classes=" + _classes +
                                     ", exclude=" + _exclude + "].", e);
        }

        List<Class<?>> classes = new ArrayList<Class<?>>();
        while (fenum.hasNext()) {
            String cname = fenum.next();
            // skip inner classes, the visualizations pick those up
            // themselves
            if (cname.indexOf("$") != -1) {
                continue;
            }
            try {
                classes.add(cl.loadClass(cname));
            } catch (Throwable t) {
                log("Unable to introspect class [class=" + cname +
                    ", error=" + t + "].");
            }
        }

//         // remove the packages on our exclusion list
//         String expkg = System.getProperty("exclude");
//         if (expkg != null) {
//             StringTokenizer tok = new StringTokenizer(expkg, ":");
//             while (tok.hasMoreTokens()) {
//                 pkgset.remove(tok.nextToken());
//             }
//         }

        // now create our visualizer and go to work
        Visualizer viz = null;
        try {
            viz = (Visualizer)Class.forName(_vizclass).newInstance();
        } catch (Throwable t) {
            throw new BuildException("Unable to instantiate visualizer: " + _vizclass, t);
        }

        viz.setPackageRoot(_pkgroot);
        viz.setClasses(classes.iterator());

        // if no output file was specified, pop up a window
        if (_output == null) {
            VizFrame frame = new VizFrame(viz);
            frame.pack();
            SwingUtil.centerWindow(frame);
            frame.setVisible(true);

            // prevent ant from kicking the JVM out from under us
            synchronized (this) {
                while (true) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

        } else {
            try {
                log("Generating visualization to '" + _output.getPath() + "'.");
                PrintUtil.print(viz, _output);
            } catch (Exception e) {
                throw new BuildException("Error printing visualization.", e);
            }
        }
    }

    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
    }

    protected String _vizclass;
    protected String _pkgroot;
    protected String _classes, _exclude;
    protected File _output;

    // use use this for accumulating our classpath
    protected CommandlineJava _cmdline = new CommandlineJava();
}
