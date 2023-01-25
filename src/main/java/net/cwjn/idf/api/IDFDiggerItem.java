package net.cwjn.idf.api;

import com.google.common.collect.ImmutableMultimap;
import net.cwjn.idf.config.json.data.ItemData;
import net.cwjn.idf.config.json.data.WeaponData;
import net.cwjn.idf.util.ItemInterface;
import net.cwjn.idf.util.Util;
import net.cwjn.idf.config.json.JSONHandler;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class IDFDiggerItem extends DiggerItem implements IDFCustomEquipment {

    private final double physicalDamage, fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage;

    public IDFDiggerItem(Tier tier, int durability, String damageClass, double physicalDamage, double fireDamage, double waterDamage, double lightningDamage,
                        double magicDamage, double darkDamage, double lifesteal, double pen, double crit, double force, double knockback, double speed, Properties p, Map<Attribute, AttributeModifier> bonusAttributes,
                         TagKey<Block> tag) {
        this(tier, durability, damageClass, physicalDamage, fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage,
                lifesteal, pen, crit, force, knockback, speed, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, p, bonusAttributes, tag);
    }

    public IDFDiggerItem(Tier tier, int durability, String damageClass, double physicalDamage, double fireDamage,
                         double waterDamage, double lightningDamage, double magicDamage, double darkDamage,
                         double lifesteal, double armourPenetration, double criticalChance, double force, double knockback,
                         double attackSpeed, double defense, double physicalResistance, double fireResistance,
                         double waterResistance, double lightningResistance, double magicResistance,
                         double darkResistance, double evasion, double maxHP, double movespeed,
                         double knockbackResistance, double luck, double strikeMultiplier, double pierceMultiplier,
                         double slashMultiplier,
                         Properties p, Map<Attribute, AttributeModifier> bonusAttributes, TagKey<Block> tag) {
        super((float) physicalDamage, (float) attackSpeed, tier, tag, p);
        this.physicalDamage = physicalDamage;
        this.fireDamage = fireDamage;
        this.waterDamage = waterDamage;
        this.lightningDamage = lightningDamage;
        this.magicDamage = magicDamage;
        this.darkDamage = darkDamage;
        ((ItemInterface) this).setDamageClass(damageClass);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        WeaponData data = new WeaponData(durability, damageClass, physicalDamage, fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage,
                        lifesteal, armourPenetration, criticalChance, force, knockback, attackSpeed, defense, physicalResistance, fireResistance,
                        waterResistance, lightningResistance, magicResistance, darkResistance, evasion, maxHP, movespeed, knockbackResistance,
                        luck, strikeMultiplier, pierceMultiplier, slashMultiplier);
        if (tier instanceof IDFTier modTier) {
            data = WeaponData.combine(data,
                    new WeaponData(0, "strike", modTier.getAttackDamageBonus(), modTier.getFireDamage(), modTier.getWaterDamage(),
                            modTier.getLightningDamage(), modTier.getMagicDamage(), modTier.getDarkDamage(), modTier.getLifesteal(), modTier.getArmourPenetration(),
                            modTier.getCriticalChance(), modTier.getForce(), modTier.getKnockback(), modTier.getSpeed(), modTier.getDefense(), modTier.getPhysicalResistance(),
                            modTier.getFireResistance(), modTier.getWaterResistance(), modTier.getLightningResistance(), modTier.getMagicResistance(), modTier.getDarkResistance(),
                            modTier.getEvasion(), modTier.getMaxHP(), modTier.getMovespeed(), modTier.getKnockbackResistance(), modTier.getLuck(), modTier.getStrikeMultiplier(),
                            modTier.getPierceMultiplier(), modTier.getSlashMultiplier()));
            bonusAttributes.putAll(modTier.getBonusAttributes());
        }
        data.forEach(pair -> {
            if (pair.getB() != 0) {
                builder.put(pair.getA(), new AttributeModifier(Util.UUID_BASE_STAT_ADDITION[0], "data0", pair.getB(), ADDITION));
            }
        });
        for (Map.Entry<Attribute, AttributeModifier> entry : bonusAttributes.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        this.defaultModifiers = builder.build();
    }

    @Override
    public float getAttackDamage() {
        return (float) (physicalDamage + fireDamage + waterDamage + lightningDamage + magicDamage + darkDamage);
    }

    public double getFireDamage() {
        return fireDamage;
    }

    public double getWaterDamage() {
        return waterDamage;
    }

    public double getLightningDamage() {
        return lightningDamage;
    }

    public double getMagicDamage() {
        return magicDamage;
    }

    public double getDarkDamage() {
        return darkDamage;
    }

}
