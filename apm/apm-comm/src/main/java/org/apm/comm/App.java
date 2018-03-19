package org.apm.comm;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	 RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
         VirtualMachine vm;
		try {
			vm = VirtualMachine.attach(runtimeMXBean.getName().split("@")[0]);
			 //vm.loadAgent("org.java.instrumentation.Angent");
	          
	          vm.detach();
		} catch (AttachNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
    }
}
