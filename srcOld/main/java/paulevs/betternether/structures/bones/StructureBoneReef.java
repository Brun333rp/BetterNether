package paulevs.betternether.structures.bones;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.structures.IStructure;
import paulevs.betternether.structures.StructureNBT;

public class StructureBoneReef implements IStructure {
	private static final StructureNBT[] BONES = new StructureNBT[] {
			new StructureNBT("bone_01"),
			new StructureNBT("bone_02"),
			new StructureNBT("bone_03")
	};

	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		if (BlocksHelper.isNetherGround(world.getBlockState(pos.down())) && world.isAir(pos.up(2)) && world.isAir(pos.up(4))) {
			StructureNBT bone = BONES[random.nextInt(BONES.length)];
			bone.randomRM(random);
			bone.generateCentered(world, pos.down(random.nextInt(4)));
		}
	}
}
