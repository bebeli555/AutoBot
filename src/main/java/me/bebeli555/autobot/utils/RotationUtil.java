package me.bebeli555.autobot.utils;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PlayerMotionUpdateEvent;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RotationUtil extends AutoBot {
	public static RotationUtil rotationUtil = new RotationUtil();
	public static float yaw, pitch;
	
	/**
	 * Rotates to the given vector
	 * @param sendPacket if true then it will also send a packet to the server
	 */
	public static void rotate(Vec3d vec, boolean sendPacket) {
        float[] rotations = getRotations(vec);
		
        if (sendPacket) mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround));
        mc.player.rotationYaw = rotations[0];
        mc.player.rotationPitch = rotations[1];
	}
	
	/**
	 * Rotates to the given vector only serverside
	 */
	public static void rotateSpoof(Vec3d vec) {
		float[] rotations = getRotations(vec);
		yaw = rotations[0];
		pitch = rotations[1];
		
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
		AutoBot.EVENT_BUS.subscribe(rotationUtil);
	}
	
	/**
	 * Stops rotating if you called the rotateSpoof then u need to call this
	 */
	public static void stopRotating() {
		AutoBot.EVENT_BUS.unsubscribe(rotationUtil);
	}
	
	public static float[] getRotations(Vec3d vec) {
		Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		double diffX = vec.x - eyesPos.x;
		double diffY = vec.y - eyesPos.y;
		double diffZ = vec.z - eyesPos.z;
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

		return new float[] { mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch) };
	}
	
    @EventHandler
    private Listener<PlayerMotionUpdateEvent> onMotionUpdate = new Listener<>(event -> {
    	try {
            event.cancel();
            
            boolean sprinting = mc.player.isSprinting();
            if (sprinting != (boolean)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "serverSprintState")) {
                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                } else {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, sprinting, "serverSprintState");
            }

            boolean sneaking = mc.player.isSneaking();
            if (sneaking != (boolean)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "serverSneakState")) {
                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                } else {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, sneaking, "serverSneakState");
            }
            
            if (mc.getRenderViewEntity() != mc.player) {
            	return;
            }
            
    		AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
    		double posXDifference = mc.player.posX - (double)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "lastReportedPosX");
    		double posYDifference = axisalignedbb.minY - (double)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "lastReportedPosY");
    		double posZDifference = mc.player.posZ - (double)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "lastReportedPosZ");
    		double yawDifference = (double) (yaw - (float)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "lastReportedYaw"));
    		double rotationDifference = (double) (pitch - (float)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "lastReportedPitch"));
    		ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, (int)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "positionUpdateTicks") + 1, "positionUpdateTicks");
    		boolean movedXYZ = posXDifference * posXDifference + posYDifference * posYDifference + posZDifference * posZDifference > 9.0E-4D || (int)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "positionUpdateTicks") >= 20;
    		boolean movedRotation = yawDifference != 0.0D || rotationDifference != 0.0D;

    		if (mc.player.isRiding()) {
    			mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, yaw, pitch, mc.player.onGround));
    			movedXYZ = false;
    		} else if (movedXYZ && movedRotation) {
    			mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, yaw, pitch, mc.player.onGround));
    		} else if (movedXYZ) {
    			mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
    		} else if (movedRotation) {
    			mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
    		} else if ((boolean)ReflectionHelper.getPrivateValue(EntityPlayerSP.class, mc.player, "prevOnGround") != mc.player.onGround) {
    			mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
    		}

    		if (movedXYZ) {
    			ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, mc.player.posX, "lastReportedPosX");
    			ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, axisalignedbb.minY, "lastReportedPosY");
    			ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, mc.player.posZ, "lastReportedPosZ");
    			ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, 0, "positionUpdateTicks");
    		}

    		if (movedRotation) {
    			ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, yaw, "lastReportedYaw");
    			ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, pitch, "lastReportedPitch");
    		}

    		ReflectionHelper.setPrivateValue(EntityPlayerSP.class, mc.player, mc.player.onGround, "prevOnGround");
    	} catch (Exception e) {
    		
    	}
    });
}
