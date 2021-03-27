package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PlayerMoveEvent;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoTotem extends AutoBot {
	private static AutoTotem autoTotem = new AutoTotem();
	
	/**
	 * Toggle off or on. It will stay on until toggled off
	 */
	public static void toggle(boolean on) {
		if (on) {
			MinecraftForge.EVENT_BUS.register(autoTotem);
		} else {
			MinecraftForge.EVENT_BUS.unregister(autoTotem);
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		if (mc.player == null) {
			return;
		}
		
		if (mc.player.getHeldItemOffhand().getItem() != Items.TOTEM_OF_UNDYING && InventoryUtil.hasItem(Items.TOTEM_OF_UNDYING)) {
			if (CrystalPvPBot.autoTotemDontMove.booleanValue()) {
				NoMovement.toggle(true);
			}
			
			InventoryUtil.clickSlot(InventoryUtil.getItem(Items.TOTEM_OF_UNDYING));
			InventoryUtil.clickSlot(45);
		} else {
			NoMovement.toggle(false);
		}
	}
	
	/**
	 * Cancels all movement
	 */
	public static class NoMovement {
		private static NoMovement noMovement = new NoMovement();

		/**
		 * When toggled on it will not allow movement until toggled off.
		 */
		public static void toggle(boolean on) {
			if (on) {
				AutoBot.EVENT_BUS.subscribe(noMovement);
			} else {
				AutoBot.EVENT_BUS.unsubscribe(noMovement);
			}
		}
		
		@EventHandler
		private Listener<PlayerMoveEvent> moveEvent = new Listener<>(event -> {
			event.cancel();
		});
	}
}
