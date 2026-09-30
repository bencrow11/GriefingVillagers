package com.bencrow11.griefingvillagers.mixins;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Shadow
    protected abstract Vec3i getPickupReach();

    @Shadow
    protected abstract void pickUpItem(ItemEntity item);

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void pickupBreeding(CallbackInfo ci) {
        // Returns if the entity isn't a villager
        if (!((Object) this instanceof Villager villager)) {
            return;
        }

        if (villager.level().isClientSide
                || !villager.isAlive()
                || villager.isRemoved()
                || !villager.canPickUpLoot()) {
            return;
        }

        // Vanilla already handles pickup when mobGriefing is enabled.
        if (villager.level().getGameRules()
                .getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        Vec3i reach = getPickupReach();

        // Checks the items within a villagers reach to pick up
        for (ItemEntity item : villager.level().getEntitiesOfClass(
                ItemEntity.class,
                villager.getBoundingBox().inflate(
                        reach.getX(), reach.getY(), reach.getZ()))) {

            if (!item.isRemoved()
                    && !item.getItem().isEmpty()
                    && !item.hasPickUpDelay()
                    && villager.wantsToPickUp(item.getItem())) {
                pickUpItem(item);
            }
        }
    }
}