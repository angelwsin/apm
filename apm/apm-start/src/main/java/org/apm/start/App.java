package org.apm.start;

import org.apm.comm.runtime.RuntimeUtils;


public class App 
{
    public static void main( String[] args )
    {
    	RuntimeUtils.attach("D:\\java\\m2\\repository\\apm-comm\\apm-comm\\0.0.1-SNAPSHOT\\apm-comm-0.0.1-SNAPSHOT.jar");
    	Say say = new Say();
    	say.say();
    }
}
