package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import java.util.ArrayList;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.utils.BaritoneUtil;
import me.bebeli555.autobot.utils.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class Walker extends AutoBot {
	
	/**
	 * Walks to the nearest hole that is made out of bedrock or obby
	 * @radius The radius where to search holes around the player
	 */
	public static boolean walkToNearestHole(int radius) {
		BlockPos hole = getClosestHole(mc.player, radius, true, Blocks.BEDROCK, Blocks.OBSIDIAN);
		
		if (hole == null) {
			return false;
		} else {
			setStatus("Walking to a nearby hole", "CrystalPvPBot");
			BaritoneUtil.walkTo(hole, true);
			Surround.center();
			return true;
		}
	}
	
	/**
	 * Tries to walk away from the target a bit
	 */
	public static void backOff(EntityPlayer target) {
		setStatus("Backing off from target", "CrystalPvP Bot");
		BlockPos goal = new BlockPos((mc.player.posX - target.posX) + mc.player.posX, mc.player.posY, (mc.player.posZ - target.posZ) + mc.player.posZ);
		BaritoneUtil.walkTo(goal, true);
	}
	
	/**
	 * Get the closest hole to given player
	 * @canPath if true then will only give a hole where baritone can walk to
	 */
	public static BlockPos getClosestHole(EntityPlayer player, int radius, boolean canPath, Block... blocks) {
		double lowestDistance = Integer.MAX_VALUE;
		BlockPos closestHole = null;
		ArrayList<BlockPos> holes = new ArrayList<BlockPos>();
		
		//Check some positions to more know if their walkable or not this will make baritone have to check less paths
		outer: for (BlockPos pos : getHoles(radius, blocks)) {
			BlockPos[] cantBeSolid = {pos.add(0, 1, 0), pos.add(0, 2, 0)};
			
			for (BlockPos check : cantBeSolid) {
				if (isSolid(check)) {
					continue outer;
				}
			}
			
			holes.add(pos);
		}
		
		for (BlockPos pos : holes) {
			double distance = player.getDistance(pos.getX(), pos.getY(), pos.getZ());
			if (distance < lowestDistance) {
				if (canPath) {
					if (!BaritoneUtil.canPath(pos)) {
						continue;
					}
				}
				
				lowestDistance = distance;
				closestHole = pos;
			}
		}
		
		return closestHole;
	}
	
	/**
	 * Get all holes in the given radius.
	 * @return The center BlockPos of the hole
	 * @blocks The list of blocks the hole can be made of
	 */
	public static ArrayList<BlockPos> getHoles(int radius, Block... blocks) {
		ArrayList<BlockPos> holes = new ArrayList<BlockPos>();
		
		outer: for (BlockPos p : BlockUtil.getAll(radius)) {
			if (!isSolid(p)) {
				BlockPos[] mustBeSolid = {p.add(1, 0, 0), p.add(-1, 0, 0), p.add(0, 0, 1), p.add(0, 0, -1), p.add(0, -1, 0)};
				
				outer2: for (BlockPos pos : mustBeSolid) {
					for (Block block : blocks) {
						if (getBlock(pos).equals(block)) {
							continue outer2;
						}
					}
					
					continue outer;
				}
				
				holes.add(p);
			}
		}
		
		return holes;
	}
	
	/**
	 * Jumps and places the given block below
	 */
	public static void jumpAndPlace(Block block) {
		if (BaritoneUtil.isPathing()) {
			return;
		}
		
		boolean oldValue = AutoCrystal.dontToggle;
		AutoCrystal.dontToggle = true;
		sleep(150);
		mc.player.jump();
		sleep(300);
		BlockUtil.placeBlock(block, getPlayerPos().add(0, -1, 0), false);
		sleepUntil(() -> mc.player.onGround, 500);
		AutoCrystal.dontToggle = oldValue;
	}
}
