package com.threecolumnsstudio.nomobsthankyou.mixin;

import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouConfig;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobCheckSpawnRulesMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private void onCheckSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (NoMobsThankYouConfig.shouldRemove(((Mob) (Object) this).getType())) {
            cir.setReturnValue(false);
        }
    }
}
