package net.cwjn.idf.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CommonConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_SOURCES_REDUCED_INVULN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNDODGABLE_SOURCES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_SOURCES_NO_INVULN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ENTITIES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> LEGENDARY_TOOLTIPS_COMPAT_MODE;
    private static final String[] defaultNoInvulnList = {"player", "fall"};
    private static final String[] undodgableList = {"inFire", "onFire", "lava", "hotFloor", "inWall",
            "cramming", "drown", "starve", "fall", "flyIntoWall", "outOfWorld", "wither", "dryout", "freeze", "bleed_effect", "blood_cauldron", "heartstop"};
    private static final String[] defaultReducedList = {"mob", "sting"};
    private static final String[] defaultBlacklist = {"minecraft:slime", "minecraft:magma_cube"};

    static {

        BUILDER.push("Common Config");

        LEGENDARY_TOOLTIPS_COMPAT_MODE = BUILDER.comment("Enable compatibility with Legendary Tooltips. If Legendary Tooltips is not installed, this will do nothing.")
                .define("Tooltips Compat Enabled", true);

        WHITELISTED_SOURCES_REDUCED_INVULN = BUILDER.comment("Damage Sources that will make the target get half the regular i-frames. Takes DamageSource object's msgId field. If you don't know what that is, don't touch this.")
                .defineList("Whitelisted reduced invulnerability sources", Arrays.asList(defaultReducedList), s -> s instanceof String);

        UNDODGABLE_SOURCES = BUILDER.comment("Damage source that are undodgable.")
                .defineList("Undodgable sources", Arrays.asList(undodgableList), s -> s instanceof String);

        WHITELISTED_SOURCES_NO_INVULN = BUILDER.comment("Damage sources that will not give i-frames to the target. Takes DamageSource object's msgId field.")
                .defineList("Whitelisted no invulnerability sources", Arrays.asList(defaultNoInvulnList), s -> s instanceof String);

        BLACKLISTED_ENTITIES = BUILDER.comment("Entities that are blacklisted from the mob damage source. Will not do anything if 'mob' is not included in Whitelisted sources. Takes the registry name of a mob.")
                .defineList("Blacklisted mobs", Arrays.asList(defaultBlacklist), s -> s instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();

    }

}
