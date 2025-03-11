package symbolics.division.spirit_vector.item;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import symbolics.division.spirit_vector.logic.ability.AbilitySlot;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorHeldAbilities;
import symbolics.division.spirit_vector.logic.vector.VectorType;
import symbolics.division.spirit_vector.screen.RuneMatrixScreenHandler;

import java.util.List;

public class SpiritVectorItem extends ArmorItem {
	public static Text RUNE_MATRIX_GUI_TITLE = Text.translatable("gui.spirit_vector.screen.rune_matrix");

    public SpiritVectorItem() {
        super(
                ArmorMaterials.DIAMOND,
                Type.BOOTS,
                new Settings()
                        .component(SpiritVectorHeldAbilities.COMPONENT, new SpiritVectorHeldAbilities())
                        .component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false))
                        .maxCount(1)
        );
    }

    @Override
    public Text getName(ItemStack stack) {
        VectorType type = stack.getOrDefault(VectorType.COMPONENT, RegistryEntry.of(VectorType.SPIRIT)).value();
        return SFXPackItem.applySFXToText(stack, this, Text.translatable("item.spirit_vector." + type.id()));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        var ab = stack.get(SpiritVectorHeldAbilities.COMPONENT);
        if (ab != null) {
            tooltip.add(Text.translatable("tooltip.spirit_vector.held_abilities").withColor(0x808080));
            tooltip.add(abilityText(ab, AbilitySlot.LEFT));
            tooltip.add(abilityText(ab, AbilitySlot.UP));
            tooltip.add(abilityText(ab, AbilitySlot.RIGHT));
        }
    }

    private MutableText abilityText(SpiritVectorHeldAbilities ab, AbilitySlot slot)  {
        // idk nobody like these
        return Text.literal("").withColor(0x808080)
                .append(Text.literal(slot.arrow).withColor(0xFFA500))
                .append(" [")
                .append(Text.keybind(slot.input.key).withColor(0xffffff))
                .append("] ")
                .append(Text.translatable(ab.get(slot).getMovement().getTranslationKey()).withColor(0xffffff));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
			user.openHandledScreen(createScreenHandlerFactory(world, user.getBlockPos()));
            ItemStack stack = user.getStackInHand(hand);
            return TypedActionResult.success(stack);
        }
        return super.use(world, user, hand);
    }

	@Nullable
	protected NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory(
			(syncId, playerInventory, player) -> new RuneMatrixScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos)),
			RUNE_MATRIX_GUI_TITLE
		);
	}
}
