package com.terraformersmc.campanion.item;

import com.terraformersmc.campanion.blockentity.TentPartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceableTentItem extends Item {
	public PlaceableTentItem(Settings settings) {
		super(settings);
	}

	public boolean hasBlocks(ItemStack stack) {
		return stack.hasNbt() && stack.getOrCreateNbt().contains("Blocks", 9);
	}

	public BlockPos getSize(ItemStack stack) {
		return NbtHelper.toBlockPos(stack.getOrCreateNbt().getCompound("TentSize"));
	}

	public NbtList getBlocks(ItemStack stack) {
		return stack.getOrCreateNbt().getList("Blocks", 10);
	}

	public abstract void onPlaceTent(ItemStack stack);

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		HitResult result = user.raycast(10, 0, true);
		if (result instanceof BlockHitResult && result.getType() == HitResult.Type.BLOCK) {
			BlockPos base = ((BlockHitResult) result).getBlockPos().up();
			if (!world.isClient && getErrorPosition(world, base, stack).isEmpty()) {
				BlockPos tentSize = getSize(stack);
				traverseBlocks(stack, (pos, state, tag) -> {
					BlockPos off = base.add(pos);

					world.setBlockState(off, state);
					BlockEntity entity = world.getBlockEntity(off);
					if (entity != null && !tag.isEmpty()) {
						tag.putInt("x", off.getX());
						tag.putInt("y", off.getY());
						tag.putInt("z", off.getZ());
						entity.readNbt(tag);
						entity.markDirty();
					}
					if (entity instanceof TentPartBlockEntity) {
						((TentPartBlockEntity) entity).setLinkedPos(base);
						((TentPartBlockEntity) entity).setTentSize(tentSize);
						entity.markDirty();
					}

				});
				onPlaceTent(stack);
			}

			return new TypedActionResult<>(ActionResult.CONSUME, stack);
		}

		return super.use(world, user, hand);
	}

	public List<BlockPos> getErrorPosition(WorldView world, BlockPos pos, ItemStack stack) {
		List<BlockPos> list = new ArrayList<>();
		if (hasBlocks(stack)) {
			Vec3d changeSize = Vec3d.of(NbtHelper.toBlockPos(stack.getOrCreateNbt().getCompound("TentSize"))).add(-1, -1, -1).multiply(1 / 2F);
			for (int x = MathHelper.floor(-changeSize.x); x <= MathHelper.floor(changeSize.x); x++) {
				for (int y = -1; y <= 2 * changeSize.getY(); y++) {
					for (int z = MathHelper.floor(-changeSize.z); z <= MathHelper.floor(changeSize.z); z++) {
						BlockPos blockPos = new BlockPos(pos.add(x, y, z));
						if (y != -1 == !world.getBlockState(blockPos).getMaterial().isReplaceable()) {
							list.add(blockPos);
						}
					}
				}
			}
		}
		return list;
	}

	public void traverseBlocks(ItemStack stack, TriConsumer<BlockPos, BlockState, NbtCompound> consumer) {
		if (hasBlocks(stack)) {
			for (NbtElement block : getBlocks(stack)) {
				NbtCompound tag = (NbtCompound) block;
				BlockPos off = NbtHelper.toBlockPos(tag.getCompound("Pos"));
				BlockState state = NbtHelper.toBlockState(tag.getCompound("BlockState"));
				NbtCompound data = tag.getCompound("BlockEntityData");
				data.remove("x");
				data.remove("y");
				data.remove("z");
				consumer.accept(off, state, data);
			}
		}
	}
}
