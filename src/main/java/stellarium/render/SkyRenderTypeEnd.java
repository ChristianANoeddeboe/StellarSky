package stellarium.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.IRenderHandler;
import stellarium.api.ICelestialRenderer;
import stellarium.api.ISkyRendererType;

public class SkyRenderTypeEnd implements ISkyRendererType {

	@Override
	public String getName() {
		return "End Sky";
	}

	@Override
	public boolean acceptFor(String worldName) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler createSkyRenderer(ICelestialRenderer subRenderer) {
		return new SkyRendererEnd(subRenderer);
	}

}