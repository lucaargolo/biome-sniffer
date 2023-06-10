package io.github.lucaargolo.biomesniffer.mixin;

import io.github.lucaargolo.biomesniffer.BiomeSniffer;
import io.github.lucaargolo.biomesniffer.mixed.SnifferEntityMixed;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnifferEntity.class)
public abstract class SnifferEntityMixin extends AnimalEntity implements SnifferEntityMixed {

    protected SnifferEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("WrongEntityDataParameterClass")
    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void injectTrackedData(CallbackInfo ci) {
        BiomeSniffer.BIOME_STRING = DataTracker.registerData(SnifferEntity.class, TrackedDataHandlerRegistry.STRING);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void injectSpawnBiome(CallbackInfo ci) {
        if(!getWorld().isClient && BiomeSniffer.CONFIG.persistentBiome) {
            String biomeString = dataTracker.get(BiomeSniffer.BIOME_STRING);
            if (biomeString.isEmpty()) {
                getWorld().getBiome(getBlockPos()).getKey().ifPresent(biomeRegistryKey -> dataTracker.set(BiomeSniffer.BIOME_STRING, biomeRegistryKey.getValue().toString()));
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void initDataTracker(CallbackInfo ci) {
        dataTracker.startTracking(BiomeSniffer.BIOME_STRING, "");
    }

    @Override
    public String getBiomeString() {
        return this.dataTracker.get(BiomeSniffer.BIOME_STRING);
    }

}
