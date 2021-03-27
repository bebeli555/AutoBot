package me.bebeli555.autobot.utils;

import java.util.ArrayList;
import java.util.function.Consumer;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import me.bebeli555.autobot.AutoBot;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

public class BaritoneUtil extends AutoBot {
	private static Timer warningTimer = new Timer();
	private static Consumer<ITextComponent> oldValue;
	
	/**
	 * Send a baritone command without the baritone chat message
	 */
	public static void sendCommand(String command) {
		if (isBaritoneInstalled()) {
			if (oldValue == null) oldValue = BaritoneAPI.getSettings().logger.value;
			BaritoneAPI.getSettings().logger.value = (component) -> {};
			BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute(command);
			
			new Thread() {
				public void run() {
					AutoBot.sleep(50);
					BaritoneAPI.getSettings().logger.value = oldValue;
				}
			}.start();
		}
	}
	
	/**
	 * Make baritone walk to given goal
	 * @sleepUntilDone if true, sleeps until baritone has walked to the goal
	 */
	public static void walkTo(BlockPos goal, boolean sleepUntilDone) {
		if (!isBaritoneInstalled()) {
			return;
		}
		
		//Mine web inside us as it will prevent us from moving
		if (getBlock(getPlayerPos()) == Blocks.WEB) {
			if (InventoryUtil.hasItem(Items.DIAMOND_SWORD)) {
				InventoryUtil.switchItem(InventoryUtil.getItem(Items.DIAMOND_SWORD), true);
				MiningUtil.mineWithoutSwitch(getPlayerPos());
			}
		}
		
		sendCommand("goto " + goal.getX() + " " + goal.getY() + " " + goal.getZ());
		
		if (sleepUntilDone) {
			sleepUntil(() -> BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing(), 100);
			sleepUntil(() -> !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing(), -1);
		}
	}
	
	/**
	 * Checks if there is a path to the given goal
	 * Pretty hacky solution but eh
	 */
	public static boolean canPath(BlockPos goal) {
		if (!isBaritoneInstalled()) {
			return false;
		}
		
		walkTo(goal, false);
		boolean value = false;
		for (int i = 0; i < 35; i++) {
			if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
				value = true;
				break;
			}
			
			sleep(1);
		}
		
		BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().forceCancel();
		return value;
	}
	
	/**
	 * Cancel everything baritone is doing
	 */
	public static void forceCancel() {
		if (isBaritoneInstalled()) {
			BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().forceCancel();
		}
	}
	
	/**
	 * Checks if baritone is pathing
	 */
	public static boolean isPathing() {
		if (isBaritoneInstalled()) {
			return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();	
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if baritone is building something
	 */
	public static boolean isBuilding() {
		if (isBaritoneInstalled()) {
			return BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isActive();
		} else {
			return false;
		}
	}
	
	/**
	 * Sets a boolean setting to the given value
	 */
	public static void setSetting(String name, boolean value) {
		sendCommand("setting " + name + " " + value);
	}
	
	public static boolean isBaritoneInstalled() {
		try {
			BaritoneAPI.getProvider();
			return true;
		} catch (NoClassDefFoundError e) {
			if (warningTimer.hasPassed(2000)) {
				warningTimer.reset();
				new AutoBot().sendMessage("ERROR: You dont have baritone installed and a module is trying to use it", true);
			}
			
			return false;
		}
	}
	
	public static class BaritoneSettings {
		public ArrayList<String> names = new ArrayList<String>();
		public ArrayList<Boolean> values = new ArrayList<Boolean>(); 
		
		/**
		 * Saves the current settings to the object
		 * Didnt find any better way to do this so it just saves the settings used by the mod
		 */
		public void saveCurrentSettings() {
			if (isBaritoneInstalled()) {
				ArrayList<Settings.Setting<Boolean>> settings = new ArrayList<Settings.Setting<Boolean>>();
				
				settings.add(BaritoneAPI.getSettings().allowInventory);
				settings.add(BaritoneAPI.getSettings().allowSprint);
				settings.add(BaritoneAPI.getSettings().allowBreak);
				settings.add(BaritoneAPI.getSettings().allowSprint);
				settings.add(BaritoneAPI.getSettings().allowPlace);
				
				for (Settings.Setting<Boolean> setting : settings) {
					names.add(setting.getName());
					values.add(setting.value);
				}
			}
		}
		
		/**
		 * Loads and sets all the settings to the previously saved ones in settings object
		 */
		public void loadSettings() {
			for (int i = 0; i < names.size(); i++) {
				setSetting(names.get(i), values.get(i));
			}
		}
	}
}
