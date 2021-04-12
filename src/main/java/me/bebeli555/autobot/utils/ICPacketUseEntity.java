package me.bebeli555.autobot.utils;

import net.minecraft.network.play.client.CPacketUseEntity;

public interface ICPacketUseEntity {

    void getEntityId(int entityId);

    void getEntityAction(CPacketUseEntity.Action action);
   
    void setEntityId(int entityId);

    void setAction(CPacketUseEntity.Action action);
}

