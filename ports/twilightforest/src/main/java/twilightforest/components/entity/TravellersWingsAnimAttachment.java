package twilightforest.components.entity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Client-side bookkeeping for the procedural Traveller's Wings animation: the accumulated flap phase and the wing
 * rotations of the previous frame, which the model eases towards the target pose of the current {@link TravellersWingsAttachment.WingState}.
 * Backport of 4.8's {@code twilightforest:travellers_wings_anim} attachment; held by {@link TravellersWingsAttachment#getAnim()}.
 */
public class TravellersWingsAnimAttachment {
	public double accumulatedPhase = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);  // Desync instances of wings when entering the game
	public double oldAgeInTicks = 0;
	public float xRotOld = 0;
	public float yRotOldLeft = 0;
	public float yRotOldRight = 0;
	public float zRotOld = 0;
	public boolean doubleJump = false;
}
