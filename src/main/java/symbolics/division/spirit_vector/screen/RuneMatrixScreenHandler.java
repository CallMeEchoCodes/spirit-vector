package symbolics.division.spirit_vector.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.item.DreamRuneItem;
import symbolics.division.spirit_vector.item.SpiritVectorItem;

public class RuneMatrixScreenHandler extends ScreenHandler {
	public static ScreenHandlerType<RuneMatrixScreenHandler> RUNE_MATRIX =
		Registry.register(Registries.SCREEN_HANDLER,
		SpiritVectorMod.id("rune_matrix"),
		new ScreenHandlerType<>(
			RuneMatrixScreenHandler::new,
			FeatureFlags.FEATURE_MANAGER.featureSetOf()
	));

	public static void init() {}

	private static class RuneSlot extends Slot {
		public RuneSlot(Inventory inventory, int index, int x, int y) { super(inventory, index, x, y); }
		@Override public boolean canInsert(ItemStack stack) {return stack.getItem() instanceof DreamRuneItem; }
	}

	private final ScreenHandlerContext context ;
	private final Property vectorMode = Property.create();
	private final Inventory storage = new SimpleInventory(3);
	private final Slot leftSlot;
	private final Slot upSlot;
	private final Slot rightSlot;
	private ItemStack svItem = ItemStack.EMPTY;
	private final Hand currentHand;

	public RuneMatrixScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	public RuneMatrixScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(RUNE_MATRIX, syncId);
		this.context = context;
		this.leftSlot = this.addSlot(new RuneSlot(this.storage, 0, 100, 100));
		this.upSlot  = this.addSlot(new RuneSlot(this.storage, 1, 200, 100));
		this.rightSlot  = this.addSlot(new RuneSlot(this.storage, 2, 300, 100));

		this.addProperty(this.vectorMode);

		int y_offset = 127; //84

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, y_offset + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 58 + y_offset));
		}


		if (playerInventory.player.getMainHandStack().getItem() instanceof SpiritVectorItem) {
			currentHand = Hand.MAIN_HAND;
		} else {
			currentHand = Hand.OFF_HAND;
		}
		this.svItem = playerInventory.player.getStackInHand(currentHand);
		playerInventory.player.setStackInHand(currentHand, ItemStack.EMPTY);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack originalStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack stack = slot.getStack();
			originalStack = stack.copy();
			if (slot.id == leftSlot.id  || slot.id == upSlot.id  || slot.id == rightSlot.id) {
				// rune slots to inv
				if (!this.insertItem(stack, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickTransfer(stack, stack);
			} else if (slot.id >= 4 && slot.id < 40 && stack.getItem() instanceof DreamRuneItem) {
				Slot[] runeSlots = {leftSlot, upSlot, rightSlot};
				for (Slot runeSlot : runeSlots) {
					if (!runeSlot.hasStack() && !this.insertItem(stack, runeSlot.id, runeSlot.id + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (stack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}

			if (stack.getCount() == originalStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTakeItem(player, originalStack);
		}

		return originalStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		if (player.getStackInHand(this.currentHand).isEmpty()) {
			player.setStackInHand(this.currentHand, this.svItem);
		} else {
			if (!player.getInventory().insertStack(this.svItem)) {
				player.dropItem(this.svItem, true);
			}
		}
	}

	@Override
	public void onContentChanged(Inventory inventory) {
		super.onContentChanged(inventory);
	}
}
