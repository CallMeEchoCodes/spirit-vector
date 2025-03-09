package symbolics.division.spirit_vector.screen;

import net.minecraft.component.ComponentMap;
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
import symbolics.division.spirit_vector.logic.ability.AbilitySlot;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorAbilitiesRegistry;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorAbility;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorHeldAbilities;

import java.util.Objects;

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
	private final Inventory storage = new SimpleInventory(3) {
		@Override
		public void markDirty() {
			super.markDirty();
			RuneMatrixScreenHandler.this.onContentChanged(this);
		}
	};
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

		int slotLeftOffset = 56;
		int slotTopOffset = 7;
		this.leftSlot = this.addSlot(new RuneSlot(this.storage, 0, slotLeftOffset, slotTopOffset + 25));
		this.upSlot  = this.addSlot(new RuneSlot(this.storage, 1, slotLeftOffset + 25 , slotTopOffset));
		this.rightSlot  = this.addSlot(new RuneSlot(this.storage, 2, slotLeftOffset + 50, slotTopOffset + 25));

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

		ComponentMap components = this.svItem.getComponents();
		SpiritVectorHeldAbilities abilities = components.get(SpiritVectorHeldAbilities.COMPONENT);
		if (abilities != null) {
			for (AbilitySlot slot : AbilitySlot.values()) {
				SpiritVectorAbility ability = abilities.get(slot);
				if (ability != SpiritVectorAbility.NONE) {
					getRuneSlot(slot).setStack(SpiritVectorAbilitiesRegistry.getRuneForAbility(ability).getDefaultStack());
				}
			}
		}
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
				if (!this.insertItem(stack, 3, 39, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickTransfer(stack, stack);
			} else if (slot.id >= 3 && slot.id < 39 && stack.getItem() instanceof DreamRuneItem) {
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
		SpiritVectorHeldAbilities newAbilities = new SpiritVectorHeldAbilities();
		SpiritVectorHeldAbilities oldAbilities = svItem.getComponents().getOrDefault(
			SpiritVectorHeldAbilities.COMPONENT,
			new SpiritVectorHeldAbilities()
		);
		for (AbilitySlot abilitySlot : AbilitySlot.values()) {
			newAbilities.set(abilitySlot, oldAbilities.get(abilitySlot));
			Slot slot = getRuneSlot(abilitySlot);
			ItemStack stack = slot.getStack();
			if (stack != null) {
				SpiritVectorAbility ability = stack.getComponents().get(SpiritVectorAbility.COMPONENT);
				newAbilities.set(abilitySlot, Objects.requireNonNullElse(ability, SpiritVectorAbility.NONE));
			}
		}
		svItem.set(SpiritVectorHeldAbilities.COMPONENT, newAbilities);
	}

	private Slot getRuneSlot(AbilitySlot slot) {
		return switch (slot) {
			case AbilitySlot.LEFT -> this.leftSlot;
			case AbilitySlot.UP -> this.upSlot;
			case AbilitySlot.RIGHT -> this.rightSlot;
		};
	}
}
