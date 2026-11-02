package baguchi.spiced_twilight.api;

/**
 * Hide state of a Helmet Crab, implemented on {@link twilightforest.entity.monster.HelmetCrab} by HelmetCrabMixin.
 * <p>
 * 1.20.1 backport: upstream keeps this in a synced NeoForge data attachment (HelmetCrabHideAttachment). Here the
 * hide flag is a synced entity data value and the cooldown and hide chance are server-side fields that are saved with
 * the entity.
 */
public interface HelmetCrabHideAccess {
    int HIDE_COOLDOWN = 40;
    int STOP_HIDE_COOLDOWN = 60;
    int FORCE_STOP_HIDE_COOLDOWN = 120;

    boolean spicedTwilight$isHiding();

    void spicedTwilight$setHiding(boolean hide);

    /**
     * Pull into the helmet for {@link #HIDE_COOLDOWN} ticks.
     */
    default void spicedTwilight$hide() {
        this.spicedTwilight$setHideCooldown(HIDE_COOLDOWN);
        this.spicedTwilight$setHiding(true);
    }

    default void spicedTwilight$hideStop() {
        this.spicedTwilight$setHideCooldown(STOP_HIDE_COOLDOWN);
        this.spicedTwilight$setHiding(false);
    }

    default void spicedTwilight$hideStopForce() {
        this.spicedTwilight$setHideCooldown(FORCE_STOP_HIDE_COOLDOWN);
        this.spicedTwilight$setHiding(false);
    }

    int spicedTwilight$getHideCooldown();

    void spicedTwilight$setHideCooldown(int cooldown);

    float spicedTwilight$getHideChance();

    void spicedTwilight$setHideChance(float hideChance);

    default void spicedTwilight$tickHide() {
        int cooldown = this.spicedTwilight$getHideCooldown();
        if (cooldown > 0) {
            this.spicedTwilight$setHideCooldown(cooldown - 1);
        }
    }
}
