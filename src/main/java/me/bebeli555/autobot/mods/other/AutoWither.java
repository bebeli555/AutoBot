package me.bebeli555.autobot.mods.other;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.mods.bots.crystalpvpbot.Surround;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;

/**
 * Not in use currently as it doesnt work well on servers with ray tracing and you can just use the AutoBuilder to build withers.
 */
public class AutoWither extends AutoBot {
	public static Setting radius = new Setting(Mode.INTEGER, "Radius", 3, "Radius around the player to search for available spot", "Like the place distance");
	public static Setting delay = new Setting(Mode.INTEGER, "Delay", 80, "Delay in ms to wait after a successfull place");
	public static Setting nametag = new Setting(Mode.BOOLEAN, "Nametag", false, "Puts a nametag to the wither after it spawns");
	
	public AutoWither() {
		super(Group.OTHER, "AutoWither", "Builds a wither to an available spot nearby", "With the best shape and position possible!");
	}
	
	@Override
	public void onEnabled() {
		new Thread() {
			public void run() {
				if (!InventoryUtil.hasBlock(Blocks.SOUL_SAND) || !InventoryUtil.hasItem(Items.SKULL)) {
					toggleModule();
					sendMessage("You dont have the required materials to build a wither", true);
					return;
				}
				
				BlockPos[][] shapes = new BlockPos[][]{
					new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 1, 0), new BlockPos(1, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(0, 2, 0), new BlockPos(1, 2, 0), new BlockPos(-1, 2, 0)},
					new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 1, -1), new BlockPos(0, 2, 0), new BlockPos(0, 2, 1), new BlockPos(0, 2, -1)},
					new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1), new BlockPos(-1, 0, -1), new BlockPos(0, 0, -2), new BlockPos(1, 0, -2), new BlockPos(-1, 0, -2)},
					new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(0, 0, 2), new BlockPos(1, 0, 2), new BlockPos(-1, 0, 2)},
					new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(-1, 0, 1), new BlockPos(-1, 0, -1), new BlockPos(-2, 0, 0), new BlockPos(-2, 0, 1), new BlockPos(-2, 0, -1)},
					new BlockPos[]{new BlockPos(0, 0, 0), new BlockPos(1, 0, 0), new BlockPos(1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(2, 0, 0), new BlockPos(2, 0, 1), new BlockPos(2, 0, -1)}
				};
				
				BlockPos bestPos = null;
				BlockPos[] bestShape = null;
				double lowestPosDistance = Integer.MAX_VALUE;
				double lowestShapeDistance = Integer.MAX_VALUE;
				
				//Get the best spot
				outer: for (BlockPos pos : BlockUtil.getAll(radius.intValue())) {
					//Dont use this pos if its the same as the player or y is lower than 0
					if (pos.equals(getPlayerPos()) || pos.getY() <= 0) {
						continue outer;
					}
					
					if (getPlayerPos().distanceSq(pos.getX(), pos.getY(), pos.getZ()) >= lowestPosDistance) {
						continue outer;
					}
					
					//Check if some shape is compatible with the pos
					shape: for (BlockPos[] shape : shapes) {
						if (!isSolid(pos.add(shape[0].getX(), shape[0].getY() - 1, shape[0].getZ()))) {
							continue shape;
						}
						
						for (BlockPos check : shape) {							
							if (getBlock(pos.add(check.getX(), check.getY(), check.getZ())) != Blocks.AIR) {
								continue shape;
							}
							
							if (getPlayerPos().getX() == pos.add(check.getX(), check.getY(), check.getZ()).getX() && getPlayerPos().getZ() == pos.add(check.getX(), check.getY(), check.getZ()).getZ()) {
								continue shape;
							}
						}
						
						lowestPosDistance = mc.player.getDistanceSq(pos);
						bestPos = pos;
						continue outer;
					}
				}
				
				//Get the best shape for the best pos
				if (bestPos != null) {
					shape: for (BlockPos[] shape : shapes) {
						//Check if the block below the first one is solid so it can be built
						if (!isSolid(bestPos.add(shape[0].getX(), shape[0].getY() - 1, shape[0].getZ()))) {
							continue shape;
						}
						
						//check if the pos is all air for the shape
						for (BlockPos check : shape) {
							if (getBlock(bestPos.add(check.getX(), check.getY(), check.getZ())) != Blocks.AIR) {
								continue shape;
							}
							
							//If one of the positions is the player pos then continue as the shape cant be built
							if (getPlayerPos().getX() == bestPos.add(check.getX(), check.getY(), check.getZ()).getX() && getPlayerPos().getZ() == bestPos.add(check.getX(), check.getY(), check.getZ()).getZ()) {
								continue shape;
							}
						}
						
						//Check the highest distance from this shape and set bestPos
						double highestDistance = Integer.MIN_VALUE;
						for (BlockPos check : shape) {
							BlockPos player = getPlayerPos();
							BlockPos pos2 = bestPos.add(check.getX(), 0, check.getZ());
							double distance = player.distanceSq(pos2);
							if (distance > highestDistance) {
								highestDistance = distance;
							}
						}
						
						if (highestDistance < lowestShapeDistance) {
							lowestShapeDistance = highestDistance;
							bestShape = shape;
						}
					}
				}
				
				//Then build it if it aint null
				if (bestShape != null && bestPos != null) {
					Surround.center();
					
					for (int i = 0; i < bestShape.length; i++) {
						BlockPos build = bestPos.add(bestShape[i].getX(), bestShape[i].getY(), bestShape[i].getZ());

						if (i > 3) {
							BlockUtil.placeItem(Items.SKULL, build, true);
						} else {
							BlockUtil.placeBlock(Blocks.SOUL_SAND, build, true);
						}
						
						AutoBot.sleep(delay.intValue());
					}
				} else {
					toggleModule();
					sendMessage("Couldnt find a suitable spot to build wither in the set radius", true);
					return;
				}
				
				toggleModule();
			}
		}.start();
	}
}
