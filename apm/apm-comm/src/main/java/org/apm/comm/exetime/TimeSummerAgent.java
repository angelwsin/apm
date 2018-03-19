package org.apm.comm.exetime;

import java.lang.instrument.Instrumentation;

public class TimeSummerAgent {
	

    public static void agentmain(String agentArgs, Instrumentation inst){
         inst.addTransformer(new TimSummer());
    }

}
