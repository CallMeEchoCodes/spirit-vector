package symbolics.division.spirit_vector.render;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.spell.Spell;

public class SpellDimensionRenderer {
	public static final SpellDimensionRenderer SDR = new SpellDimensionRenderer();
	private final PriorityQueue<BlockPos> placements = new ObjectArrayFIFOQueue<>();

//	public static final RenderPhase.ShaderProgram MATERIA_PROGRAM = new RenderPhase.ShaderProgram(GameRenderer::getRenderTypeTranslucentProgram);
//	public static final RenderLayer MATERIA_LAYER = RenderLayer.of("materia", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 786432, true, true, RenderLayer.of(MATERIA_PROGRAM));

	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double camX, double camY, double camZ) {
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(5));
		Matrix4f matrix = matrices.peek().getPositionMatrix();

		int argb = 0xFF000000;
		if (MinecraftClient.getInstance().player instanceof ISpiritVectorUser user && user.spiritVector() != null)  {
			argb |= user.spiritVector().getSFX().color();
		}

		while (!placements.isEmpty()) {
			// lightning
			var p1 = placements.dequeue();
			vc.vertex(matrix, (float)(p1.getX() - camX), (float)(p1.getY() - camY), (float)(p1.getZ() - camZ)).color(argb);
		}
	}

	public void configureSpell(Spell spell) {
		spell.setPlacementCallback(placements::enqueue);
	}

	private static int materiaColor = 0xFFFFFF;
	public static void setMateriaColor(int color) {
		materiaColor = color;
	}

	public static int provideMateriaColor () {
		return materiaColor;
	}
}
