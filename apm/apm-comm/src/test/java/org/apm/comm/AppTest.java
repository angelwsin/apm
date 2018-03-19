package org.apm.comm;

import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class AppTest
{
	
	@Test
	public void test() throws Exception{
		//ClassFile classFile = new ClassFile(new DataInputStream(in));
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get("org.apm.comm.Test");
		CtMethod ctMd = ctClass.getDeclaredMethod("exe");
		ctMd.insertBefore("System.out.println(0);");
		Class<?> x = ctClass.toClass();
		org.apm.comm.Test t = (org.apm.comm.Test) x.newInstance();
		t.exe();
		
	}
}
