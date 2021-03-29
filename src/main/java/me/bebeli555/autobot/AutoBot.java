package me.bebeli555.autobot;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.bebeli555.autobot.gui.*;
import me.bebeli555.autobot.mods.Mods;
import me.bebeli555.autobot.mods.bots.crystalpvpbot.CrystalPvPBot;
import me.bebeli555.autobot.mods.bots.elytrabot.ElytraBot;
import me.bebeli555.autobot.mods.bots.obbybuilderbot.ObbyBuilderBot;
import me.bebeli555.autobot.mods.games.Snake;
import me.bebeli555.autobot.mods.games.Tetris;
import me.bebeli555.autobot.mods.other.*;
import me.bebeli555.autobot.rendering.Renderer;
import me.bebeli555.autobot.utils.EatingUtil;
import me.bebeli555.autobot.utils.MessageUtil;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.BooleanSupplier;

@Mod(modid = AutoBot.MODID, name = AutoBot.NAME, version = AutoBot.VERSION)
public class AutoBot {

    public static final String MODID = "autobot";
    public static final String NAME = "AutoBot";
    public static final String VERSION = "1.02";
    public static final String DISCORD = "discord.gg/xSukBcyd8m";

    public static Minecraft mc = Minecraft.getMinecraft();
    public static final EventBus EVENT_BUS = new EventManager();

    public String name = "";
    public String[] description;
    public Group group;
    public boolean toggled, disableToggleMessage;
    public static ArrayList<Mods> modules = new ArrayList<>();

    @EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManagerInit();
    }

    public void moduleManagerInit() {
        // im quite sure doing init on a separate thread will cause some problems
        new Thread(() -> {
            //Initialize classes
            MinecraftForge.EVENT_BUS.register(new HelpMessage());
            MinecraftForge.EVENT_BUS.register(new Commands());
            MinecraftForge.EVENT_BUS.register(new Renderer());
            MinecraftForge.EVENT_BUS.register(new Keybind());
            MinecraftForge.EVENT_BUS.register(new EatingUtil());

            modules.add(new CrystalPvPBot());
            modules.add(new ElytraBot());
            modules.add(new ObbyBuilderBot());

            modules.add(new GuiSettings());
            //modules.add(new AutoWither());
            modules.add(new AutoFirework());
            modules.add(new AutoEnderChestMiner());
            modules.add(new AutoBuilder());
            modules.add(new AutoMend());
            modules.add(new AutoInventoryManager());
            modules.add(new AutoTrap());
            modules.add(new Burrow());

            modules.add(new Snake());
            modules.add(new Tetris());


            //Initialize stuff
            new File(Settings.path).mkdir();
            SetGuiNodes.setGuiNodes();
            Settings.loadSettings();
            Keybind.setKeybinds();

            for (Mods module : modules) {
                module.onPostInit();
            }
        }).start();
    }

    public static void toggleMod(String name, boolean on) {
        GuiNode node = Settings.getGuiNodeFromId(name);

        node.toggled = on;
        node.setSetting();

        for (Mods module : modules) {
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

    public static String[] addToArray(String[] myArray, String newItem) {
        int currentSize = myArray.length;
        int newSize = currentSize + 1;
        String[] tempArray = new String[newSize];
        System.arraycopy(myArray, 0, tempArray, 0, currentSize);
        tempArray[newSize - 1] = newItem;

        return tempArray;
    }

    @Deprecated
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {

        }
    }

    @Deprecated
    public static void sleepUntil(BooleanSupplier condition, int timeout) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (condition.getAsBoolean()) {
                break;
            } else if (timeout != -1 && System.currentTimeMillis() - startTime >= timeout) {
                break;
            }

            sleep(10);
        }
    }

    @Deprecated
    public static void sleepUntil(BooleanSupplier condition, int timeout, int amountToSleep) {
        long startTime = System.currentTimeMillis();
        while (true) {
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
                    MessageUtil.sendChatMessage("Welcome to " + ChatFormatting.GREEN + NAME + ChatFormatting.WHITE + " version " + ChatFormatting.GREEN + VERSION);
                    MessageUtil.sendChatMessage("You can open the GUI by typing " + ChatFormatting.GREEN + Commands.prefix + "gui" + ChatFormatting.WHITE + " on chat");
                    Settings.saveSettings();
                }

                check = true;
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }
}
