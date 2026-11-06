/*
 * This file ("ActuallyAdditions.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.api.farmer.IFarmerBehavior;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.config.ClientConfig;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.config.conditions.BoolConfigCondition;
import de.ellpeck.actuallyadditions.mod.crafting.ActuallyRecipes;
import de.ellpeck.actuallyadditions.mod.crafting.CrusherCrafting;
import de.ellpeck.actuallyadditions.mod.crafting.TargetNBTIngredient;
import de.ellpeck.actuallyadditions.mod.data.WorldData;
import de.ellpeck.actuallyadditions.mod.entity.EntityWorm;
import de.ellpeck.actuallyadditions.mod.entity.InitEntities;
import de.ellpeck.actuallyadditions.mod.event.CommonEvents;
import de.ellpeck.actuallyadditions.mod.fluids.InitFluids;
import de.ellpeck.actuallyadditions.mod.inventory.ActuallyContainers;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.items.Worm;
import de.ellpeck.actuallyadditions.mod.misc.BannerHelper;
import de.ellpeck.actuallyadditions.mod.misc.DungeonLoot;
import de.ellpeck.actuallyadditions.mod.misc.apiimpl.LaserRelayConnectionHandler;
import de.ellpeck.actuallyadditions.mod.misc.apiimpl.MethodHandler;
import de.ellpeck.actuallyadditions.mod.network.PacketHandler;
import de.ellpeck.actuallyadditions.mod.particle.ActuallyParticles;
import de.ellpeck.actuallyadditions.mod.update.UpdateChecker;
import de.ellpeck.actuallyadditions.mod.util.ResourceReloader;
import de.ellpeck.actuallyadditions.mod.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ActuallyAdditions.MODID)
public class ActuallyAdditions {

    public static final String MODID = ActuallyAdditionsAPI.MOD_ID;

    @Deprecated
    public static final String NAME = "Actually Additions";
    @Deprecated
    public static final String VERSION = "@VERSION@";
    @Deprecated
    public static final String GUIFACTORY = "de.ellpeck.actuallyadditions.mod.config.GuiFactory";
    public static final String DEPS = "required:forge@[14.23.5.2836,);before:craftingtweaks;after:fastbench@[1.3.2,)";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<EntityWorm>> ENTITY_WORM = ENTITIES.register("worm", () -> EntityType.Builder.of(EntityWorm::new, MobCategory.MISC).build(MODID + ":worm"));

    public static boolean commonCapsLoaded;

    public ActuallyAdditions() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.COMMON_CONFIG);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ActuallyBlocks.init(eventBus);
        ActuallyItems.init(eventBus);
        ActuallyTabs.init(eventBus);
        ActuallyRecipes.init(eventBus);
        AASounds.init(eventBus);
        ActuallyContainers.CONTAINERS.register(eventBus);
        ENTITIES.register(eventBus);
        eventBus.addListener(this::onConfigReload);
        ActuallyParticles.init(eventBus);
        ActuallyTags.init();

        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        MinecraftForge.EVENT_BUS.register(new DungeonLoot());
        MinecraftForge.EVENT_BUS.addListener(ActuallyAdditions::reloadEvent);
        MinecraftForge.EVENT_BUS.addListener(Worm::onHoe);
        InitFluids.init(eventBus);

        eventBus.addListener(this::setup);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // SkyTekx: the client config of upstream 9c6859cd, on the physical client only
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);
            // SkyTekx: the listeners are ActuallyAdditionsClient's own methods, so this class names
            // the client class only inside this lambda
            eventBus.addListener(ActuallyAdditionsClient::setup);
            eventBus.addListener(ActuallyAdditionsClient::setupSpecialRenders);
            eventBus.addListener(ActuallyAdditionsClient::registerParticleFactories);
            eventBus.addListener(ActuallyAdditionsClient::registerKeyMappings);
            MinecraftForge.EVENT_BUS.addListener(ActuallyAdditionsClient::onClientTick);
        });
        IFarmerBehavior.initBehaviors();

        Util.curiosLoaded = ModList.get().isLoaded("curios");
    }

    private static void reloadEvent(AddReloadListenerEvent event) {
        event.addListener(new ResourceReloader(event.getServerResources()));
    }

    private void setup(FMLCommonSetupEvent event) {
        PacketHandler.init();

        event.enqueueWork(() -> {
            CraftingHelper.register(BoolConfigCondition.Serializer.INSTANCE);
        });

        ActuallyAdditionsAPI.methodHandler = new MethodHandler();
        ActuallyAdditionsAPI.connectionHandler = new LaserRelayConnectionHandler();
        //Lenses.init();
//        CompatUtil.registerCraftingTweaks();
        event.enqueueWork(() -> CraftingHelper.register(TargetNBTIngredient.Serializer.NAME, TargetNBTIngredient.SERIALIZER));
        event.enqueueWork(ActuallyAdditions::registerCompostables);

        commonCapsLoaded = false; // Loader.isModLoaded("commoncapabilities");

        new UpdateChecker();
        BannerHelper.init();
        InitEntities.init(); // todo: [port] replace
        //AAWorldGen gen = new AAWorldGen();
        CrusherCrafting.init();
        //LensMining.init();
    }

    // SkyTekx: upstream 3a2f298e (#1431, changelog 1.3.8) writes a compostables data map. Forge 47 has none,
    // so the chances go into the vanilla table. They are the values of main's generated data map, in which
    // the later crop entries of coffee beans and flax seeds replace their seed entries
    private static void registerCompostables() {
        ComposterBlock.COMPOSTABLES.put(ActuallyItems.CANOLA.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ActuallyItems.CANOLA_SEEDS.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ActuallyItems.COFFEE_BEANS.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ActuallyItems.FLAX_SEEDS.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ActuallyItems.RICE.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ActuallyItems.RICE_SEEDS.get(), 0.3F);
    }

    private void onConfigReload(ModConfigEvent event) {
        // SkyTekx: upstream 9c6859cd and b66af844. Only the common config holds the configurator
        // items, and an id that does not parse falls back to air instead of throwing
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            CommonConfig.Other.redstoneConfigureItem = configuredItem(CommonConfig.Other.REDSTONECONFIGURATOR.get());
            CommonConfig.Other.relayConfigureItem = configuredItem(CommonConfig.Other.RELAYCONFIGURATOR.get());
            CommonConfig.Other.farmerConfigureItem = configuredItem(CommonConfig.Other.FARMERCONFIG.get());
        }
    }

    private static Item configuredItem(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        Item item = location != null ? ForgeRegistries.ITEMS.getValue(location) : null;
        return item != null ? item : Items.AIR;
    }

    public void serverStarted(ServerStartedEvent event) {
        // TODO: [port] check if this is needed

        //        if (event.getServer() != null) {
        //            World world = event.getServer().getWorld(OVERWORLD);
        //            if (world != null && !world.isRemote) {
        //                WorldData.get(world, true).markDirty();
        //            }
        //        }
    }

    public void serverStopped(ServerStoppedEvent event) {
        // TODO: [port] check if this is needed
        WorldData.clear();
    }
}
