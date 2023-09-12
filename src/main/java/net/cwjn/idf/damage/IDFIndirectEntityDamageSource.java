package net.cwjn.idf.damage;

import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class IDFIndirectEntityDamageSource extends IndirectEntityDamageSource implements IDFInterface {

    private final float fire, water, lightning, magic, dark, holy, pen, lifesteal, weight, knockback;
    private final String damageClass; //strike, pierce, _slash, _crush, genric
    private boolean isTrue = false, isConversion = false;

    public IDFIndirectEntityDamageSource(String msgId, Entity source, Entity trueSource, String dc) {
        super(msgId, source, trueSource);
        fire = 0;
        water = 0;
        lightning = 0;
        magic = 0;
        dark = 0;
        holy = 0;
        pen = 0;
        lifesteal = 0;
        weight = -1;
        knockback = 0;
        damageClass = dc;
    }

    public IDFIndirectEntityDamageSource (String msgId, Entity source, Entity trueSource, float f, float w, float l, float m, float d, float h, float p, float ls, String dc) {
        super(msgId, source, trueSource);
        fire = f;
        water = w;
        lightning = l;
        magic = m;
        dark = d;
        holy = h;
        pen = p;
        lifesteal = ls;
        weight = -1;
        knockback = 0;
        damageClass = dc;
    }

    public IDFIndirectEntityDamageSource (String msgId, Entity source, Entity trueSource, float f, float w, float l, float m, float d, float h, float p, float ls, float wt, String dc) {
        super(msgId, source, trueSource);
        fire = f;
        water = w;
        lightning = l;
        magic = m;
        dark = d;
        holy = h;
        pen = p;
        lifesteal = ls;
        weight = wt;
        knockback = 0;
        damageClass = dc;
    }

    public IDFIndirectEntityDamageSource (String msgId, Entity source, Entity trueSource, float f, float w, float l, float m, float d, float h, float p, float ls, float kb, float wt, String dc) {
        super(msgId, source, trueSource);
        fire = f;
        water = w;
        lightning = l;
        magic = m;
        dark = d;
        holy = h;
        pen = p;
        lifesteal = ls;
        weight = wt;
        knockback = kb;
        damageClass = dc;
    }

    public IDFIndirectEntityDamageSource setTrue() {
        this.isTrue = true;
        return this;
    }

    public IDFIndirectEntityDamageSource setIsConversion() {
        this.isConversion = true;
        return this;
    }

    public boolean isTrue() {
        return isTrue;
    }

    public boolean isConversion() {
        return isConversion;
    }

    public float getFire() {
        return fire;
    }

    public float getWater() {
        return water;
    }

    public float getLightning() {
        return lightning;
    }

    public float getMagic() {
        return magic;
    }

    public float getDark() { return dark; }

    public float getHoly() {
        return holy;
    }

    public float getPen() {
        return pen;
    }

    public float getLifesteal() {
        return lifesteal;
    }

    public float getForce() {
        return weight;
    }

    public String getDamageClass() {
        return damageClass;
    }

    public String getName() {
        return this.msgId;
    }

    public boolean hasDamage() {
        return fire > 0 || water > 0 || lightning > 0 || magic > 0 || dark > 0 || holy > 0;
    }

    @Override
    public float getKnockback() {
        return knockback;
    }
}