package symbolics.division.spirit_vector;

import net.minecraft.client.MinecraftClient;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.input.Arrow;
import symbolics.division.spirit_vector.logic.input.ArrowManager;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.input.InputManager;

public final class InputHandler {
    public static void tick(MinecraftClient client) {
        if (client.player != null && !client.player.isDead() && client.player instanceof ISpiritVectorUser user) {
            user.getSpiritVector().ifPresent(sv -> {
                InputManager input = sv.inputManager();
                input.update(Input.JUMP, client.options.jumpKey.isPressed());
                input.update(Input.CROUCH, client.options.sneakKey.isPressed());
                input.update(Input.SPRINT, client.options.sprintKey.isPressed());

				ArrowManager arrow = sv.arrowManager();
				arrow.update(Arrow.UP, client.options.forwardKey.isPressed());
				arrow.update(Arrow.DOWN, client.options.backKey.isPressed());
				arrow.update(Arrow.LEFT, client.options.leftKey.isPressed());
				arrow.update(Arrow.RIGHT, client.options.rightKey.isPressed());
            });
        }
    }
}
