package com.mygdx.game.entities.structures;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.ai.MessageType;
import com.mygdx.game.audio.SpatialMusic;
import com.mygdx.game.audio.SpatialSoundLooping;
import com.mygdx.game.utils.AudioAssets;
import com.mygdx.game.utils.Constants;
import com.mygdx.game.world.Game;

public class Ship extends Structure {

    private boolean isStarting = false;
    private boolean started = false;
    private SpatialMusic startingSFX;

    public Ship(Builder builder) {
        super(builder);

        game.ui.setShipButtonListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TextButton button = (TextButton) actor;
                if (button.getLabel().getText().toString().equals(Constants.START)) {
                    button.setText(Constants.LAUNCH);
                    start();
                }
            }
        });
    }

    private void start() {
        isStarting = true;
        startingSFX = game.audio.newSpatialMusicSFX(AudioAssets.SHIP_START);
        startingSFX.setPosition(getCenter());
        startingSFX.getMusic().play();
    }

    private void checkStarting() {
        if (isStarting && !startingSFX.getMusic().isPlaying()) { // finished starting
            startingSFX.dispose();
            started = true;
            isStarting = false;
            SpatialSoundLooping music = game.audio.newLoopingSpatialSoundEffect(AudioAssets.SHIP_MINE);
            music.setPosition(getCenter());
            music.play();
            MessageManager.getInstance().dispatchMessage(MessageType.SHIP_STARTED.ordinal());
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        checkStarting();
        if (started) {
            // TODO: do something
        }
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
