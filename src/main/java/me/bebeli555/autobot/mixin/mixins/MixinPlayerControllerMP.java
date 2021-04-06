package me.bebeli555.autobot.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PlayerDamageBlockEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

	@Inject(method = "clickBlock", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerDamageBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
    	PlayerDamageBlockEvent event = new PlayerDamageBlockEvent(loc, face);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		cir.cancel();
    	}
	}
}
