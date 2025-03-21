package symbolics.division.spirit_vector.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HandledScreen.class)
public abstract class HandledScreenAccessor {
	@Invoker
	public abstract void invokeDrawItem(DrawContext context, ItemStack stack, int x, int y, String amountText);
}
