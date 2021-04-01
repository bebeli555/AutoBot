package me.bebeli555.autobot;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.BooleanSupplier;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.GuiNode;
import me.bebeli555.autobot.gui.GuiSettings;
import me.bebeli555.autobot.gui.Keybind;
import me.bebeli555.autobot.gui.SetGuiNodes;
import me.bebeli555.autobot.gui.Settings;
import me.bebeli555.autobot.mods.bots.crystalpvpbot.CrystalPvPBot;
import me.bebeli555.autobot.mods.bots.elytrabot.ElytraBot;
import me.bebeli555.autobot.mods.bots.obbybuilderbot.ObbyBuilderBot;
import me.bebeli555.autobot.mods.games.Snake;
import me.bebeli555.autobot.mods.games.Tetris;
import me.bebeli555.autobot.mods.other.AutoBuilder;
import me.bebeli555.autobot.mods.other.AutoEnderChestMiner;
import me.bebeli555.autobot.mods.other.AutoFirework;
import me.bebeli555.autobot.mods.other.AutoInventoryManager;
import me.bebeli555.autobot.mods.other.AutoMend;
import me.bebeli555.autobot.mods.other.AutoTrap;
import me.bebeli555.autobot.mods.other.AutoTrapIndicator;
import me.bebeli555.autobot.mods.other.AutoWeb;
import me.bebeli555.autobot.mods.other.Burrow;
import me.bebeli555.autobot.mods.other.CrystalBlock;
import me.bebeli555.autobot.rendering.Renderer;
import me.bebeli555.autobot.utils.EatingUtil;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid = AutoBot.MODID, name = AutoBot.NAME, version = AutoBot.VERSION)
public class AutoBot {
    public static final String MODID = "autobot";
    public static final String NAME = "AutoBot";
    public static final String VERSION = "1.04";
    public static final String DISCORD = "discord.gg/xSukBcyd8m";
    
    public static Minecraft mc = Minecraft.getMinecraft();
    public static final EventBus EVENT_BUS = new EventManager();

    public String name = "";
    public String[] description;
    public Group group;
    public boolean toggled, disableToggleMessage;
    public static ArrayList<AutoBot> modules = new ArrayList<AutoBot>();
    
    public AutoBot(Group group, String name, String... description) {
    	this.group = group;
    	this.name = name;
    	this.description = description;
    	modules.add(this);
    }
    
    public AutoBot(Group group, String name, boolean disableToggleMessage, String... description) {
    	this(group, name, description);
    	this.disableToggleMessage = disableToggleMessage;
    }
    
    public AutoBot(Group group) {
    	this.group = group;
    	modules.add(this);
    }
    
    public AutoBot() {
    	
    }
    
    @EventHandler
	public void init(FMLInitializationEvent event) {
    	new Thread() {
    		public void run() {
    			//Initialize classes
    	    	MinecraftForge.EVENT_BUS.register(new HelpMessage());
    	    	MinecraftForge.EVENT_BUS.register(new Commands());
    	    	MinecraftForge.EVENT_BUS.register(new Renderer());
    	    	MinecraftForge.EVENT_BUS.register(new Keybind());
    	    	MinecraftForge.EVENT_BUS.register(new EatingUtil());
    	    	
    			new GuiSettings();
    	    	
    	    	new CrystalPvPBot();
    	    	new ElytraBot();
    	    	new ObbyBuilderBot();
    	    	
    			//new AutoWither();
    			new AutoFirework();
    			new AutoEnderChestMiner();
    			new AutoBuilder();
    			new AutoMend();
    			new AutoInventoryManager();
    			new AutoTrap();
    			new AutoTrapIndicator();
    			new AutoWeb();
    			new CrystalBlock();
    			new Burrow();
    			
    			new Snake();
    			new Tetris();
    			
    	    	//Initialize stuff
    			new File(Settings.path).mkdir();
    	    	SetGuiNodes.setGuiNodes();
    			Settings.loadSettings();
    			Keybind.setKeybinds();
    			
    			for (AutoBot module : modules) {
    				module.onPostInit();
    			}
    		}
    	}.start();
    }
    
    public void onEnabled(){};
    public void onDisabled(){};
    public void onPostInit(){};
    
    /**
     * Sends a clientSided message
     * @param red if true then message will be red if false then it will be some other color
     * @param name of the module it will add in the message
     * @param remove removes all the past messages made by the mod if true
     */
    public void sendMessage(String text, boolean red) {
    	if (mc.player == null) {
    		return;
    	}

    	//Send message
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
    
    public void toggleModule() {
    	Settings.getGuiNodeFromId(name).click();
    }
    
    public boolean isToggled() {
    	return toggled;
    }
    
    public static void toggleMod(String name, boolean on) {
    	GuiNode node = Settings.getGuiNodeFromId(name);
    	
    	node.toggled = on;
    	node.setSetting();
    	
    	for (AutoBot module : modules) {
    		if (module.name.equals(name)) {
    			if (on) {
    				module.onEnabled();
    			} else {
    				module.onDisabled();
    			}
    			
    			module.toggled = on;
    			break;
    		}
    	}
    }
    
    public void onGuiDrawScreen(int mouseX, int mouseY, float partialTicks){}
    public void onGuiClick(int x, int y, int button){}
    public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent.Post e){}
    
    /**
     * 1 = 50% change and so on
     */
    public boolean random(int i) {
    	return new Random().nextInt(i + 1) == 0;
    }
    
	public void setStatus(String status) {
		setStatus(status, name);
	}
	
	public static void setStatus(String status, String module) {
    	if (!module.isEmpty()) {
    		module = "-" + module;
    	}
    	
    	Renderer.status = new String[10];
    	Renderer.status[0] = ChatFormatting.GREEN + "[" + ChatFormatting.LIGHT_PURPLE + "AutoBot" + module + ChatFormatting.GREEN + "] " + ChatFormatting.WHITE + status;
	}
	
	public void addToStatus(String status, int index) {
		addToStatus(status, index, name);
	}
	
	public static void addToStatus(String status, int index, String module) {
    	if (!module.isEmpty()) {
    		module = "-" + module;
    	}
    	
    	Renderer.status[index] = ChatFormatting.WHITE + status;
	}
	
	public static void clearStatus() {
		Renderer.status = null;
	}
	
	@SuppressWarnings("deprecation")
	public static void suspend(Thread thread) {
		if (thread != null) thread.suspend();
	}
	
	public static Block getBlock(BlockPos pos) {
		try {
			return mc.world.getBlockState(pos).getBlock();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static boolean isSolid(BlockPos pos) {
		try {
			return mc.world.getBlockState(pos).getMaterial().isSolid();
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public static BlockPos getPlayerPos() {
		return new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
	}
	
	/**
	 * Checks if the player has the given potion effect like "regeneration"
	 */
	public static boolean isPotionActive(String name, EntityPlayer player) {
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (effect.getEffectName().contains(name.toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String[] addToArray(String[] myArray, String newItem) {
		int currentSize = myArray.length;
		int newSize = currentSize + 1;
		String[] tempArray = new String[ newSize ];
		for (int i = 0; i < currentSize; i++) {
		    tempArray[i] = myArray [i];
		}
		tempArray[newSize- 1] = newItem;
		
		return tempArray;
	}
	
    public static void sleep(int ms) {
    	try {
    		Thread.sleep(ms);
    	} catch (Exception ignored) {
    		
    	}
    }
    
	public static void sleepUntil(BooleanSupplier condition, int timeout) {
		long startTime = System.currentTimeMillis();
		while(true) {
			if (condition.getAsBoolean()) {
				break;
			} else if (timeout != -1 && System.currentTimeMillis() - startTime >= timeout) {
				break;
			}
			
			sleep(10);
		}
	}
	
	public static void sleepUntil(BooleanSupplier condition, int timeout, int amountToSleep) {
		long startTime = System.currentTimeMillis();
		while(true) {
			if (condition.getAsBoolean()) {
				break;
			} else if (timeout != -1 && System.currentTimeMillis() - startTime >= timeout) {
				break;
			}
			
			sleep(amountToSleep);
		}
	}
	
	//Send a help message telling the prefix and stuff if the settings file doesnt exist which would mean the person is using the mod for the first time
	public static class HelpMessage {
		boolean check = false;
		
		@SubscribeEvent
		public void onTick(ClientTickEvent e) {
	    	if (!check && mc.player != null) {
	    		if (!Settings.settings.exists()) {
	    			new AutoBot().sendMessage("Welcome to " + ChatFormatting.GREEN + NAME + ChatFormatting.WHITE + " version " + ChatFormatting.GREEN + VERSION, false);
	    			new AutoBot().sendMessage("You can open the GUI by typing " + ChatFormatting.GREEN + Commands.prefix + "gui" + ChatFormatting.WHITE + " on chat", false);
	    			Settings.saveSettings();
	    		}
	    		
	    		check = true;
	    		MinecraftForge.EVENT_BUS.unregister(this);
	    	}
		}
	}
}
