package twilightforest.components.entity;

/**
 * The bounce of an entity whose Traveller's Boots carry Slimy Soles: the upward speed of the next bounce (set when a
 * fall that would hurt is cancelled), whether it already happened, whether the next landing bounces even without fall
 * damage, and the extra speed the next Double Jump gets from it.
 * <p>
 * Backport of 4.8's {@code twilightforest:slimy_soles_bounce_info} data attachment. 4.8 saves it; it only describes the
 * landing in progress, so here it is held unsaved by {@link TravellersWingsAttachment#getSlimySoles()}.
 */
public class SlimySolesAttachment {
	public double bounceVelocity;
	public double doubleJumpBoostVelocity;
	public boolean forceBounce;
	public boolean hasBounced;
}
