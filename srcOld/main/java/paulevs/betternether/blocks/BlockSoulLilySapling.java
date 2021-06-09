package paulevs.betternether.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.registry.BlocksRegistry;

public class BlockSoulLilySapling extends BlockCommonSapling {
	public BlockSoulLilySapling() {
		super(BlocksRegistry.SOUL_LILY, MaterialColor.ORANGE);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState ground = world.getBlockState(pos.down());
		return BlocksHelper.isSoulSand(ground) || ground.getBlock() == BlocksRegistry.FARMLAND;
	}
}
