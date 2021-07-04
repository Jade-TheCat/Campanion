package com.terraformersmc.campanion.entity;

import com.terraformersmc.campanion.advancement.criterion.CampanionCriteria;
import com.terraformersmc.campanion.stat.CampanionStats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SkippingStoneEntity extends ThrownItemEntity {

	private static final TrackedData<Integer> NUMBER_OF_SKIPS = DataTracker.registerData(SkippingStoneEntity.class, TrackedDataHandlerRegistry.INTEGER);

	public SkippingStoneEntity(World world, LivingEntity owner) {
		super(EntityType.SNOWBALL, owner, world);
	}

	public SkippingStoneEntity(World world, double x, double y, double z) {
		super(EntityType.SNOWBALL, x, y, z, world);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.SNOWBALL;
	}

	@Override
	protected void initDataTracker() {
		this.dataTracker.startTracking(NUMBER_OF_SKIPS, 0);
		super.initDataTracker();
	}

	@Environment(EnvType.CLIENT)
	private ParticleEffect getParticleParameters() {
		ItemStack itemStack = this.getItem();
		return itemStack.isEmpty() ? ParticleTypes.SPLASH : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleStatus(byte status) {
		if (status == 3) {
			ParticleEffect particleEffect = this.getParticleParameters();
			for (int i = 0; i < 8; ++i) {
				this.world.addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	protected void onSwimmingStart() {
		if (!this.world.isClient) {
			Vec3d vel = this.getVelocity();
			double squaredHorizontalVelocity = (vel.getX() * vel.getX()) + (vel.getZ() * vel.getZ());

			this.world.sendEntityStatus(this, (byte) 3);
			if (vel.getY() * vel.getY() > squaredHorizontalVelocity || this.random.nextInt(3) == 0) {
				this.remove(RemovalReason.DISCARDED);
			} else {
				this.dataTracker.set(NUMBER_OF_SKIPS, this.dataTracker.get(NUMBER_OF_SKIPS) + 1);
				if (getOwner() instanceof PlayerEntity) {
					((PlayerEntity) getOwner()).incrementStat(CampanionStats.STONE_SKIPS);
				}
				this.addVelocity(0, (0.5 + -vel.getY()) / 1.5, 0);
			}
		}
	}

	@Override
	public void onCollision(HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult) hitResult).getEntity();
			entity.damage(DamageSource.thrownProjectile(this, this.getOwner()), this.dataTracker.get(NUMBER_OF_SKIPS) + 1);
			Entity owner = getOwner();
			if (!entity.isAlive() && owner != null) {
				CampanionCriteria.KILLED_WITH_STONE.trigger((ServerPlayerEntity) owner, entity, this.dataTracker.get(NUMBER_OF_SKIPS));
			}
		}

		if (!this.world.isClient) {
			this.remove(RemovalReason.DISCARDED);
			this.world.sendEntityStatus(this, (byte) 3);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (getOwner() instanceof ServerPlayerEntity) {
			CampanionCriteria.STONE_SKIPS.trigger((ServerPlayerEntity) getOwner(), this.dataTracker.get(NUMBER_OF_SKIPS));
		}
		super.remove(reason);
	}
}
