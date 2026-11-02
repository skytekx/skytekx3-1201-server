package baguchi.spiced_twilight.entity.goal;

import baguchi.spiced_twilight.api.HelmetCrabHideAccess;
import net.minecraft.world.entity.ai.goal.Goal;
import twilightforest.entity.monster.HelmetCrab;

import java.util.EnumSet;

public class HidingGoal extends Goal {
    public final HelmetCrab helmetCrab;

    public HidingGoal(HelmetCrab helmetCrab) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        this.helmetCrab = helmetCrab;
    }

    private HelmetCrabHideAccess hide() {
        return (HelmetCrabHideAccess) this.helmetCrab;
    }

    @Override
    public boolean canUse() {
        return this.hide().spicedTwilight$isHiding();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hide().spicedTwilight$getHideCooldown() <= 0) {
            this.hide().spicedTwilight$hideStop();
        }
    }
}
