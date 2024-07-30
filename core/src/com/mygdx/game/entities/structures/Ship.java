package com.mygdx.game.entities.structures;

import com.mygdx.game.utils.Constants;
import com.mygdx.game.world.Game;

public class Ship extends Structure {

    public Ship(Builder builder) {
        super(builder);
    }

    public static class Builder extends Structure.Builder {
        private StructurePart mainPart;

        public Builder(Game game) {
            super(game);
            this.width = Constants.SHIP_SIZE;
            this.height = Constants.SHIP_SIZE;
            this.maxHp = 1000;
        }

        public void setMainPart(StructurePart part) {
            part.sprite.setSize(this.width, this.height);
            part.sprite.setOriginCenter();
            this.mainPart = part;
            addPart(mainPart);
        }

        @Override
        public Structure build() {
            if (mainPart == null) {
                throw new IllegalArgumentException("need main ship part");
            }
            return new Ship(this);
        }
    }
}
