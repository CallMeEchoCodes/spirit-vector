package symbolics.division.spirit_vector;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.registry.SpiritVectorDamageTypes;
import symbolics.division.spirit_vector.sfx.SFXPack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SpiritVectorDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(SVModelGenerator::new);
		pack.addProvider(SVItemTagGenerator::new);
		pack.addProvider(SVBlockTagGenerator::new);
		pack.addProvider(SVRecipeGenerator::new);
		pack.addProvider(SVSoundTagGenerator::new);
	}

	private static class SVModelGenerator extends FabricModelProvider {

		public SVModelGenerator(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
			TextureMap map = TextureMap.all(SpiritVectorBlocks.MATERIA);
			Model model = new Model(
				Optional.of(Identifier.ofVanilla("block/leaves")),
				Optional.empty(),
				TextureKey.ALL
			);

			blockStateModelGenerator.registerSingleton(
				SpiritVectorBlocks.MATERIA,
				map,
				model
			);
		}

		@Override
		public void generateItemModels(ItemModelGenerator itemModelGenerator) {
			for (net.minecraft.item.Item item : SpiritVectorItems.getGeneratedItems()) {
				itemModelGenerator.register(item, Models.GENERATED);
			}
		}
	}

	private static class SVItemTagGenerator extends FabricTagProvider.ItemTagProvider {

		public SVItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
			super(output, completableFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

			getOrCreateTagBuilder(SpiritVectorTags.Items.SPIRIT_VECTOR_CRAFTING_MATERIALS)
				.add(Items.GOLD_INGOT)
				.addOptionalTag(ConventionalItemTags.GOLD_INGOTS)
				.addOptionalTag(SpiritVectorTags.common(RegistryKeys.ITEM, "ingots/brass"));

			getOrCreateTagBuilder(SpiritVectorTags.Items.SFX_PACK_ADDITIONS)
				.add(net.minecraft.item.Items.DIAMOND);

			getOrCreateTagBuilder(SpiritVectorTags.Items.SFX_PACK_TEMPLATES)
				.add(SpiritVectorItems.getSfxUpgradeItems().toArray(net.minecraft.item.Item[]::new));

			getOrCreateTagBuilder(SpiritVectorTags.Items.SLOT_UPGRADE_RUNES)
				.add(SpiritVectorItems.LEFT_SLOT_TEMPLATE)
				.add(SpiritVectorItems.UP_SLOT_TEMPLATE)
				.add(SpiritVectorItems.RIGHT_SLOT_TEMPLATE);

			getOrCreateTagBuilder(SpiritVectorTags.Items.ABILITY_UPGRADE_RUNES)
				.add(SpiritVectorItems.getDreamRunes().toArray(net.minecraft.item.Item[]::new));

			getOrCreateTagBuilder(ItemTags.FOOT_ARMOR).add(SpiritVectorItems.SPIRIT_VECTOR);

		}
	}

	private static class SVBlockTagGenerator extends FabricTagProvider.BlockTagProvider {
		public SVBlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
			getOrCreateTagBuilder(SpiritVectorTags.Blocks.RAIL_GRINDABLE)
				.addOptionalTag(BlockTags.FENCES)
				.addOptionalTag(BlockTags.FENCE_GATES)
				.addOptionalTag(BlockTags.WALLS);

			getOrCreateTagBuilder(SpiritVectorTags.Blocks.WALL_JUMPABLE)
				.add(SpiritVectorBlocks.MATERIA);

			getOrCreateTagBuilder(SpiritVectorTags.Blocks.WALL_RUSHABLE)
				.add(SpiritVectorBlocks.MATERIA);
		}
	}

	private static class SVSoundTagGenerator extends FabricTagProvider<SoundEvent> {
		public SVSoundTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.SOUND_EVENT, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
			getOrCreateTagBuilder(SpiritVectorTags.Misc.JUKEBOX_LOOPING)
				.add(SpiritVectorSounds.TAKE_BREAK_LOOP)
				.add(SpiritVectorSounds.SHOW_DONE_LOOP);
		}
	}

	private static class SVDamageTagGenerator extends FabricTagProvider<DamageType> {
		public SVDamageTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
			getOrCreateTagBuilder(DamageTypeTags.DAMAGES_HELMET)
				.add(SpiritVectorDamageTypes.FOOTSTOOL);

			getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD)
				.add(SpiritVectorDamageTypes.FOOTSTOOL);
		}
	}

	private static class SVRecipeGenerator extends FabricRecipeProvider {

		public SVRecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		public void generate(RecipeExporter exporter) {
			// sv per-core recipes
			for (var core : SpiritVectorItems.getSfxUpgradeItems()) {
				genSpiritVectorRecipe(exporter, core);
			}
		}

		private void genVectorRuneRecipe(RecipeExporter exporter, Item rune, String... shape) {
			var builder = ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, rune)
				.input('e', ConventionalItemTags.EMERALD_GEMS)
				.input('s', ConventionalItemTags.STONES);
			for (String line : shape) builder.pattern(line);
			builder.criterion("has_emerald", conditionsFromItem(net.minecraft.item.Items.EMERALD))
				.offerTo(exporter);
		}

		private void genSpiritVectorRecipe(RecipeExporter exporter, Item core) {
			Identifier recipeId = Identifier.of(RecipeProvider.getItemPath(SpiritVectorItems.SPIRIT_VECTOR) + "_crafted_from_" + RecipeProvider.getItemPath(core));
			ItemStack stack = SpiritVectorItems.SPIRIT_VECTOR.getDefaultStack();

			stack.set(SFXPack.COMPONENT, core.getComponents().get(SFXPack.COMPONENT));
			stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(core.getDefaultStack())));


			Advancement.Builder builder = exporter.getAdvancementBuilder()
				.criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
				.rewards(AdvancementRewards.Builder.recipe(recipeId))
				.criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
			builder.criterion("has_core", conditionsFromItem(core));
			RawShapedRecipe rawRecipe = RawShapedRecipe.create(
				Map.of('g', Ingredient.fromTag(SpiritVectorTags.Items.SPIRIT_VECTOR_CRAFTING_MATERIALS), 'c', Ingredient.ofItems(core)),
				"gcg",
				"g g"
			);
			ShapedRecipe recipe = new ShapedRecipe(
				"",
				CraftingRecipeJsonBuilder.toCraftingCategory(RecipeCategory.TRANSPORTATION),
				rawRecipe,
				stack.copy(),
				true
			);
			exporter.accept(recipeId, recipe, builder.build(recipeId.withPrefixedPath("recipes/" + RecipeCategory.TRANSPORTATION.getName() + "/")));
		}
	}
}
