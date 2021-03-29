package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PacketEvent;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.mods.Mods;
import me.bebeli555.autobot.mods.RegisterMod;
import me.bebeli555.autobot.mods.bots.crystalpvpbot.Surround;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.MessageUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;

@RegisterMod(displayName = "Burrow", description = "Glitches you inside obsidian", group = Group.OTHER)
public class Burrow extends Mods {
	private static Burrow burrow;
	
	public static Setting delay = new Setting(Mode.INTEGER, "Delay", 250, "Delay in ms to wait after the first jump", "To placing the block and second jump");

	public Burrow() {
		burrow = this;
	}

	@Override
	public void onEnabled() {
		if (mc.player == null) {
			toggleModule();
			return;
		}
		
		new Thread(() -> {
			if (!InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
				MessageUtil.sendWarningMessage("You need obsidian");
				toggleModule();
				return;
			}

			if (isSolid(getPlayerPos())) {
				MessageUtil.sendWarningMessage("You are already burrowed");
				toggleModule();
				return;
			}

			//Switch to obsidian
			InventoryUtil.switchItem(InventoryUtil.getSlot(Blocks.OBSIDIAN), false);

			//Center
			Surround.center();

			//Jump
			BlockPos start = getPlayerPos();
			mc.player.jump();

			//Sleep
			sleep(delay.intValue());

			//Place block and jump
			BlockUtil.placeBlock(Blocks.OBSIDIAN, start, true);
			mc.player.jump();

			//Cancel force rotation from server
			AutoBot.EVENT_BUS.subscribe(burrow);
		}).start();
	}
	
	@Override
	public void onDisabled() {
		RotationUtil.stopRotating();
	}
	
    @EventHandler
    private Listener<PacketEvent> packetEvent = new Listener<>(event -> {
    	if (event.packet instanceof SPacketPlayerPosLook) {
    		event.cancel();
    		
    		SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.packet;
    		mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
    		mc.player.motionY = 0;
    		
            mc.getConnection().sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
            mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, packet.getYaw(), packet.getPitch(), false));
    		
			AutoBot.EVENT_BUS.unsubscribe(burrow);
			toggleModule();
    	}
    });
}
