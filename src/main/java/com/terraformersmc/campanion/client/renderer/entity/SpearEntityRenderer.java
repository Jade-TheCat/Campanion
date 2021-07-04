package com.terraformersmc.campanion.client.renderer.entity;

import com.terraformersmc.campanion.Campanion;
import com.terraformersmc.campanion.client.model.entity.SpearEntityModel;
import com.terraformersmc.campanion.entity.SpearEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class SpearEntityRenderer extends EntityRenderer<SpearEntity> {
	private static final Map<EntityType<?>, Identifier> TEXTURES = new HashMap<>();
	private final SpearEntityModel model = new SpearEntityModel();

	public SpearEntityRenderer(EntityRendererFactory.Context factoryCtx) {
		super(factoryCtx);
	}

	@Override
	public void render(SpearEntity spear, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		matrixStack.push();
		matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(MathHelper.lerp(g, spear.prevYaw, spear.getYaw()) - 90.0F));
		matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.lerp(g, spear.prevPitch, spear.getPitch()) + 90.0F));
		VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, model.getLayer(this.getTexture(spear)), false, spear.method_23751());
		model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStack.scale(2.0F, -2.0F, -2.0F);
		matrixStack.pop();
		super.render(spear, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	public Identifier getTexture(SpearEntity spear) {
		return getTexture(spear.getType());
	}

	public static Identifier getTexture(EntityType<?> type) {
		if (!TEXTURES.containsKey(type)) {
			TEXTURES.put(type, new Identifier(Campanion.MOD_ID, "textures/entity/spear/" + Registry.ENTITY_TYPE.getId(type).getPath() + ".png"));
		}
		return TEXTURES.get(type);
	}
}
