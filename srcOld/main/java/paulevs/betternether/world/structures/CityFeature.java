package paulevs.betternether.world.structures;

import java.util.List;

import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import paulevs.betternether.world.structures.city.CityGenerator;
import paulevs.betternether.world.structures.city.palette.Palettes;
import paulevs.betternether.world.structures.piece.CavePiece;
import paulevs.betternether.world.structures.piece.CityPiece;

public class CityFeature extends StructureFeature<DefaultFeatureConfig> {
	private static CityGenerator generator;
	public static final int RADIUS = 8 * 8;

	public CityFeature() {
		super(DefaultFeatureConfig.CODEC);
	}

	public static void initGenerator() {
		generator = new CityGenerator();
	}

	@Override
	public StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
		return CityFeature.CityStart::new;
	}

	public static class CityStart extends StructureStart<DefaultFeatureConfig> {
		public CityStart(StructureFeature<DefaultFeatureConfig> structureFeature, int chunkX, int chunkZ, BlockBox blockBox, int i, long l) {
			super(structureFeature, chunkX, chunkZ, blockBox, i, l);
		}

		@Override
		public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, int x, int z, Biome biome, DefaultFeatureConfig featureConfig) {
			int px = (x << 4) | 8;
			int pz = (z << 4) | 8;
			int y = 40;
			if (chunkGenerator instanceof FlatChunkGenerator) {
				y = chunkGenerator.getHeight(px, pz, Type.WORLD_SURFACE);
			}

			BlockPos center = new BlockPos(px, y, pz);

			// CityPalette palette = Palettes.getRandom(random);
			List<CityPiece> buildings = generator.generate(center, this.random, Palettes.EMPTY);
			BlockBox cityBox = BlockBox.empty();
			for (CityPiece p : buildings)
				cityBox.encompass(p.getBoundingBox());

			int d1 = Math.max((center.getX() - cityBox.minX), (cityBox.maxX - center.getX()));
			int d2 = Math.max((center.getZ() - cityBox.minZ), (cityBox.maxZ - center.getZ()));
			int radius = Math.max(d1, d2);
			if (radius / 2 + center.getY() < cityBox.maxY) {
				radius = (cityBox.maxY - center.getY()) / 2;
			}

			if (!(chunkGenerator instanceof FlatChunkGenerator)) {
				CavePiece cave = new CavePiece(center, radius, random);
				this.children.add(cave);
				this.children.addAll(buildings);
				this.boundingBox = cave.getBoundingBox();
			}
			else {
				this.children.addAll(buildings);
				this.setBoundingBoxFromChildren();
			}

			/*
			 * for (CityPiece p: buildings) { int count =
			 * p.getBoundingBox().getBlockCountX() *
			 * p.getBoundingBox().getBlockCountY() *
			 * p.getBoundingBox().getBlockCountZ(); if (count > 0) { count =
			 * random.nextInt(count / 512); for (int i = 0; i < count; i++)
			 * this.children.add(new DestructionPiece(p.getBoundingBox(),
			 * random)); } }
			 */

			this.boundingBox.minX -= 12;
			this.boundingBox.maxX += 12;
			this.boundingBox.minZ -= 12;
			this.boundingBox.maxZ += 12;
		}
	}
}
