package symbolics.division.spirit_vector.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.SpiritVectorSounds;
import symbolics.division.spirit_vector.logic.vector.VectorType;
import symbolics.division.spirit_vector.render.SpiritVectorHUD;

public class RuneMatrixScreen extends HandledScreen<RuneMatrixScreenHandler> {
	private static final Identifier TEXTURE = SpiritVectorMod.id("textures/gui/container/rune_matrix.png");
	private static final Identifier INVENTORY_TEXTURE = SpiritVectorMod.id("textures/gui/container/basic_inventory.png");

	private static final int MODE_SLOT_LEFT_OFFSET = 56 + 25;
	private static final int MODE_SLOT_TOP_OFFSET = 7 + 50;
	private Identifier modeSprite = SpiritVectorMod.id("rune_matrix/vector_mode_spirit");
	private int prevMode = 0;

	private static final int SLOT_FULL_SIZE = 28;
	private static final Identifier SLOT_FULL_TEXTURE = SpiritVectorMod.id("rune_matrix/slot_full");
	private static final Identifier CORE_EMPTY_TEXTURE = SpiritVectorMod.id("rune_matrix/socket_empty");

	public RuneMatrixScreen(RuneMatrixScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		modeSprite = getModeSprite();
		prevMode = this.handler.getVectorMode();
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY + 44, 4210752, false);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.drawTexture(TEXTURE, this.x + 29, this.y - 20, 0, 0, this.backgroundWidth, this.backgroundHeight);
		context.drawTexture(INVENTORY_TEXTURE, this.x, this.y + 110, 0, 0, this.backgroundWidth, this.backgroundHeight);

		if (prevMode != this.handler.getVectorMode()) {
			prevMode = this.handler.getVectorMode();
			this.modeSprite = getModeSprite();
		}
		context.drawGuiTexture(this.modeSprite, this.x + MODE_SLOT_LEFT_OFFSET, this.y + MODE_SLOT_TOP_OFFSET, 16, 16);

		renderSlotInset(context, this.handler.leftSlot, 0, 0);
		renderSlotInset(context, this.handler.upSlot, 25, -25);
		renderSlotInset(context, this.handler.rightSlot, 50, 0);
		if (!this.handler.coreSlot.hasStack()) {
			context.drawGuiTexture(CORE_EMPTY_TEXTURE, this.x + 50 + 29, this.y + 26 + 4, 20, 20);
		}
	}

	private void renderSlotInset(DrawContext context, Slot slot, int x, int y) {
		if (!slot.hasStack()) return;
		context.drawGuiTexture(SLOT_FULL_TEXTURE, this.x + 50 + x, this.y + 26 + y, SLOT_FULL_SIZE, SLOT_FULL_SIZE);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int x1 = MODE_SLOT_LEFT_OFFSET + this.x;
		int x2 = x1 + 16;
		int y1 = MODE_SLOT_TOP_OFFSET + this.y;
		int y2 = y1 + 16;
		if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
			SpiritVectorHUD.playUISound(SpiritVectorSounds.RUNE_MATRIX_BUZZ, 1);
			this.client.interactionManager.clickButton(this.handler.syncId, 1);
		} else if (this.focusedSlot != null && (!this.handler.getCursorStack().isEmpty() || this.focusedSlot.hasStack())) {
			SpiritVectorHUD.playUISound(SpiritVectorSounds.RUNE_MATRIX_CLICK, 1);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}


	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
		// just ensure we're in the correct column
		return super.isClickOutsideBounds(mouseX, top, left, top, button);
	}

	private Identifier getModeSprite() {
		var vectorMode = VectorType.REGISTRY.getEntry(this.handler.getVectorMode()).orElseThrow();
		var id = vectorMode.getKey().orElseThrow().getValue().getPath();
		return SpiritVectorMod.id("rune_matrix/vector_mode_" + id);
	}

	@Override
	protected void drawItem(DrawContext context, ItemStack stack, int x, int y, String amountText) {
		if (stack.equals(handler.coreSlot.getStack())) return;
		super.drawItem(context, stack, x, y, amountText);
	}

	@Override
	protected void drawSlot(DrawContext context, Slot slot) {
		if (slot == handler.coreSlot) {
			return;
//			int i = slot.x;
//			int j = slot.y;
//			ItemStack itemStack = slot.getStack();
//			boolean bl = false;
//			ItemStack itemStack2 = this.handler.getCursorStack();
//			String string = null;
//			int k;
//
//			context.getMatrices().push();
//			context.getMatrices().translate(0.0F, 0.0F, 100.0F);
//			if (itemStack.isEmpty() && slot.isEnabled()) {
//				Pair<Identifier, Identifier> pair = slot.getBackgroundSprite();
//				if (pair != null) {
//					Sprite sprite = (Sprite)this.client.getSpriteAtlas((Identifier)pair.getFirst()).apply((Identifier)pair.getSecond());
//					context.drawSprite(i, j, 0, 16, 16, sprite);
//				}
//			}
//
//			if (!bl2) {
//			if (bl) {
//				context.fill(i, j, i + 16, j + 16, -2130706433);
//			}
//
//			k = slot.x + slot.y * this.backgroundWidth;
//			if (slot.disablesDynamicDisplay()) {
//				context.drawItemWithoutEntity(itemStack, i, j, k);
//			} else {
//				context.drawItem(itemStack, i, j, k);
//			}
//
//			context.drawItemInSlot(this.textRenderer, itemStack, i, j, string);
//			}
//
//			context.getMatrices().pop();
		}
		super.drawSlot(context, slot);
	}
}
