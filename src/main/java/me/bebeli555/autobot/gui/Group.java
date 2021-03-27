package me.bebeli555.autobot.gui;

public enum Group {
	BOTS("Bots", 430 + 55, 50, "The bots"),
	GUI("GUI", 190 + 55, 50, "Settings about the GUI design and stuff"),
	OTHER("Other", 550 + 55, 50, "Other \"smaller\" modules"),
	GAMES("Games", 310 + 55, 50, "Fun games to play");
	
	public String name;
	public int x, y;
	public String[] description;
	Group(String name, int x, int y, String... description) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.description = description;
	}
}
