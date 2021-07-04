package com.terraformersmc.campanion.mixin;

import com.terraformersmc.campanion.item.BackpackItem;
import com.terraformersmc.campanion.item.SleepingBagItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "isSleepingInBed", at = @At("HEAD"), cancellable = true)
	protected void isSleepingInBed(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (SleepingBagItem.getUsingStack((LivingEntity) (Object) this).isPresent()) {
			callbackInfo.setReturnValue(true);
		}
	}

	@Inject(method = "wakeUp", at = @At("HEAD"), cancellable = true)
	protected void wakeUp(CallbackInfo callbackInfo) {
		for (Hand value : Hand.values()) {
			ItemStack item = this.getStackInHand(value);
			if (SleepingBagItem.inUse(item)) {
				item.damage(1, (LivingEntity) (Object) this, e -> e.sendToolBreakStatus(value));
				SleepingBagItem.setInUse(item, false);
			}
		}
	}

	@Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
	private static void onGetPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> info) {
		Item item = stack.getItem();
		if (item instanceof BackpackItem) {
			info.setReturnValue(EquipmentSlot.CHEST);
		}
	}

	@Shadow
	public ItemStack getStackInHand(Hand hand) {
		return null;
	}

	@Shadow
	public Random getRandom() {
		return null;
	}
}
