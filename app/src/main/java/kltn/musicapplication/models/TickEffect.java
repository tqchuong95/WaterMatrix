package kltn.musicapplication.models;

import java.io.Serializable;

/**
 * Created by UITCV on 11/29/2017.
 */

public class TickEffect implements Serializable {
    Effect effect;
    boolean active;

    public TickEffect(Effect effect) {
        this.effect = effect;
        this.active = true;
    }

    public TickEffect(Effect effect, boolean active) {
        this.effect = effect;
        this.active = active;
    }

    public Effect getEffect() {
        return effect;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.effect.getTitle();
    }
}
