package me.bebeli555.autobot.mods;

import me.bebeli555.autobot.gui.Group;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterMod {

    String displayName();

    String[] description() default "no description";

    Group group();

    boolean hidden() default false;

}
