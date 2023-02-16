package io.github.lucaargolo.biomesniffer.mixin;

import io.github.lucaargolo.biomesniffer.client.BiomeSnifferFeatureRenderer;
import net.minecraft.class_8153;
import net.minecraft.class_8185;
import net.minecraft.class_8190;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_8190.class)
public abstract class SnifferEntityRendererMixin extends MobEntityRenderer<class_8153, class_8185<class_8153>> {

    public SnifferEntityRendererMixin(EntityRendererFactory.Context context, class_8185<class_8153> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void injectBiomeSnifferOverlay(EntityRendererFactory.Context context, CallbackInfo ci) {
        addFeature(new BiomeSnifferFeatureRenderer<>(this, context.getModelLoader()));
    }


}
