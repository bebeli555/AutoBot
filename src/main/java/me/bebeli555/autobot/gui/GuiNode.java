package me.bebeli555.autobot.gui;

import java.util.ArrayList;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.bebeli555.autobot.AutoBot;

public class GuiNode extends AutoBot {
	public static ArrayList<GuiNode> all = new ArrayList<GuiNode>();
	
	public GuiNode parent;
	public String name;
	public String id = "";
	public String stringValue = "";
	public String defaultValue = "";
	public String modeName = "";
	public boolean isTypeable;
	public boolean onlyNumbers;
	public boolean acceptDoubleValues;
	public boolean isVisible;
	public boolean toggled;
	public boolean isLabel;
	public boolean isKeybind;
	public boolean isExtended;
	public Group group;
	public String[] description;
	public ArrayList<String> modes = new ArrayList<String>();
	public ArrayList<ArrayList<String>> modeDescriptions = new ArrayList<ArrayList<String>>();
	public ArrayList<GuiNode> parentedNodes = new ArrayList<GuiNode>();
	public ArrayList<ClickListener> clickListeners = new ArrayList<ClickListener>();
	public ArrayList<KeyListener> keyListeners = new ArrayList<KeyListener>();
	
	public GuiNode(boolean dontAdd) {

	}
	
	public GuiNode() {
		all.add(this);
	}
	
	//Sets the setting with the same ID as this GuiNode
	public void setSetting() {
		if (isTypeable || modes.size() != 0) {
			if (acceptDoubleValues) {
				try {
					Setting.getSettingWithId(id).value = Double.parseDouble(stringValue);
				} catch (Exception e) {
					stringValue = "";
					Setting.getSettingWithId(id).value = -1;
				}
			} else if (onlyNumbers) {
				try {
					Setting.getSettingWithId(id).value = Integer.parseInt(stringValue);
				} catch (Exception e) {
					stringValue = "";
					Setting.getSettingWithId(id).value = -1;
				}
			} else {
				if (!isKeybind) {
					Setting.getSettingWithId(id).value = stringValue;
				}
			}
		} else {
			Setting.getSettingWithId(id).value = toggled;
		}
	}
	
	//Add click listener
	public void addClickListener(ClickListener listener) {
		clickListeners.add(listener);
	}
	
	//Add key listener
	public void addKeyListener(KeyListener listener) {
		keyListeners.add(listener);
	}
	
	//Sets this to the default value
	public void setDefaultValue() {
		if (this.isTypeable) {
			stringValue = defaultValue;
		} else {
			toggled = Boolean.parseBoolean(defaultValue);
		}
	}
	
	//Gets called when this node is clicked on the gui
	public void click() {
		if (isLabel) {
			return;
		}
		
		//Mode
		if (modes.size() != 0) {
			extend(false);
			
			try {
				stringValue = modes.get(modes.indexOf(stringValue) + 1);
			} catch (IndexOutOfBoundsException e) {
				stringValue = modes.get(0);
			}
		}
		
		//Toggle
		if (!this.isTypeable && modes.size() == 0) {
			toggled = !toggled;
			
			for (AutoBot module : modules) {
				if (module.name.equals(name)) {
					if (toggled) {
						sendMessage(ChatFormatting.LIGHT_PURPLE + module.name + ChatFormatting.WHITE + " toggled " + ChatFormatting.GREEN + "ON", false);
						module.onEnabled();
						module.toggled = true;
					} else {
						sendMessage(ChatFormatting.LIGHT_PURPLE + module.name + ChatFormatting.WHITE + " toggled " + ChatFormatting.RED + "OFF", false);
						module.onDisabled();
						module.toggled = false;
					}
					
					break;
				}
			}
		}
		
		//Notify listeners
		setSetting();
		notifyClickListeners();
	}
	
	/**
	 * Extend this node and reveal all the nodes that parent this node
	 * @param extend if true it will extend it if false it will un extend it
	 */
 	public void extend(boolean extend) {
 		for (GuiNode node : parentedNodes) {
			if (!node.modeName.isEmpty() && !node.modeName.equals(stringValue)) {
				continue;
			}
 			
 			node.isVisible = extend;
 			isExtended = extend;
 			
 			//Un extend all the other nodes that parent the extends too
 			if (extend == false) {
 	 			for (GuiNode n : all) {
 	 				if (n.id.contains(node.id)) {  	 					
 	 					n.isVisible = false;
 	 				}
 	 			}
 			}
 		}
 	}
 	
	//Sets the ID for this node.
	public void setId() {
		if (this.parent != null) {
			ArrayList<GuiNode> parents = getAllParents();
			
			for (int i = parents.size(); i-- > 0;) {
				this.id += parents.get(i).name;
			}
			
			this.id += name;
		} else {
			this.id = name;
		}
	}
	
	//Get text color
	public int getTextColor() {
		if (isLabel) {
			return GuiSettings.labelColor.intValue();
		} else if (toggled) {
			return GuiSettings.textColor.intValue();
		} else {
			return GuiSettings.textColorOff.intValue();
		}
	}
	
	//Get the top parent of this node
	public GuiNode getTopParent() {
		ArrayList<GuiNode> parents = getAllParents();
		return parents.get(parents.size() - 1);
	}
	
	//Gets all parents from this node. First in list is this objects parent
	public ArrayList<GuiNode> getAllParents() {
		ArrayList<GuiNode> parents = new ArrayList<GuiNode>();
		
		GuiNode parent = this.parent;
		while(true) {
			if (parent != null) {
				parents.add(parent);
				
				if (parent.parent != null) {
					parent = parent.parent;
					continue;
				}
			}
			
			break;
		}
		
		return parents;
	}
	
	public void notifyClickListeners() {
		for (ClickListener listener : clickListeners) {
			listener.clicked();
		}
	}
	
	public void notifyKeyListeners() {
		for (KeyListener listener : keyListeners) {
			listener.pressed();
		}	
	}
	
	public static class ClickListener {
		public void clicked() {
			
		}
	}
	
	public static class KeyListener {
		public void pressed() {
			
		}
	}
}
