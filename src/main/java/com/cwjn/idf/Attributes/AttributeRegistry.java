package com.cwjn.idf.Attributes;

import com.cwjn.idf.ImprovedDamageFramework;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class AttributeRegistry {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ImprovedDamageFramework.MOD_ID);

    public static final RegistryObject<Attribute> FIRE_DAMAGE = register("idf.fire_damage", () -> new RangedAttribute("idf.attribute.fire_damage", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> WATER_DAMAGE = register("idf.water_damage", () -> new RangedAttribute("idf.attribute.water_damage", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> LIGHTNING_DAMAGE = register("idf.lightning_damage", () -> new RangedAttribute("idf.attribute.lightning_damage", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> MAGIC_DAMAGE = register("idf.magic_damage", () -> new RangedAttribute("idf.attribute.magic_damage", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> DARK_DAMAGE = register("idf.dark_damage", () -> new RangedAttribute("idf.attribute.dark_damage", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));

    public static final RegistryObject<Attribute> FIRE_RESISTANCE = register("idf.fire_resistance", () -> new RangedAttribute("idf.attribute.fire_resistance", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> WATER_RESISTANCE = register("idf.water_resistance", () -> new RangedAttribute("idf.attribute.water_resistance", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> LIGHTNING_RESISTANCE = register("idf.lightning_resistance", () -> new RangedAttribute("idf.attribute.lightning_resistance", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> MAGIC_RESISTANCE = register("idf.magic_resistance", () -> new RangedAttribute("idf.attribute.magic_resistance", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));
    public static final RegistryObject<Attribute> DARK_RESISTANCE = register("idf.dark_resistance", () -> new RangedAttribute("idf.attribute.dark_resistance", 0.0D, 0.0D, Double.MAX_VALUE).setSyncable(true));

    private static <T extends Attribute> RegistryObject<T> register(final String name, final Supplier<T> attribute) {
        return ATTRIBUTES.register(name, attribute);
    }

}
