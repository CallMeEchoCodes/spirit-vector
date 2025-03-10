package symbolics.division.spirit_vector.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.render.SpiritVectorHUD;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @WrapOperation(
            method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V")
    )
    public void moveBubbleRenderUp(DrawContext ctx, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
		SpiritVector sv = SpiritVectorHUD.getSpiritVector();
        if (sv != null && SpiritVectorHUD.numFeathers(sv) > 0) {
            original.call(ctx,texture, x, y-10, width, height);
        } else {
            original.call(ctx,texture, x, y, width, height);
        }
    }

    @WrapOperation(
            method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderFood(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;II)V")
    )
    public void renderPoiseAboveFood(InGameHud self, DrawContext ctx, PlayerEntity player, int top, int right, Operation<Void> operation) {
        operation.call(self, ctx, player, top, right);
        SpiritVectorHUD.renderPoise(ctx, top-10, right);
    }

	@Inject(
		method = "renderStatusBars",
		at = @At("HEAD")
	) public void renderSoaringWings(DrawContext context, CallbackInfo ci) {
		SpiritVectorHUD.renderSoaring(context);
	}

	@Inject(
		method = "renderHotbar",
		at = @At("HEAD")
	) public void renderEigenCode(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		SpiritVectorHUD.renderEigenCode(context);
	}
}
