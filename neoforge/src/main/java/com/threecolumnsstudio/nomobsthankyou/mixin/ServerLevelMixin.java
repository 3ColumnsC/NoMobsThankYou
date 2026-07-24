package com.threecolumnsstudio.nomobsthankyou.mixin;

import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (NoMobsThankYouConfig.shouldRemove(entity.getType())) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "addWorldGenChunkEntities", at = @At("HEAD"), argsOnly = true)
    private Stream<Entity> filterWorldGenChunkEntities(Stream<Entity> stream) {
        return stream.filter(entity -> !NoMobsThankYouConfig.shouldRemove(entity.getType()));
    }
}
