package me.bebeli555.autobot.mods.bots.crystalpvpbot;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.EntityPushEvent;
import me.bebeli555.autobot.events.PacketEvent;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public class DisableKnockback extends AutoBot {
	private static DisableKnockback disableKnockback = new DisableKnockback();
	
	/**
	 * Toggles it on or off. If on then knockback will be disabled until u toggle it off
	 */
	public static void toggle(boolean on) {
		if (on) {
			EVENT_BUS.subscribe(disableKnockback);
		} else {
			EVENT_BUS.unsubscribe(disableKnockback);
		}
	}
	
    @EventHandler
    private Listener<EntityPushEvent> entityPushEvent = new Listener<>(event -> {
    	event.cancel();
    });
    
	@EventHandler
	private Listener<PacketEvent> packetEvent = new Listener<>(event -> {
        if (event.packet instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus)event.packet;
            
            if (packet.getOpCode() == 31) {
                Entity entity = packet.getEntity(mc.world);
                
                if (entity != null && entity instanceof EntityFishHook) {
                    EntityFishHook fishHook = (EntityFishHook) entity;
                    
                    if (fishHook.caughtEntity == mc.player) {
                    	event.cancel();
                    }
                }
            }
        } else if (event.packet instanceof SPacketEntityVelocity || event.packet instanceof SPacketExplosion) {
        	event.cancel();
		}
	});
}
