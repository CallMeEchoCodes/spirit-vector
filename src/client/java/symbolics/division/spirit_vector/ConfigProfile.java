package symbolics.division.spirit_vector;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "spirit_vector")
public class ConfigProfile implements ConfigData {
	boolean playSound = true;

	public static boolean playSound() {
		return AutoConfig.getConfigHolder(ConfigProfile.class).getConfig().playSound;
	}

	public static void setPlaySound(boolean v) {
		ConfigProfile c = AutoConfig.getConfigHolder(ConfigProfile.class).getConfig();
		c.playSound = v;
		AutoConfig.getConfigHolder(ConfigProfile.class).save();
	}
}
