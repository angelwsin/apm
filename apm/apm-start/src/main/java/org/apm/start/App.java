package org.apm.start;

import org.apm.comm.runtime.RuntimeUtils;


public class App 
{
    public static void main( String[] args )
    {
    	RuntimeUtils.attach("org.apm.comm.exetime.TimeSummerAgent");
    }
}
