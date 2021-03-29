package me.bebeli555.autobot.gui;

import java.lang.reflect.Field;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.mods.Mods;

public class SetGuiNodes {
	
	//Sets the GuiNodes by looping through the modules
	//Then checking all the variables in the class and if its an Setting variable add that as a GuiNode
	public static void setGuiNodes() {
		try {
			for (Mods module : AutoBot.modules) {
				if (module.hidden) {
					continue;
				}
				GuiNode mainNode;
				Setting s = null;
				
				if (module.name.isEmpty()) {
					mainNode = new GuiNode(true);
					mainNode.group = module.group;
				} else {
					mainNode = new GuiNode();
					mainNode.group = module.group;
					mainNode.name = module.name;
					mainNode.description = module.description;
					mainNode.isVisible = true;
					mainNode.setId();
					s = new Setting(Mode.BOOLEAN, mainNode.name, false, mainNode.description);
				}
				
				for (Field field : module.getClass().getFields()) {
					Class<?> myType = Setting.class;
					
					if (field.getType().isAssignableFrom(myType)) {
						Setting setting = (Setting)field.get(module);
						if (!mainNode.id.isEmpty()) {
							setting.id = mainNode.id + setting.id;
						}

						GuiNode node = new GuiNode();
						node.name = setting.name;
						node.description = setting.description;
						node.defaultValue = String.valueOf(setting.defaultValue);
						node.group = mainNode.group;
						node.modeName = setting.modeName;
						
						if (!mainNode.id.isEmpty()) {
							if (setting.parent != null) {
								GuiNode p = Settings.getGuiNodeFromId(setting.parent.id);
								node.parent = p;
								p.parentedNodes.add(node);
							} else {
								node.parent = mainNode;
								mainNode.parentedNodes.add(node);
							}
						} else {
							node.isVisible = true;
						}
						
						if (setting.mode == Mode.TEXT) {
							node.isTypeable = true;
						} else if (setting.mode == Mode.INTEGER) {
							node.isTypeable = true;
							node.onlyNumbers = true;
						} else if (setting.mode == Mode.DOUBLE) {
							node.isTypeable = true;
							node.onlyNumbers = true;
							node.acceptDoubleValues = true; 
						} else if (setting.mode == Mode.LABEL) {
							node.isLabel = true;
						} else if (setting.modes.size() != 0) {
							node.modes = setting.modes;
							node.modeDescriptions = setting.modeDescriptions;
						}
						
						if (node.isTypeable || node.modes.size() != 0) {
							node.defaultValue = setting.stringValue();
							node.stringValue = setting.stringValue();
						} else {
							node.toggled = setting.booleanValue();
						}

						node.setId();
					}
				}
				
				//Keybind setting and node
				GuiNode node = new GuiNode();
				node.isVisible = true;
				if (s != null) {
					mainNode.parentedNodes.add(node);
					node.description = new String[]{"Keybind for " + mainNode.name};
					node.parent = mainNode;
					node.isVisible = false;
				} else {
					node.description = new String[]{"Keybind for " + module.group.name};
				}
				node.group = module.group;
				node.isTypeable = true;
				node.isKeybind = true;
				node.name = "Keybind";
				node.setId();
				
			}
		} catch (Exception e) {
			System.out.println("AutoBot - Exception setting gui nodes");
			e.printStackTrace();
		}
	}
}
