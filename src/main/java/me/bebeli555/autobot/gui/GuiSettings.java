package me.bebeli555.autobot.gui;

import me.bebeli555.autobot.AutoBot;

public class GuiSettings extends AutoBot {
	public GuiSettings() {
		super(Group.GUI);
	}
	
	public static Setting width = new Setting(Mode.INTEGER, "Width", 110, "Gui node width");
	public static Setting height = new Setting(Mode.INTEGER, "Height", 18, "Gui node height");
	public static Setting borderColor = new Setting(Mode.TEXT, "Border color", "0xFF32a86d", "Color of the border in hex and with 0xAA");
	public static Setting borderSize = new Setting(Mode.INTEGER, "Border size", 1, "The size of the border in the node");
	public static Setting backgroundColor = new Setting(Mode.TEXT, "Color", "0x36325bc2", "The background color");
	public static Setting textColor = new Setting(Mode.TEXT, "Text Color", "0xFF00ff00", "Text color when module is toggled on");
	public static Setting textColorOff = new Setting(Mode.TEXT, "Text Color Off", "0xFFff0000", "Text color when module is toggled off");
	public static Setting labelColor = new Setting(Mode.TEXT, "Label color", "0xFF6b6b6b", "The color of the label text which is an toggleable module");
	public static Setting extendMove = new Setting(Mode.INTEGER, "Extend Move", 8, "How much to move in x coordinates when parent is extended");
	public static Setting groupTextColor = new Setting(Mode.TEXT, "Group color", "0xFFe3a520", "The text color of the group");
	public static Setting groupScale = new Setting(Mode.DOUBLE, "Group scale", 1.25, "The group text scale");
	public static Setting groupBackground = new Setting(Mode.TEXT, "Group background", "0x3650b57c", "The group background color");
}