package me.bebeli555.autobot.utils;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class MessageUtil {
    protected static Minecraft mc = Minecraft.getMinecraft();

    /**
     * Sends a message that warns the player for something with the AutoBot chat prefix
     * @param msg the message to send
     */
    public static void sendWarningMessage(String msg) {
        mc.player.sendMessage(new TextComponentString(ChatFormatting.RED + "[" + ChatFormatting.LIGHT_PURPLE + "AutoBot" + ChatFormatting.RED + "] " + msg));
    }

    /**
     * Sends a client sided message with the AutoBot chat prefix
     * @param msg the message to send
     */
    public static void sendChatMessage(String msg) {
        mc.player.sendMessage(new TextComponentString(ChatFormatting.GREEN + "[" + ChatFormatting.LIGHT_PURPLE + "AutoBot" + ChatFormatting.GREEN + "] " + ChatFormatting.WHITE + msg));
    }

    /**
     * Sends a clientSided message with a module prefix
     * @param red if true then message will be red if false then it will be some other color
     * @param name of the module it will add in the message
     * @param remove removes all the past messages made by the mod if true
     */
    public static void sendModuleMessage(String text, String name, boolean red) {

        String module = "";
        ChatFormatting color = ChatFormatting.WHITE;
        if (red) {
            color = ChatFormatting.RED;
        }
        if (!name.isEmpty()) {
            module = "-" + name;
        }

        mc.player.sendMessage(new TextComponentString(ChatFormatting.GREEN + "[" + ChatFormatting.LIGHT_PURPLE + "AutoBot" + module + ChatFormatting.GREEN + "] " + color + text));
    }
}
