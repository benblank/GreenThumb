package com.five35.minecraft.greenthumb;

import java.util.ListIterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class BonemealTransformer implements IClassTransformer {
	private static final String CLASS_NAME = "net.minecraft.item.ItemDye";
	private static final String CLASS_NAME_OBFUSCATED = "xk";
	private static final String METHOD_NAME = "applyBonemeal";

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
		if (!name.equals(BonemealTransformer.CLASS_NAME) && !name.equals(BonemealTransformer.CLASS_NAME_OBFUSCATED)) {
			return bytes;
		}

		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);

		classReader.accept(classNode, 0);

		for (final MethodNode method : classNode.methods) {
			final ListIterator<AbstractInsnNode> insns = method.instructions.iterator();

			while (insns.hasNext()) {
				final AbstractInsnNode insn = insns.next();

				if (insn instanceof MethodInsnNode) {
					final MethodInsnNode methodInsn = (MethodInsnNode) insn;

					if (methodInsn.name.equals(BonemealTransformer.METHOD_NAME)) {
						methodInsn.owner = Type.getInternalName(GreenThumb.class);
					}
				}
			}
		}

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		classNode.accept(writer);

		return writer.toByteArray();
	}
}
