package twilightforest.init;

import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

public final class TFSoundTypes {

	public static final SoundType JAR = new ForgeSoundType(1.0F, 1.0F, TFSounds.JAR_BREAK, TFSounds.JAR_STEP, TFSounds.JAR_PLACE, TFSounds.JAR_HIT, TFSounds.JAR_FALL);

	private TFSoundTypes() {
	}
}
