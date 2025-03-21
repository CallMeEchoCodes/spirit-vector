package symbolics.division.spirit_vector.screen;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.SpiritVectorSounds;
import symbolics.division.spirit_vector.item.DreamRuneItem;
import symbolics.division.spirit_vector.item.SpiritVectorItem;
import symbolics.division.spirit_vector.logic.ability.AbilitySlot;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorAbilitiesRegistry;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorAbility;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorHeldAbilities;
import symbolics.division.spirit_vector.logic.vector.VectorType;
import symbolics.division.spirit_vector.sfx.SFXPack;

import java.util.List;
import java.util.Objects;

public class RuneMatrixScreenHandler extends ScreenHandler {
	public static ScreenHandlerType<RuneMatrixScreenHandler> RUNE_MATRIX =
		Registry.register(Registries.SCREEN_HANDLER,
			SpiritVectorMod.id("rune_matrix"),
			new ScreenHandlerType<>(
				RuneMatrixScreenHandler::new,
				FeatureFlags.FEATURE_MANAGER.featureSetOf()
			));

	public static void init() {
	}

	public static NamedScreenHandlerFactory createScreenHandlerFactory(boolean wearing, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory(
			(syncId, playerInventory, player) -> new RuneMatrixScreenHandler(wearing, syncId, playerInventory, ScreenHandlerContext.create(world, pos)),
			SpiritVectorItem.RUNE_MATRIX_GUI_TITLE
		);
	}

	private static class RuneSlot extends Slot {
		public RuneSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return stack.getItem() instanceof DreamRuneItem;
		}

		@Override
		public boolean canBeHighlighted() {
			return false;
		}
	}

	private static class CoreSlot extends Slot {
		public CoreSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return coreLike(stack);
		}

		@Override
		public boolean canBeHighlighted() {
			return false;
		}

		public static boolean coreLike(ItemStack stack) {
			return stack.get(SFXPack.COMPONENT) != null;
		}
	}

	private final ScreenHandlerContext context;
	private final Property vectorMode = Property.create();
	private final Inventory storage = new SimpleInventory(4) {
		@Override
		public void markDirty() {
			super.markDirty();
			RuneMatrixScreenHandler.this.onContentChanged(this);
		}
	};
	public final Slot leftSlot;
	public final Slot upSlot;
	public final Slot rightSlot;
	public final Slot coreSlot;
	private final Hand currentHand;
	private final PlayerEntity player;
	private final boolean wearing;

	public RuneMatrixScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(true, syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}

	public RuneMatrixScreenHandler(boolean wearing, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(RUNE_MATRIX, syncId);
		this.context = context;
		this.wearing = wearing;
		this.player = playerInventory.player;
		if (player.getWorld().isClient) {
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SpiritVectorSounds.RUNE_MATRIX_CLICK, SoundCategory.PLAYERS, 1.0f, 1);
		}

		int slotLeftOffset = 56;
		int slotTopOffset = 7;
		this.leftSlot = this.addSlot(new RuneSlot(this.storage, 0, slotLeftOffset, slotTopOffset + 25));
		this.upSlot = this.addSlot(new RuneSlot(this.storage, 1, slotLeftOffset + 25, slotTopOffset));
		this.rightSlot = this.addSlot(new RuneSlot(this.storage, 2, slotLeftOffset + 50, slotTopOffset + 25));
		this.coreSlot = this.addSlot(new CoreSlot(this.storage, 3, slotLeftOffset + 25, slotTopOffset + 25));

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

		ComponentMap components = getTargetStack().getComponents();
		SpiritVectorHeldAbilities abilities = components.get(SpiritVectorHeldAbilities.COMPONENT);
		if (abilities != null) {
			for (AbilitySlot slot : AbilitySlot.values()) {
				SpiritVectorAbility ability = abilities.get(slot);
				if (ability != SpiritVectorAbility.NONE) {
					getRuneSlot(slot).setStack(SpiritVectorAbilitiesRegistry.getRuneForAbility(ability).getDefaultStack());
				}
			}
		}

		ContainerComponent containerComponent = components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
		if (containerComponent != null) {
			ItemStack stack = containerComponent.copyFirstStack();
			if (!stack.isEmpty()) {
				this.coreSlot.setStack(stack);
			}
		}

		RegistryEntry<VectorType> mode = getTargetStack().get(VectorType.COMPONENT);
		if (mode != null) {
			var v = mode.value();
			this.vectorMode.set(VectorType.REGISTRY.getRawId(v));
		} else {
			this.vectorMode.set(VectorType.REGISTRY.getRawIdOrThrow(VectorType.SPIRIT));
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack originalStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack stack = slot.getStack();
			originalStack = stack.copy();
			if (slot.id == leftSlot.id || slot.id == upSlot.id || slot.id == rightSlot.id || slot.id == coreSlot.id) {
				// rune slots to inv
				if (!this.insertItem(stack, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickTransfer(stack, stack);
			} else if (slot.id >= 4 && slot.id < 40) {
				if (stack.getItem() instanceof DreamRuneItem) {
					Slot[] runeSlots = {leftSlot, upSlot, rightSlot};
					for (Slot runeSlot : runeSlots) {
						if (!runeSlot.hasStack() && !this.insertItem(stack, runeSlot.id, runeSlot.id + 1, false)) {
							return ItemStack.EMPTY;
						}
					}
				} else if (CoreSlot.coreLike(stack) && !coreSlot.hasStack() && !this.insertItem(stack, coreSlot.id, coreSlot.id + 1, false)) {
					return ItemStack.EMPTY;
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
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == 1) {
			updateVectorMode();
			return true;
		} else {
			return super.onButtonClick(player, id);
		}
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		int ix = player.getInventory().selectedSlot;
		if (actionType != SlotActionType.THROW && currentHand == Hand.MAIN_HAND && ix == slotIndex - 31) {
			return; //prevent grabbing spirit vector in hotbar
		}
		super.onSlotClick(slotIndex, button, actionType, player);
	}

	@Override
	public void onContentChanged(Inventory inventory) {
		super.onContentChanged(inventory);
		ItemStack stack = getTargetStack().copy();
		SpiritVectorHeldAbilities newAbilities = new SpiritVectorHeldAbilities();
		SpiritVectorHeldAbilities oldAbilities = stack.getComponents().getOrDefault(
			SpiritVectorHeldAbilities.COMPONENT,
			new SpiritVectorHeldAbilities()
		);
		for (AbilitySlot abilitySlot : AbilitySlot.values()) {
			newAbilities.set(abilitySlot, oldAbilities.get(abilitySlot));
			Slot slot = getRuneSlot(abilitySlot);
			ItemStack runeStack = slot.getStack();
			if (runeStack != null) {
				SpiritVectorAbility ability = runeStack.getComponents().get(SpiritVectorAbility.COMPONENT);
				newAbilities.set(abilitySlot, Objects.requireNonNullElse(ability, SpiritVectorAbility.NONE));
			}
		}
		stack.set(SpiritVectorHeldAbilities.COMPONENT, newAbilities);

		ItemStack core = coreSlot.getStack();
		if (!core.isEmpty()) {
			stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(core)));
		} else {
			stack.set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
		}

		stack.set(SFXPack.COMPONENT, core.get(SFXPack.COMPONENT));

		setTargetStack(stack);
	}

	private Slot getRuneSlot(AbilitySlot slot) {
		return switch (slot) {
			case AbilitySlot.LEFT -> this.leftSlot;
			case AbilitySlot.UP -> this.upSlot;
			case AbilitySlot.RIGHT -> this.rightSlot;
		};
	}

	private void updateVectorMode() {
		int rawId = (this.vectorMode.get() + 1) % VectorType.REGISTRY.size();
		this.vectorMode.set(rawId);
		getTargetStack().set(VectorType.COMPONENT, VectorType.REGISTRY.getEntry(rawId).orElseThrow());
	}

	public int getVectorMode() {
		return this.vectorMode.get();
	}

	private ItemStack getTargetStack() {
		if (wearing) {
			return player.getEquippedStack(EquipmentSlot.FEET);
		}
		return player.getStackInHand(this.currentHand);
	}

	private void setTargetStack(ItemStack stack) {
		if (wearing) {
			player.equipStack(EquipmentSlot.FEET, stack);
		} else {
			player.setStackInHand(currentHand, stack);
		}
		player.getInventory().markDirty();
	}
}
