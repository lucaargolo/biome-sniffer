package io.github.lucaargolo.biomesniffer.mixin;

import io.github.lucaargolo.biomesniffer.client.BiomeSnifferFeatureRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.SnifferEntityRenderer;
import net.minecraft.client.render.entity.model.SnifferEntityModel;
import net.minecraft.entity.passive.SnifferEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnifferEntityRenderer.class)
public abstract class SnifferEntityRendererMixin extends MobEntityRenderer<SnifferEntity, SnifferEntityModel<SnifferEntity>> {

    public SnifferEntityRendererMixin(EntityRendererFactory.Context context, SnifferEntityModel<SnifferEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void injectBiomeSnifferOverlay(EntityRendererFactory.Context context, CallbackInfo ci) {
        addFeature(new BiomeSnifferFeatureRenderer<>(this, context.getModelLoader()));
    }


}
