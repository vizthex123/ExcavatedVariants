package dev.lukebemish.excavatedvariants.impl;

import com.google.auto.service.AutoService;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.lukebemish.defaultresources.api.GlobalResourceManager;
import dev.lukebemish.excavatedvariants.api.RegistrationListener;
import dev.lukebemish.excavatedvariants.api.data.GroundType;
import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.modifier.Modifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.function.BiConsumer;

@AutoService(RegistrationListener.class)
public class DefaultRegistrationListener implements RegistrationListener {
    @Override
    public void provideEntries(Registrar registrar) {
        loadType(
                RegistriesImpl.GROUND_TYPE_KEY.location().getPath(),
                GroundType.CODEC,
                registrar.groundTypes,
                true
        );
        loadType(
                RegistriesImpl.STONE_KEY.location().getPath(),
                Stone.CODEC,
                registrar.stones,
                true
        );
        loadType(
                RegistriesImpl.ORE_KEY.location().getPath(),
                Ore.CODEC,
                registrar.ores,
                false
        );
        loadType(
                RegistriesImpl.MODIFIER_KEY.location().getPath(),
                Modifier.CODEC,
                registrar.modifiers,
                true
        );
    }

    private static final GlobalResourceManager DATA = GlobalResourceManager.getGlobalData().prefix(ExcavatedVariants.MOD_ID);

    public static <T> void loadType(String prefix, Codec<T> codec, BiConsumer<ResourceLocation, T> consumer, boolean firstOnly) {
        BiConsumer<ResourceLocation, List<Resource>> foundConsumer = (rl, resources) -> resources.forEach(resource -> {
            ResourceLocation processed = new ResourceLocation(rl.getNamespace(), rl.getPath().substring(prefix.length()+1, rl.getPath().length() - 5));
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = ExcavatedVariants.GSON.fromJson(reader, JsonElement.class);
                var t = codec.parse(JsonOps.INSTANCE, json);
                if (t.result().isPresent()) {
                    consumer.accept(processed, t.result().get());
                } else {
                    //noinspection OptionalGetWithoutIsPresent
                    ExcavatedVariants.LOGGER.error("Could not parse {} as {}: {}", rl, prefix, t.error().get().message());
                }
            } catch (IOException e) {
                ExcavatedVariants.LOGGER.error("Could not read {}", rl, e);
            }
        });
        if (firstOnly) {
            DATA.listResources(prefix, rl -> rl.getPath().endsWith(".json")).forEach((rl, r) -> foundConsumer.accept(rl, List.of(r)));
        } else {
            DATA.listResourceStacks(prefix, rl -> rl.getPath().endsWith(".json")).forEach(foundConsumer);
        }
    }

    @Override
    public int priority() {
        return ExcavatedVariants.DEFAULT_COMPAT_PRIORITY;
    }
}
