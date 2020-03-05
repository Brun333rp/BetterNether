package paulevs.betternether.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import paulevs.betternether.registers.BlocksRegister;
import paulevs.betternether.registers.EntityRegister;

public class EntityFirefly extends AnimalEntity implements Flutterer
{
	private static final HashSet<Block> FLOWERS;
	private static final Vec3i[] SERCH;
	
	private static final TrackedData<Float> COLOR_RED = DataTracker.registerData(EntityFirefly.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> COLOR_GREEN = DataTracker.registerData(EntityFirefly.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> COLOR_BLUE = DataTracker.registerData(EntityFirefly.class, TrackedDataHandlerRegistry.FLOAT);
	
	private boolean mustSit = false;
	
	static
	{
		ArrayList<Vec3i> points = new ArrayList<Vec3i>();
		int radius = 6;
		int r2 = radius * radius;
		for (int x = -radius; x <= radius; x++)
			for (int y = -radius; y <= radius; y++)
				for (int z = -radius; z <= radius; z++)
					if (x * x + y * y + z * z <= r2)
						points.add(new Vec3i(x, y, z));
		points.sort(new Comparator<Vec3i>()
		{
			@Override
			public int compare(Vec3i v1, Vec3i v2)
			{
				int d1 = v1.getX() * v1.getX() + v1.getY() * v1.getY() + v1.getZ() * v1.getZ();
				int d2 = v2.getX() * v2.getX() + v2.getY() * v2.getY() + v2.getZ() * v2.getZ();
				return d1 - d2;
			}
		});
		SERCH = points.toArray(new Vec3i[] {});
		
		FLOWERS = new HashSet<Block>();
		FLOWERS.add(BlocksRegister.NETHER_GRASS);
		FLOWERS.add(BlocksRegister.SOUL_GRASS);
		FLOWERS.add(BlocksRegister.SWAMP_GRASS);
		FLOWERS.add(BlocksRegister.BLACK_APPLE);
		FLOWERS.add(BlocksRegister.MAGMA_FLOWER);
		FLOWERS.add(BlocksRegister.SOUL_VEIN);
		FLOWERS.add(BlocksRegister.NETHER_REED);
		FLOWERS.add(BlocksRegister.INK_BUSH);
		FLOWERS.add(BlocksRegister.INK_BUSH_SEED);
		FLOWERS.add(BlocksRegister.POTTED_PLANT);
		FLOWERS.add(Blocks.NETHER_WART);
	}

	public EntityFirefly(EntityType<? extends EntityFirefly> type, World world)
	{
		super(type, world);
		this.moveControl = new FlightMoveControl(this, 20, true);
		this.lookControl = new FreflyLookControl(this);
		this.setPathfindingPenalty(PathNodeType.LAVA, -1.0F);
		this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
		this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, 0.0F);
		this.experiencePoints = 1;
	}

	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		this.dataTracker.startTracking(COLOR_RED, random.nextFloat() * 0.5F + 0.5F);
		this.dataTracker.startTracking(COLOR_GREEN, random.nextFloat() * 0.5F + 0.5F);
		this.dataTracker.startTracking(COLOR_BLUE, random.nextFloat() * 0.5F + 0.5F);
	}

	@Override
	protected void initAttributes()
	{
		super.initAttributes();
		this.getAttributes().register(EntityAttributes.FLYING_SPEED);
		this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(1.0);
		this.getAttributeInstance(EntityAttributes.FLYING_SPEED).setBaseValue(0.6F);
		this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.25F);
		this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
	}

	@Override
	protected EntityNavigation createNavigation(World world)
	{
		BirdNavigation birdNavigation = new BirdNavigation(this, world)
		{
			public boolean isValidPosition(BlockPos pos)
			{
				BlockState state = this.world.getBlockState(pos.down());
				boolean valid = !state.isAir() && state.getMaterial() != Material.LAVA;
				if (valid)
				{
					state = this.world.getBlockState(pos);
					valid = state.isAir() || !state.getMaterial().blocksMovement();
					valid = valid && state.getBlock() != BlocksRegister.EGG_PLANT;
					valid = valid && !state.getBlock().getMaterial(state).blocksMovement();
				}
				return valid;
			}

			public void tick()
			{
				super.tick();
			}
		};
		birdNavigation.setCanPathThroughDoors(false);
		birdNavigation.setCanSwim(false);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}

	@Override
	protected void initGoals()
	{
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(3, new FollowParentGoal(this, 1.0D));
		this.goalSelector.add(4, new SittingGoal());
		this.goalSelector.add(5, new MoveToFlowersGoal());
		this.goalSelector.add(6, new WanderAroundGoal());
		this.goalSelector.add(7, new MoveRandomGoal());
	}

	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView worldView)
	{
		return worldView.getBlockState(pos).isAir() ? 10.0F : 0.0F;
	}
	
	@Override
	public boolean isBreedingItem(ItemStack stack)
	{
		return stack.getItem() == Items.GLOWSTONE_DUST;
	}

	@Override
	protected boolean hasWings()
	{
		return true;
	}

	@Override
	public EntityGroup getGroup()
	{
		return EntityGroup.ARTHROPOD;
	}
	
	@Override
	protected void swimUpward(Tag<Fluid> fluid)
	{
		this.setVelocity(this.getVelocity().add(0.0D, 0.01D, 0.0D));
	}

	@Override
	public float getBrightnessAtEyes()
	{
		return 1.0F;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier)
	{
		return false;
	}
	
	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {}

	@Override
	public boolean canClimb()
	{
		return false;
	}
	
	@Override
    public boolean hasNoGravity()
	{
        return true;
    }

	public float getRed()
	{
		return this.dataTracker.get(COLOR_RED);
	}

	public float getGreen()
	{
		return this.dataTracker.get(COLOR_GREEN);
	}

	public float getBlue()
	{
		return this.dataTracker.get(COLOR_BLUE);
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag)
	{
		super.writeCustomDataToTag(tag);
		
		tag.putFloat("ColorRed", getRed());
		tag.putFloat("ColorGreen", getGreen());
		tag.putFloat("ColorBlue", getBlue());
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag)
	{
		super.readCustomDataFromTag(tag);
		
		if (tag.contains("ColorRed"))
		{
			this.dataTracker.set(COLOR_RED, tag.getFloat("ColorRed"));
		}
		
		if (tag.contains("ColorGreen"))
		{
			this.dataTracker.set(COLOR_GREEN, tag.getFloat("ColorGreen"));
		}
		
		if (tag.contains("ColorBlue"))
		{
			this.dataTracker.set(COLOR_BLUE, tag.getFloat("ColorBlue"));
		}
	}

	@Override
	public PassiveEntity createChild(PassiveEntity mate)
	{
		return EntityRegister.FIREFLY.create(world);
	}

	class FreflyLookControl extends LookControl
	{
		FreflyLookControl(MobEntity entity)
		{
			super(entity);
		}

		protected boolean shouldStayHorizontal()
		{
			return true;
		}
	}

	class WanderAroundGoal extends Goal
	{
		WanderAroundGoal()
		{
			this.setControls(EnumSet.of(Goal.Control.MOVE));
		}

		public boolean canStart()
		{
			return EntityFirefly.this.navigation.isIdle() && EntityFirefly.this.random.nextInt(10) == 0;
		}

		public boolean shouldContinue()
		{
			return EntityFirefly.this.navigation.method_23966();
		}

		public void start()
		{
			BlockPos pos = this.getRandomLocation();
			//if (pos != null)
			//{
				Path path = EntityFirefly.this.navigation.findPathTo(pos, 1);
				if (path != null)
					EntityFirefly.this.navigation.startMovingAlong(path, 1.0D);
				else
					EntityFirefly.this.setVelocity(0, -0.2, 0);
			//}
			super.start();
		}

		private BlockPos getRandomLocation()
		{
			World w = EntityFirefly.this.world;
			Mutable bpos = new Mutable();
			bpos.set(EntityFirefly.this);
			
			if (w.isAir(bpos.down(2)) && w.isAir(bpos.down()))
			{
				int y = bpos.getY() - 1;
				while(w.isAir(bpos.down(2)) && y > 0)
					bpos.setY(y--);
				return bpos;
			}
			
			Vec3d angle = EntityFirefly.this.getRotationVec(0.0F);
			Vec3d airTarget = TargetFinder.findAirTarget(EntityFirefly.this, 8, 7, angle, 1.5707964F, 2, 1);

			if (airTarget == null)
			{
				airTarget = TargetFinder.findAirTarget(EntityFirefly.this, 16, 10, angle, 1.5707964F, 3, 1);
			}

			if (airTarget == null)
			{
				bpos.setX(bpos.getX() + randomRange(8));
				bpos.setZ(bpos.getZ() + randomRange(8));
				bpos.setY(bpos.getY() + randomRange(2));
				return bpos;
			}
			
			bpos.set(airTarget.getX(), airTarget.getY(), airTarget.getZ());
			
			return bpos;
		}
		
		private int randomRange(int side)
		{
			Random random = EntityFirefly.this.random;
			return random.nextInt(side + 1) - (side >> 1);
		}
		
		@Override
		public void tick()
		{
			checkMovement();
			super.tick();
		}
	}
	
	class MoveToFlowersGoal extends Goal
	{
		MoveToFlowersGoal()
		{
			this.setControls(EnumSet.of(Goal.Control.MOVE));
		}

		@Override
		public boolean canStart()
		{
			return EntityFirefly.this.navigation.isIdle() && EntityFirefly.this.random.nextInt(30) == 0;
		}

		@Override
		public boolean shouldContinue()
		{
			return EntityFirefly.this.navigation.method_23966();
		}

		@Override
		public void start()
		{
			BlockPos pos = this.getFlowerLocation();
			if (pos != null)
			{
				Path path = EntityFirefly.this.navigation.findPathTo((BlockPos)(new BlockPos(pos)), 1);
				EntityFirefly.this.navigation.startMovingAlong(path, 1.0D);
			}
			super.start();
		}
		
		@Override
		public void stop()
		{
			if (isFlower(EntityFirefly.this.getBlockState()))
				EntityFirefly.this.mustSit = true;
			super.stop();
		}

		private BlockPos getFlowerLocation()
		{
			World w = EntityFirefly.this.world;
			Mutable bpos = new Mutable();

			for (Vec3i offset: SERCH)
			{
				bpos.set(
						EntityFirefly.this.getX() + offset.getX(),
						EntityFirefly.this.getY() + offset.getY(),
						EntityFirefly.this.getZ() + offset.getZ()
						);
				if (isFlower(w.getBlockState(bpos)))
					return bpos;
			}
			
			return null;
		}
		
		private boolean isFlower(BlockState state)
		{
			Block b = state.getBlock();
			return FLOWERS.contains(b);
		}
		
		@Override
		public void tick()
		{
			checkMovement();
			super.tick();
		}
	}
	
	private void checkMovement()
	{
		Vec3d vel = EntityFirefly.this.getVelocity();
		if (Math.abs(vel.x) > 0.1 || Math.abs(vel.z) > 0.1)
		{
			double d = Math.abs(EntityFirefly.this.prevX - EntityFirefly.this.getX());
			d += Math.abs(EntityFirefly.this.prevZ - EntityFirefly.this.getZ());
			if (d < 0.1)
				EntityFirefly.this.navigation.stop();
		}
	}
	
	class SittingGoal extends Goal
	{
		int timer;
		int ammount;
		
		SittingGoal() {}

		@Override
		public boolean canStart()
		{
			if (EntityFirefly.this.mustSit && EntityFirefly.this.navigation.isIdle())
			{
				BlockState state = EntityFirefly.this.world.getBlockState(EntityFirefly.this.getBlockPos().down());
				return !state.isAir() && !state.getMaterial().isLiquid();
			}
			return false;
		}

		@Override
		public boolean shouldContinue()
		{
			return timer < ammount;
		}

		@Override
		public void start()
		{
			timer = 0;
			ammount = EntityFirefly.this.random.nextInt(21) + 20;
			EntityFirefly.this.mustSit = false;
			EntityFirefly.this.setVelocity(0, -0.1, 0);
			super.start();
		}
		
		@Override
		public void stop()
		{
			EntityFirefly.this.setVelocity(0, 0.1, 0);
			super.stop();
		}

		@Override
		public void tick()
		{
			timer ++;
			super.tick();
		}
	}
	
	class MoveRandomGoal extends Goal
	{
		int timer;
		int ammount;
		
		MoveRandomGoal() {}

		@Override
		public boolean canStart()
		{
			return EntityFirefly.this.navigation.isIdle() && EntityFirefly.this.random.nextInt(20) == 0;
		}

		@Override
		public boolean shouldContinue()
		{
			return timer < ammount;
		}

		@Override
		public void start()
		{
			timer = 0;
			ammount = EntityFirefly.this.random.nextInt(30) + 10;
			Vec3d velocity = new Vec3d(
					EntityFirefly.this.random.nextDouble(),
					EntityFirefly.this.random.nextDouble(),
					EntityFirefly.this.random.nextDouble()
					);
			if (velocity.lengthSquared() == 0)
				velocity = new Vec3d(1, 0, 0);
			EntityFirefly.this.setVelocity(velocity.normalize().multiply(EntityFirefly.this.flyingSpeed));
			super.start();
		}

		@Override
		public void tick()
		{
			timer ++;
			super.tick();
		}
	}
}