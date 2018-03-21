package org.apm.comm.exetime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LSUB;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/*
 * CGLIB、ASM、BCEL、JAVASSIST  
 * 字节码操作可以使用以上类库
 */
public class TimSummer implements ClassFileTransformer{

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		//System.out.println(className+",加载");
		return becl(classBeingRedefined, className);
	}
	
	public byte[]  becl(Class<?> classBeingRedefined,String className) {
		ClassGen  classGenx = null;
		try {
			JavaClass javaClass = Repository.lookupClass(classBeingRedefined);
			ClassGen classGen = new ClassGen(javaClass);
			classGenx = classGen;
			ConstantPoolGen constPool = classGen.getConstantPool();
			int tIndex = constPool.addMethodref("java/lang/System", "currentTimeMillis", "()J");
			int index = constPool.addFieldref("java/lang/System", "out", "Ljava/io/PrintStream;");
			int mIndex = constPool.addMethodref("java/io/PrintStream", "println", "(J)V");
			constPool.addUtf8("start");
			Arrays.asList(classGen.getMethods()).forEach(method->{
				MethodGen sayGen = new MethodGen(method, className, constPool);
				InstructionList li = sayGen.getInstructionList();
				InstructionHandle s = li.getStart();
				InstructionList start = new InstructionList();
				start.append(new INVOKESTATIC(tIndex));
				LocalVariableGen localV = sayGen.addLocalVariable("start", Type.getType(long.class),null, null);
				InstructionHandle locStart = start.append(new LSTORE(localV.getIndex()));
				localV.setStart(locStart);
				li.insert(s.getInstruction(), start);
				InstructionHandle end = li.getEnd();
				InstructionList out = new InstructionList();
				out.append(new GETSTATIC(index));
				out.append(new INVOKESTATIC(tIndex));
				out.append(new LLOAD(1));
				out.append(new LSUB());
				InstructionHandle locEnd = out.append(new INVOKEVIRTUAL(mIndex));
				localV.setEnd(locEnd);
				li.insert(end.getInstruction(), out);
				sayGen.setMaxStack();
				classGen.replaceMethod(method, sayGen.getMethod());
				
			});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try(ByteArrayOutputStream bytes = new ByteArrayOutputStream();) {
			classGenx.getJavaClass().dump(bytes);
			
			/*Arrays.asList(classGenx.getJavaClass().getMethods()).forEach(e->{
				Code code = e.getCode();
				System.out.println(code);
			});
			classGenx.getJavaClass().dump(new File(String.format("C:\\java\\%s.class", className)));*/
			return bytes.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}

}
