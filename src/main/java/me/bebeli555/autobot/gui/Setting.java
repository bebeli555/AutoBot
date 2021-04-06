package me.bebeli555.autobot.gui;

import java.util.ArrayList;

public class Setting {
	public Mode mode;
	public String name;
	public String[] description;
	public Object value;
	public Object defaultValue;
	public Setting parent;
	public String id;
	public String modeName = "";
	public ArrayList<String> modes = new ArrayList<String>();
	public ArrayList<ArrayList<String>> modeDescriptions = new ArrayList<ArrayList<String>>();
	
	public static ArrayList<Setting> all = new ArrayList<Setting>();
	
	public Setting(Mode mode, String name, Object defaultValue, String... description) {
		this.mode = mode;
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.description = description;
		setId();
		all.add(this);
	}
	
	public Setting(Setting parent, Mode mode, String name, Object defaultValue, String... description) {
		this(mode, name, defaultValue, description);
		this.parent = parent;
		setId();
	}
	
	public Setting(Setting parent, String modeName, Mode mode, String name, Object defaultValue, String... description) {
		this(mode, name, defaultValue, description);
		this.parent = parent;
		this.modeName = modeName;
		setId();
	}
	
	/**
	 * Used for creating a mode thing
	 * @param modes first string is the name and the others will be description.
	 */
	public Setting(Setting parent, String name, String defaultValue, String[]... modes) {
		for (String[] mode : modes) {
			this.modes.add(mode[0]);
			
			ArrayList<String> descriptions = new ArrayList<String>();
			for (int i = 1; i < mode.length; i++) {
				descriptions.add(mode[i]);
			}
			
			this.modeDescriptions.add(descriptions);
		} 
		
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.parent = parent;
		this.name = name;
		setId();
		all.add(this);
	}
	
	public boolean booleanValue() {
		return (Boolean)value;
	}
	
	public int intValue() {
		if (stringValue().isEmpty()) {
			return -1;
		}
		
		try {
			return (int)value;
		} catch (ClassCastException e) {
			try {
				return Integer.parseUnsignedInt(((String)value).replace("0x", ""), 16);
			} catch (Exception e2) {
				return Integer.parseUnsignedInt(((String)defaultValue).replace("0x", ""), 16);
			}
		}
	}
	
	public void updateGuiNode() {
		if (mode == Mode.BOOLEAN) {
			Settings.getGuiNodeFromId(id).toggled = booleanValue();
		} else {
			Settings.getGuiNodeFromId(id).stringValue = stringValue();
		}
	}
	
	public double doubleValue() {
		if (stringValue().isEmpty()) {
			return -1;
		}
		
		try {
			return (double)value;
		} catch (Exception e) {
			return (int)value;
		}
	}
	
	public String stringValue() {
		return String.valueOf(value);
	}
	
	public void setId() {
		id = "";
		
		if (this.parent != null) {
			ArrayList<Setting> parents = getSettingParents();

			for (int i = parents.size(); i-- > 0;) {
				id += parents.get(i).name;
			}
			
			id += this.name;
		} else {
			id = this.name;
		}
	}
	
	/**
	 * Get all parents for this setting
	 */
	public ArrayList<Setting> getSettingParents() {
		ArrayList<Setting> parents = new ArrayList<Setting>();
		
		Setting parent = this.parent;
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
	
	public static Setting getSettingWithId(String id) {
		for (Setting setting : all) {
			if (setting.id.equals(id)) {
				return setting;
			}
		}
		
		return null;
	}
}
