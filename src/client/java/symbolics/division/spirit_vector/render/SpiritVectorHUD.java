package symbolics.division.spirit_vector.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.ability.AbilitySlot;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class SpiritVectorHUD {

    private static final Identifier POISE_FULL_TEXTURE = SpiritVectorMod.id("hud/poise_full");
	private static final Identifier WING_LEFT_TEXTURE = SpiritVectorMod.id("hud/wing_left");
	private static final Identifier WING_RIGHT_TEXTURE = SpiritVectorMod.id("hud/wing_right");

	public static final Identifier SLOT_LEFT = SpiritVectorMod.id("textures/gui/slot_indicator_left.png");
	public static final Identifier SLOT_UP = SpiritVectorMod.id("textures/gui/slot_indicator_up.png");
	public static final Identifier SLOT_RIGHT = SpiritVectorMod.id("textures/gui/slot_indicator_right.png");

	@Nullable
	public static SpiritVector getSpiritVector() {
		var client = MinecraftClient.getInstance();
		var player = client.player;
		if (     player != null
			&& player.isAlive()
			&& player instanceof ISpiritVectorUser user
		) {
			return user.spiritVector();
		}
		return null;
	}

    public static int numFeathers(SpiritVector sv) {
		return (int)((float)sv.getMomentum() / SpiritVector.MAX_MOMENTUM * 10);
    }

    public static void renderPoise(DrawContext ctx, int top, int right) {
		SpiritVector sv = getSpiritVector();
		if (sv == null) return;
        int n = numFeathers(sv);
        if (n <= 0) return;

        RenderSystem.enableBlend();
        int POISE_TEXTURE_SIZE = 8;
        for (int i = 0; i < n; i++) {
            ctx.drawGuiTexture(POISE_FULL_TEXTURE, right-(POISE_TEXTURE_SIZE * (i + 1)), top, POISE_TEXTURE_SIZE, POISE_TEXTURE_SIZE);
        }

		drawSlot(ctx, sv, SLOT_LEFT, AbilitySlot.LEFT, sv.getMomentum(), n, right-5, top - 15);
		drawSlot(ctx, sv, SLOT_UP, AbilitySlot.UP, sv.getMomentum(), n, right-5, top - 15);
		drawSlot(ctx, sv, SLOT_RIGHT, AbilitySlot.RIGHT, sv.getMomentum(), n, right-5, top - 15);

        RenderSystem.disableBlend();
    }

	private static void drawSlot(DrawContext drawContext, SpiritVector sv, Identifier slotTexture, AbilitySlot slot, int momentum, int feathers, int x, int y) {
		int color = sv.getSFX().color();
		final float red = ((color >>> 16) & 0xFF) / 255f;
		final float green = ((color >>> 8) & 0xFF) / 255f;
		final float blue = (color & 0xFF) / 255f;

		var ability = sv.heldAbilities().get(slot);
		float cost = ability.cost();
		if (cost <= 0 || momentum< cost) return;

		int SLOT_WIDTH = 11;
		int SLOT_HEIGHT = 11;

		SpiritVectorHUD.drawTexture(
			drawContext.getMatrices(),
			slotTexture,
			x - (int)(cost / SpiritVector.MAX_MOMENTUM * 75), y,
			0, 0,
			SLOT_WIDTH, SLOT_HEIGHT,
			SLOT_WIDTH, SLOT_HEIGHT,
			red, green, blue,1
		);
	}

	public static void renderSoaring(DrawContext ctx) {
		int mid = ctx.getScaledWindowWidth() / 2;
		int left = mid - 160;
		int right = mid + 160;
		int top = ctx.getScaledWindowHeight() - 39;

		var client = MinecraftClient.getInstance();
		var player = client.player;
		if (     player != null
			&& player.isAlive()
			&& player instanceof ISpiritVectorUser user
		) {
			SpiritVector sv = user.spiritVector();
			if (sv == null) return;
			int WING_TEXTURE_SIZE = 32;

			if (sv.isSoaring()) {
				ctx.drawGuiTexture(WING_LEFT_TEXTURE, left + WING_TEXTURE_SIZE, top, WING_TEXTURE_SIZE, WING_TEXTURE_SIZE);
				ctx.drawGuiTexture(WING_RIGHT_TEXTURE, right - WING_TEXTURE_SIZE * 2, top, WING_TEXTURE_SIZE, WING_TEXTURE_SIZE);
			}
		}

	}

	public static void drawTexture(MatrixStack matrices, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, float r, float g, float b, float a) {
		int x2 = x + width;
		int y2 = y + height;
		drawTexturedQuad(matrices, texture, x, x2, y, y2, 0, (u + 0.0F) / (float)textureWidth, (u + (float)width) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)height) / (float)textureHeight, r, g, b, a);
	}

	public static void drawTexturedQuad(MatrixStack matrices, Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		RenderSystem.enableBlend();
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).texture(u1, v1).color(red, green, blue, alpha);
		bufferBuilder.vertex(matrix4f, (float)x1, (float)y2, (float)z).texture(u1, v2).color(red, green, blue, alpha);
		bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z).texture(u2, v2).color(red, green, blue, alpha);
		bufferBuilder.vertex(matrix4f, (float)x2, (float)y1, (float)z).texture(u2, v1).color(red, green, blue, alpha);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
}
