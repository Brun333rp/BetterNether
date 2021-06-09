package paulevs.betternether.blockentities;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import paulevs.betternether.BlocksHelper;
import paulevs.betternether.blocks.BlockChestOfDrawers;
import paulevs.betternether.registry.BlockEntitiesRegistry;

public class BlockEntityChestOfDrawers extends LootableContainerBlockEntity {
	private DefaultedList<ItemStack> inventory;
	private int watchers = 0;

	public BlockEntityChestOfDrawers() {
		super(BlockEntitiesRegistry.CHEST_OF_DRAWERS);
		this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
	}

	@Override
	public int size() {
		return 27;
	}

	@Override
	protected DefaultedList<ItemStack> getInvStackList() {
		return this.inventory;
	}

	@Override
	protected void setInvStackList(DefaultedList<ItemStack> list) {
		this.inventory = list;
	}

	@Override
	protected Text getContainerName() {
		return new TranslatableText("container.chest_of_drawers", new Object[0]);
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
	}

	public void fromTag(BlockState blockState, CompoundTag tag) {
		super.fromTag(blockState, tag);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		if (!this.deserializeLootTable(tag)) {
			Inventories.fromTag(tag, this.inventory);
		}
	}

	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		if (!this.serializeLootTable(tag)) {
			Inventories.toTag(tag, this.inventory);
		}
		return tag;
	}

	public void onInvOpen(PlayerEntity player) {
		if (!player.isSpectator()) {
			if (this.watchers < 0) {
				this.watchers = 0;
			}
			if (this.watchers == 0) {
				this.playSound(this.getCachedState(), SoundEvents.BLOCK_BARREL_OPEN);
			}

			++this.watchers;
			this.onInvOpenOrClose();
		}
	}

	@Override
	public void onClose(PlayerEntity player) {
		if (!player.isSpectator()) {
			--this.watchers;
			this.onInvOpenOrClose();
		}
	}

	protected void onInvOpenOrClose() {
		BlockState state = this.getCachedState();
		Block block = state.getBlock();
		if (block instanceof BlockChestOfDrawers && !world.isClient) {
			if (watchers > 0 && !state.get(BlockChestOfDrawers.OPEN)) {
				BlocksHelper.setWithoutUpdate((ServerWorld) world, pos, state.with(BlockChestOfDrawers.OPEN, true));
			}
			else if (watchers == 0 && state.get(BlockChestOfDrawers.OPEN)) {
				BlocksHelper.setWithoutUpdate((ServerWorld) world, pos, state.with(BlockChestOfDrawers.OPEN, false));
			}
		}
	}

	private void playSound(BlockState blockState, SoundEvent soundEvent) {
		Vec3i vec3i = ((Direction) blockState.get(BlockChestOfDrawers.FACING)).getVector();
		double d = (double) this.pos.getX() + 0.5D + (double) vec3i.getX() / 2.0D;
		double e = (double) this.pos.getY() + 0.5D + (double) vec3i.getY() / 2.0D;
		double f = (double) this.pos.getZ() + 0.5D + (double) vec3i.getZ() / 2.0D;
		this.world.playSound((PlayerEntity) null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
	}

	public void addItemsToList(List<ItemStack> items) {
		for (ItemStack item : inventory)
			if (item != null)
				items.add(item);
	}
}
