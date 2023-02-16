package io.github.lucaargolo.biomesniffer.client;

import io.github.lucaargolo.biomesniffer.BiomeSniffer;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.io.InputStream;
import java.util.Optional;

public class BiomeSnifferResource implements SimpleSynchronousResourceReloadListener {

    NativeImageBackedTexture nativeImageBackedTexture = null;

    @Override
    public Identifier getFabricId() {
        return new Identifier(BiomeSniffer.MOD_ID, "biome_sniffer");
    }

    @Override
    public void reload(ResourceManager manager) {
        Optional<Resource> optional = manager.getResource(new Identifier("textures/entity/sniffer/sniffer.png"));
        if(optional.isPresent()) {
            Resource resource = optional.get();
            try {
                InputStream stream = resource.getInputStream();
                NativeImage original = NativeImage.read(stream);
                NativeImage image = null;
                if(nativeImageBackedTexture != null) {
                    image = nativeImageBackedTexture.getImage();
                }
                if(image == null) {
                    image = original;
                }
                for(int x = 0; x < original.getWidth(); x++) {
                    for(int y = 0; y < original.getHeight(); y++) {
                        int abgr = original.getColor(x, y);
                        int a = abgr >> 24 & 0xFF;
                        int b = abgr >> 16 & 0xFF;
                        int g = abgr >> 8 & 0xFF;
                        int r = abgr & 0xFF;
                        if(g - r > 20) {
                            float[] hsb = Color.RGBtoHSB(r, g, b, null);
                            hsb[1] = 0f;
                            int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                            int nr = rgb >> 16 & 0xFF;
                            int ng = rgb >> 8 & 0xFF;
                            int nb = rgb & 0xFF;
                            int color = (a << 24) | (nb << 16) | (ng << 8) | nr;
                            image.setColor(x, y, color);
                        }else{
                            image.setColor(x, y, 0);
                        }
                    }
                }
                if(nativeImageBackedTexture == null) {
                    nativeImageBackedTexture = new NativeImageBackedTexture(image);
                }else{
                    nativeImageBackedTexture.upload();
                }
                if(BiomeSnifferClient.TEXTURE == null) {
                    BiomeSnifferClient.TEXTURE = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("biome_sniffer", nativeImageBackedTexture);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
