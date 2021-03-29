package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.utils.BlockUtil.Place;
import me.bebeli555.autobot.events.PacketEvent;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.mods.bots.crystalpvpbot.Surround;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Blocks;
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
				if (!InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
					sendMessage("You need obsidian", true);
					toggleModule();
					return;
				}
				
				if (isSolid(getPlayerPos())) {
					sendMessage("You are already burrowed", true);
					toggleModule();
					return;
				}
				
				
				//Switch to obsidian
				int oldSlot = mc.player.inventory.currentItem;
				InventoryUtil.switchItem(InventoryUtil.getSlot(Blocks.OBSIDIAN), false);
				
				//Center
				if (center.booleanValue()) {
					Surround.center();
				}
				
				//Instant mode. this is run on the client thread.
				if (mode.stringValue().equals("Instant")) {
					MinecraftForge.EVENT_BUS.register(burrow);
					sleepUntil(() -> done, 200, 5);
				} 
				
				//Jump mode
				else if (mode.stringValue().equals("Jump")) {
					BlockPos start = getPlayerPos();
					mc.player.jump();
					AutoBot.sleep(delay.intValue());
					BlockUtil.placeBlock(Blocks.OBSIDIAN, start, true);
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
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		BlockPos start = getPlayerPos();
		
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214D, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ, true));
        mc.player.setPosition(mc.player.posX, mc.player.posY + 1.16610926093821D, mc.player.posZ);
        
        new Place(null, Blocks.OBSIDIAN, start, true).onTick(null);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 10, mc.player.posZ, false));
        
        MinecraftForge.EVENT_BUS.unregister(burrow);
        done = true;
	}
	
	@Override
	public void onDisabled() {
		RotationUtil.stopRotating();
		done = false;
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
