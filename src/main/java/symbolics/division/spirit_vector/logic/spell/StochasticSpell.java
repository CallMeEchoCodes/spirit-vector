package symbolics.division.spirit_vector.logic.spell;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.logic.input.Arrow;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.ArrayList;
import java.util.List;

public class StochasticSpell extends Spell {
	protected List<Eidos> eidola = new ArrayList<>();

	public StochasticSpell(SpiritVector sv) {
		super(sv, List.of());
		for (int i = 0; i<20; i++) {
			int n = sv.user.getRandom().nextBetween(8, 16);
			List<Arrow> code = new ArrayList<>();
			for (int j = 0; j < n; j++) {
				code.add(Arrow.values()[sv.user.getRandom().nextInt(4)]);
			}
			eidola.add(makeCore(code));
		}
		this.complexity = 12;
		this.ticksLeft = (int) (MIN_SPELL_TICKS + (MAX_SPELL_TICKS - MIN_SPELL_TICKS) * sv.user.getRandom().nextBetween(10, 16));
	}

	@Override
	protected Eidos emplace(World world, BlockPos bp, Direction d, float decay) {
		Eidos e = eidola.get(world.getRandom().nextInt(eidola.size()));
		e.emplace(world, bp, d, 0);
		return e;
	}
}
