package org.apm.comm.exetime;

import java.lang.instrument.ClassFileTransformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/*
 * CGLIB、ASM、BCEL、JAVASSIST  
 * 字节码操作可以使用以上类库
 */
public class TimSummer implements ClassFileTransformer{

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		System.out.println("加载");
		return null;
	}

}
