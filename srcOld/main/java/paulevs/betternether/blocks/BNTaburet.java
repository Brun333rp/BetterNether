package paulevs.betternether.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class BNTaburet extends BNChair {
	private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 10, 14);

	public BNTaburet(Block block) {
		super(block, 9);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ePos) {
		return SHAPE;
	}
}
