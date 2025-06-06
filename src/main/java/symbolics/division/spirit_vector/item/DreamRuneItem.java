package symbolics.division.spirit_vector.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.ability.SpiritVectorAbility;

import java.util.List;

public class DreamRuneItem extends Item {
	public DreamRuneItem(SpiritVectorAbility ability) {
		super(new Item.Settings().component(SpiritVectorAbility.COMPONENT, ability).maxCount(1));
	}

	@SuppressWarnings("deprecated")
	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		SpiritVectorAbility ability = stack.getOrDefault(SpiritVectorAbility.COMPONENT, SpiritVectorAbility.NONE);
		tooltip.add(Text.translatable("tooltip.spirit_vector.dream_rune_contents", Text.translatable(ability.getMovement().getTranslationKey()).withColor(0xfffffff)).withColor(0x808080));

		String desc = Text.translatable("tooltip.desc." + ability.abilityTranslationKey()).getString();
		final int MAX_CHARS_PER_LINE = 25;
		String wrapped = WordUtils.wrap(desc, MAX_CHARS_PER_LINE, "%%", false);
		for (String s : wrapped.split("%%")) {
			tooltip.add(Text.literal(s).withColor(0xcccccc));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		SpiritVectorAbility ability = stack.get(SpiritVectorAbility.COMPONENT);
		if (ability != null && player instanceof ISpiritVectorUser svUser) {
			boolean succ = svUser.getSpiritVector().map(sv -> sv.enqueueAbility(ability)).orElse(false);
			return succ ? TypedActionResult.success(stack, true) : TypedActionResult.fail(stack);
		}
		return super.use(world, player, hand);
	}
}
