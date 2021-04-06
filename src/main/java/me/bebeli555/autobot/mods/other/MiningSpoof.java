package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PacketEvent;
import me.bebeli555.autobot.events.PlayerDamageBlockEvent;
import me.bebeli555.autobot.events.PlayerMotionUpdateEvent;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

public class MiningSpoof extends AutoBot {
	private static boolean noPick;

	public MiningSpoof() {
		super(Group.OTHER, "MiningSpoof", "Allows you to mine blocks with your pickaxe", "Even when your not actually holding it", "Just start mining a block with this enabled", "And it will mine it as u would be using the pickaxe", "But you will not hold the pickaxe server/client side", "You need a pickaxe in ur hotbar for this to work");
	}
	
	@Override
	public void onEnabled() {
		AutoBot.EVENT_BUS.subscribe(this);
	}
	
	@Override
	public void onDisabled() {
		AutoBot.EVENT_BUS.unsubscribe(this);
		noPick = false;
	}
	
	@EventHandler
	private Listener<PlayerMotionUpdateEvent> onMotionUpdate = new Listener<>(event -> {
		if (mc.player == null || !mc.gameSettings.keyBindAttack.isKeyDown() || mc.objectMouseOver.getBlockPos() == null) {
			return;
		}
		
		//Return if block cant be mined
		if (mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlockHardness(mc.world, mc.objectMouseOver.getBlockPos()) <= 0) {
			noPick = true;
			return;
		}
		
		int pickSlot = getPickaxeSlot();
		if (pickSlot == -1) {
			noPick = true;
			return;
		} else if (pickSlot == mc.player.inventory.currentItem) {
			return;
		}
	
		noPick = false;
		int oldSlot = mc.player.inventory.currentItem;
		
		//Switch to pickaxe
		mc.player.inventory.currentItem = pickSlot;
		mc.playerController.updateController();
		
		//Mine block
		mc.player.swingArm(EnumHand.MAIN_HAND);
		mc.playerController.onPlayerDamageBlock(mc.objectMouseOver.getBlockPos(), mc.objectMouseOver.sideHit);
		
		//Switch to old slot
		mc.player.inventory.currentItem = oldSlot;
		mc.playerController.updateController();
	});
	
	@EventHandler
	private Listener<PlayerDamageBlockEvent> playerDamageBlockEvent = new Listener<>(event -> {
		if (mc.player.inventory.currentItem != getPickaxeSlot() && !noPick) {
			event.cancel();
		}
	});
	
	public static int getPickaxeSlot() {
		Item[] picks = {Items.DIAMOND_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE};
		
		for (Item pick : picks) {
			if (InventoryUtil.hasHotbarItem(pick)) {
				return InventoryUtil.getSlot(pick);
			}
		}
		
		return -1;
	}
    
    public static class CancelForceRotation {
    	public static CancelForceRotation instance = new CancelForceRotation();
    	
        @EventHandler
        private Listener<PacketEvent> packetEvent = new Listener<>(event -> {
        	if (mc.player == null) {
    			return;
        	}
        	
        	if (event.packet instanceof SPacketPlayerPosLook) {
        		event.cancel();
        		
        		SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.packet;
        		mc.player.setPosition(packet.getX(), packet.getY(), packet.getZ());
        		mc.player.motionY = 0;
        		
        		float[] rotations = RotationUtil.getRotations(new Vec3d(getPlayerPos().add(0, -1, 0)).add(0.5, 0.5, 0.5).add(new Vec3d(EnumFacing.UP.getDirectionVec()).scale(0.5)));
        		
                mc.getConnection().sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                mc.getConnection().sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.getEntityBoundingBox().minY, mc.player.posZ, rotations[0], rotations[1], false));
        	}
        });
    }
}
