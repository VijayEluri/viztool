//
// viztool - a tool for visualizing collections of java classes
// Copyright (c) 2001-2013, Michael Bayne - All rights reserved.
// http://github.com/samskivert/viztool/blob/master/LICENSE

package com.samskivert.viztool;

/**
 * A placeholder class that contains a reference to the log object used by
 * the viztool package.
 */
public class Log
{
    public static com.samskivert.util.Log log =
        new com.samskivert.util.Log("viztool");

    /** Convenience function. */
    public static void debug (String message)
    {
        log.debug(message);
    }

    /** Convenience function. */
    public static void info (String message)
    {
        log.info(message);
    }

    /** Convenience function. */
    public static void warning (String message)
    {
        log.warning(message);
    }

    /** Convenience function. */
    public static void logStackTrace (Throwable t)
    {
        log.logStackTrace(com.samskivert.util.Log.WARNING, t);
    }
}
