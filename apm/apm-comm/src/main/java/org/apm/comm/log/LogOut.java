package org.apm.comm.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ArrayElementValue;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LSUB;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apm.comm.annotation.StatisticsTime;
import org.slf4j.Logger;

public class LogOut {

	public static byte[] becl(byte[] classfileBuffer, String className) throws ClassFormatException, IOException {
		ClassGen classGen = null;
		ByteArrayInputStream is = new ByteArrayInputStream(classfileBuffer);
		ClassParser classParser = new ClassParser(is, className);
		JavaClass javaClass = classParser.parse();
		AnnotationEntry statisticsTime = null;
		for (AnnotationEntry entry : javaClass.getAnnotationEntries()) {
			if (Type.getType(StatisticsTime.class).getSignature().equals(entry.getAnnotationType())) {
				statisticsTime = entry;
				break;
			}
		}
		if (Objects.isNull(statisticsTime)) {
			return null;
		}
		// 去重方法
		Set<String> includeMds = new HashSet<>();
		ElementValuePair valPair = statisticsTime.getElementValuePairs()[0];
		ArrayElementValue vals = (ArrayElementValue) valPair.getValue();
		for (ElementValue s : vals.getElementValuesArray()) {
			includeMds.add(s.stringifyValue());
		}
		//log
		if(Objects.isNull(javaClass.getFields()))
			return null;
		Field log = null;
		for(Field field : javaClass.getFields()) {
			if(Type.getType(Logger.class).getSignature().equals(field.getSignature())) {
				log = field;
				break;
			}
		}
        
		if(Objects.isNull(log))
		  return null;
	
		classGen = new ClassGen(javaClass);
		InstructionFactory ifact = new InstructionFactory(classGen);
		ConstantPoolGen constPool = classGen.getConstantPool();
		int ms = constPool.addString("ms");
		for (Method method : classGen.getMethods()) {
			if (!includeMds.contains(method.getName())) {
				continue;
			}
			// 代理方法
			String proxyMdName = method.getName() + "$Imp";
			MethodGen methodGen = new MethodGen(method, className, constPool);
			MethodGen proxyMethod = new MethodGen(method, className, constPool);
			classGen.removeMethod(methodGen.getMethod());
			methodGen.setName(proxyMdName);
			classGen.addMethod(methodGen.getMethod());
			Type returnType = methodGen.getReturnType();
			// 参数
			proxyMethod.removeLocalVariables();
			ObjectType thisType = new ObjectType(className);
			
			// 时间统计
			InstructionList instructionProxy = new InstructionList();
			InstructionHandle s = instructionProxy.append(ifact.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
					Const.INVOKESTATIC));
			int ofs = 0;
			if (!proxyMethod.isStatic()) {
				 proxyMethod.addLocalVariable("this", thisType,0, null, null);
				ofs = 1;
			}
			for (int i = 0; i < proxyMethod.getArgumentTypes().length; i++) {
				String name = proxyMethod.getArgumentName(i);
				Type type = proxyMethod.getArgumentType(i);
				proxyMethod.addLocalVariable(name, type, ofs, null, null);
				ofs+= type.getSize();
			}
			LocalVariableGen localV = proxyMethod.addLocalVariable("start", Type.getType(long.class), null, null);
			InstructionHandle locStart = instructionProxy.append(new LSTORE(localV.getIndex()));
			localV.setStart(locStart);
			
			// 调用原始方法
			short inovke = Const.INVOKESTATIC;
			int offset = 0;
			if (!methodGen.isStatic()) {
				inovke = Const.INVOKEVIRTUAL;
				offset = 1;
				instructionProxy.append(new ALOAD(0));
			}
			LocalVariableGen[] localVars  = proxyMethod.getLocalVariables();
			for (int i = 0; i < proxyMethod.getArgumentTypes().length; i++) {
				int index = localVars[i+offset].getIndex();
				Type type = proxyMethod.getArgumentType(i);
				instructionProxy.append(InstructionFactory.createLoad(type, index));
				
			}
			InstructionHandle invoke = instructionProxy.append(
					ifact.createInvoke(className, proxyMdName, returnType, methodGen.getArgumentTypes(), inovke));
			for (int i=0; i < proxyMethod.getArgumentTypes().length; i++) {
				 localVars[offset+i].setEnd(invoke);
			}
			LocalVariableGen ret = null;
			if (returnType != Type.VOID) {
				ret = proxyMethod.addLocalVariable("result", returnType, null, null);
				InstructionHandle retStart = instructionProxy
						.append(InstructionFactory.createStore(returnType, ret.getIndex()));
				ret.setStart(retStart);
			}
			
			instructionProxy.append(ifact.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
					Const.INVOKESTATIC));
			instructionProxy.append(new LLOAD(localV.getIndex()));
			instructionProxy.append(new LSUB());
			LocalVariableGen endLoc = proxyMethod.addLocalVariable("end", Type.getType(long.class), null, null);
			InstructionHandle endInt = instructionProxy.append(new LSTORE(endLoc.getIndex()));
			endLoc.setStart(endInt);
			instructionProxy.append(ifact.createFieldAccess(className,log.getName(),
					log.getType(),log.isStatic()? Const.GETSTATIC:Const.GETFIELD));
			instructionProxy.append(ifact.createNew(new ObjectType(StringBuilder.class.getName())));
			instructionProxy.append(new DUP());
			instructionProxy.append(new LLOAD(endLoc.getIndex()));
			instructionProxy.append(ifact.createInvoke(String.class.getName(), "valueOf", Type.getType(String.class), new Type[] {Type.LONG}, Const.INVOKESTATIC));
			instructionProxy.append(ifact.createInvoke(StringBuilder.class.getName(), "<init>", Type.VOID, new Type[] {Type.STRING}, Const.INVOKESPECIAL));
			instructionProxy.append(new LDC(ms));
			instructionProxy.append(ifact.createInvoke(StringBuilder.class.getName(), "append", Type.getType(StringBuilder.class), new Type[] {Type.STRING}, Const.INVOKEVIRTUAL));
			instructionProxy.append(ifact.createInvoke(StringBuilder.class.getName(), "toString", Type.STRING, Type.NO_ARGS, Const.INVOKEVIRTUAL));
			InstructionHandle locEnd = instructionProxy.append(ifact.createInvoke(Logger.class.getName(), "info",
					Type.VOID, new Type[] { Type.STRING }, Const.INVOKEINTERFACE));
			localV.setEnd(locEnd);
			if (Objects.nonNull(ret)) {
				instructionProxy.append(InstructionFactory.createLoad(returnType, ret.getIndex()));
			}
			instructionProxy.append(InstructionFactory.createReturn(returnType));
			if (!methodGen.isStatic()) {
				localVars[0].setEnd(instructionProxy.getEnd());
			}
			proxyMethod.setInstructionList(instructionProxy);
			proxyMethod.setMaxLocals();
			proxyMethod.setMaxStack();
			classGen.addMethod(proxyMethod.getMethod());

		}

		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();) {
			classGen.getJavaClass().dump(bytes);
         
			Arrays.asList(classGen.getJavaClass().getMethods()).forEach(e -> {
				Code code = e.getCode();
				System.out.println(code);
			});
			classGen.getJavaClass().dump(new File(String.format("C:\\java\\%s.class", className.replace('.', '/'))));
			return bytes.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

}
