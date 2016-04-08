package stellarium.world;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sciapi.api.value.euclidian.EVector;
import stellarium.api.ISkyProvider;
import stellarium.render.SkyRenderer;
import stellarium.stellars.StellarManager;
import stellarium.stellars.view.StellarDimensionManager;
import stellarium.util.math.SpCoord;
import stellarium.util.math.Spmath;

public class StellarWorldProvider extends WorldProvider implements ISkyProvider {
	
	private static final int DEFAULT_OFFSET = 1000;
	
	private WorldProvider parProvider;
	private StellarManager manager;
	private StellarDimensionManager dimManager;
	
	private final float[] colorsSunriseSunset = new float[4];
		
	private long cloudColour = 16777215L;
	
	public StellarWorldProvider(World world, WorldProvider provider,
			StellarManager manager, StellarDimensionManager dimManager) {
		this.parProvider = provider;
		this.worldObj = world;
		this.manager = manager;
		this.dimManager = dimManager;
	}

	@Override
    public float calculateCelestialAngle(long par1, float par3) {
		double dayLength = manager.getSettings().day;
		double longitude = dimManager.getSettings().longitude / 360.0;
		double skyTime = manager.getSkyTime(par1 + par3);
		double angle = skyTime / dayLength + longitude + 0.5;
		return (float) (angle - Math.floor(angle));
    }
	
	public float calculateSunHeight(long par1, float par3) {
    	if(!manager.updated())
    	{
    		manager.update(par1+par3);
    		dimManager.update(this.worldObj, par1+par3);
    	}
   	
    	return (float) Spmath.sind(dimManager.sunAppPos.y);
	}
	
	public float calculateSunlightFactor(long par1, float par3) {
		return (float) ((2.0*this.calculateSunHeight(par1, par3)+0.5)*dimManager.getSettings().sunlightMultiplier);
	}
    
    @Override
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1)
    {
        float f1 = 1.0F - (this.calculateSunlightFactor(worldObj.getWorldTime(), par1) - 0.3F);
        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(worldObj.getRainStrength(par1) * 5.0F) / 16.0D));
        f1 = (float)((double)f1 * (1.0D - (double)(worldObj.getThunderStrength(par1) * 5.0F) / 16.0D));
        return f1 * 0.8F + 0.2F;
    }
	
	@Override
	public float getSunBrightnessFactor(float par1) {
        float f1 = 1.0F - this.calculateSunlightFactor(worldObj.getWorldTime(), par1);
        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(worldObj.getRainStrength(par1) * 5.0F) / 16.0D));
        f1 = (float)((double)f1 * (1.0D - (double)(worldObj.getThunderStrength(par1) * 5.0F) / 16.0D));
        return f1;
	}

	@Override
    public int getMoonPhase(long par1) {
    	if(!manager.updated())
    		return parProvider.getMoonPhase(par1);
    	
    	return (int)(dimManager.moonFactors[2]*8);
    }
	
	@Override
	public float getCurrentMoonPhaseFactor() {
    	if(!manager.updated())
    		return parProvider.getCurrentMoonPhaseFactor();
		return (float) dimManager.moonFactors[1];
	}
	
	
	/* ---------------------------------------
	 * Start of Stellar Sky API Implementation
	 * --------------------------------------- */
	@Override
	public double getDayLength() {
		return manager.getSettings().day;
	}

	@Override
	public double getLunarMonthLength() {
		double period = dimManager.moonFactors[0];
		return period / (1.0 - period) * manager.getSettings().year;
	}

	@Override
	public double getYearLength() {
		return manager.getSettings().year;
	}

	@Override
	public double getDaytimeOffset() {
		return this.getDaytimeOffset(worldObj.getWorldTime());
	}
	
	@Override
	public double getDaytimeOffset(long tick) {
		return Spmath.fmod((tick + manager.getSettings().tickOffset + DEFAULT_OFFSET) / manager.getSettings().day, 1.0) + dimManager.getSettings().longitude / 360.0 + 0.5;
	}

	@Override
	public double getYearlyOffset() {
		return this.getYearlyOffset(worldObj.getWorldTime());
	}
	
	@Override
	public double getYearlyOffset(long tick) {
		return Spmath.fmod(((tick + manager.getSettings().tickOffset + DEFAULT_OFFSET) / manager.getSettings().day + manager.getSettings().dayOffset) / manager.getSettings().year, 1.0);
	}
	
	@Override
	public double dayOffsetUntilSunReach(double heightAngle) {
		double radLatitude = Spmath.Radians(dimManager.getSettings().latitude);

		SpCoord coord = new SpCoord();
		coord.setWithVec(dimManager.sunEquatorPos);
		
		return this.hourAngleForHeight(heightAngle, Spmath.Radians(coord.y), radLatitude) / (2 * Math.PI);
	}
	
	@Override
	public double dayOffsetUntilMoonReach(double heightAngle) {
		double radLatitude = Spmath.Radians(dimManager.getSettings().latitude);

		SpCoord coord = new SpCoord();
		coord.setWithVec(dimManager.moonEquatorPos);
		
		return this.hourAngleForHeight(heightAngle, Spmath.Radians(coord.y), radLatitude) / (2 * Math.PI);
	}
	
	private double hourAngleForHeight(double heightAngle, double dec, double lat) {
		return Math.acos((Spmath.sind(heightAngle) - Math.sin(dec) * Math.sin(lat)) / (Math.cos(dec) * Math.cos(lat)));
	}

	@Override
	public Vector3f getCurrentSunPosition() {
		EVector sun = dimManager.sunAppPos.getVec();
    	
    	return new Vector3f(sun.getCoord(0).asFloat(),
    			sun.getCoord(1).asFloat(),
    			sun.getCoord(2).asFloat());
	}

	@Override
	public Vector3f getCurrentMoonPosition() {
    	EVector moon = dimManager.moonAppPos.getVec();
    	
    	return new Vector3f(moon.getCoord(0).asFloat(),
    			moon.getCoord(1).asFloat(),
    			moon.getCoord(2).asFloat());
	}

	@Override
	public double getHighestSunHeightAngle() {
		SpCoord crd = new SpCoord();
		crd.setWithVec(dimManager.sunEquatorPos);
		
		return 90.0 - Math.abs(dimManager.getSettings().latitude - crd.y);
	}

	@Override
	public double getHighestMoonHeightAngle() {
		SpCoord crd = new SpCoord();
		crd.setWithVec(dimManager.moonEquatorPos);
		
		return 90.0 - Math.abs(dimManager.getSettings().latitude - crd.y);
	}
	
	/* ---------------------------------------
	 * End of Stellar Sky API Implementation
	 * --------------------------------------- */
    /**
     * Returns a new chunk provider which generates chunks for this world
     */
	@Override
    public IChunkProvider createChunkGenerator()
    {
        return parProvider.createChunkGenerator();
    }

    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
	@Override
    public boolean canCoordinateBeSpawn(int p_76566_1_, int p_76566_2_)
    {
        return parProvider.canCoordinateBeSpawn(p_76566_1_, p_76566_2_);
    }

    /**
     * Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
     */
	@Override
    public boolean isSurfaceWorld()
    {
        return parProvider.isSurfaceWorld();
    }

    /**
     * Returns array with sunrise/sunset colors
     */
    @SideOnly(Side.CLIENT)
    @Override
    public float[] calcSunriseSunsetColors(float p_76560_1_, float p_76560_2_)
    {
        float f = 0.4F;
        float f1 = this.calculateSunHeight(worldObj.getWorldTime(), p_76560_2_) - 0.0F;
        float f2 = -0.0F;

        if (f1 >= f2 - f && f1 <= f2 + f)
        {
            float f3 = (f1 - f2) / f * 0.5F + 0.5F;
            float f4 = 1.0F - (1.0F - MathHelper.sin(f3 * (float)Math.PI)) * 0.99F;
            f4 = f4 * f4;
            this.colorsSunriseSunset[0] = f3 * 0.3F + 0.7F;
            this.colorsSunriseSunset[1] = f3 * f3 * 0.7F + 0.2F;
            this.colorsSunriseSunset[2] = f3 * f3 * 0.0F + 0.2F;
            this.colorsSunriseSunset[3] = f4;
            
            for(int i = 0; i < 4; i++)
            	this.colorsSunriseSunset[i] = this.colorsSunriseSunset[i] * (float)Math.sqrt(dimManager.getSettings().sunlightMultiplier);
            
            return this.colorsSunriseSunset;
        }
        else
        {
            return null;
        }
    }

    /**
     * Return Vec3D with biome specific fog color
     */
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3 getFogColor(float p_76562_1_, float p_76562_2_)
    {
        float f = this.calculateSunlightFactor(worldObj.getWorldTime(), p_76562_2_);
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        float f1 = 0.7529412F;
        float f2 = 0.84705883F;
        float f3 = 1.0F;
        f1 = f1 * (f * 0.94F + 0.06F);
        f2 = f2 * (f * 0.94F + 0.06F);
        f3 = f3 * (f * 0.91F + 0.09F);
        return new Vec3((double)f1, (double)f2, (double)f3);
    }

    /**
     * True if the player can respawn in this dimension (true = overworld, false = nether).
     */
    @Override
    public boolean canRespawnHere()
    {
        return parProvider.canRespawnHere();
    }

    /**
     * the y level at which clouds are rendered.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public float getCloudHeight()
    {
        return parProvider.getCloudHeight();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSkyColored()
    {
        return parProvider.isSkyColored();
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension.
     */
    @Override
    public BlockPos getSpawnCoordinate()
    {
        return parProvider.getSpawnCoordinate();
    }

    @Override
    public int getAverageGroundLevel()
    {
        return parProvider.getAverageGroundLevel();
    }
    
    /**
     * Returns a double value representing the Y value relative to the top of the map at which void fog is at its
     * maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at
     * (256*0.03125), or 8.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public double getVoidFogYFactor()
    {
        return parProvider.getVoidFogYFactor();
    }

    /**
     * Returns true if the given X,Z coordinate should show environmental fog.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean doesXZShowFog(int p_76568_1_, int p_76568_2_)
    {
        return parProvider.doesXZShowFog(p_76568_1_, p_76568_2_);
    }

    /**
     * Returns the dimension's name, e.g. "The End", "Nether", or "Overworld".
     */
    @Override
    public String getDimensionName() {
    	return parProvider.getDimensionName();
    }
    
    @Override
    public String getInternalNameSuffix() {
    	return parProvider.getInternalNameSuffix();
    }
    
    @Override
    public WorldChunkManager getWorldChunkManager()
    {
        return parProvider.getWorldChunkManager();
    }
    
    @Override
    public boolean doesWaterVaporize()
    {
        return parProvider.doesWaterVaporize();
    }
    
    @Override
    public boolean getHasNoSky()
    {
        return parProvider.getHasNoSky();
    }
    
    @Override
    public float[] getLightBrightnessTable()
    {
        return parProvider.getLightBrightnessTable();
    }
    
    /**
     * Gets the dimension of the provider
     */
    @Override
    public int getDimensionId()
    {
        return parProvider.getDimensionId();
    }
    
    @Override
    public WorldBorder getWorldBorder()
    {
        return parProvider.getWorldBorder();
    }
    
    /*======================================= Forge Start =========================================*/
    
    /**
     * Sets the providers current dimension ID, used in default getSaveFolder()
     * Added to allow default providers to be registered for multiple dimensions.
     * 
     * @param dim Dimension ID
     */
    @Override
    public void setDimension(int dim)
    {
        this.dimensionId = dim;
    }

    /**
     * Returns the sub-folder of the world folder that this WorldProvider saves to.
     * EXA: DIM1, DIM-1
     * @return The sub-folder name to save this world's chunks to.
     */
    @Override
    public String getSaveFolder()
    {
        return parProvider.getSaveFolder();
    }

    /**
     * A message to display to the user when they transfer to this dimension.
     *
     * @return The message to be displayed
     */
    @Override
    public String getWelcomeMessage()
    {
        return parProvider.getWelcomeMessage();
    }

    /**
     * A Message to display to the user when they transfer out of this dismension.
     *
     * @return The message to be displayed
     */
    @Override
    public String getDepartMessage()
    {
        return parProvider.getDepartMessage();
    }

    /**
     * The dimensions movement factor. Relative to normal overworld.
     * It is applied to the players position when they transfer dimensions.
     * Exa: Nether movement is 8.0
     * @return The movement factor
     */
    @Override
    public double getMovementFactor()
    {
        return parProvider.getMovementFactor();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setSkyRenderer(net.minecraftforge.client.IRenderHandler skyRenderer)
    {
        if(skyRenderer instanceof SkyRenderer)
        	super.setSkyRenderer(skyRenderer);
    }

    @Override
    public BlockPos getRandomizedSpawnPoint()
    {
        return parProvider.getRandomizedSpawnPoint();
    }
    
    /**
     * Determine if the cusor on the map should 'spin' when rendered, like it does for the player in the nether.
     * 
     * @param entity The entity holding the map, playername, or frame-ENTITYID
     * @param x X Position
     * @param y Y Position
     * @param z Z Postion
     * @return True to 'spin' the cursor
     */
    @Override
    public boolean shouldMapSpin(String entity, double x, double y, double z)
    {
        return parProvider.shouldMapSpin(entity, x, y, z);
    }

    /**
     * Determines the dimension the player will be respawned in, typically this brings them back to the overworld.
     * 
     * @param player The player that is respawning
     * @return The dimension to respawn the player in
     */
    @Override
    public int getRespawnDimension(EntityPlayerMP player)
    {
        return parProvider.getRespawnDimension(player);
    }

    /*======================================= Start Moved From World =========================================*/
    
    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        return parProvider.getBiomeGenForCoords(pos);
    }

    @Override
    public boolean isDaytime()
    {
        return parProvider.isDaytime();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 getSkyColor(Entity cameraEntity, float partialTicks)
    {
        float f1 = this.calculateSunlightFactor(worldObj.getWorldTime(), partialTicks);
        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        int i = MathHelper.floor_double(cameraEntity.posX);
        int j = MathHelper.floor_double(cameraEntity.posY);
        int k = MathHelper.floor_double(cameraEntity.posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        int l = net.minecraftforge.client.ForgeHooksClient.getSkyBlendColour(this.worldObj, blockpos);
        float f3 = (float)(l >> 16 & 255) / 255.0F;
        float f4 = (float)(l >> 8 & 255) / 255.0F;
        float f5 = (float)(l & 255) / 255.0F;
        f3 = f3 * f1;
        f4 = f4 * f1;
        f5 = f5 * f1;
        float f6 = worldObj.getRainStrength(partialTicks);

        if (f6 > 0.0F)
        {
            float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
            float f8 = 1.0F - f6 * 0.75F;
            f3 = f3 * f8 + f7 * (1.0F - f8);
            f4 = f4 * f8 + f7 * (1.0F - f8);
            f5 = f5 * f8 + f7 * (1.0F - f8);
        }

        float f10 = worldObj.getThunderStrength(partialTicks);

        if (f10 > 0.0F)
        {
            float f11 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
            float f9 = 1.0F - f10 * 0.75F;
            f3 = f3 * f9 + f11 * (1.0F - f9);
            f4 = f4 * f9 + f11 * (1.0F - f9);
            f5 = f5 * f9 + f11 * (1.0F - f9);
        }

        if (worldObj.getLastLightningBolt() > 0)
        {
            float f12 = (float)worldObj.getLastLightningBolt() - partialTicks;

            if (f12 > 1.0F)
            {
                f12 = 1.0F;
            }

            f12 = f12 * 0.45F;
            f3 = f3 * (1.0F - f12) + 0.8F * f12;
            f4 = f4 * (1.0F - f12) + 0.8F * f12;
            f5 = f5 * (1.0F - f12) + 1.0F * f12;
        }

        return new Vec3((double)f3, (double)f4, (double)f5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 drawClouds(float partialTicks)
    {
        float f1 = this.calculateSunlightFactor(worldObj.getWorldTime(), partialTicks);
        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        float f2 = (float)(this.cloudColour >> 16 & 255L) / 255.0F;
        float f3 = (float)(this.cloudColour >> 8 & 255L) / 255.0F;
        float f4 = (float)(this.cloudColour & 255L) / 255.0F;
        float f5 = worldObj.getRainStrength(partialTicks);

        if (f5 > 0.0F)
        {
            float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
            float f7 = 1.0F - f5 * 0.95F;
            f2 = f2 * f7 + f6 * (1.0F - f7);
            f3 = f3 * f7 + f6 * (1.0F - f7);
            f4 = f4 * f7 + f6 * (1.0F - f7);
        }

        f2 = f2 * (f1 * 0.9F + 0.1F);
        f3 = f3 * (f1 * 0.9F + 0.1F);
        f4 = f4 * (f1 * 0.85F + 0.15F);
        float f9 = worldObj.getThunderStrength(partialTicks);

        if (f9 > 0.0F)
        {
            float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
            float f8 = 1.0F - f9 * 0.95F;
            f2 = f2 * f8 + f10 * (1.0F - f8);
            f3 = f3 * f8 + f10 * (1.0F - f8);
            f4 = f4 * f8 + f10 * (1.0F - f8);
        }
        
        worldObj.drawCloudsBody(partialTicks);

        return new Vec3((double)f2, (double)f3, (double)f4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1)
    {
        float f1 = 1.0F - (this.calculateSunlightFactor(worldObj.getWorldTime(), par1) - 0.25F);
        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    @Override
    public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful)
    {
    	parProvider.setAllowedSpawnTypes(allowHostile, allowPeaceful);
    }

    @Override
    public void calculateInitialWeather()
    {
    	parProvider.calculateInitialWeather();
    }

    @Override
    public void updateWeather()
    {
    	parProvider.updateWeather();
    }

    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean byWater)
    {
        return parProvider.canBlockFreeze(pos, byWater);
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight)
    {
        return parProvider.canSnowAt(pos, checkLight);
    }

    @Override
    public void setWorldTime(long time)
    {
    	parProvider.setWorldTime(time);
    }

    @Override
    public long getSeed()
    {
    	return parProvider.getSeed();
    }

    @Override
    public long getWorldTime()
    {
        return parProvider.getWorldTime();
    }

    @Override
    public BlockPos getSpawnPoint()
    {
        return parProvider.getSpawnPoint();
    }

    @Override
    public void setSpawnPoint(BlockPos pos)
    {
    	parProvider.setSpawnPoint(pos);
    }

    @Override
    public boolean canMineBlock(EntityPlayer player, BlockPos pos)
    {
    	return parProvider.canMineBlock(player, pos);
    }

    @Override
    public boolean isBlockHighHumidity(BlockPos pos)
    {
        return parProvider.isBlockHighHumidity(pos);
    }

    @Override
    public int getHeight()
    {
        return parProvider.getHeight();
    }

    @Override
    public int getActualHeight()
    {
        return parProvider.getActualHeight();
    }

    @Override
    public double getHorizon()
    {
        return parProvider.getHorizon();
    }

    @Override
    public void resetRainAndThunder()
    {
    	parProvider.resetRainAndThunder();
    }

    @Override
    public boolean canDoLightning(Chunk chunk)
    {
    	return parProvider.canDoLightning(chunk);
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk)
    {
    	return parProvider.canDoRainSnowIce(chunk);
    }
}