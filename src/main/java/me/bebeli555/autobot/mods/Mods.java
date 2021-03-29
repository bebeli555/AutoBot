package me.bebeli555.autobot.mods;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.bebeli555.autobot.gui.Group;
import me.bebeli555.autobot.gui.Settings;
import me.bebeli555.autobot.rendering.Renderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.Random;
import java.util.function.BooleanSupplier;

public class Mods {

    public static final Minecraft mc = Minecraft.getMinecraft();

    public String name = getAnnotation().displayName();
    public String[] description = getAnnotation().description();
    public Group group = getAnnotation().group();
    public boolean toggled;
    public boolean disableToggleMessage = false;
    public boolean hidden = getAnnotation().hidden();


    public RegisterMod getAnnotation() {
        if (this.getClass().isAnnotationPresent(RegisterMod.class)) {
            return getClass().getAnnotation(RegisterMod.class);
        }
        throw new IllegalStateException("Annotation does not exist in " + getClass().getCanonicalName() + ", this should not happen, this is a bug");
    }

    public void onEnabled() {
    }

    public void onDisabled() {
    }

    public void onPostInit() {
    }

    public void toggleModule() {
        Settings.getGuiNodeFromId(name).click();
    }

    public boolean isToggled() {
        return toggled;
    }

    @SuppressWarnings("deprecation")
    public static void suspend(Thread thread) {
        if (thread != null) thread.suspend();
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {

        }
    }

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

    public static boolean isSolid(BlockPos pos) {
        try {
            return mc.world.getBlockState(pos).getMaterial().isSolid();
        } catch (NullPointerException e) {
            return false;
        }
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

    public static void clearStatus() {
        Renderer.status = null;
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

    /**
     * 1 = 50% change and so on
     */
    public boolean random(int i) {
        return new Random().nextInt(i + 1) == 0;
    }

    public static Block getBlock(BlockPos pos) {
        try {
            return mc.world.getBlockState(pos).getBlock();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void onGuiDrawScreen(int mousex, int mousey, float partialTicks) {

    }

    public void onGuiClick(int x, int y, int button) {

    }

    public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent.Post event) {

    }
}
