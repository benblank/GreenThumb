package com.five35.minecraft.greenthumb;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

public class LoadingPlugin implements IFMLLoadingPlugin {
	@Override
	public String[] getASMTransformerClass() {
		return new String[] { BonemealTransformer.class.getName(), StemTransformer.class.getName() };
	}

	@Override
	@Deprecated
	public String[] getLibraryRequestClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModContainerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSetupClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectData(final Map<String, Object> data) {
		// TODO Auto-generated method stub
	}
}
