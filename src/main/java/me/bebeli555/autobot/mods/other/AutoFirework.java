package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.mods.Mods;
import me.bebeli555.autobot.mods.RegisterMod;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.MessageUtil;
import me.bebeli555.autobot.utils.PlayerUtil;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

@RegisterMod(displayName = "AutoFirework", description = "Clicks on fireworks for you when flying with elytra", group = Group.OTHER)
public class AutoFirework extends Mods {
	private static Thread thread, thread2;
	private static boolean lagback;
	private static int lagbackCounter;
	
	public static Setting delay = new Setting(Mode.DOUBLE, "Delay", 2.8, "The delay between clicks on the firework", "In seconds");
	public static Setting antiLagback = new Setting(Mode.BOOLEAN, "AntiLagback", true, "Doesnt click on a firework if ur lagbacking on 2b2t");
	
	@Override
	public void onEnabled() {
		thread = new Thread() {
			public void run() {
				while (thread != null && thread.equals(this)) {
					loop();

					Mods.sleep(50);
				}
			}
		};
		thread.start();
		
		thread2 = new Thread() {
			public void run() {
				while (thread2 != null && thread2.equals(this)) {
					double speed = PlayerUtil.getSpeed(mc.player);
					if (speed > 4) {
						lagback = true;
					}
					
					if (lagback) {
						if (speed < 1) {
							lagbackCounter++;
							if (lagbackCounter > 4) {
								lagback = false;
								lagbackCounter = 0;
							}
						} else {
							lagbackCounter = 0;
						}
					}
					
					Mods.sleep(50);
				}
			}
		};
		thread2.start();
	}
	
	@Override
	public void onDisabled() {
		thread = null;
		thread2 = null;
	}
	
	public void loop() {
		if (mc.player == null || !mc.player.isElytraFlying()) {
			return;
		}
		
		if (!InventoryUtil.hasItem(Items.FIREWORKS)) {
			MessageUtil.sendWarningMessage("You have no fireworks in inventory");
			toggleModule();
			return;
		}
		
		//Put the best firework to hand
		if (mc.player.getHeldItemMainhand().getItem() != Items.FIREWORKS) {
			InventoryUtil.switchItem(InventoryUtil.getSlot(Items.FIREWORKS), false);
		}
		
		//Click
		if (!lagback) {
			mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
			sleepUntil(() -> !mc.player.isElytraFlying(), (int)(delay.doubleValue() * 1000));
		}
	}
}
