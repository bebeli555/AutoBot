package me.bebeli555.autobot.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.EntityPushEvent;
import me.bebeli555.autobot.events.PlayerMotionUpdateEvent;
import me.bebeli555.autobot.events.PlayerMoveEvent;
import me.bebeli555.autobot.events.PlayerUpdateEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
	@Inject(method = "pushOutOfBlocks(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfo) {
    	EntityPushEvent event = new EntityPushEvent(null);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
    
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType type, double x, double y, double z, CallbackInfo callbackInfo) {
    	PlayerMoveEvent event = new PlayerMoveEvent(type, x, y, z);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
    
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void motionUpdate(CallbackInfo callbackInfo) {
    	PlayerMotionUpdateEvent event = new PlayerMotionUpdateEvent();
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
    
    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void onUpdate(CallbackInfo callbackInfo) {
    	PlayerUpdateEvent event = new PlayerUpdateEvent();
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
}
