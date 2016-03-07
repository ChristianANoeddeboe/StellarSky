package stellarium.sync;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import stellarium.stellars.StellarManager;

public class StellarNetworkManager {
	
	private SimpleNetworkWrapper wrapper;
	
	public StellarNetworkManager() {
		this.wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("stellarskychannel");
		
		wrapper.registerMessage(MessageSyncCommon.MessageSyncCommonHandler.class,
				MessageSyncCommon.class, 0, Side.CLIENT);
		wrapper.registerMessage(MessageLock.MessageLockHandler.class,
				MessageLock.class, 1, Side.SERVER);
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		StellarManager manager = StellarManager.getManager(event.player.worldObj);
		NBTTagCompound compound = new NBTTagCompound();
		manager.writeToNBT(compound);
		
		wrapper.sendTo(new MessageSyncCommon(compound), (EntityPlayerMP)event.player);
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerChangedDimensionEvent event) {
		StellarManager manager = StellarManager.getManager(event.player.worldObj);
		NBTTagCompound compound = new NBTTagCompound();
		manager.writeToNBT(compound);
		
		wrapper.sendTo(new MessageSyncCommon(compound), (EntityPlayerMP)event.player);
	}
	
	public void onTryLock() {
		wrapper.sendToServer(new MessageLock());
	}
	

}