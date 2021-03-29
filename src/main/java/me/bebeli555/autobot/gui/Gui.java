package me.bebeli555.autobot.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.Commands;
import me.bebeli555.autobot.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Gui extends GuiScreen {	
	private static Minecraft mc = Minecraft.getMinecraft();
	public static ArrayList<GuiClick> visibleNodes = new ArrayList<GuiClick>();
	public static boolean isOpen;
	public static GuiClick selected, description;
	public static Group dragging;
	public static int lastMouseX, lastMouseY;
	public static Timer backspaceTimer = new Timer();
	public static boolean backspaceStarted, pasting;
	public static char pasteChar;
	public static Gui gui = new Gui();
	
	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		//Scale the gui to match the resolution and the gui scale.
		Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		GlStateManager.pushMatrix();
		float guiScale = (float)((float)mc.displayWidth / resolution.getWidth());
		if (mc.isFullScreen()) {
			this.setGuiSize(mc.displayWidth / 2, mc.displayHeight / 2);
		} else {
			this.setGuiSize((int)(mc.displayWidth * 1.15), (int)(mc.displayHeight * 1.15));
		}
		
		if (mc.gameSettings.guiScale == 0) {
			guiScale += -0.5 * guiScale;
		} else if (mc.gameSettings.guiScale == 1) {
			guiScale += 1 * guiScale;
		} else if (mc.gameSettings.guiScale == 3) {
			guiScale += -0.3 * guiScale;
		}
		
		mouseX = (int)(mouseX / guiScale);
		mouseY = (int)(mouseY / guiScale);
		
		GlStateManager.scale(guiScale, guiScale, guiScale);
		
		this.drawDefaultBackground();
		visibleNodes.clear();
		description = null;
		
		//Fast backspace
		if (Keyboard.isKeyDown(Keyboard.KEY_BACK) || Keyboard.isKeyDown(Keyboard.KEY_DELETE)) {
			if (selected != null && backspaceTimer.hasPassed(69)) {
				if (!backspaceStarted) {
					if (!selected.guiNode.stringValue.isEmpty()) {
						selected.guiNode.stringValue = selected.guiNode.stringValue.substring(0, selected.guiNode.stringValue.length() - 1);
					} else {
						selected.guiNode.stringValue = selected.guiNode.defaultValue;
					}
					
					backspaceTimer.reset();	
				} else if (backspaceTimer.hasPassed(420)) {
					backspaceStarted = false;
				}
			}
		} else {
			backspaceTimer.reset();	
			backspaceStarted = true;
		}
		
		//Drag group
		if (dragging != null) {
			if (Mouse.isButtonDown(1) || Mouse.isButtonDown(0)) {
				if (mouseY > 10) {
					dragging.x += mouseX - lastMouseX;
					dragging.y += mouseY - lastMouseY;
				}
			} else {
				dragging = null;
			}
		}
		
		//Top left info text
		int infoWidth = mc.fontRenderer.getStringWidth(ChatFormatting.RED + "Discord: " + ChatFormatting.GREEN + AutoBot.DISCORD + 2);
		drawRect(0, 0, infoWidth, 34, 0xFF000000);
		drawRect(0, 33, infoWidth, 34, 0x99d303fc);
		drawRect(infoWidth, 0, infoWidth + 1, 34, 0x99d303fc);
		mc.fontRenderer.drawStringWithShadow(ChatFormatting.RED + "AutoBot" + ChatFormatting.GREEN + " V" + AutoBot.VERSION, 2, 2, 0xFF000000);
		mc.fontRenderer.drawStringWithShadow(ChatFormatting.RED + "Made by: " + ChatFormatting.GREEN + "bebeli555", 2, 12, 0xFF000000);
		mc.fontRenderer.drawStringWithShadow(ChatFormatting.RED + "Discord: " + ChatFormatting.GREEN + AutoBot.DISCORD, 2, 22, 0xFF000000);
		
		//Draw all visible nodes
		for (Group group : Group.values()) {
			//All visible nodes
			int nodes = 0;
			for (GuiNode node : GuiNode.all) {
				if (node.isVisible && node.group == group) {
					nodes++;
				}
			}
			
			int count = 0;
			for (GuiNode node : GuiNode.all) {
				if (node.group == group && node.isVisible) {
					drawGuiNode(mouseX, mouseY, node, count, nodes);
					count++;
				}
			}
			
			//Draw the Group thing
			int x = group.x;
			int x2 = group.x + GuiSettings.width.intValue();
			int y = group.y - GuiSettings.height.intValue();
			int y2 = group.y;
			GuiNode guiNode = new GuiNode(true);
			guiNode.description = group.description;
			GuiClick guiClick = new GuiClick(x, y, x2, y2, guiNode);
			
			drawRect(x, y, x2, y2, GuiSettings.groupBackground.intValue());
			drawBorder(true, true, true, true, GuiSettings.borderColor.intValue(), guiClick);
			GlStateManager.pushMatrix();
			float scale = (float)GuiSettings.groupScale.doubleValue();
			GlStateManager.scale(scale, scale, scale);
			mc.fontRenderer.drawStringWithShadow(group.name, (((x2 / scale) - (x / scale)) / 2) + (x / scale) - ((mc.fontRenderer.getStringWidth(group.name)) / 2), (y + (5 / scale)) / scale, GuiSettings.groupTextColor.intValue());
			GlStateManager.popMatrix();
			
			if (x < mouseX && x2 > mouseX && y < mouseY && y2 > mouseY) {
				description = guiClick;
				
				//Drag the group if holding mouse
				if (Mouse.isButtonDown(1) || Mouse.isButtonDown(0)) {
					if (dragging == null) {
						dragging = group;
					}
				}
			}
		}
		
		//Draw descriptions so they will overlay everything else
		for (GuiClick g : new GuiClick[]{selected, description}) {
			if (g != null) {
				String[] description = g.guiNode.description;
				
				if (g.guiNode.modes.size() != 0) {
					Object[] array = null;
					try {
						array = g.guiNode.modeDescriptions.get(g.guiNode.modes.indexOf(g.guiNode.stringValue)).toArray();
					} catch (Exception e) {
						//This is probably caused because the saved setting had a mode value that is no longer existing so it will just set it to the default
						//Probably because a new version update that modified the mode names
						g.guiNode.stringValue = g.guiNode.modes.get(0);
						break;
					}
					description = (String[])Arrays.copyOf(array, array.length, String[].class);
					description = AutoBot.addToArray(description, ChatFormatting.GREEN + "Click to switch modes");
				}
				
		 		for (GuiNode node : g.guiNode.parentedNodes) {
					if (!node.modeName.isEmpty() && !node.modeName.equals(g.guiNode.stringValue)) {
						continue;
					}
					
					if (description == null || description.length == 0) {
						description = new String[]{ChatFormatting.GREEN + "Right click to extend"};
					} else {
						description = AutoBot.addToArray(description, ChatFormatting.GREEN + "Right click to extend");
					}
					break;
		 		}
				
				if (selected != null && selected.equals(g)) {
					if (g.guiNode.onlyNumbers) {
						description = new String[]{ChatFormatting.GOLD + "Type numbers in your keyboard to set this"};
					} else if (g.guiNode.isKeybind) {
						description = new String[]{ChatFormatting.GOLD + "Click a key to set the keybind"};
					} else {
						description = new String[]{ChatFormatting.GOLD + "Type with your keyboard to set this"};
					}
				}
				
				if (description != null) {
					for (int i = 0; i < description.length; i++) {
						int y = (int)(g.y + 6) + (i * 10);
						
						drawRect(g.x2 + 8, y - 2, g.x2 + (int)mc.fontRenderer.getStringWidth(description[i]) + 12, y + 10, 0xFF000000);
						mc.fontRenderer.drawStringWithShadow(description[i], (g.x2) + 10, ((g.y + 6) + (i * 10)), 0xffff);
					}
				}
				
				break;
			}
		}
		
		for (AutoBot module : AutoBot.modules) {
			if (module.isToggled()) {
				module.onGuiDrawScreen(mouseX, mouseY, partialTicks);
			}
		}
		
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		GlStateManager.popMatrix();
	}
	
	//This calculates the coordinates for the node and draws everything for the node.
	public void drawGuiNode(int mouseX, int mouseY, GuiNode node, int aboveNodes, int nodes) {
		int extendMoveMultiplier = node.getAllParents().size() * GuiSettings.extendMove.intValue();
		GuiClick g = new GuiClick(node.group.x + extendMoveMultiplier, 
				node.group.y + (aboveNodes * GuiSettings.height.intValue()), 
				node.group.x + GuiSettings.width.intValue() + extendMoveMultiplier, 
				node.group.y + GuiSettings.height.intValue() + (aboveNodes * GuiSettings.height.intValue()), node);
		
		//Draw box
		drawRect(g.x, g.y, g.x2, g.y2, GuiSettings.backgroundColor.intValue());

		//Draw text if its too big for the width then lower the scale on it
		String text = g.guiNode.name;
		if (g.guiNode.isTypeable) {
			ChatFormatting color = ChatFormatting.AQUA;
			if (g.guiNode.isKeybind) {
				color = ChatFormatting.LIGHT_PURPLE;
			}
			
			if (g.guiNode.stringValue.isEmpty()) {
				g.guiNode.stringValue = "";
				text = color + g.guiNode.name + ": " + ChatFormatting.RED + "NONE";
			} else {
				text = color + g.guiNode.name + ": " + ChatFormatting.GOLD + g.guiNode.stringValue;
			}
		} else if (g.guiNode.modes.size() != 0) {
			text = ChatFormatting.GREEN + g.guiNode.name + ": " + ChatFormatting.WHITE + g.guiNode.stringValue;
		}
		
		float scale = 1F;
		if (mc.fontRenderer.getStringWidth(text) > g.x2 - g.x) {
			GlStateManager.pushMatrix();
			
			int width = (int)mc.fontRenderer.getStringWidth(text);
			while (width * scale > g.x2 - g.x) {
				scale -= 0.03;
			}
			
			GlStateManager.scale(scale, scale, scale);
		}
		
		//This is some serious math
		mc.fontRenderer.drawStringWithShadow(text, (((g.x2 / scale) - (g.x / scale)) / 2) + (g.x / scale) - ((mc.fontRenderer.getStringWidth(text)) / 2), (g.y + 6) / scale, g.guiNode.getTextColor());
		
		if (scale != 1F) {
			GlStateManager.popMatrix();
		}
		
		//Draw border
		drawBorder(true, true, true, true, GuiSettings.borderColor.intValue(), g);
		
		//also calculate the thing to draw above it so it will match the other border if its more x than it is
		if (!visibleNodes.isEmpty()) {
			GuiClick last = visibleNodes.get(visibleNodes.size() - 1);
			
			if (last.guiNode.group == node.group) {
				//Last is more on left
	 			if (last.x < g.x) {
	 				drawRect(last.x, g.y, last.x + GuiSettings.borderSize.intValue(), g.y + GuiSettings.borderSize.intValue(), GuiSettings.borderColor.intValue());
	 				drawRect(last.x2, g.y, last.x2 + GuiSettings.extendMove.intValue(), g.y + GuiSettings.borderSize.intValue(), GuiSettings.borderColor.intValue());
	 			} 
	 			
	 			//Last is more on right
	 			else if (last.x > g.x) {
	 				drawRect(last.x, g.y, last.x - GuiSettings.extendMove.intValue(), g.y + GuiSettings.borderSize.intValue(), GuiSettings.borderColor.intValue());
	 				drawRect(last.x2, g.y, last.x2 - GuiSettings.extendMove.intValue(), g.y + GuiSettings.borderSize.intValue(), GuiSettings.borderColor.intValue());
	 			}
			}
		}
		
		//Set description
		if (g.x < mouseX && g.x2 > mouseX && g.y < mouseY && g.y2 > mouseY) {
			description = g;
		}
		
		//Add GuiClick to visibleNodes list
		visibleNodes.add(g);
	}
	
	public void drawBorder(boolean right, boolean left, boolean up, boolean down, int color, GuiClick n) {
		if (up) drawRect(n.x, n.y, n.x2, n.y + GuiSettings.borderSize.intValue(), color);
		if (down) drawRect(n.x, n.y2, n.x2, n.y2 + GuiSettings.borderSize.intValue(), color);
		if (left) drawRect(n.x, n.y, n.x + GuiSettings.borderSize.intValue(), n.y2, color);
		if (right) drawRect(n.x2, n.y, n.x2 + GuiSettings.borderSize.intValue(), n.y2 + GuiSettings.borderSize.intValue(), color);
	}
	
	@Override
	protected void mouseClicked(int x, int y, int button) {
		//Just use the lastMouse positions saved by the rendering loop because these are different
		x = lastMouseX;
		y = lastMouseY;
		
		//Open discord link if the thing is clicked
		if (0 < x && 150 > x && 0 < y && 34 > y) {
			try {
				URI link = new URI("https://" + AutoBot.DISCORD);
				ReflectionHelper.setPrivateValue(GuiScreen.class, this, link, "clickedLinkURI", "field_175286_t");
				mc.displayGuiScreen(new GuiConfirmOpenLink(this, link.toString(), 31102009, true));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		if (selected != null) {
			selected.guiNode.setSetting();
		}
		selected = null;
		
		for (GuiClick guiClick : visibleNodes) {
			if (guiClick.x < x && guiClick.x2 > x && guiClick.y < y && guiClick.y2 > y) {
				if (button == 1) {
					if (!guiClick.guiNode.parentedNodes.isEmpty()) {
						guiClick.guiNode.extend(!guiClick.guiNode.parentedNodes.get(0).isVisible);
					}
				} else {
					if (guiClick.guiNode.isTypeable) {
						selected = guiClick;
					}
					
					guiClick.guiNode.click();
				}
			}
		}
		
		for (AutoBot module : AutoBot.modules) {
			if (module.isToggled()) {
				module.onGuiClick(x, y, button);
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyPress(GuiScreenEvent.KeyboardInputEvent.Post e) {
		if (selected != null && Keyboard.isKeyDown(Keyboard.getEventKey())) {
			char key = Keyboard.getEventCharacter();
			if (pasting) {
				key = pasteChar;
			}
			
			//Paste
			if (!pasting && Keyboard.isKeyDown(Keyboard.KEY_V) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				pasting = true;
				selected.guiNode.stringValue = "";
				
				try {
					String clipboard = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor); 
					
					for (char c : clipboard.toCharArray()) {
						pasteChar = c;
						onKeyPress(null);
					}
				} catch (Exception ignored) {
					
				}
				
				pasting = false;
				return;
			}
			
			//Stuff for keybinds
			if (selected.guiNode.isKeybind) {
				if (Keyboard.getEventKey() != Keyboard.KEY_BACK) {
					selected.guiNode.stringValue = Keyboard.getKeyName(Keyboard.getEventKey());
				} else {
					selected.guiNode.stringValue = "";
				}
				
				Keybind.setKeybinds();
				selected.guiNode.notifyKeyListeners();
				return;
			}
			
			//Ignore if the key is shift as the user is probably trying to write uppercased letter
			if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT) {
				return;
			}
			
			//Backspace one key and if already empty then set to default value
			if (Keyboard.isKeyDown(Keyboard.KEY_BACK) || Keyboard.isKeyDown(Keyboard.KEY_DELETE)) {
				if (!selected.guiNode.stringValue.isEmpty()) {
					selected.guiNode.stringValue = selected.guiNode.stringValue.substring(0, selected.guiNode.stringValue.length() - 1);
				} else {
					selected.guiNode.stringValue = selected.guiNode.defaultValue;
				}
				
				backspaceTimer.reset();
				backspaceStarted = true;
				selected.guiNode.notifyKeyListeners();
				return;
			}
			
			char[] acceptedKeys;
			if (selected.guiNode.onlyNumbers) {
				if (selected.guiNode.acceptDoubleValues) {
					acceptedKeys = new char[]{'0','1','2','3','4','5','6','7','8','9','-','.'};
				} else {
					acceptedKeys = new char[]{'0','1','2','3','4','5','6','7','8','9','-'};
				}
			} else {
				selected.guiNode.stringValue += key;
				selected.guiNode.notifyKeyListeners();
				return;
			}
			
			//Check if key is in the acceptedKeys list and then put it to the stringValue
			for (char accept : acceptedKeys) {
				if (accept == key) {
					if (key == '-') {
						selected.guiNode.stringValue = "";
					}
					
					selected.guiNode.stringValue += key;
					selected.guiNode.notifyKeyListeners();
					return;
				}
 			}
		}
		
		for (AutoBot module : AutoBot.modules) {
			if (module.isToggled()) {
				module.onGuiKeyPress(e);
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent e) {
		//Have to open the GUI this way because if you try to open it in the chat event it wont work and if you try to put it to a new thread the mouse will be invisible.
		if (Commands.openGui) {
			mc.displayGuiScreen(new Gui());
			Commands.openGui = false;
			isOpen = true;
			return;
		}
		
		//Save settings when GUI is closed
		if (isOpen && mc.currentScreen == null) {
			Settings.saveSettings();
			isOpen = false;
			selected = null;
			pasting = false;
			dragging = null;
			description = null;
			MinecraftForge.EVENT_BUS.unregister(gui);
		}
	}
	
	public static class GuiClick {
		public int x, y, x2, y2;
		public GuiNode guiNode;
		
		public GuiClick(int x, int y, int x2, int y2, GuiNode guiNode) {
			this.x = x;
			this.y = y;
			this.x2 = x2;
			this.y2 = y2;
			this.guiNode = guiNode;
		}
	}
}
