package symbolics.division.spirit_vector.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import symbolics.division.spirit_vector.SpiritVectorItems;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.logic.ability.AbilitySlot;

// todo: remove gauge, superseded by HUD
@Deprecated
public class SpiritGaugeHUD {

	public static final Identifier SLOT_LEFT = SpiritVectorMod.id("textures/gui/slot_indicator_left.png");
	public static final Identifier SLOT_UP = SpiritVectorMod.id("textures/gui/slot_indicator_up.png");
	public static final Identifier SLOT_RIGHT = SpiritVectorMod.id("textures/gui/slot_indicator_right.png");

    private static final int WIDTH = 9;
    private static final int HEIGHT = 85;
    private static final int VALUE_WIDTH = 3;
    private static final int VALUE_HEIGHT = 77;
    private static final int VALUE_OFFSET = 4;
    private static final int SLOT_WIDTH = 11;
    private static final int SLOT_HEIGHT = 11;
    private static final int SLOT_OFFSET = 5;
    private static final Identifier BAR_BG = SpiritVectorMod.id("textures/gui/momentum_meter_bg.png");
    private static final Identifier BAR_FG = SpiritVectorMod.id("textures/gui/momentum_meter_fg.png");
    private static final Identifier BAR_VALUE = SpiritVectorMod.id("textures/gui/momentum_meter_value.png");

    private static final SpiritGaugeHUD hud = new SpiritGaugeHUD();

    public static void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (    player.isAlive()
                && shouldRenderGauge(player)
                && player instanceof ISpiritVectorUser user) {
            var sv = user.spiritVector();
            if (sv == null) return;
            hud.render(drawContext, tickCounter, sv);
        }
    }

    private static boolean shouldRenderGauge(ClientPlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).isOf(SpiritVectorItems.MOMENTUM_GAUGE)
                || player.getStackInHand(Hand.OFF_HAND).isOf(SpiritVectorItems.MOMENTUM_GAUGE);
    }

    private int trackedMomentum;

    public void render(DrawContext drawContext, RenderTickCounter tickCounter, SpiritVector sv) {

        int momentumDiff = sv.getMomentum() - trackedMomentum;
        if (Math.abs(momentumDiff) <= 1) {
            trackedMomentum = sv.getMomentum();
        } else {
            int delta = (int)((float)momentumDiff / 20f);
            trackedMomentum += delta != 0 ? delta : Math.signum(momentumDiff);
        }

        int visibleHeight = (int)(VALUE_HEIGHT * ((float)trackedMomentum / SpiritVector.MAX_MOMENTUM));
        int x = 5;
        int y = (int)(drawContext.getScaledWindowHeight() * 0.05);

        drawContext.drawTexture(
                BAR_BG, x, y,0, 0,WIDTH, HEIGHT, WIDTH, HEIGHT
        );

        int color = sv.getSFX().color();
        final float red = ((color >>> 16) & 0xFF) / 255f;
        final float green = ((color >>> 8) & 0xFF) / 255f;
        final float blue = (color & 0xFF) / 255f;

		SpiritVectorHUD.drawTexture(
                drawContext.getMatrices(),
                BAR_VALUE,
                x + 3, y + (HEIGHT + 1 - VALUE_OFFSET - visibleHeight),
                0, VALUE_HEIGHT - visibleHeight,
                VALUE_WIDTH, visibleHeight ,
                VALUE_WIDTH, VALUE_HEIGHT,
                red, green, blue, 1
        );

        drawContext.drawTexture(
                BAR_FG, x, y,0, 0,WIDTH, HEIGHT, WIDTH, HEIGHT
        );

        final int baseY = y + VALUE_OFFSET;
        this.drawSlot(drawContext, sv, SLOT_LEFT, AbilitySlot.LEFT, x+VALUE_OFFSET+2, baseY, VALUE_HEIGHT, red, green, blue);
        this.drawSlot(drawContext, sv, SLOT_UP, AbilitySlot.UP, x+VALUE_OFFSET+2, baseY, VALUE_HEIGHT, red, green, blue);
        this.drawSlot(drawContext, sv, SLOT_RIGHT, AbilitySlot.RIGHT, x+VALUE_OFFSET+2, baseY, VALUE_HEIGHT, red, green, blue);

        boolean debugHUD = true;
        if (debugHUD) {
            drawContext.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    Text.literal(sv.getMoveState().getID().toString()),
                    30, 30, 0xFFFFFF, false
            );
        }
    }

    private void drawSlot(DrawContext drawContext, SpiritVector sv, Identifier slotTexture, AbilitySlot slot, int x, int baseY, int h, float r, float g, float b) {
        var ability = sv.heldAbilities().get(slot);
        float cost = (float)ability.cost() / SpiritVector.MAX_MOMENTUM;
        if (cost <= 0) return;
        int offset = h - (int)(h * cost);

		SpiritVectorHUD.drawTexture(
                drawContext.getMatrices(),
                slotTexture,
                x + SLOT_OFFSET, baseY + offset - SLOT_OFFSET,
                0, 0,
                SLOT_WIDTH, SLOT_HEIGHT,
                SLOT_WIDTH, SLOT_HEIGHT,
                r, g, b,1
        );
    }
}
