package me.bebeli555.autobot;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;

import me.bebeli555.autobot.gui.Gui;
import me.bebeli555.autobot.gui.GuiNode;
import me.bebeli555.autobot.gui.Settings;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Commands extends AutoBot {
	public static boolean openGui;
	public static String prefix = "++";

	@SubscribeEvent
	public void onChat(ClientChatEvent e) {
		String messageReal = e.getMessage();
		String message = messageReal.toLowerCase();
		
		if (message.startsWith(prefix)) {
			e.setCanceled(true);
			mc.ingameGUI.getChatGUI().addToSentMessages(messageReal);
			message = message.replace(prefix, "");

			//Open gui command
			if (message.equals("gui")) {
				openGui = true;
				MinecraftForge.EVENT_BUS.register(Gui.gui);
			}
			
			//Set settings
			else if (message.startsWith("set")) {
				String split[] = messageReal.split(" ");
				String id = split[1].replace("_", " ");
				String value = split[2];
				
				GuiNode guiNode = Settings.getGuiNodeFromId(id);
				if (guiNode == null) {
					sendMessage("Cant find setting with id: " + id, true);
				} else {
					if (guiNode.isTypeable != Settings.isBoolean(value)) {
						if (!guiNode.isTypeable) {
							guiNode.toggled = Boolean.parseBoolean(value);
							guiNode.setSetting();
						} else {
							try {
								guiNode.setSetting();
								
								guiNode.stringValue = value;
							} catch (Exception ex) {
								sendMessage("Wrong input. This might be caused if u input a string value and the setting only accepts integer or double", true);
								return;
							}
						}
						
						sendMessage("Set " + id + " to " + value, false);
						
						if (Settings.isBoolean(value)) {
							try {
								AutoBot.toggleMod(id, Boolean.parseBoolean(value));
							} catch (Exception ignored) {

							}
						}
					} else {
						if (guiNode.isTypeable) {
							sendMessage("This setting requires a boolean value", true);
						} else {
							sendMessage("This setting requires a string or integer value", true);
						}
					}
				}
			}
			
			//List of settings
			else if (message.equals("list")) {
				String list = "";
				for (GuiNode node : GuiNode.all) {
					list += node.id.replace(" ", "_") + ", ";
				}
				
				sendMessage(list, false);
			}
			
			//Help
			else if (message.equals("help")) {
				sendMessage(prefix + "gui - Opens the GUI", false);
				sendMessage(prefix + "set settingId value - sets setting with given id to given value", false);
				sendMessage(prefix + "list - Gives a list of all the settingIds", false);
			}
			
			//Create a fakeplayer on ur position (Used for development)
			else if (message.equals("fkplayer")) {
				EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("6ab32213-179a-4c41-8ab9-66789121e051"), "bebeli555"));
				fakePlayer.copyLocationAndAnglesFrom(mc.player);
				fakePlayer.rotationYawHead = mc.player.rotationYawHead;
				mc.world.addEntityToWorld(-100, fakePlayer);
				sendMessage("Summoned a fake player ", false);
			}
			
			//Set custom render distance
			else if (message.startsWith("renderdistance")) {
				int value = Integer.parseInt(message.split(" ")[1]);
				mc.gameSettings.renderDistanceChunks = value;
				sendMessage("Set render distance to " + value, false);
			}
			
			//Unknown command
			else {
				sendMessage("Unknown command. Type " + ChatFormatting.GREEN + prefix + "help" + ChatFormatting.RED + " for help", true);
			}
		}
	}
}
