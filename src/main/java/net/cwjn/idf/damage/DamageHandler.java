package net.cwjn.idf.damage;

import net.cwjn.idf.api.event.LivingLifestealEvent;
import net.cwjn.idf.api.event.PostMitigationDamageEvent;
import net.cwjn.idf.api.event.PreDamageMultipliersEvent;
import net.cwjn.idf.api.event.PreMitigationDamageEvent;
import net.cwjn.idf.attribute.IDFAttributes;
import net.cwjn.idf.config.json.records.SourceCatcherData;
import net.cwjn.idf.util.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static net.cwjn.idf.attribute.IDFElement.*;
import static net.cwjn.idf.config.CommonConfig.MAX_FORCE_WEIGHT_MULTIPLIER;
import static net.cwjn.idf.config.CommonConfig.MIN_FORCE_WEIGHT_MULTIPLIER;

public class DamageHandler {

    public static final float DEFAULT_KNOCKBACK = 0.4f;
    public static final float DEFAULT_ATTACK_SPEED = 4f;

    public static float handleDamage(LivingEntity target, DamageSource source, float amount) {

        //Integrate the source to an IDFSource
        IDFInterface convertedSource = SourceCatcherData.convert(source);

        //create variables to hold the damage, damage class, pen, and lifesteal. damage is flat numbers, pen and lifesteal are % values ranging from 0-100.
        float fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage, holyDamage, physicalDamage, pen, lifesteal, knockback;
        String damageClass;

        //if the source is a conversion from a vanilla source, we take the physical amount provided and spread it across all damage types
        //according the ratios given. This means the values of all the damage types should be < 1.0, and any leftovers should remain as
        //physical damage. Otherwise, the source is a regular instance of IDFDamage, so we don't have to convert anything. The values given here
        //should be flat values we want to use directly. Amount just corresponds to the physical damage.
        if (convertedSource.isConversion()) {
            fireDamage = convertedSource.getFire() * amount;
            waterDamage = convertedSource.getWater() * amount;
            lightningDamage = convertedSource.getLightning() * amount;
            magicDamage = convertedSource.getMagic() * amount;
            darkDamage = convertedSource.getDark() * amount;
            holyDamage = convertedSource.getHoly() * amount;
            physicalDamage = amount - (fireDamage + waterDamage + lightningDamage + magicDamage + darkDamage + holyDamage);
        } else {
            fireDamage = convertedSource.getFire();
            waterDamage = convertedSource.getWater();
            lightningDamage = convertedSource.getLightning();
            magicDamage = convertedSource.getMagic();
            darkDamage = convertedSource.getDark();
            holyDamage = convertedSource.getHoly();
            physicalDamage = amount;
        }

        //run our numbers through the pre-multiplier event. Use the armour formula to convert elemental armour values to resistances.
        PreDamageMultipliersEvent event = new PreDamageMultipliersEvent(target, fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage, holyDamage, physicalDamage,
                convertedSource.getPen(), convertedSource.getLifesteal(), convertedSource.getKnockback(), convertedSource.getForce(),
                (float) armourFormula(target.getAttributeValue(FIRE.defence)), (float) armourFormula(target.getAttributeValue(WATER.defence)),
                (float) armourFormula(target.getAttributeValue(LIGHTNING.defence)), (float) armourFormula(target.getAttributeValue(MAGIC.defence)),
                (float) armourFormula(target.getAttributeValue(DARK.defence)), (float) armourFormula(target.getAttributeValue(HOLY.defence)),
                (float) armourFormula(target.getAttributeValue(Attributes.ARMOR)),
                (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS), convertedSource.getDamageClass());
        MinecraftForge.EVENT_BUS.post(event);

        //grab the values from the event after it's been fired. We put the damage and resistance values into arrays, so they can be looped through.
        //this is the only place we use force and force, so we can just calculate the ratio here.
        float[] dv = {event.getFireDmg(), event.getWaterDmg(), event.getLightningDmg(), event.getMagicDmg(), event.getDarkDmg(), event.getHolyDmg(), event.getPhysicalDmg()};
        pen = event.getPen();
        lifesteal = event.getLifesteal();
        damageClass = event.getDamageClass();
        double weightMultiplier = event.getForce() < 0 ? 1 : Mth.clamp(Util.fastSqrt(event.getForce())/Util.fastSqrt(event.getWeight() <= 0 ? 1 : event.getWeight()), MIN_FORCE_WEIGHT_MULTIPLIER.get(), MAX_FORCE_WEIGHT_MULTIPLIER.get());
        knockback = event.getKnockback();
        float[] rv = {event.getFireDef(), event.getWaterDef(), event.getLightningDef(), event.getMagicDef(), event.getDarkDef(), event.getHolyDef(), event.getPhysicalDef()};

        //now we can knockback the target based on the weightMultiplier. The first code is copied from the
        //vanilla knockback handler to get the direction of the knockback. We add the bonus knockback value from
        //the attack afterwards.
        if (source.getEntity() != null) {
            double d1 = source.getEntity().getX() - target.getX();
            double d0;
            for (d0 = source.getEntity().getZ() - target.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                d1 = (Math.random() - Math.random()) * 0.01D;
            }
            target.hurtDir = (float)(Mth.atan2(d0, d1) * (double)(180F / (float)Math.PI) - (double)target.getYRot());
            target.knockback(knockback*Mth.clamp(weightMultiplier, 0.5, 1.5), d1, d0);
        }

        //we put the damage class multipliers into a map with the strings as keys. This is to easily apply the correct multiplier
        //to all the damage types. Then iterate through the damage values and apply the force/force multiplier followed by
        //the damage class multiplier. Then multiply each damage value by the correct multiplier.
        Map<String, Double> mappedMultipliers = new HashMap<>(2);
        mappedMultipliers.put("strike", target.getAttributeValue(IDFAttributes.STRIKE_MULT.get()));
        mappedMultipliers.put("pierce", target.getAttributeValue(IDFAttributes.PIERCE_MULT.get()));
        mappedMultipliers.put("slash", target.getAttributeValue(IDFAttributes.SLASH_MULT.get()));
        mappedMultipliers.put("none", 1D);
        for (int i = 0; i < 7; i++) {
            dv[i] *= weightMultiplier;
            dv[i] *= mappedMultipliers.get(damageClass);
        }

        //now that we have the final pre-mitigation values, check if the source is true damage. If it is,
        //there's no need to do the rest of the method.
        if (convertedSource.isTrue()) return sum(dv);

        //run the PreMitigation event so other mods can activate effects if they want
        PreMitigationDamageEvent event2 = new PreMitigationDamageEvent(target, dv[0], dv[1], dv[2], dv[3], dv[4], dv[5], dv[6],
                pen, lifesteal, rv[0], rv[1], rv[2], rv[3], rv[4], rv[5], rv[6]);
        MinecraftForge.EVENT_BUS.post(event2);
        dv = new float[]{event2.getFireDmg(), event2.getWaterDmg(), event2.getLightningDmg(), event2.getMagicDmg(), event2.getDarkDmg(), event2.getHolyDmg(), event2.getPhysicalDmg()};
        pen = event2.getPen();
        lifesteal = event2.getLifesteal();
        rv = new float[]{event2.getFireDef(), event2.getWaterDef(), event2.getLightningDef(), event2.getMagicDef(), event2.getDarkDef(), event2.getHolyDef(), event2.getPhysicalDef()};

        //now we can factor in fall protection, blast protection, fire protection and projectile protection. Reduce each damage type by 6.25% per
        //level of blast and projectile. Assuming the highest any player could get is 16 (prot 4 on each armour piece),
        //this means having maxed out of either makes you take 100% reduced damage from these sources.
        if (source.isFall()) {
            double fallLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                fallLevel += item.getEnchantmentLevel(Enchantments.FALL_PROTECTION);
            }
            if (fallLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (fallLevel * 0.0625));
                }
            }
        }
        if (source.isProjectile()) {
            double projectileLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                projectileLevel += item.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION);
            }
            if (projectileLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (projectileLevel * 0.0625));
                }
            }
        }
        if (source.isExplosion()) {
            double blastLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                blastLevel += item.getEnchantmentLevel(Enchantments.BLAST_PROTECTION);
            }
            if (blastLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (blastLevel * 0.0625));
                }
            }
        }
        if (source.isFire()) {
            double fireLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                fireLevel += item.getEnchantmentLevel(Enchantments.FIRE_PROTECTION);
            }
            if (fireLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (fireLevel * 0.0625));
                }
            }
        }
        //now we calculate for general protection enchantment. We want this to be weaker than specific protection enchants,
        //so lets make it reduce damage taken by 1.875% per level, capping out at 30% at full prot 4 armour.
        double protLevel = 0;
        for (ItemStack item: target.getArmorSlots()) {
            protLevel += item.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION);
        }
        if (protLevel > 0) {
            for (int i = 0; i < 7; i++) {
                dv[i] *= (1 - (protLevel * 0.01875));
            }
        }
        //now we factor in the resistance effect. Increases all resistances by 20% per level.
        if (target.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            double resistanceLevel = ((double)target.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1)*0.2;
            for (int i = 0; i < 7; i++) {
                rv[i] *= (1 + resistanceLevel);
            }
        }
        //hurt the player's armour for the sum of the damage now.
        target.hurtArmor(source, sum(dv));
        //now we start calculating the final damage value. We need to post the PostMitigation event,
        //then, create a variable to store the final damage,
        //then we do the damage math for fire, water, lightning, magic, dark, and holy.
        PostMitigationDamageEvent postMitigation = new PostMitigationDamageEvent(target, 0, 0, 0, 0, 0, 0, 0);
        rv[6] = rv[6] * (1.0f - (pen/100)); //factor in armour pen
        for (int i = 0; i < 7; i++) {
            if (dv[i] > 0) {
                postMitigation.setDamage(
                        i,
                        damageFormula(dv[i], rv[i])
                );
            }
        }
        MinecraftForge.EVENT_BUS.post(postMitigation);
        float returnValue = sum(postMitigation.getDamage());
        //final thing we do is give lifesteal to the attacker, if it was a source of entity damage that was done.
        if (convertedSource instanceof EntityDamageSource) {
            Entity sourceEntity = ((EntityDamageSource) convertedSource).getEntity();
            if (sourceEntity instanceof LivingEntity livingEntity) {
                if (lifesteal != 0) {
                    float percent = lifesteal/100;
                    LivingLifestealEvent lsEvent = new LivingLifestealEvent(livingEntity, target, returnValue * percent, returnValue, percent);
                    MinecraftForge.EVENT_BUS.post(lsEvent);
                    livingEntity.heal(lsEvent.getHealAmount());
                }
            }
        }
        return returnValue;
    }

    public static float handleDamageWithDebug(LivingEntity target, DamageSource source, float amount, Logger log) {

        log.debug("---------------STARTING DEBUGGER---------------");
        log.debug("--> Source: " + source.msgId);
        log.debug("--> Amount: " + amount);
        log.debug("--> Target: " + target.getName());
        if (source.isFall()) log.debug("--> isFall");
        if (source.isFire()) log.debug("--> isFire");
        if (source.isProjectile()) log.debug("--> isProjectile");
        if (source.isExplosion()) log.debug("--> isExplosion");
        if (source.isBypassInvul()) log.debug("--> bypassInvul");
        if (source instanceof IDFInterface) log.debug("--> Source is already an instance of IDFDamage!");
        else log.debug("--> Source is NOT an instance of IDFDamage!");

        //Integrate the source to an IDFSource
        IDFInterface convertedSource = SourceCatcherData.convert(source);

        //create variables to hold the damage, damage class, pen, and lifesteal. damage is flat numbers, pen and lifesteal are % values ranging from 0-100.
        float fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage, holyDamage, physicalDamage, pen, lifesteal, knockback;
        String damageClass;

        //if the source is a conversion from a vanilla source, we take the physical amount provided and spread it across all damage types
        //according the ratios given. This means the values of all the damage types should be < 1.0, and any leftovers should remain as
        //physical damage. Otherwise, the source is a regular instance of IDFDamage, so we don't have to convert anything. The values given here
        //should be flat values we want to use directly. Amount just corresponds to the physical damage.
        if (convertedSource.isConversion()) {
            fireDamage = convertedSource.getFire() * amount;
            waterDamage = convertedSource.getWater() * amount;
            lightningDamage = convertedSource.getLightning() * amount;
            magicDamage = convertedSource.getMagic() * amount;
            darkDamage = convertedSource.getDark() * amount;
            holyDamage = convertedSource.getHoly() * amount;
            physicalDamage = amount - (fireDamage + waterDamage + lightningDamage + magicDamage + darkDamage + holyDamage);
        } else {
            fireDamage = convertedSource.getFire();
            waterDamage = convertedSource.getWater();
            lightningDamage = convertedSource.getLightning();
            magicDamage = convertedSource.getMagic();
            darkDamage = convertedSource.getDark();
            holyDamage = convertedSource.getHoly();
            physicalDamage = amount;
        }

        log.debug("");
        log.debug("-> Converted Source Information:");
        if (convertedSource.isConversion()) log.debug("--> isConversion");
        if (convertedSource.isTrue()) log.debug("--> isTrue");
        log.debug("--> Damage Numbers:");
        log.debug("---> Fire: " + fireDamage);
        log.debug("---> Water: " + waterDamage);
        log.debug("---> Lightning: " + lightningDamage);
        log.debug("---> Magic: " + magicDamage);
        log.debug("---> Dark: " + darkDamage);
        log.debug("---> Holy: " + holyDamage);
        log.debug("---> Physical: " + physicalDamage);
        log.debug("--> Auxiliary Information:");
        log.debug("---> Damage Class: " + convertedSource.getDamageClass());
        log.debug("---> Force: " + convertedSource.getForce());
        log.debug("---> Pen: " + convertedSource.getPen());
        log.debug("---> Lifesteal: " + convertedSource.getLifesteal());
        log.debug("---> Knockback: " + convertedSource.getKnockback());

        //run our numbers through the pre-multiplier event. Use the armour formula to convert elemental armour values to resistances.
        PreDamageMultipliersEvent event = new PreDamageMultipliersEvent(target, fireDamage, waterDamage, lightningDamage, magicDamage, darkDamage, holyDamage, physicalDamage,
                convertedSource.getPen(), convertedSource.getLifesteal(), convertedSource.getKnockback(), convertedSource.getForce(),
                (float) armourFormula(target.getAttributeValue(FIRE.defence)), (float) armourFormula(target.getAttributeValue(WATER.defence)),
                (float) armourFormula(target.getAttributeValue(LIGHTNING.defence)), (float) armourFormula(target.getAttributeValue(MAGIC.defence)),
                (float) armourFormula(target.getAttributeValue(DARK.defence)), (float) armourFormula(target.getAttributeValue(HOLY.defence)),
                (float) armourFormula(target.getAttributeValue(Attributes.ARMOR)),
                (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS), convertedSource.getDamageClass());
        MinecraftForge.EVENT_BUS.post(event);

        //grab the values from the event after it's been fired. We put the damage and resistance values into arrays, so they can be looped through.
        //this is the only place we use force and force, so we can just calculate the ratio here.
        float[] dv = {event.getFireDmg(), event.getWaterDmg(), event.getLightningDmg(), event.getMagicDmg(), event.getDarkDmg(), event.getHolyDmg(), event.getPhysicalDmg()};
        pen = event.getPen();
        lifesteal = event.getLifesteal();
        damageClass = event.getDamageClass();
        double weightMultiplier = event.getForce() < 0 ? 1 : Mth.clamp(Util.fastSqrt(event.getForce())/Util.fastSqrt(event.getWeight() <= 0 ? 1 : event.getWeight()), MIN_FORCE_WEIGHT_MULTIPLIER.get(), MAX_FORCE_WEIGHT_MULTIPLIER.get());
        knockback = event.getKnockback();
        float[] rv = {event.getFireDef(), event.getWaterDef(), event.getLightningDef(), event.getMagicDef(), event.getDarkDef(), event.getHolyDef(), event.getPhysicalDef()};

        log.debug("");
        log.debug("-> Pre Damage Multipliers:");
        log.debug("--> Damage Numbers:");
        log.debug("---> Fire: " + dv[0]);
        log.debug("---> Water: " + dv[1]);
        log.debug("---> Lightning: " + dv[2]);
        log.debug("---> Magic: " + dv[3]);
        log.debug("---> Dark: " + dv[4]);
        log.debug("---> Holy: " + dv[5]);
        log.debug("---> Physical: " + dv[6]);
        log.debug("--> Resistance Numbers:");
        log.debug("---> Fire: " + rv[0]);
        log.debug("---> Water: " + rv[1]);
        log.debug("---> Lightning: " + rv[2]);
        log.debug("---> Magic: " + rv[3]);
        log.debug("---> Dark: " + rv[4]);
        log.debug("---> Holy: " + rv[5]);
        log.debug("---> Physical: " + rv[6]);
        log.debug("--> Auxiliary Information:");
        log.debug("---> Damage Class: " + damageClass);
        log.debug("---> Force: " + event.getForce());
        log.debug("---> Defense: " + event.getWeight());
        log.debug("---> Force/Defense: " + weightMultiplier);
        log.debug("---> Pen: " + pen);
        log.debug("---> Lifesteal: " + lifesteal);
        log.debug("---> Knockback: " + knockback);

        //now we can knockback the target based on the weightMultiplier. The first code is copied from the
        //vanilla knockback handler to get the direction of the knockback. We add the bonus knockback value from
        //the attack afterwards.
        if (source.getEntity() != null) {
            double d1 = source.getEntity().getX() - target.getX();
            double d0;
            for (d0 = source.getEntity().getZ() - target.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                d1 = (Math.random() - Math.random()) * 0.01D;
            }
            target.hurtDir = (float)(Mth.atan2(d0, d1) * (double)(180F / (float)Math.PI) - (double)target.getYRot());
            target.knockback(knockback*weightMultiplier, d1, d0);
        }

        //we put the damage class multipliers into a map with the strings as keys. This is to easily apply the correct multiplier
        //to all the damage types. Then iterate through the damage values and apply the force/force multiplier followed by
        //the damage class multiplier. Then multiply each damage value by the correct multiplier.
        Map<String, Double> mappedMultipliers = new HashMap<>(2);
        mappedMultipliers.put("strike", target.getAttributeValue(IDFAttributes.STRIKE_MULT.get()));
        mappedMultipliers.put("pierce", target.getAttributeValue(IDFAttributes.PIERCE_MULT.get()));
        mappedMultipliers.put("slash", target.getAttributeValue(IDFAttributes.SLASH_MULT.get()));
        mappedMultipliers.put("none", 1D);
        for (int i = 0; i < 7; i++) {
            dv[i] *= weightMultiplier;
            dv[i] *= mappedMultipliers.get(damageClass);
        }

        log.debug("");
        log.debug("-> Post Damage Multipliers");
        log.debug("--> Damage Numbers:");
        log.debug("---> Fire: " + dv[0]);
        log.debug("---> Water: " + dv[1]);
        log.debug("---> Lightning: " + dv[2]);
        log.debug("---> Magic: " + dv[3]);
        log.debug("---> Dark: " + dv[4]);
        log.debug("---> Holy: " + dv[5]);
        log.debug("---> Physical: " + dv[6]);
        log.debug("--> Resistance Numbers:");
        log.debug("---> Fire: " + rv[0]);
        log.debug("---> Water: " + rv[1]);
        log.debug("---> Lightning: " + rv[2]);
        log.debug("---> Magic: " + rv[3]);
        log.debug("---> Dark: " + rv[4]);
        log.debug("---> Holy: " + rv[5]);
        log.debug("---> Physical: " + rv[6]);
        log.debug("--> Auxiliary Information:");
        log.debug("---> Pen: " + pen);
        log.debug("---> Lifesteal: " + lifesteal);

        //now that we have the final pre-mitigation values, check if the source is true damage. If it is,
        //there's no need to do the rest of the method.
        if (convertedSource.isTrue()) return sum(dv);

        //run the PreMitigation event so other mods can activate effects if they want
        PreMitigationDamageEvent event2 = new PreMitigationDamageEvent(target, dv[0], dv[1], dv[2], dv[3], dv[4], dv[5], dv[6],
                pen, lifesteal, rv[0], rv[1], rv[2], rv[3], rv[4], rv[5], rv[6]);
        MinecraftForge.EVENT_BUS.post(event2);
        dv = new float[]{event2.getFireDmg(), event2.getWaterDmg(), event2.getLightningDmg(), event2.getMagicDmg(), event2.getDarkDmg(), event2.getHolyDmg(), event2.getPhysicalDmg()};
        pen = event2.getPen();
        lifesteal = event2.getLifesteal();
        rv = new float[]{event2.getFireDef(), event2.getWaterDef(), event2.getLightningDef(), event2.getMagicDef(), event2.getDarkDef(), event2.getHolyDef(), event2.getPhysicalDef()};

        log.debug("");
        log.debug("-> Pre Enchantments");
        log.debug("--> Damage Numbers:");
        log.debug("---> Fire: " + dv[0]);
        log.debug("---> Water: " + dv[1]);
        log.debug("---> Lightning: " + dv[2]);
        log.debug("---> Magic: " + dv[3]);
        log.debug("---> Dark: " + dv[4]);
        log.debug("---> Holy: " + dv[5]);
        log.debug("---> Physical: " + dv[6]);
        log.debug("--> Resistance Numbers:");
        log.debug("---> Fire: " + rv[0]);
        log.debug("---> Water: " + rv[1]);
        log.debug("---> Lightning: " + rv[2]);
        log.debug("---> Magic: " + rv[3]);
        log.debug("---> Dark: " + rv[4]);
        log.debug("---> Holy: " + rv[5]);
        log.debug("---> Physical: " + rv[6]);
        log.debug("--> Auxiliary Information:");
        log.debug("---> Pen: " + pen);
        log.debug("---> Lifesteal: " + lifesteal);

        //now we can factor in fall protection, blast protection, fire protection and projectile protection. Reduce each damage type by 6.25% per
        //level of blast and projectile. Assuming the highest any player could get is 16 (prot 4 on each armour piece),
        //this means having maxed out of either makes you take 100% reduced damage from these sources.
        if (source.isFall()) {
            double fallLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                fallLevel += item.getEnchantmentLevel(Enchantments.FALL_PROTECTION);
            }
            if (fallLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (fallLevel * 0.0625));
                }
            }
        }
        if (source.isProjectile()) {
            double projectileLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                projectileLevel += item.getEnchantmentLevel(Enchantments.PROJECTILE_PROTECTION);
            }
            if (projectileLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (projectileLevel * 0.0625));
                }
            }
        }
        if (source.isExplosion()) {
            double blastLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                blastLevel += item.getEnchantmentLevel(Enchantments.BLAST_PROTECTION);
            }
            if (blastLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (blastLevel * 0.0625));
                }
            }
        }
        if (source.isFire()) {
            double fireLevel = 0;
            for (ItemStack item : target.getArmorSlots()) {
                fireLevel += item.getEnchantmentLevel(Enchantments.FIRE_PROTECTION);
            }
            if (fireLevel > 0) {
                for (int i = 0; i < 7; i++) {
                    dv[i] *= (1 - (fireLevel * 0.0625));
                }
            }
        }
        //now we calculate for general protection enchantment. We want this to be weaker than specific protection enchants,
        //so lets make it reduce damage taken by 1.875% per level, capping out at 30% at full prot 4 armour.
        double protLevel = 0;
        for (ItemStack item: target.getArmorSlots()) {
            protLevel += item.getEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION);
        }
        if (protLevel > 0) {
            for (int i = 0; i < 7; i++) {
                dv[i] *= (1 - (protLevel * 0.01875));
            }
        }
        //now we factor in the resistance effect. Increases all resistances by 20% per level.
        if (target.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            double resistanceLevel = ((double)target.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1)/5;
            for (int i = 0; i < 7; i++) {
                rv[i] *= (1 + resistanceLevel);
            }
        }
        //hurt the player's armour for the sum of the damage now.
        target.hurtArmor(source, sum(dv));

        log.debug("");
        log.debug("-> Pre Mitigation");
        log.debug("--> Damage Numbers:");
        log.debug("---> Fire: " + dv[0]);
        log.debug("---> Water: " + dv[1]);
        log.debug("---> Lightning: " + dv[2]);
        log.debug("---> Magic: " + dv[3]);
        log.debug("---> Dark: " + dv[4]);
        log.debug("---> Holy: " + dv[5]);
        log.debug("---> Physical: " + dv[6]);
        log.debug("--> Resistance Numbers:");
        log.debug("---> Fire: " + rv[0]);
        log.debug("---> Water: " + rv[1]);
        log.debug("---> Lightning: " + rv[2]);
        log.debug("---> Magic: " + rv[3]);
        log.debug("---> Dark: " + rv[4]);
        log.debug("---> Holy: " + rv[5]);
        log.debug("---> Physical: " + rv[6]);
        log.debug("--> Auxiliary Information:");
        log.debug("---> Pen: " + pen);
        log.debug("---> Lifesteal: " + lifesteal);

        //now we start calculating the final damage value. We need to post the PostMitigation event,
        //then, create a variable to store the final damage,
        //then we do the damage math for fire, water, lightning, magic, dark, and holy.
        PostMitigationDamageEvent postMitigation = new PostMitigationDamageEvent(target, 0, 0, 0, 0, 0, 0, 0);
        rv[6] = rv[6] * (1.0f - (pen/100)); //factor in armour pen
        for (int i = 0; i < 7; i++) {
            if (dv[i] > 0) {
                postMitigation.setDamage(
                        i,
                        damageFormula(dv[i], rv[i])
                );
            }
        }
        MinecraftForge.EVENT_BUS.post(postMitigation);

        log.debug("");
        log.debug("-> Post Mitigation");
        log.debug("--> Damage Numbers:");
        log.debug("---> Fire: " + postMitigation.getFire());
        log.debug("---> Water: " + postMitigation.getWater());
        log.debug("---> Lightning: " + postMitigation.getLightning());
        log.debug("---> Magic: " + postMitigation.getMagic());
        log.debug("---> Dark: " + postMitigation.getDark());
        log.debug("---> Holy: " + postMitigation.getHoly());
        log.debug("---> Physical: " + postMitigation.getPhysical());
        log.debug("--> Auxiliary Information:");
        log.debug("---> Lifesteal: " + lifesteal);

        float returnValue = sum(postMitigation.getDamage());
        //final thing we do is give lifesteal to the attacker, if it was a source of entity damage that was done.
        if (convertedSource instanceof EntityDamageSource) {
            Entity sourceEntity = ((EntityDamageSource) convertedSource).getEntity();
            if (sourceEntity instanceof LivingEntity livingEntity) {
                if (lifesteal != 0) {
                    float percent = lifesteal/100;
                    LivingLifestealEvent lsEvent = new LivingLifestealEvent(livingEntity, target, returnValue * percent, returnValue, percent);
                    MinecraftForge.EVENT_BUS.post(lsEvent);
                    livingEntity.heal(lsEvent.getHealAmount());
                    log.debug("");
                    log.debug("-> Lifesteal healed entity for " + lsEvent.getHealAmount());
                }
            }
        }
        return returnValue;
    }

    private static float sum(float[] a) {
        float returnFloat = 0;
        for (float f : a) {
            returnFloat+=f;
        }
        return returnFloat;
    }

    public static double armourFormula(double d) {
        if (d == 0) return 0; //if res is 0, then give 0 res 4HEAD
        //the following formula is a reversal of the main armour formula. Used for negative armor values.
        if (d < 0) return -100/(1 + Math.exp((d/10) + 2));
        //the following formula is a function that has a horizontal asymptote at y = 100.
        //this is so the player can never go over 100% damage reduction.
        return 100/(1 + Math.exp((-d/10) + 2));
    }

    public static float damageFormula(float a, float d) {
        if (d == 0) return a; //if res is 0, then we should return true damage.
        if (d >= 100) return 0; //if res is 100, then no damage should be taken.
        //the following formula is a sigmoid function. At 0 res it's true damage, at equal it's 50%.
        // return a - (a/(1 + Math.pow( (e/2.3) , ((a - d)/9) )));

        //the following formula is just a simple % reduction, i.e d = 50 ->  return 0.5a
        return a * (1 - (d/100));
    }

}
