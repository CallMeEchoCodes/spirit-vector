package symbolics.division.spirit_vector.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.SpiritVectorMod;

public class RuneMatrixScreen extends HandledScreen<RuneMatrixScreenHandler> {
	private static final Identifier TEXTURE = SpiritVectorMod.id("textures/gui/container/rune_matrix.png");
	private static final Identifier INVENTORY_TEXTURE = SpiritVectorMod.id("textures/gui/container/basic_inventory.png");

	public RuneMatrixScreen(RuneMatrixScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);

	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
//		context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
		context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY + 44, 4210752, false);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.drawTexture(TEXTURE, this.x+29, this.y-20, 0, 0, this.backgroundWidth,  this.backgroundHeight);
		context.drawTexture(INVENTORY_TEXTURE, this.x, this.y+110, 0, 0, this.backgroundWidth,  this.backgroundHeight);
	}

	private void onInventoryChanged() {

	}

	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
		// just ensure we're in the correct column
		return super.isClickOutsideBounds(mouseX, top, left, top, button);
	}
}
