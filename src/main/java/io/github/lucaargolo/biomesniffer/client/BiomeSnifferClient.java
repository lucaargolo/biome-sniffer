package io.github.lucaargolo.biomesniffer.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BiomeSnifferClient implements ClientModInitializer {

    public static BiomeSnifferResource RESOURCE = new BiomeSnifferResource();
    public static Identifier TEXTURE = null;

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(RESOURCE);
    }
}
