package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.mods.Mods;
import me.bebeli555.autobot.utils.BaritoneUtil;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class Surround extends Mods {
	private static long lastToggled;
	
	/**
	 * Centers the player then builds the surround thing and returns
	 */
	public static void toggle() {
		if (BaritoneUtil.isPathing() || !InventoryUtil.hasBlock(Blocks.OBSIDIAN) || isSurrounded(getPlayerPos())) {
			return;
		}
		
		//Only allow toggles every 0.5 seconds
		if (System.currentTimeMillis() / 500 == lastToggled) {
			return;
		}
		lastToggled = System.currentTimeMillis() / 500;
		
		setStatus("Using surround", "CrystalPvPBot");
		center();
		
		//Build
		for (BlockPos position : getBlocksToPlace()) {
			if (BlockUtil.placeBlock(Blocks.OBSIDIAN, position, false)) {
				sleep(CrystalPvPBot.surroundDelay.intValue());
			}
		}
	}
	
	/**
	 * Centers the player
	 */
	public static void center() {
		if (isCentered()) {
			return;
		}
		
		double[] centerPos = {Math.floor(mc.player.posX) + 0.5, Math.floor(mc.player.posY), Math.floor(mc.player.posZ) + 0.5};
		
		mc.player.motionX = (centerPos[0] - mc.player.posX) / 2;
		mc.player.motionZ = (centerPos[2] - mc.player.posZ) / 2;
		
		sleepUntil(() -> Math.abs(centerPos[0] - mc.player.posX) <= 0.1 && Math.abs(centerPos[2] - mc.player.posZ) <= 0.1, 1000);
		mc.player.motionX = 0;
		mc.player.motionZ = 0;
	}
	
	/**
	 * Checks if the player is centered on the block
	 */
	public static boolean isCentered() {
		double[] centerPos = {Math.floor(mc.player.posX) + 0.5, Math.floor(mc.player.posY), Math.floor(mc.player.posZ) + 0.5};
		return Math.abs(centerPos[0] - mc.player.posX) <= 0.1 && Math.abs(centerPos[2] - mc.player.posZ) <= 0.1;
	}
	
	/**
	 * Checks if the given BlockPos is surrounded with obby or bedrock
	 */
	public static boolean isSurrounded(BlockPos p) {
		BlockPos[] positions = {p.add(1, 0, 0), p.add(-1, 0, 0), p.add(0, 0, 1), p.add(0, 0, -1)};
		
 		for (BlockPos pos : positions) {
 			if (getBlock(pos) != Blocks.OBSIDIAN && getBlock(pos) != Blocks.BEDROCK) {
 				return false;
 			}
 		}
 		
 		return true;
	}
	
	/**
	 * Get the blockpositions where to place obby
	 */
	public static BlockPos[] getBlocksToPlace() {
		BlockPos p = getPlayerPos();
		return new BlockPos[]{p.add(1, -1, 0), p.add(-1, -1, 0), p.add(0, -1, 1), p.add(0, -1, -1), p.add(1, 0, 0), p.add(-1, 0, 0), p.add(0, 0, 1), p.add(0, 0, -1)};
	}
}
