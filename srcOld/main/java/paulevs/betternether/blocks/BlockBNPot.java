package paulevs.betternether.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import paulevs.betternether.BlocksHelper;

public class BlockBNPot extends BlockBaseNotFull {
	private static final VoxelShape SHAPE = Block.createCuboidShape(3, 0, 3, 13, 8, 13);

	public BlockBNPot(Block material) {
		super(FabricBlockSettings.copy(material).nonOpaque());
	}

	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ePos) {
		return SHAPE;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockPos plantPos = pos.up();
		if (hit.getSide() == Direction.UP && world.isAir(plantPos)) {
			BlockState plant = BlockPottedPlant.getPlant(player.getMainHandStack().getItem());
			if (plant != null) {
				if (!world.isClient())
					BlocksHelper.setWithUpdate((ServerWorld) world, plantPos, plant);
				world.playSound(
						pos.getX() + 0.5,
						pos.getY() + 1.5,
						pos.getZ() + 0.5,
						SoundEvents.ITEM_CROP_PLANT,
						SoundCategory.BLOCKS,
						0.8F,
						1.0F,
						true);
				if (!player.isCreative())
					player.getMainHandStack().decrement(1);
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.FAIL;
	}
}
