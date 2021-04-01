package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import java.util.ConcurrentModificationException;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.mods.other.AutoMend;
import me.bebeli555.autobot.utils.BaritoneUtil;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.CrystalUtil;
import me.bebeli555.autobot.utils.EatingUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.InventoryUtil.ItemStackUtil;
import me.bebeli555.autobot.utils.MiningUtil;
import me.bebeli555.autobot.utils.PlayerUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrystalPvPBot extends AutoBot {
	public static AutoCrystal autoCrystal = new AutoCrystal(null);
	public static Thread thread;
	public static int lastDamageSeconds, triedTraps;
	public static long lastDamageMs;
	public static BlockPos hole;
	
	public static Setting targetName = new Setting(Mode.TEXT, "Target", "", "Name of the target you want the bot to attack");
	public static Setting lockTarget = new Setting(Mode.BOOLEAN, "Lock Target", false, "Locks the target so the target will be the given name above", "If off then target will be the closest entity");
	public static Setting mode = new Setting(null, "Mode", "Defensive", new String[]{"Defensive", "Doesnt go after target and just tries to defend itself"}, new String[]{"Aggressive", "Plays Aggressively. Goes after target and tries to kill it."});
	public static Setting disableKnockback = new Setting(Mode.BOOLEAN, "Disable Knockback", true, "Disables knockback.", "Only disable this if some servers anticheat kicks u for it or something.");
	public static Setting autoCrystalSetting = new Setting(Mode.LABEL, "AutoCrystal", true, "AutoCrystal for the bot");
		public static Setting autoCrystalMinTargetDmg = new Setting(autoCrystalSetting, Mode.INTEGER, "MinTargetDmg", 5, "Minimum damage to do to target to active auto crystal");
		public static Setting autoCrystalRange = new Setting(autoCrystalSetting, Mode.INTEGER, "Range", 4, "The range how far it can place and break crystals");
		public static Setting autoCrystalDelay = new Setting(autoCrystalSetting, Mode.INTEGER, "Delay", 50, "Delay in ms it will wait after a place/break");
	public static Setting autoTrap = new Setting(Mode.LABEL, "AutoTrap", true, "AutoTrap for the bot");
		public static Setting autoTrapDelay = new Setting(autoTrap, Mode.INTEGER, "Delay", 50, "Delay in ms it will wait after a successful place");
		public static Setting autoTrapDistance = new Setting(autoTrap, Mode.INTEGER, "Distance", 4, "Allowed place distance");
	public static Setting surround = new Setting(Mode.LABEL, "Surround", true, "Surround for the bot");
		public static Setting surroundDelay = new Setting(surround, Mode.INTEGER, "Delay", 50, "Delay in ms it will wait after a successful place");
	public static Setting autoTotem = new Setting(Mode.LABEL, "AutoTotem", true, "AutoTotem for the bot");
		public static Setting autoTotemDontMove = new Setting(autoTotem, Mode.BOOLEAN, "Dont move", true, "If true then the bot will not move", "Until it has put a new totem to offhand", "Because some servers like 2b2t will not allow inventory clicks if moving");
		
	public CrystalPvPBot() {
		super(Group.BOTS, "CrystalPvPBot", "A bot that crystalpvps for you", "(Doesnt work very well yet!)");
	}
	
	@Override
	public void onEnabled() {
		//Create a new thread to call the loop so sleeping wont freeze the game and it wont go out of sync
		thread = new Thread() {
			public void run() {
				while(thread != null && thread.equals(this)) {
					try {
						loop();
					} catch (ConcurrentModificationException e) {
						
					}
					
					AutoBot.sleep(50);
				}
			}
		};
		thread.start();
		
		MinecraftForge.EVENT_BUS.register(this);
		DisableKnockback.toggle(disableKnockback.booleanValue());
		AutoTotem.toggle(true);
		autoCrystal.toggle(true);
	}
	
	@Override
	public void onDisabled() {
		clearStatus();
		BaritoneUtil.forceCancel();
		MinecraftForge.EVENT_BUS.unregister(this);
		autoCrystal.toggle(false);
		DisableKnockback.toggle(false);
		AutoTotem.toggle(false);
		AutoCrystal.dontToggle = false;
		MiningUtil.isMining = false;
		suspend(thread);
		thread = null;
	}
	
	//This is the bot.
	public void loop() {
		if (mc.player == null) {
			return;
		}
		
		//Set target
		EntityPlayer target;
		if (lockTarget.booleanValue()) {
			target = PlayerUtil.getPlayer(targetName.stringValue());
		} else {
			target = PlayerUtil.getClosest();
		}

		//Repair armor
		repairArmor(target);
		
		if (target == null) {
			setStatus("No target found");
			return;
		} 
		
		if (target.isDead) {
			return;
		}
		
		targetName.value = target.getName();
		targetName.updateGuiNode();
		autoCrystal.target = target;
		
		//Update the seconds how long ago we last took damage
		if (System.currentTimeMillis() / 1000 != lastDamageMs) {
			lastDamageMs = System.currentTimeMillis() / 1000;
			lastDamageSeconds++;
		}
		
		//Calculate the possible damage target and self can take. And other variables
		double selfCrystalDamage = CrystalUtil.calculateDamage(AutoCrystal.getMostDamageSpot(mc.player), mc.player);
		double targetCrystalDamage = CrystalUtil.calculateDamage(AutoCrystal.getMostDamageSpot(target), target);
		double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
		double targetHealth = target.getHealth() + target.getAbsorptionAmount();
		double targetDistance = target.getDistanceSq(mc.player);
		
		BlockPos targetPos = new BlockPos(target.posX, target.posY, target.posZ);
		
		//Save current hole we are in to a variable
		if (Surround.isSurrounded(getPlayerPos())) {
			hole = getPlayerPos();
		}
		
		//Eat gapples
		if (health < 15 && selfCrystalDamage < 6 && targetCrystalDamage < 8 && targetHealth > 8 || health <= 20 && targetDistance > 6 || health < 13 && Surround.isSurrounded(getPlayerPos())) {
			if (InventoryUtil.hasItem(Items.GOLDEN_APPLE)) {
				if (!isPotionActive("regeneration", mc.player) || health < 10) {
					if (!EatingUtil.isEating() && !BaritoneUtil.isPathing()) {
						setStatus("Eating a gapple");
						EatingUtil.eatItem(Items.GOLDEN_APPLE, true);
					}
				}
			}
		}
		
		//If target is in same block as us then attack him with sword
		if (PlayerUtil.isInSameBlock(mc.player, target, 1)) {
			attackEntity(target);
		}
		
		//Check if were trapped and if so then do something to escape or prevent getting comboed
		if (AutoTrap.isTrapped(mc.player, true)) {
			if (!PlayerUtil.isInSameBlock(mc.player, target, 1)) {				
				if (InventoryUtil.hasItem(Items.CHORUS_FRUIT) && random(1)) {
					//Eat chorus to escape trap
					setStatus("Eating chorus to escape trap");
					EatingUtil.eatItem(Items.CHORUS_FRUIT, true);
				} else {
					if (MiningUtil.hasPickaxe()) {
						//Mine downwards if there is support below the mined block. If defensive mode then mine down anyway
						if (isSolid(getPlayerPos().add(0, -2, 0)) || mode.stringValue().equals("Defensive") || !InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
							if (getBlock(getPlayerPos().add(0, -1, 0)) != Blocks.BEDROCK) {
								setStatus("Mining down to escape trap");
								MiningUtil.mine(getPlayerPos().add(0, -1, 0), false);
							}
						} 
						
						//Mine up
						else {
							setStatus("Mining up to escape trap");
							if (MiningUtil.mine(getPlayerPos().add(0, 2, 0), false)) {
								Walker.jumpAndPlace(Blocks.OBSIDIAN);
							}
						}
					} 
					
					//No pickaxe.
					else {
						if (InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
							setStatus("Were trapped and you have no pickaxe to escape. Using surround");
						}
						
						if (InventoryUtil.hasItem(Items.CHORUS_FRUIT)) {
							return;
						}
						
						setStatus("Were trapped and you have no pickaxe or chorus fruit. Cant do much");
					}
				}
			}
		}
		
		//If target is far away then go sit on a hole or chase the target if not defensive mode
		if (target.getDistance(mc.player) >= 10) {
			if (mode.stringValue().equals("Defensive")) {
				if (!Surround.isSurrounded(getPlayerPos())) {
					if (!Walker.walkToNearestHole(5)) {
						Surround.toggle();
						
						sleep(50);
						if (!Surround.isSurrounded(getPlayerPos())) {
							if (getBlock(getPlayerPos().add(0, -1, 0)) != Blocks.BEDROCK && MiningUtil.hasPickaxe()) {
								setStatus("Mining down to seek better cover");
								MiningUtil.mine(getPlayerPos().add(0, -1, 0), false);
							}
						}
					} else {
						setStatus("Walking to nearest hole");
					}
				}
			} 
			
			else {
				BaritoneUtil.walkTo(target.getPosition(), false);
				sleep(2000);
			}
		}
		
		//If self damage is big then do something to prevent getting killed.
		if (selfCrystalDamage >= 10 && lastDamageSeconds <= 2 || selfCrystalDamage >= 10 && targetDistance < 4) {
			if (targetCrystalDamage < selfCrystalDamage) {
				if (random(3)) {
					Surround.toggle();
					sleep(250);
				}
				
				if (!Surround.isSurrounded(getPlayerPos())) {
					//Target is surrounded
					if (Surround.isSurrounded(targetPos)) {
						//Walk to targets position
						if (BaritoneUtil.canPath(targetPos) && random(1)) {
							setStatus("Walking to targets position");
							BaritoneUtil.walkTo(targetPos, false);
							sleep(2000);
						}
						
						//Flee
						if (random(2)) {
							Walker.backOff(target);
						}
						
						//Walk to hole
						else if (Walker.walkToNearestHole(6)) {
							
						}
					}
					
					//Target isnt surrounded
					else {
						if (targetCrystalDamage >= 8) {			
							if (mc.player.posY <= target.posY) {
								//Walk to target
								if (BaritoneUtil.canPath(targetPos)) {
									setStatus("Walking to targets location");
									BaritoneUtil.walkTo(targetPos, false);
									sleep(2000);
								}
							}
						}
						
						
						else {
							//Walk to nearby hole
							if (Walker.walkToNearestHole(5)) {

							}
							
							//Auto trap
							else if (random(1) && target.getDistance(mc.player) <= autoTrapDistance.intValue()) {
								AutoTrap.toggle(target, autoTrapDelay.intValue());
							}
							
							//Flee
							else if (!Surround.isSurrounded(getPlayerPos())) {
								Walker.backOff(target);
							}
						}
					}
				}
			}
		}
		
		//Trap target or attack him with sword
		if (Surround.isSurrounded(getPlayerPos()) && Surround.isSurrounded(targetPos) && !PlayerUtil.isInSameBlock(mc.player, target, 1) && target.getDistance(mc.player) <= autoTrapDistance.intValue()) {
			if (triedTraps <= 5) {
				AutoTrap.toggle(target, autoTrapDelay.intValue());
			} else {
				attackEntity(target);
			}
			triedTraps++;
		} else {
			triedTraps = 0;
		}
		
		//Mine targets feet
		if (Surround.isSurrounded(getPlayerPos()) && AutoTrap.isTrapped(target, false) && !PlayerUtil.isInSameBlock(mc.player, target, 1)) {
			if (target.getDistance(mc.player) <= autoTrapDistance.intValue()) {
				//Check if some of the feet spots can be mined
				BlockPos[] feet = {targetPos.add(1, 0, 0), targetPos.add(-1, 0, 0), targetPos.add(0, 0, 1), targetPos.add(0, 0, -1)};
				
				for (BlockPos pos : feet) {
					if (MiningUtil.canMine(pos)) {
						setStatus("Mining targets feet");
						MiningUtil.mine(pos, false);
						break;
					}
				}
			}
		}
		
		//Place a block on the side of targets feet so we can place crystals on it
		if (target.posY - mc.player.posY >= 1 && InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
			BlockPos[] feet = {targetPos.add(1, -1, 0), targetPos.add(-1, -1, 0), targetPos.add(0, -1, 1), targetPos.add(0, -1, -1)};
			
			BlockPos best = null;
			for (BlockPos pos : feet) {
				if (getBlock(pos) == Blocks.AIR) {
					if (best == null || mc.player.getDistanceSq(pos) < mc.player.getDistanceSq(best)) {
						best = pos;
					}
				} else {
					best = null;
					break;
				}
			}
			
			if (best != null && target.posY - mc.player.posY < 3) {
				if (BlockUtil.distance(getPlayerPos(), best) <= 5) {
					setStatus("Placing a block below targets feet");
					BlockUtil.placeBlock(Blocks.OBSIDIAN, best, false);
				}
			} 
			
			//If target is bridged up then bridge up aswell if aggressive mode
			else if (target.posY - mc.player.posY >= 3 && mode.stringValue().equals("Aggressive")) {
				setStatus("Bridging up to target");
				Walker.jumpAndPlace(Blocks.OBSIDIAN);
			}
		}
		
		//If we are in the same spot as the saved hole but no longer surrounded then do something
		if (hole != null && hole.equals(getPlayerPos()) && !Surround.isSurrounded(hole)) {
			//Walk to target if agrressive mode
			if (mode.stringValue().equals("Aggressive") && random(1) && BaritoneUtil.canPath(targetPos)) {
				setStatus("Walking to targets position");
				BaritoneUtil.walkTo(targetPos, true);
			}
			
			//Bridge up
			else if (random(15) && !isSolid(getPlayerPos().add(0, 2, 0)) && InventoryUtil.hasBlock(Blocks.OBSIDIAN)) {
				Walker.jumpAndPlace(Blocks.OBSIDIAN);
			}
			
			//Eat chorus
			else if (random(1) && InventoryUtil.hasItem(Items.CHORUS_FRUIT)) {
				EatingUtil.eatItem(Items.CHORUS_FRUIT, true);
			}
			
			//Mine down
			else if (MiningUtil.canMine(getPlayerPos().add(0, -1, 0)) && MiningUtil.hasPickaxe()) {
				MiningUtil.mine(getPlayerPos().add(0, -1, 0), false);
			}
			
			//Backoff
			else {
				Walker.backOff(target);
			}
		}
	}
	
	/**
	 * Checks and repairs armor with XP-Bottles if nesseccary
	 */
	public void repairArmor(EntityPlayer target) {
		if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > 15) {
			if (target == null || target.getDistanceSq(mc.player) > 10 || Surround.isSurrounded(getPlayerPos()) && !PlayerUtil.isInSameBlock(mc.player, target, 2)) {
				ItemStackUtil[] armor = new ItemStackUtil[]{new ItemStackUtil(InventoryUtil.getItemStack(39), 39), new ItemStackUtil(InventoryUtil.getItemStack(38), 38), 
										new ItemStackUtil(InventoryUtil.getItemStack(37), 37), new ItemStackUtil(InventoryUtil.getItemStack(36), 36)};
				
				int lowDur = 200;
				for (ItemStackUtil itemStack : armor) {
					if (itemStack.itemStack.getItem() != Items.AIR && AutoMend.getDurability(itemStack.itemStack) <= lowDur && InventoryUtil.hasItem(Items.EXPERIENCE_BOTTLE)) {
						setStatus("Mending armor with xp bottles");
						
						for (int i = 0; i < 15; i++) {
							if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > 13) {
								RotationUtil.rotate(new Vec3d(getPlayerPos().add(0, -1, 0)).add(0.5, 0.5, 0.5), false);
								
								if (mc.player.getHeldItemMainhand().getItem() != Items.EXPERIENCE_BOTTLE) {
									InventoryUtil.switchItem(InventoryUtil.getSlot(Items.EXPERIENCE_BOTTLE), true);
								}
								
								PlayerUtil.rightClick();
								sleep(100);	
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Attacks the given entity. Like killaura
	 */
	public void attackEntity(Entity entity) {
		setStatus("Attacking target with sword");
		if (InventoryUtil.hasItem(Items.DIAMOND_SWORD)) {
			InventoryUtil.switchItem(InventoryUtil.getSlot(Items.DIAMOND_SWORD), false);
		}
		
		sleep(600);
		
		RotationUtil.rotate(new Vec3d(new BlockPos(entity.posX, entity.posY, entity.posZ)).add(0.5, 1.5, 0.5), true);
		mc.playerController.attackEntity(mc.player, entity);
		mc.player.swingArm(EnumHand.MAIN_HAND);
	}
	
	@SubscribeEvent
	public void onDamage(LivingAttackEvent e) {
		if (e.getEntity().equals(mc.player)) {
			lastDamageSeconds = 0;
		}
	}
} 
