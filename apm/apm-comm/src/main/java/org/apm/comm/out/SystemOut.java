package org.apm.comm.out;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ArrayElementValue;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.LSUB;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apm.comm.annotation.StatisticsTime;

public class SystemOut {
	
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
		
		classGen = new ClassGen(javaClass);
		InstructionFactory ifact = new InstructionFactory(classGen);
		ConstantPoolGen constPool = classGen.getConstantPool();
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
			if (!proxyMethod.isStatic())
				proxyMethod.addLocalVariable("this", new ObjectType(className), null, null);
			for (int i = 0; i < proxyMethod.getArgumentTypes().length; i++) {
				String name = proxyMethod.getArgumentName(i);
				Type type = proxyMethod.getArgumentType(i);
				proxyMethod.addLocalVariable(name, type, null, null);
			}
			// 时间统计
			InstructionList instructionProxy = new InstructionList();
			instructionProxy.append(ifact.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
					Const.INVOKESTATIC));
			LocalVariableGen localV = proxyMethod.addLocalVariable("start", Type.getType(long.class), null, null);
			InstructionHandle locStart = instructionProxy.append(new LSTORE(localV.getIndex()));
			localV.setStart(locStart);
			// 调用原始方法
			short inovke = Const.INVOKESTATIC;
			if (!methodGen.isStatic()) {
				inovke = Const.INVOKEVIRTUAL;
				instructionProxy.append(new ALOAD(0));
			}
			instructionProxy.append(
					ifact.createInvoke(className, proxyMdName, returnType, methodGen.getArgumentTypes(), inovke));
			LocalVariableGen ret = null;
			if (returnType != Type.VOID) {
				ret = proxyMethod.addLocalVariable("result", returnType, null, null);
				InstructionHandle retStart = instructionProxy
						.append(InstructionFactory.createStore(returnType, ret.getIndex()));
				ret.setStart(retStart);
			}
			instructionProxy.append(ifact.createFieldAccess("java.lang.System", "out",
					new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
			instructionProxy.append(ifact.createInvoke("java.lang.System", "currentTimeMillis", Type.LONG, Type.NO_ARGS,
					Const.INVOKESTATIC));
			instructionProxy.append(new LLOAD(localV.getIndex()));
			instructionProxy.append(new LSUB());
			InstructionHandle locEnd = instructionProxy.append(ifact.createInvoke("java.io.PrintStream", "println",
					Type.VOID, new Type[] { Type.LONG }, Const.INVOKEVIRTUAL));
			localV.setEnd(locEnd);
			if (Objects.nonNull(ret)) {
				instructionProxy.append(InstructionFactory.createLoad(returnType, ret.getIndex()));
			}
			instructionProxy.append(InstructionFactory.createReturn(returnType));
			proxyMethod.setInstructionList(instructionProxy);
			proxyMethod.setMaxLocals();
			proxyMethod.setMaxStack();
			classGen.addMethod(proxyMethod.getMethod());

		}

		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();) {
			classGen.getJavaClass().dump(bytes);
         /*
			Arrays.asList(classGen.getJavaClass().getMethods()).forEach(e -> {
				Code code = e.getCode();
				System.out.println(code);
			});
			classGen.getJavaClass().dump(new File(String.format("C:\\java\\%s.class", className.replace('.', '/'))));*/
			return bytes.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

}
