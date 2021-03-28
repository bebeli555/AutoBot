package me.bebeli555.autobot.utils;

import java.util.ArrayList;

import me.bebeli555.autobot.AutoBot;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

public class CrystalUtil extends AutoBot{
	
	/**
	 * Gets all the crystals around you if the distance is lower or equal
	 *
	 */
	public static ArrayList<EntityEnderCrystal> getCrystals(double distance) {
		ArrayList<EntityEnderCrystal> list = new ArrayList<EntityEnderCrystal>();
		
		for (Entity entity : mc.world.loadedEntityList) {
			if (entity instanceof EntityEnderCrystal) {
				if (entity.getDistance(mc.player) <= distance) {
					list.add((EntityEnderCrystal)entity);
				}
			}
		} 
		
		return list;
	}
	
    /**
     * Calculates crystal damage if the crystal is on pos to the entity
     */
    public static float calculateDamage(Vec3d pos, EntityPlayer entity) {
    	try {
    		if (entity.getDistance(pos.x, pos.y, pos.z) > 12) {
    			return 0;
    		}
    		
            double blockDensity = entity.world.getBlockDensity(pos, entity.getEntityBoundingBox());
            double power = (1.0D - (entity.getDistance(pos.x, pos.y, pos.z) / 12.0D)) * blockDensity;
            float damage = (float) ((int) ((power * power + power) / 2.0D * 7.0D * 12.0D + 1.0D));

            int difficulty = mc.world.getDifficulty().getId();
            damage *= (difficulty == 0 ? 0 : (difficulty == 2 ? 1 : (difficulty == 1 ? 0.5f : 1.5f)));

            return getReduction(entity, damage, new Explosion(mc.world, null, pos.x, pos.y, pos.z, 6F, false, true));
    	} catch (NullPointerException e) {
    		return 0;
    	}
    }

    public static float calculateDamage(BlockPos pos, EntityPlayer entity) {
    	try {
    		return calculateDamage(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), entity);
    	} catch (NullPointerException e) {
    		return 0;
    	}
    }
    
    public static float getReduction(EntityPlayer player, float damage, Explosion explosion) {
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) player.getTotalArmorValue(), (float) player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        damage *= (1.0F - (float) EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)) / 25.0F);
        
        if(player.isPotionActive(Potion.getPotionById(11))) damage -= damage / 4;

        return damage;
    }
    
    public static boolean canPlaceCrystal(BlockPos pos) {
        Block block = getBlock(pos);

         if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
             Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
             Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

             if (floor == Blocks.AIR && ceil == Blocks.AIR) {
                 return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.add(0, 1, 0))).isEmpty();
             }
         }

         return false;
     }
}
