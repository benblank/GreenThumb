package com.five35.minecraft.greenthumb;

import java.io.IOException;
import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class StemTransformer extends AccessTransformer {
	public StemTransformer() throws IOException {
		super("greenthumb_at.cfg");
	}
}
