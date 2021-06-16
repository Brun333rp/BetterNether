package paulevs.betternether.structures.plants;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.registry.BlocksRegistry;
import paulevs.betternether.structures.IStructure;

import java.util.Random;

public class StructureHookMushroom implements IStructure {
	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		if (pos.getY() < 90 || !BlocksHelper.isNetherrack(world.getBlockState(pos.up()))) return;
		BlocksHelper.setWithUpdate(world, pos, BlocksRegistry.HOOK_MUSHROOM.getDefaultState());
	}
}