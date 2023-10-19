package net.cwjn.idf.mixin;

import net.cwjn.idf.capability.data.ProjectileHelper;
import net.cwjn.idf.capability.provider.ArrowHelperProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow {

    @Shadow public abstract double getBaseDamage();

    @Unique @Final @Mutable private boolean isCrit;

    @Redirect(
            method = "onHitEntity",                                  
            at = @At(                                                
                    value = "INVOKE",                                
                    target = "Lnet/minecraft/util/Mth;ceil(D)I"      
            )                                                        
    )
    private int ceil(double useless) {
        AbstractArrow thisArrow = (AbstractArrow)(Object)this;
        if (thisArrow.getOwner() instanceof LivingEntity livingEntity) {
            ProjectileHelper helper = livingEntity.getCapability(ArrowHelperProvider.PROJECTILE_HELPER).orElseGet(ProjectileHelper::new);
            double arrowVel = thisArrow.getDeltaMovement().length();
            helper.setWeight((float) (helper.getWeight() * arrowVel * 0.67));
            return (int) (helper.getPhys() + this.getBaseDamage() * (isCrit? helper.getCritDmg()*0.01 : 1));
        } else {
            return (int) this.getBaseDamage();
        }
    }


    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void changeCritMechanic(EntityType arrowEntity, LivingEntity owner, Level level, CallbackInfo ci) {
        if (owner instanceof Player) {
            ProjectileHelper helper = owner.getCapability(ArrowHelperProvider.PROJECTILE_HELPER).orElseGet(ProjectileHelper::new);
            this.isCrit = helper.getCrit();
        } else {
            this.isCrit = false;
        }
    }

    @Inject(method = "isCritArrow", at = @At("HEAD"), cancellable = true)
    private void useNewCrit(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(isCrit);
    }

}
