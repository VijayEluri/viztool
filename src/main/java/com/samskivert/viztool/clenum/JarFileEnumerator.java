//
// viztool - a tool for visualizing collections of java classes
// Copyright (c) 2001-2013, Michael Bayne - All rights reserved.
// http://github.com/samskivert/viztool/blob/master/LICENSE

package com.samskivert.viztool.clenum;

/**
 * The jar file enumerator enumerates all of the classes in a .jar class archive.
 */
public class JarFileEnumerator extends ZipFileEnumerator
{
    /**
     * Constructs a prototype enumerator that can be used for matching.
     */
    public JarFileEnumerator ()
    {
    }

    /**
     * Constructs a jar file enumerator with the specified jar file for enumeration.
     */
    public JarFileEnumerator (String jarpath)
        throws EnumerationException
    {
        super(jarpath);
    }

    // documentation inherited from interface
    public boolean matchesComponent (String component)
    {
        return component.endsWith(JAR_SUFFIX);
    }

    protected static final String JAR_SUFFIX = ".jar";
}
