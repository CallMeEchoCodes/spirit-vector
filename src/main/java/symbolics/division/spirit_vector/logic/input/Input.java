package symbolics.division.spirit_vector.logic.input;

public enum Input {
    JUMP("key.jump"), CROUCH("key.sneak"), SPRINT("key.sprint");

    public final String key;
    Input(String key) {
        this.key = key;
    }
}
