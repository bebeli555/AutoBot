package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.mods.Mods;
import me.bebeli555.autobot.mods.RegisterMod;
import me.bebeli555.autobot.utils.BaritoneUtil;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.CrystalUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.MiningUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@RegisterMod(displayName = "AutoCrystal", description = "A module for the crystal pvp bot", group = Group.OTHER)
public class AutoCrystal extends Mods {
	public EntityPlayer target;
	public Thread thread;
	public static BlockPos placed;
	public static boolean dontToggle;
	
	public AutoCrystal(EntityPlayer target) {
		this.target = target;
	}
	
	@SuppressWarnings("deprecation")
	public void toggle(boolean on) {
		if (on) {
			if (thread == null) {
				thread = new Thread(() -> {
					while(true) {
						try {
							loop();

							AutoBot.sleep(35);
						} catch (Exception ignored) {

						}
					}
				});
				
				thread.start();
			} else {
				thread.resume();
			}
		} else {
			thread.suspend();
		}
	}

	public void loop() {
		if (mc.player == null) {
			return;
		}
		
		if (shouldToggleAutoCrystal()) {
			setStatus("Using Auto Crystal to damage target", "CrystalPvPBot");
			placed = null;
			
			//Break crystals
			EntityEnderCrystal breakCrystal = getBestCrystal();
			if (breakCrystal != null) {
				breakCrystal(breakCrystal);
				AutoBot.sleep(CrystalPvPBot.autoCrystalDelay.intValue());
			} 
			
			//Place crystals.
			else {
				BlockPos placeCrystal = getBestCrystalSpot(true);
				if (placeCrystal != null) {
					BlockUtil.placeItem(Items.END_CRYSTAL, placeCrystal, false);
					placed = placeCrystal;
					AutoBot.sleep(CrystalPvPBot.autoCrystalDelay.intValue());
				}	
			}
		}
	}
	
	//Checks if auto crystal should be toggled on
	public boolean shouldToggleAutoCrystal() {
		if (target == null || target.isDead || dontToggle || BaritoneUtil.isPathing() || MiningUtil.isMining) {
			return false;
		}
		
		if (placed != null) {
			return true;
		}
		
		BlockPos bestPos = getBestCrystalSpot(true);
		double selfCrystalDamage = CrystalUtil.calculateDamage(bestPos, mc.player);
		double targetCrystalDamage = CrystalUtil.calculateDamage(bestPos, target);
		double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
		double targetHealth = target.getHealth() + target.getAbsorptionAmount();
		
		int minTargetDmg = CrystalPvPBot.autoCrystalMinTargetDmg.intValue();
		if (targetCrystalDamage > minTargetDmg  && selfCrystalDamage <= targetCrystalDamage || targetHealth < targetCrystalDamage) {
			if (health > selfCrystalDamage || targetCrystalDamage > targetHealth && InventoryUtil.getAmountOfItem(Items.TOTEM_OF_UNDYING) > 3 && targetHealth != 0) {
				return true;
			}
		}
		
		return false;
	}
	
	//Break the crystal
	public void breakCrystal(EntityEnderCrystal crystal) {
		RotationUtil.rotate(new Vec3d(crystal.posX + 0.5, crystal.posY + 0.5, crystal.posZ + 0.5), true);
		
		mc.playerController.attackEntity(mc.player, crystal);
		mc.player.swingArm(EnumHand.MAIN_HAND);
	}
	
	//Calculates the best crystal spot to place on
	//It calculates it by calculating the enemy damage - self damage / 2 and the spot with highest dmg is returned
	public BlockPos getBestCrystalSpot(boolean calculateSelfDamage) {
		double mostDamage = Integer.MIN_VALUE;
		BlockPos best = null;
		
		for (BlockPos pos : BlockUtil.getAll(CrystalPvPBot.autoCrystalRange.intValue() - 1)) {			
        	if (CrystalUtil.canPlaceCrystal(pos)) {
            	double damage = CrystalUtil.calculateDamage(pos, target);
            	if (calculateSelfDamage) {
            		damage -= CrystalUtil.calculateDamage(pos, mc.player) / 2;
            	}
            	
            	if (damage > mostDamage) {
            		mostDamage = damage;
            		best = pos;
            	}
        	}
		}
		
        return best;
	}
	
	//Gets the best crystal around you to break and to cause dmg to target
	//Same calc as getBestCrystalSpot
	public EntityEnderCrystal getBestCrystal() {
		double mostDamage = Integer.MIN_VALUE;
		EntityEnderCrystal best = null;
		
		for (EntityEnderCrystal crystal : CrystalUtil.getCrystals(CrystalPvPBot.autoCrystalRange.intValue())) {
        	double damage = CrystalUtil.calculateDamage(crystal.getPositionVector(), target);
        	damage -= CrystalUtil.calculateDamage(crystal.getPositionVector(), mc.player) / 2;
        	
        	if (damage > mostDamage) {
        		mostDamage = damage;
        		best = crystal;
        	}
		}
		
		return best;
	}
	
	/**
	 * Gets the spot where placing a crystal will deal the most damage to given target
	 */
	public static BlockPos getMostDamageSpot(EntityPlayer target) {
		return new AutoCrystal(target).getBestCrystalSpot(false);
	}
}
