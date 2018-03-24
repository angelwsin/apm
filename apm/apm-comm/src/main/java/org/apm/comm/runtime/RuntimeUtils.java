package org.apm.comm.runtime;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class RuntimeUtils {
	
	private static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	
	
	public static String getProcessName() {
		return runtimeMXBean.getName().split("@")[0];
	}
	
	
	public static void attach(String agent) {
		try {
			VirtualMachine vm = VirtualMachine.attach(getProcessName());
			try {
				vm.loadAgent(agent);
				Thread.sleep(1000);
			} catch (AgentLoadException e) {
				e.printStackTrace();
			} catch (AgentInitializationException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			vm.detach();
		} catch (AttachNotSupportedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
