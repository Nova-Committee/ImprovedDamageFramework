package net.cwjn.idf.mixin;

import net.cwjn.idf.Color;
import net.cwjn.idf.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    /**
     * @author cwJn
     * @reason
     * looks better. I don't think other mods will be using this, could be wrong tho.
     */
    @Overwrite
    public Component getFullname(int level) {
        Enchantment thisEnchant = (Enchantment)(Object)this;
        MutableComponent component = Util.translationComponent(thisEnchant.getDescriptionId());
        MutableComponent mutablecomponent;
        double maxLevel = thisEnchant.getMaxLevel();
        double levelAsPercentage = (double) level / maxLevel;

        if (thisEnchant.isCurse()) {
            mutablecomponent = Util.withColor(component, Color.RED);
        } else {
            if (maxLevel == 1) {
                mutablecomponent = Util.withColor(component, Color.MAGICBLUE);
            } else {
                if (levelAsPercentage <= 0.33) {
                    mutablecomponent = Util.withColor(component, Color.ENCHANTINGGRAY).append(" ").append(Util.withColor(Util.translationComponent("enchantment.level." + level), Color.ENCHANTINGGRAY));
                } else if (levelAsPercentage <= 0.66) {
                    mutablecomponent = Util.withColor(component, Color.WEAKPURPLE).append(" ").append(Util.withColor(Util.translationComponent("enchantment.level." + level), Color.WEAKPURPLE));
                } else if (levelAsPercentage <= 0.99) {
                    mutablecomponent = Util.withColor(component, Color.FULLPURPLE).append(" ").append(Util.withColor(Util.translationComponent("enchantment.level." + level), Color.FULLPURPLE));
                } else {
                    mutablecomponent = Util.withColor(component, Color.MAGICBLUE).append(" ").append(Util.withColor(Util.translationComponent("enchantment.level." + level), Color.MAGICBLUE));
                }
            }
        }
        return mutablecomponent;
    }
}
