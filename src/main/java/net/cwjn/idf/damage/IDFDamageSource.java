package net.cwjn.idf.damage;

import net.minecraft.world.damagesource.DamageSource;

public class IDFDamageSource extends DamageSource implements IDFInterface {

    private final float fire, water, lightning, magic, dark, holy, pen, lifesteal, force, knockback;
    private final String damageClass; //strike, pierce, _slash, _crush, genric
    private boolean isTrue = false, isConversion = false;

    public IDFDamageSource(String msgId, String dc) {
        super(msgId);
        fire = 0;
        water = 0;
        lightning = 0;
        magic = 0;
        dark = 0;
        holy = 0;
        pen = 0;
        lifesteal = 0;
        force = -1;
        knockback = 0;
        damageClass = dc;
    }

    public IDFDamageSource(String msgId, float f, float w, float l, float m, float d, float h, float pen, float ls, String dc) {
        super(msgId);
        fire = f;
        water = w;
        lightning = l;
        magic = m;
        dark = d;
        holy = h;
        damageClass = dc;
        lifesteal = ls;
        force = -1;
        knockback = 0;
        this.pen = pen;
    }

    public IDFDamageSource(String msgId, float f, float w, float l, float m, float d, float h, float pen, float ls, float frc, String dc) {
        super(msgId);
        fire = f;
        water = w;
        lightning = l;
        magic = m;
        dark = d;
        holy = h;
        damageClass = dc;
        lifesteal = ls;
        force = frc;
        knockback = 0;
        this.pen = pen;
    }

    public IDFDamageSource(String msgId, float f, float w, float l, float m, float d, float h, float pen, float ls, float kb, float frc, String dc) {
        super(msgId);
        fire = f;
        water = w;
        lightning = l;
        magic = m;
        dark = d;
        holy = h;
        damageClass = dc;
        lifesteal = ls;
        force = frc;
        knockback = kb;
        this.pen = pen;
    }

    public IDFDamageSource setTrue() {
        this.isTrue = true;
        return this;
    }

    public IDFDamageSource setIsConversion() {
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
        return force;
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

    public float getKnockback() {
        return knockback;
    }

}
