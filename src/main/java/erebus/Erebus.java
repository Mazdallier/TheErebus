package erebus;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import erebus.client.render.entity.MobGrabbingHealthBarRemoval;
import erebus.client.render.entity.RenderRhinoBeetleChargeBar;
import erebus.client.sound.AmbientMusicManager;
import erebus.core.handler.BonemealHandler;
import erebus.core.handler.BucketHandler;
import erebus.core.handler.ConfigHandler;
import erebus.core.handler.HomingBeeconTextureHandler;
import erebus.core.proxy.CommonProxy;
import erebus.entity.util.RandomMobNames;
import erebus.integration.FMBIntegration;
import erebus.integration.IModIntegration;
import erebus.lib.Reference;
import erebus.network.PacketPipeline;
import erebus.recipes.AltarRecipe;
import erebus.recipes.RecipeHandler;
import erebus.world.WorldProviderErebus;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, dependencies = Reference.MOD_DEPENDENCIES)
public class Erebus {

	@SidedProxy(clientSide = Reference.SP_CLIENT, serverSide = Reference.SP_SERVER)
	public static CommonProxy proxy;

	@Instance(Reference.MOD_ID)
	public static Erebus instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.loadConfig(event);

		if (event.getSide() == Side.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new RenderRhinoBeetleChargeBar());
			MinecraftForge.EVENT_BUS.register(new HomingBeeconTextureHandler());
			MinecraftForge.EVENT_BUS.register(new MobGrabbingHealthBarRemoval());
			AmbientMusicManager.register();
		}

		ModBlocks.init();
		ModItems.init();
		ModEntities.init();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);

		DimensionManager.registerProviderType(ConfigHandler.erebusDimensionID, WorldProviderErebus.class, true);
		DimensionManager.registerDimension(ConfigHandler.erebusDimensionID, ConfigHandler.erebusDimensionID);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerKeyHandlers();
		proxy.registerTileEntities();
		proxy.registerRenderInformation();

		PacketPipeline.initializePipeline();
		ModBiomes.init();
		RecipeHandler.init();
		AltarRecipe.init();

		MinecraftForge.EVENT_BUS.register(new BonemealHandler());
		MinecraftForge.EVENT_BUS.register(ModBlocks.bambooShoot);
		MinecraftForge.EVENT_BUS.register(ModBlocks.flowerPlanted);
		MinecraftForge.EVENT_BUS.register(ModBlocks.quickSand);
		MinecraftForge.EVENT_BUS.register(ModBlocks.insectRepellent);
		MinecraftForge.EVENT_BUS.register(ModBlocks.erebusHoneyBlock);
		MinecraftForge.EVENT_BUS.register(ModItems.armorGlider);
		MinecraftForge.EVENT_BUS.register(ModItems.jumpBoots);
		BucketHandler.INSTANCE.buckets.put(ModBlocks.erebusHoneyBlock, ModItems.bucketHoney);
		MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);

		if (ConfigHandler.randomNames)
			MinecraftForge.EVENT_BUS.register(RandomMobNames.instance);

		if (Loader.isModLoaded("ForgeMicroblock"))
			FMBIntegration.integrate();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		try {
			for (ClassInfo clsInfo : ClassPath.from(getClass().getClassLoader()).getTopLevelClasses("erebus.integration")) {
				Class<?> cls = clsInfo.load();

				if (IModIntegration.class.isAssignableFrom(cls) && !cls.isInterface())
					try {
						IModIntegration obj = (IModIntegration) cls.newInstance();
						if (Loader.isModLoaded(obj.getModId()))
							obj.integrate();
					} catch (Throwable e) {
						e.printStackTrace();
					}
			}
		} catch (Exception e) {
		}
	}
}