package com.cwjn.idf.Config;

import com.cwjn.idf.Network.IDFPackerHandler;
import com.cwjn.idf.Network.SendServerDamageJsonMessage;
import com.cwjn.idf.Network.SendServerResistanceJsonMessage;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.world.entity.ai.attributes.Attributes.ARMOR;
import static net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;

@Mod.EventBusSubscriber
public class JSONHandler {

    public static final Map<ResourceLocation, EntityData> entityMap = new HashMap<>();
    public static Map<ResourceLocation, ResistanceData> resistanceMap = new HashMap<>();
    public static Map<ResourceLocation, DamageData> damageMap = new HashMap<>();
    public static final Map<ResourceLocation, ResistanceData> serverResistanceMap = new HashMap<>();
    public static final Map<ResourceLocation, DamageData> serverDamageDataMap = new HashMap<>();

    public static void init(File configDir) {
        Map<String, EntityData> defaultEntityData = Maps.newHashMap();
        Map<String, ResistanceData> defaultResistanceData = Maps.newHashMap();
        Map<String, DamageData> defaultDamageData = Maps.newHashMap();
        //TODO: iron golems, snow golems, villagers not included in this list.
        for (EntityType<?> entityType : ForgeRegistries.ENTITIES.getValues()) {
            MobCategory type = entityType.getCategory();
            if (type != MobCategory.MISC) { //make sure this isnt an arrow entity or something
                defaultEntityData.put(entityType.getRegistryName().toString(), new EntityData(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, "strike", 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D));
            }
        }
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            Collection<AttributeModifier> armour0 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.HEAD).get(ARMOR);
            Collection<AttributeModifier> armour1 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.FEET).get(ARMOR);
            Collection<AttributeModifier> armour2 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.LEGS).get(ARMOR);
            Collection<AttributeModifier> armour3 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.CHEST).get(ARMOR);
            double armorVal = armour0.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                              armour1.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                              armour2.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                              armour3.stream().mapToDouble(AttributeModifier::getAmount).sum();
            if (armorVal > 0) {
                defaultResistanceData.put(item.getRegistryName().toString(), new ResistanceData(0, 0, 0, 0 ,0));
            }
        }
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            Collection<AttributeModifier> weapon0 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.MAINHAND).get(ATTACK_DAMAGE);
            Collection<AttributeModifier> weapon1 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.OFFHAND).get(ATTACK_DAMAGE);
            double damageVal = weapon0.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                    weapon1.stream().mapToDouble(AttributeModifier::getAmount).sum();
            if (damageVal > 0 || item instanceof BowItem || item instanceof CrossbowItem) {
                defaultDamageData.put(item.getRegistryName().toString(), new DamageData(0, 0, 0, 0, 0, "strike"));
            }
        }
        entityMap.clear();
        resistanceMap.clear();
        damageMap.clear();
        Map<String, EntityData> tempEntityMap = JSONUtil.getOrCreateConfigFile(configDir, "entity_data.json", defaultEntityData, new TypeToken<Map<String, EntityData>>() {
        }.getType());
        Map<String, ResistanceData> tempResistanceMap = JSONUtil.getOrCreateConfigFile(configDir, "resistance_data.json", defaultResistanceData, new TypeToken<Map<String, ResistanceData>>() {
        }.getType());
        Map<String, DamageData> tempDamageMap = JSONUtil.getOrCreateConfigFile(configDir, "damage_data.json", defaultDamageData, new TypeToken<Map<String, DamageData>>() {
        }.getType());
        if (tempEntityMap != null && !tempEntityMap.isEmpty()) {
            for (Map.Entry<String, EntityData> entry : tempEntityMap.entrySet()) {
                entityMap.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
        if (tempResistanceMap != null && !tempResistanceMap.isEmpty()) {
            for (Map.Entry<String, ResistanceData> entry : tempResistanceMap.entrySet()) {
                resistanceMap.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
        if (tempDamageMap != null && !tempDamageMap.isEmpty()) {
            for (Map.Entry<String, DamageData> entry : tempDamageMap.entrySet()) {
                damageMap.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
    }

    public static void serverInit(File configDir) {
        Map<String, ResistanceData> defaultResistanceData = Maps.newHashMap();
        Map<String, DamageData> defaultDamageData = Maps.newHashMap();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            Collection<AttributeModifier> armour0 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.HEAD).get(ARMOR);
            Collection<AttributeModifier> armour1 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.FEET).get(ARMOR);
            Collection<AttributeModifier> armour2 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.LEGS).get(ARMOR);
            Collection<AttributeModifier> armour3 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.CHEST).get(ARMOR);
            double armorVal = armour0.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                    armour1.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                    armour2.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                    armour3.stream().mapToDouble(AttributeModifier::getAmount).sum();
            if (armorVal > 0) {
                defaultResistanceData.put(item.getRegistryName().toString(), new ResistanceData(0, 0, 0, 0 ,0));
            }
        }
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            Collection<AttributeModifier> weapon0 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.MAINHAND).get(ATTACK_DAMAGE);
            Collection<AttributeModifier> weapon1 = item.getDefaultInstance().getAttributeModifiers(EquipmentSlot.OFFHAND).get(ATTACK_DAMAGE);
            double damageVal = weapon0.stream().mapToDouble(AttributeModifier::getAmount).sum() +
                    weapon1.stream().mapToDouble(AttributeModifier::getAmount).sum();
            if (damageVal > 0 || item instanceof BowItem || item instanceof CrossbowItem) {
                defaultDamageData.put(item.getRegistryName().toString(), new DamageData(0, 0, 0, 0, 0, "strike"));
            }
        }
        serverResistanceMap.clear();
        serverDamageDataMap.clear();
        Map<String, ResistanceData> tempResistanceMap = JSONUtil.getOrCreateConfigFile(configDir, "resistance_data.json", defaultResistanceData, new TypeToken<Map<String, ResistanceData>>() {
        }.getType());
        Map<String, DamageData> tempDamageMap = JSONUtil.getOrCreateConfigFile(configDir, "damage_data.json", defaultDamageData, new TypeToken<Map<String, DamageData>>() {
        }.getType());
        if (tempResistanceMap != null && !tempResistanceMap.isEmpty()) {
            for (Map.Entry<String, ResistanceData> entry : tempResistanceMap.entrySet()) {
                serverResistanceMap.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
        if (tempDamageMap != null && !tempDamageMap.isEmpty()) {
            for (Map.Entry<String, DamageData> entry : tempDamageMap.entrySet()) {
                serverDamageDataMap.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent
    public static void playerJoinWorldEvent(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        if (!serverResistanceMap.isEmpty()) {
            IDFPackerHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SendServerResistanceJsonMessage(serverResistanceMap));
            System.out.println("Sending server resistance data values to player: " + player.getScoreboardName());
        }
        if (!serverDamageDataMap.isEmpty()) {
            IDFPackerHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SendServerDamageJsonMessage(serverDamageDataMap));
            System.out.println("Sending server damage data values to player: " + player.getScoreboardName());
        }
    }

    public static EntityData getEntityData(ResourceLocation key) {
        if (entityMap.containsKey(key)) return entityMap.get(key);
        else return null;
    }

    public static DamageData getDamageData(ResourceLocation key) {
        if (damageMap.containsKey(key)) return damageMap.get(key);
        else return null;
    }

    public static ResistanceData getResistanceData(ResourceLocation key) {
        if (resistanceMap.containsKey(key)) return resistanceMap.get(key);
        else return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateClientResistanceData(Map<ResourceLocation, ResistanceData> map) {
        resistanceMap = map;
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateClientDamageData(Map<ResourceLocation, DamageData> map) {
        damageMap = map;
    }


}
