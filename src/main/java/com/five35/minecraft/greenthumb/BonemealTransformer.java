package com.five35.minecraft.greenthumb;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class BonemealTransformer implements IClassTransformer {
	private static final String BONEMEAL_METHOD = "applyBonemeal";
	private static final String DYE_CLASS = "net.minecraft.item.ItemDye";
	private static final String PROXY_CLASS = "com/five35/minecraft/greenthumb/GreenThumb";

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] unmodified) {
		if (!transformedName.equals(BonemealTransformer.DYE_CLASS)) {
			return unmodified;
		}

		System.out.printf("Patching %s.\n", BonemealTransformer.DYE_CLASS);

		final ClassNode node = new ClassNode();
		final ClassReader reader = new ClassReader(unmodified);

		reader.accept(node, 0);

		for (final MethodNode method : node.methods) {
			for (final AbstractInsnNode instruction : method.instructions.toArray()) {
				if (instruction instanceof MethodInsnNode) {
					final MethodInsnNode methodInsruction = (MethodInsnNode) instruction;

					if (methodInsruction.name.equals(BonemealTransformer.BONEMEAL_METHOD)) {
						System.out.printf("Found call to %s, redirecting to %s.\n", BonemealTransformer.BONEMEAL_METHOD, BonemealTransformer.PROXY_CLASS);

						methodInsruction.owner = BonemealTransformer.PROXY_CLASS;
					}
				}
			}
		}

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		node.accept(writer);

		return writer.toByteArray();
	}
}
