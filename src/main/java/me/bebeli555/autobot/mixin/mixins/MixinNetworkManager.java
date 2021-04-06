package me.bebeli555.autobot.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;
import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PacketEvent;
import me.bebeli555.autobot.events.PacketPostEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
	
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, CallbackInfo callbackInfo) {
    	PacketEvent event = new PacketEvent(packet);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
    
    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
    	PacketEvent event = new PacketEvent(packet);
    	AutoBot.EVENT_BUS.post(event);
    	
    	if (event.isCancelled()) {
    		callbackInfo.cancel();
    	}
    }
    
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void onPostSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
      	PacketPostEvent event = new PacketPostEvent(packet);
    	AutoBot.EVENT_BUS.post(event);
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void onPostChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
    	PacketPostEvent event = new PacketPostEvent(packet);
    	AutoBot.EVENT_BUS.post(event);
    }
}
