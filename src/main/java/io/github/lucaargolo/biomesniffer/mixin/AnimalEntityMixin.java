package io.github.lucaargolo.biomesniffer.mixin;

import io.github.lucaargolo.biomesniffer.BiomeSniffer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    public void writeCustomTrackedDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if((Object) (this) instanceof SnifferEntity) {
            nbt.putString("biomesniffer:biome_string", this.dataTracker.get(BiomeSniffer.BIOME_STRING));
        }
    }

    @SuppressWarnings("ConstantValue")
    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    public void readCustomTrackedDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if((Object) (this) instanceof SnifferEntity) {
            this.dataTracker.set(BiomeSniffer.BIOME_STRING, nbt.getString("biomesniffer:biome_string"));
        }
    }

}
