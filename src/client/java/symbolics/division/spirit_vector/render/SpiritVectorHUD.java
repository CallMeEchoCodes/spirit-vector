package symbolics.division.spirit_vector.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class SpiritVectorHUD {

    private static final Identifier POISE_FULL_TEXTURE = SpiritVectorMod.id("hud/poise_full");
	private static final Identifier WING_LEFT_TEXTURE = SpiritVectorMod.id("hud/wing_left");
	private static final Identifier WING_RIGHT_TEXTURE = SpiritVectorMod.id("hud/wing_right");

    public static int numFeathers() {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (     player != null
                && player.isAlive()
                && player instanceof ISpiritVectorUser user
        ) {
            SpiritVector sv = user.spiritVector();
            if (sv == null) return 0;
            return (int)((float)sv.getMomentum() / SpiritVector.MAX_MOMENTUM * 10);
        }
        return 0;
    }

    public static void renderPoise(DrawContext ctx, int top, int right) {
        int n = numFeathers();
        if (n <= 0) return;
        RenderSystem.enableBlend();
        int POISE_TEXTURE_SIZE = 8;
        for (int i = 0; i < n; i++) {
            ctx.drawGuiTexture(POISE_FULL_TEXTURE, right-(POISE_TEXTURE_SIZE * (i + 1)), top, POISE_TEXTURE_SIZE, POISE_TEXTURE_SIZE);
        }
        RenderSystem.disableBlend();
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
}
