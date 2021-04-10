package me.bebeli555.autobot.mods.other;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.lwjgl.input.Mouse;

import me.bebeli555.autobot.AutoBot;
import me.bebeli555.autobot.events.PlayerUpdateEvent;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.GuiNode;
import me.bebeli555.autobot.gui.GuiNode.ClickListener;
import me.bebeli555.autobot.gui.Mode;
import me.bebeli555.autobot.gui.Setting;
import me.bebeli555.autobot.gui.Settings;
import me.bebeli555.autobot.rendering.RenderBlock;
import me.bebeli555.autobot.utils.BaritoneUtil;
import me.bebeli555.autobot.utils.BlockUtil;
import me.bebeli555.autobot.utils.InventoryUtil;
import me.bebeli555.autobot.utils.RotationUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;

public class AutoBuilder extends AutoBot {
	private static Thread thread;
	private static ArrayList<BuildPosition> positions = new ArrayList<BuildPosition>();
	private static boolean check, down;
	private static BlockPos playerPos;
	private static AutoBuilder autoBuilder;
	
	public static Setting setStructure = new Setting(Mode.BOOLEAN, "SetStructure", false, "Click this to set the structure", "You can middleclick on blocks while this is on", "to add them to the structure", "When ur done toggle this off and autobuilder", "will now build the structure you made");
	public static Setting delay = new Setting(Mode.INTEGER, "Delay", 50, "Delay in milliseconds between placing blocks");
	public static Setting toggle = new Setting(Mode.BOOLEAN, "Toggle", false, "If true then it will toggle the module off", "After the structure has been built", "Note: This has no effect if you have move on");
	public static Setting move = new Setting(Mode.BOOLEAN, "Move", false, "When the structure has been built it will move", "To the given coordinates below and then build it again", "The coordinates are relative to the player so", "For example X: 1 will make it walk 1 block right");
		public static Setting moveX = new Setting(move, Mode.INTEGER, "X", 0, "X-Coordinate relative to players position");
		public static Setting moveY = new Setting(move, Mode.INTEGER, "Y", 0, "Y-Coordinate relative to players position");
		public static Setting moveZ = new Setting(move, Mode.INTEGER, "Z", 0, "Z-Coordinate relative to players position");
		
	public AutoBuilder() {
		super(Group.OTHER, "AutoBuilder", "Builds anything you want relative to the players position");
		autoBuilder = this;
	}
	
	@Override
	public void onEnabled() {
		if (!check) {
			check = true;
			readFile();
		}
		
		thread = new Thread() {
			public void run() {
				while(thread != null && thread.equals(this)) {
					loop();
					
					AutoBot.sleep(35);
				}
			}
		};
		
		thread.start();
	}
	
	@Override
 	public void onDisabled() {
		clearStatus();
		RotationUtil.stopRotating();
		BaritoneUtil.forceCancel();
		suspend(thread);
		thread = null;
	}
	
	@Override
	public void onPostInit() {
		GuiNode node = Settings.getGuiNodeFromId(setStructure.id);
		
		node.addClickListener(new ClickListener() {
			public void clicked() {
				if (!node.toggled) {
					AutoBot.EVENT_BUS.unsubscribe(autoBuilder);
					RenderBlock.clear();
					saveFile();
					sendMessage("Successfully saved structure!", false);
				} else {
					positions.clear();
					playerPos = getPlayerPos();
					RenderBlock.add(playerPos, Color.GREEN, 3);
					AutoBot.EVENT_BUS.subscribe(autoBuilder);
				}
			}
		});
	}
	
	public void loop() {
		if (mc.player == null) {
			return;
		}
		
		for (int i = 0; i < positions.size(); i++) {
			BuildPosition buildPosition = positions.get(i);
			BlockPos pos = getPlayerPos().add(buildPosition.blockPos.getX(), buildPosition.blockPos.getY(), buildPosition.blockPos.getZ());
			
			if (BlockUtil.canPlaceBlock(pos)) {
				if (!InventoryUtil.hasBlock(buildPosition.block)) {
					if (Block.getIdFromBlock(buildPosition.block) != 144 || Block.getIdFromBlock(buildPosition.block) == 144 && !InventoryUtil.hasItem(Items.SKULL)) {
						sendMessage("You dont have the required materials to build this structure", true);
						toggleModule();
						return;
					}
				}
				
				if (Block.getIdFromBlock(buildPosition.block) == 144) {
					setStatus("Placing skull");
					BlockUtil.placeItemNoSleep(Items.SKULL, pos, true);
				} else {
					setStatus("Placing block");
					BlockUtil.placeBlockNoSleep(buildPosition.block, pos, true);
				}
				
				sleep(delay.intValue());
			}
		}
		
		if (move.booleanValue()) {
			BlockPos pos = getPlayerPos().add(moveX.intValue(), moveY.intValue(), moveZ.intValue());
			setStatus("Moving to X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());
			BaritoneUtil.walkTo(pos, true);
		} else if (toggle.booleanValue()) {
			toggleModule();
		}
	}
	
    @EventHandler
    private Listener<PlayerUpdateEvent> onUpdate = new Listener<>(event -> {
    	if (Mouse.isButtonDown(2)) {
    		if (down == true) {
    			return;
    		}
    		
    		down = true;
    		BlockPos pos = mc.objectMouseOver.getBlockPos();
    		
    		if (pos != null) {
    			BlockPos relative = new BlockPos(pos.getX() - playerPos.getX(), pos.getY() - playerPos.getY(), pos.getZ() - playerPos.getZ());
    			
    			for (BuildPosition buildPosition : positions) {
    				if (buildPosition.blockPos.equals(relative)) {
    					positions.remove(buildPosition);
    					RenderBlock.remove(pos);
    					return;
    				}
    			}
    			
    			positions.add(new BuildPosition(relative, getBlock(pos)));
    			RenderBlock.add(pos, Color.CYAN, 1);
    		}
    	} else {
    		down = false;
    	}
    });
    
	//Reads the file and updates the list
	public void readFile() {
		try {
			File file = new File(Settings.path + "/AutoBuilder.txt");
			if (!file.exists()) {
				sendMessage("Create a structure first", true);
				toggleModule();
				return;
			}
			
			Scanner scanner = new Scanner(file);
			positions.clear();

			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (!line.isEmpty()) {
					String[] split = line.split(",");
					positions.add(new BuildPosition(new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])), Block.getBlockById(Integer.parseInt(split[3]))));
				}
			}
			
			scanner.close();
		} catch (Exception e) {
			sendMessage("Error reading structure file. More info in your games log", true);
			e.printStackTrace();
		}
	}
	
	//Saves the structure into a file
	//0 = X. 1 = Y. 2 = Z. 3 = blockid.
	//Prefix change: ,
	public void saveFile() {
		try {
			 File file = new File(Settings.path + "/AutoBuilder.txt");
			 file.delete();
			 file.createNewFile();
			 
			 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			 for (BuildPosition b : positions) {
				 bw.write(b.blockPos.getX() + "," + b.blockPos.getY() + "," + b.blockPos.getZ() + "," + Block.getIdFromBlock(b.block));
				 bw.newLine();
			 }
			 
			 bw.close();
		} catch (Exception e) {
			sendMessage("Error saving structure. More info in your games log", true);
			e.printStackTrace();
		}
	}
	
	public static class BuildPosition {
		public BlockPos blockPos;
		public Block block;
		
		public BuildPosition(BlockPos blockPos, Block block) {
			this.blockPos = blockPos;
			this.block = block;
		}
	}
}
