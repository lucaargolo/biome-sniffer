package io.github.lucaargolo.biomesniffer.client;

import io.github.lucaargolo.biomesniffer.BiomeSniffer;
import io.github.lucaargolo.biomesniffer.mixed.SnifferEntityMixed;
import net.minecraft.class_8153;
import net.minecraft.class_8185;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public class BiomeSnifferFeatureRenderer<T extends class_8153> extends FeatureRenderer<T, class_8185<T>> {
    private final EntityModel<T> model;

    public BiomeSnifferFeatureRenderer(FeatureRendererContext<T, class_8185<T>> context, EntityModelLoader loader) {
        super(context);
        this.model = new class_8185<>(loader.getModelPart(EntityModelLayers.SNIFFER));
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float j, float k, float l) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        boolean bl = minecraftClient.hasOutline(livingEntity) && livingEntity.isInvisible();
        if (livingEntity.isInvisible() && !bl) {
            return;
        }
        VertexConsumer vertexConsumer = bl ? vertexConsumerProvider.getBuffer(RenderLayer.getOutline(this.getTexture(livingEntity))) : vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(livingEntity)));
        this.getContextModel().copyStateTo(this.model);
        this.model.animateModel(livingEntity, limbAngle, limbDistance, tickDelta);
        this.model.setAngles(livingEntity, limbAngle, limbDistance, j, k, l);
        String biomeString = "";
        if(BiomeSniffer.CONFIG.persistentBiome && livingEntity instanceof SnifferEntityMixed mixed) {
            biomeString = mixed.getBiomeString();
        }
        Identifier biomeIdentifier = new Identifier(biomeString);
        World world = livingEntity.getWorld();
        Optional<Biome> optional = world.getRegistryManager().get(RegistryKeys.BIOME).getOrEmpty(biomeIdentifier);
        Biome biome = optional.orElse(livingEntity.getWorld().getBiome(livingEntity.getBlockPos()).value());
        int color = biome.getFoliageColor();
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f ;
        this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(livingEntity, 0.0f), r, g, b, 1.0f);
    }

    @Override
    protected Identifier getTexture(T entity) {
        if(BiomeSnifferClient.TEXTURE != null){
            return BiomeSnifferClient.TEXTURE;
        }else{
            return super.getTexture(entity);
        }
    }
}