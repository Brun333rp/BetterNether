package paulevs.betternether.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.DeltaFeature;
import net.minecraft.world.gen.feature.DeltaFeatureConfig;
import paulevs.betternether.blocks.BlockRubeusLeaves;
import paulevs.betternether.blocks.BlockWillowLeaves;

@Mixin(DeltaFeature.class)
public class DeltaFeatureMixin {
	@Inject(method = "method_27103", at = @At("HEAD"), cancellable = true)
	private static void checkBlock(WorldAccess worldAccess, BlockPos blockPos, DeltaFeatureConfig deltaFeatureConfig, CallbackInfoReturnable<Boolean> info) {
		BlockState blockState = worldAccess.getBlockState(blockPos);
		if (!isValidBlock(blockState)) {
			info.setReturnValue(false);
			info.cancel();
		}
	}

	private static boolean isValidBlock(BlockState state) {
		return !(state.getBlock() instanceof BlockRubeusLeaves) && !(state.getBlock() instanceof BlockWillowLeaves) && !(state.getFluidState().isEmpty() && state.getMaterial().isReplaceable());
	}
}
