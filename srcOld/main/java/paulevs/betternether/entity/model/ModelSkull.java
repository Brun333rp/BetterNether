package paulevs.betternether.entity.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import paulevs.betternether.entity.EntitySkull;

public class ModelSkull extends AnimalModel<EntitySkull> {
	private final ModelPart head;
	private float pitch;

	public ModelSkull() {
		textureHeight = 16;
		textureWidth = 32;

		head = new ModelPart(this, 0, 0);
		head.setPivot(0, 20, 0);
		head.addCuboid(-4, -4, -4, 8, 8, 8);
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(head);
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of();
	}

	@Override
	public void animateModel(EntitySkull livingEntity, float f, float g, float h) {
		this.pitch = livingEntity.getLeaningPitch(h);
		super.animateModel(livingEntity, f, g, h);
	}

	@Override
	public void setAngles(EntitySkull entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		// head.pitch = (float) Math.toRadians(headPitch);

		boolean rollTooBig = entity.getRoll() > 4;
		this.head.yaw = headYaw * 0.017453292F;
		if (rollTooBig) {
			this.head.pitch = -0.7853982F;
		}
		else if (this.pitch > 0.0F) {
			this.head.pitch = this.lerpAngle(this.head.pitch, headPitch * 0.017453292F, this.pitch);
		}
		else {
			this.head.pitch = headPitch * 0.017453292F;
		}
	}

	protected float lerpAngle(float from, float to, float position) {
		float angle = (to - from) % 6.2831855F;

		if (angle < -3.1415927F) {
			angle += 6.2831855F;
		}

		if (angle >= 3.1415927F) {
			angle -= 6.2831855F;
		}

		return from + position * angle;
	}
}
