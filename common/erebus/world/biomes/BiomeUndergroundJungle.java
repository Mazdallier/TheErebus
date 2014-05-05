package erebus.world.biomes;
import java.util.Random;
import net.minecraft.block.Block;
import erebus.ModBiomes;
import erebus.ModBlocks;
import erebus.entity.EntityBeetle;
import erebus.entity.EntityBeetleLarva;
import erebus.entity.EntityBombardierBeetle;
import erebus.entity.EntityBotFly;
import erebus.entity.EntityCentipede;
import erebus.entity.EntityFly;
import erebus.entity.EntityJumpingSpider;
import erebus.entity.EntityMosquito;
import erebus.entity.EntityMoth;
import erebus.entity.EntityPrayingMantis;
import erebus.entity.EntityScytodes;
import erebus.entity.EntityTarantula;
import erebus.entity.EntityVelvetWorm;
import erebus.entity.EntityWasp;
import erebus.world.biomes.BiomeBaseErebus.SpawnEntry;
import erebus.world.biomes.decorators.BiomeDecoratorUndergroundJungle;

// @formatter:off
public class BiomeUndergroundJungle extends BiomeBaseErebus{
	public BiomeUndergroundJungle(int biomeID){
		super(biomeID,new BiomeDecoratorUndergroundJungle());
		
		setBiomeName("Undergound Jungle");
		setColors(0x53CA37,0x29BC05);
		setFog(8,128,8);
		setTemperatureRainfall(1.35F,0.9F);
		setWeight(22);

		spawnableMonsterList.add(new SpawnEntry(EntityScytodes.class,35,1,4));
		spawnableMonsterList.add(new SpawnEntry(EntityWasp.class,30,4,8));
		spawnableMonsterList.add(new SpawnEntry(EntityCentipede.class,10,4,8));
		spawnableMonsterList.add(new SpawnEntry(EntityPrayingMantis.class,10,4,8));
		spawnableMonsterList.add(new SpawnEntry(EntityJumpingSpider.class,10,1,4));
		spawnableMonsterList.add(new SpawnEntry(EntityTarantula.class,5,4,8));
		spawnableMonsterList.add(new SpawnEntry(EntityBombardierBeetle.class,4,1,1));
		spawnableMonsterList.add(new SpawnEntry(EntityVelvetWorm.class,10,1,2));

		spawnableCaveCreatureList.add(new SpawnEntry(EntityMosquito.class,60,1,3));
		spawnableCaveCreatureList.add(new SpawnEntry(EntityFly.class,10,8,8));
		spawnableCaveCreatureList.add(new SpawnEntry(EntityBotFly.class,10,2,3));
		spawnableCaveCreatureList.add(new SpawnEntry(EntityBeetleLarva.class,8,2,4));
		spawnableCaveCreatureList.add(new SpawnEntry(EntityBeetle.class,8,1,2));

		topBlock = (byte)Block.grass.blockID;
		fillerBlock = (byte)Block.dirt.blockID;
	}

	@Override
	public float getSpawningChance(){
		return 0.2F;
	}
	
	@Override
	public byte placeCaveBlock(byte blockID, int x, int y, int z, Random rand){
		return blockID == (byte)ModBlocks.umberstone.blockID || blockID == topBlock || blockID == fillerBlock || blockID == Block.sandStone.blockID ? (y < 24 ? (byte)Block.waterMoving.blockID : 0) : blockID;
	}
	
	@Override
	public BiomeBaseErebus getRandomSubBiome(int randomValue){
		return ModBiomes.jungleSubLake;
	}
}
// @formatter:on