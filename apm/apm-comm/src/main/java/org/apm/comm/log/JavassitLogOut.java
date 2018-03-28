package org.apm.comm.log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apm.comm.annotation.StatisticsTime;
import org.slf4j.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class JavassitLogOut {
	
	public static byte[] becl(byte[] classfileBuffer, String className) throws ClassFormatException, IOException, NotFoundException, ClassNotFoundException, CannotCompileException {
		ClassPool pool = ClassPool.getDefault();
		ByteArrayInputStream is = new ByteArrayInputStream(classfileBuffer);
        CtClass clazz = pool.makeClass(is);
       
		StatisticsTime statisticsTime = (StatisticsTime) clazz.getAnnotation(StatisticsTime.class);
		if (Objects.isNull(statisticsTime)) {
			return null;
		}
		// 去重方法
		Set<String> includeMds = new HashSet<>();
		
		for (String s : statisticsTime.value()) {
			includeMds.add(s);
		}
		
		for (CtMethod method : clazz.getMethods()) {
			if (!includeMds.contains(method.getName())) {
				continue;
			}
			// 代理方法
			String proxyMdName = method.getName() + "$Imp";
			clazz.removeMethod(method);
			method.setName(proxyMdName);
			clazz.addMethod(method);
			
			CtMethod ct = new CtMethod(method.getReturnType(), method.getName(), method.getParameterTypes(), clazz);
			
			StringBuilder build = new StringBuilder("{long start = System.currentTimeMillis();");
			if(method.getReturnType()!=CtClass.voidType) {
				build.append(method.getReturnType().getSimpleName()).append(" result = ");
			}
			int i = 0;
			
			if(!Modifier.isStatic(method.getModifiers())) {
				build.append("$").append(i).append(".").append(method.getName()).append("(");
				i++;
			}else {
				build.append(method.getName()).append("(");	
			}
			
			for(;i<method.getParameterTypes().length-1;i++) {
				build.append("$").append(i).append(",");
			}
			build.append("$").append(i).append(");");
			build.append("long end = System.currentTimeMillis()-start;");
			CtField[] fileds = clazz.getDeclaredFields();
			for(CtField filed : fileds) {
				if(filed.getType().toClass()==Logger.class) {
					build.append(filed.getName()).append(".info(");
					build.append("end+").append("\"ms\"").append(");");
					break;
				}
			}
			if(method.getReturnType()!=CtClass.voidType) {
				build.append("return result;");
			}
			build.append("}");
			System.out.println(build.toString());
			ct.setBody(build.toString());
			clazz.addMethod(ct);

		}
		clazz.writeFile("c:\\x.java");
		clazz.toBytecode();
		return null;

	}

}
