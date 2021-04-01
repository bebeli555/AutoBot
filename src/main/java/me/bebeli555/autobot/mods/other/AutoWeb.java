package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.PlayerUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoWeb extends AutoBot {
	public static Setting toggle = new Setting(Mode.BOOLEAN, "Toggle", false, "Toggle the module off after the place");
	public static Setting autoDetect = new Setting(Mode.BOOLEAN, "AutoDetect", false, "Detects when a nearby player is trying", "To enter your hole and then places the web");
	
	public AutoWeb() {
		super(Group.OTHER, "AutoWeb", "Places a web inside you");
	}
	
	@Override
	public void onEnabled() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onDisabled() {
		MinecraftForge.EVENT_BUS.unregister(this);
		RotationUtil.stopRotating();
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		if (mc.player == null || getBlock(getPlayerPos()) == Blocks.WEB) {
			return;
		}
		
		if (!InventoryUtil.hasBlock(Blocks.WEB)) {
			sendMessage("You have no webs", true);
			toggleModule();
			return;
		}
		
		//Return if autoDetect is true and the player is not trying to enter the hole
		if (autoDetect.booleanValue()) {
			EntityPlayer player = PlayerUtil.getClosest();

			if (player == null || player.posY - mc.player.posY < 0.25 || Math.abs(mc.player.posX - player.posX) > 2 || Math.abs(mc.player.posZ - player.posZ) > 2) {
				return;
			}
		}
		
		//Place web
		BlockUtil.placeBlockOnThisThread(Blocks.WEB, getPlayerPos(), true);
		RotationUtil.stopRotating();
		
		//Toggle off if toggle is true
		if (toggle.booleanValue()) {
			toggleModule();
		}
	}
}
