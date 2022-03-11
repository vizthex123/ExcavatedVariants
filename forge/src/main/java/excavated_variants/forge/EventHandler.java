package excavated_variants.forge;


import excavated_variants.BiomeInjector;
import excavated_variants.ExcavatedVariants;
import excavated_variants.ModifiedOreBlock;
import excavated_variants.RegistryUtil;
import excavated_variants.mixin.MinecraftServerMixin;
import excavated_variants.worldgen.OreFinderUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {
/*
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void biomeModifier(BiomeLoadingEvent event) {
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            event.getGeneration().addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION.ordinal()+1, Holder.Reference.createStandAlone(BuiltinRegistries.PLACED_FEATURE,ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY,new ResourceLocation(ExcavatedVariants.MOD_ID, "ore_replacer"))));
        }
    }
*/
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStarting(ServerAboutToStartEvent event) {
        //Properties
        for (ModifiedOreBlock block : ExcavatedVariants.getBlocks().values()) {
            block.copyProperties();
        }
        //Ore Gen
        RegistryUtil.reset();
        ExcavatedVariants.oreStoneList = null;
        OreFinderUtil.reset();
        ExcavatedVariants.setupMap();
        if (ExcavatedVariants.getConfig().attempt_ore_replacement) {
            MinecraftServer server = event.getServer();
            BiomeInjector.addFeatures(((MinecraftServerMixin)server).getRegistryHolder().registry(Registry.BIOME_REGISTRY).get());
        }
    }
}
