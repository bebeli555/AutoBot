package me.bebeli555.autobot.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.EntityPushEvent;
import me.bebeli555.autobot.events.TravelEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
	
	public MixinEntityPlayer(World worldIn) {
		super(worldIn);
	}
	
    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollision(Entity entity, CallbackInfo callbackInfo) {
    	EntityPushEvent event = new EntityPushEvent(entity);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
	
    @Inject(method = "isPushedByWater()Z", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> callbackInfo) {
    	EntityPushEvent event = new EntityPushEvent(null);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo callbackInfo) {
    	TravelEvent event = new TravelEvent();
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		move(MoverType.SELF, motionX, motionY, motionZ);
    		callbackInfo.cancel();
    	}
    }
}
