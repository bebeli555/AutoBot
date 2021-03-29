package me.bebeli555.autobot.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.mods.Mods;

public class Settings extends AutoBot {
	public static String path = mc.gameDir.getPath() + "/AutoBot";
	public static File settings = new File(path + "/Settings.txt");
	
	/**
	 * Saves the settings from the GUI to a file located at .minecraft/AutoBot/Settings.txt
	 */
	public static void saveSettings() {
		new Thread() {
			public void run() {
				try {
					settings.delete();
					settings.createNewFile();
					
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(settings)));
					for (GuiNode node : GuiNode.all) {
						if (node.id.equals("ExecuteCodeCode")) {
							continue;
						}
						
						bw.write(node.id + "=");
						if (!node.isTypeable && node.modes.size() == 0) {
							bw.write("" + node.toggled);
						} else {
							bw.write("" + node.stringValue);
						}
						
						bw.newLine();
					}
					
					//Also save the group coordinates
					for (Group group : Group.values()) {
						bw.write("Group88" + group.name + "=" + group.x + "," + group.y);
						bw.newLine();
					}
					
					bw.close();
				} catch (Exception e) {
					System.out.println("AutoBot - Error saving settings");
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/**
	 * Loads the settings saved in the file
	 */
	public static void loadSettings() {
		try {
			if (!settings.exists()) {
				return;
			}
			
			Scanner scanner = new Scanner(settings);
			while(scanner.hasNextLine()) {
				String split[] = scanner.nextLine().split("=");
				String id = split[0];
				String value;
				try {
					value = split[1];
				} catch (IndexOutOfBoundsException e) {
					value = "";
				}
				
				//If setting is group then do this trick. What? Trick. Ok! Haha get tricked. Shut up
				if (id.startsWith("Group88")) {
					String name = id.replace("Group88", "");
					int x = Integer.parseInt(value.split(",")[0]);
					int y = Integer.parseInt(value.split(",")[1]);
					
					for (Group group : Group.values()) {
						if (group.name.equals(name)) {
							group.x = x;
							group.y = y;
						}
					}
					
					continue;
				}
				
				GuiNode node = getGuiNodeFromId(id);
				if (node == null) {
					continue;
				}
				
				if (isBoolean(value)) {
					node.toggled = Boolean.parseBoolean(value);
					Setting.getSettingWithId(node.id).value = node.toggled;
					
					for (Mods module : modules) {
						if (module.name.equals(id)) {
							if (node.toggled) {
								module.onEnabled();
							}
						}
					}
				} else {
					node.stringValue = value;
					try {
						node.setSetting();
					} catch (NullPointerException e) {
						//Ingore exception bcs its probably caused by the keybind which doesnt have a setting only the node
					}
				}
			}
			scanner.close();
		} catch (Exception e) {
			System.out.println("AutoBot - Error loading settings");
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if the setting with given ID is toggled
	 */
	public static boolean isOn(String id) {
		return getGuiNodeFromId(id).toggled;
	}
	
	/**
	 * @return String value of GuiNode with given ID
	 */
	public static String getStringValue(String id) {
		return getGuiNodeFromId(id).stringValue;
	}
	
	/**
	 * String value of this setting turned into integer
	 */
	public static int getIntValue(String id) {
		return Integer.parseInt(getGuiNodeFromId(id).stringValue);
	}
	
	/**
	 * String value of this setting turned into double
	 */
	public static double getDoubleValue(String id) {
		return Double.parseDouble(getGuiNodeFromId(id).stringValue);
	}
	
	/**
	 * Get GuiNode with given ID
	 */
	public static GuiNode getGuiNodeFromId(String id) {
		for (GuiNode node : GuiNode.all) {
			if (node.id.equals(id)) {
				return node;
			}
		}
		
		return null;
	}
	
	//Checks if string is boolean
	public static boolean isBoolean(String string) {
		return "true".equals(string) || "false".equals(string);
	}
}
