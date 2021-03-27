package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.utils.BaritoneUtil;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class AutoTrap extends AutoBot {
	private static long lastToggled;
	
	/**
	 * Traps the targeted player with obsidian then returns.
	 * @delay the ms it will sleep after a successfull place
	 */
	public static void toggle(EntityPlayer target, int delay) {
		if (BaritoneUtil.isPathing() || !InventoryUtil.hasBlock(Blocks.OBSIDIAN) || isTrapped(target, true)) {
			return;
		}
		
		//Only allow toggles every 0.5 seconds
		if (System.currentTimeMillis() / 500 == lastToggled) {
			return;
		}
		lastToggled = System.currentTimeMillis() / 500;
		
		setStatus("Trapping target with AutoTrap", "CrystalPvP Bot");
		
		for (BlockPos position : getBlocksToPlace(target)) {
			if (BlockUtil.placeBlock(Blocks.OBSIDIAN, position, false)) {
				sleep(delay);
			}
		}
	}
	
	/**
	 * Checks if the target is trapped fully, like has obby all around
	 */
	public static boolean isTrappedFully(EntityPlayer target) {
		for (BlockPos position : getBlocksToPlace(target)) {
			if (!isSolid(position)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if there is no escape from targets current position without mining blocks
	 * @blocks the blocks the trap must be made of
	 * @roof if true then trap must have a roof on it
	 */
	public static boolean isTrapped(EntityPlayer target, boolean roof) {
		BlockPos p = new BlockPos(target.posX, target.posY, target.posZ);
		
		//Roof
		if (roof && !isSolid(p.add(0, 2, 0))) {
			return false;
		}
		
		//+X
		if (!isSolid(p.add(1, 0, 0)) && !isSolid(p.add(1, 1, 0))) {
			return false;
		}
		
		//-X
		if (!isSolid(p.add(-1, 0, 0)) && !isSolid(p.add(-1, 1, 0))) {
			return false;
		}
		
		//+Z
		if (!isSolid(p.add(0, 0, 1)) && !isSolid(p.add(0, 1, 1))) {
			return false;
		}
		
		//-Z
		if (!isSolid(p.add(0, 0, -1)) && !isSolid(p.add(0, 1, -1))) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the blocks to make the AutoTrap place
	 * @p the target entitys blockpos
	 */
	public static BlockPos[] getBlocksToPlace(EntityPlayer target) {
		BlockPos p = new BlockPos(target.posX, target.posY, target.posZ);
		return new BlockPos[]{p.add(1, 0, 0), p.add(-1, 0, 0), p.add(0, 0, 1), p.add(0, 0, -1),
							  p.add(1, 1, 0), p.add(-1, 1, 0), p.add(0, 1, 1), p.add(0, 1, -1),
							  p.add(1, 2, 0), p.add(0, 2, 0)};
	}
}
