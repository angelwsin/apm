package org.apm.comm.exetime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

import org.apm.comm.log.JavassitLogOut;

/*
 * CGLIB、ASM、BCEL、JAVASSIST  
 * 字节码操作可以使用以上类库
 */
public class TimSummer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		//System.out.println(className + ",加载");
		// 根类加载器
		if (Objects.isNull(loader))
			return null;
		byte[] b = null;
		try {

			b = JavassitLogOut.becl(classfileBuffer, className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

	
	

}
