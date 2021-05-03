package me.bebeli555.autobot.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import me.bebeli555.autobot.utils.ICPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity;

@Mixin(CPacketUseEntity.class)
public abstract class MixinCPacketUseEntity implements ICPacketUseEntity {

    @Shadow
    protected CPacketUseEntity.Action action;

    @Shadow
    protected int entityId;
    
    @Override
    public void setEntityId(int entityId) {
    	this.entityId = entityId;
    }
    
    @Override
    public void setAction(CPacketUseEntity.Action action) {
    	this.action = action;
    }
}
