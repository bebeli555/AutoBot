package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PacketEvent;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.mods.bots.crystalpvpbot.Surround;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.InventoryUtil.ItemStackUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class Burrow extends AutoBot {
	private static Burrow burrow;
	private static boolean done;
	private static Block placeBlock;
	
	public static Setting mode = new Setting(null, "Mode", "Instant", new String[]{"Instant", "Places the block instantly"}, new String[]{"Jump", "Jumps and places the block"});
		public static Setting delay = new Setting(mode, "Jump", Mode.INTEGER, "Delay", 250, "Delay in ms to wait after the first jump", "To placing the block and second jump");
	public static Setting center = new Setting(Mode.BOOLEAN, "Center", true, "Centers you in the middle of the block before doing the thing");
	
	public Burrow() {
		super(Group.OTHER, "Burrow", true, "Glitches you inside obsidian");
		burrow = this;
	}
	
	@Override
	public void onEnabled() {
		if (mc.player == null) {
			toggleModule();
			return;
		}
		
		new Thread() {
			public void run() {				
				if (isSolid(getPlayerPos())) {
					sendMessage("You are already burrowed", true);
					toggleModule();
					return;
				}
				
				Block block = null;
				
				//If the player has obsidian then use that as its the best
				if (InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
					block = Blocks.OBSIDIAN;
				} else {
					//First search the hotbar for blocks
					for (int i = 0; i < 9; i++) {
						if (InventoryUtil.getItemStack(i).getItem() instanceof ItemBlock) {
							block = Block.getBlockFromItem(InventoryUtil.getItemStack(i).getItem());
							break;
						}
					}
					
					//Then search the entire inventory if no blocks were in hotbar
					if (block == null) {
						for (ItemStackUtil itemStack : InventoryUtil.getAllItems()) {
							if (itemStack.itemStack.getItem() instanceof ItemBlock) {
								block = Block.getBlockFromItem(itemStack.itemStack.getItem());
								break;
							}
						}
					}
				}
				
				if (block == null) {
					sendMessage("You dont have any placeable block", true);
					toggleModule();
					return;
				}
				
				//Switch to block
				int oldSlot = mc.player.inventory.currentItem;
				InventoryUtil.switchItem(InventoryUtil.getSlot(block), false);
				
				//Center
				if (center.booleanValue()) {
					Surround.center();
				}
				
				//Instant mode. this is run on the minecraft thread.
				if (mode.stringValue().equals("Instant")) {
					placeBlock = block;
					MinecraftForge.EVENT_BUS.register(burrow);
					sleepUntil(() -> done, 200, 5);
				} 
				
				//Jump mode
				else if (mode.stringValue().equals("Jump")) {
					BlockPos start = getPlayerPos();
					mc.player.jump();
					AutoBot.sleep(delay.intValue());
					BlockUtil.placeBlock(block, start, true);
					mc.player.jump();
				}
				
				//Put old hotbar slot back
				mc.player.inventory.currentItem = oldSlot;
				
				//Cancel force rotation from server
				AutoBot.EVENT_BUS.subscribe(burrow);
				toggleModule();
			}
		}.start();
	}
	
	@Override
	public void onDisabled() {
		RotationUtil.stopRotating();
		done = false;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		BlockPos start = getPlayerPos();
		
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));
        mc.player.setPosition(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ);
        
        BlockUtil.placeBlockOnThisThread(placeBlock, start, true);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 10, mc.player.posZ, false));
        mc.player.setPosition(mc.player.posX, start.getY(), mc.player.posZ);
        
        MinecraftForge.EVENT_BUS.unregister(burrow);
        done = true;
	}
	
    @EventHandler
    private Listener<PacketEvent> packetEvent = new Listener<>(event -> {
    	if (mc.player == null) {
			AutoBot.EVENT_BUS.unsubscribe(burrow);
			return;
    	}
    	
    	if (event.packet instanceof SPacketPlayerPosLook) {
    		event.cancel();
    		
    		SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.packet;
    		mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
    		mc.player.motionY = 0;
    		
            mc.getConnection().sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
            mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, packet.getYaw(), packet.getPitch(), false));
    		
			AutoBot.EVENT_BUS.unsubscribe(burrow);
    	}
    });
}
