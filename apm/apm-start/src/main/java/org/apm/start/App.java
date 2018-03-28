package org.apm.start;

import org.apm.comm.runtime.RuntimeUtils;


public class App 
{
    public static void main( String[] args )throws Exception
    {
    	
    	
    
    	RuntimeUtils.attach("C:\\local\\repo\\apm-comm\\apm-comm\\0.0.1-SNAPSHOT\\apm-comm-0.0.1-SNAPSHOT.jar");
    	
    	Thread.sleep(1000);
    	Say say = new Say();
    	say.say(1000);
    }
}
