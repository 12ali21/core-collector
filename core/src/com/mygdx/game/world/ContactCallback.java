package com.mygdx.game.world;

import com.badlogic.gdx.physics.box2d.Contact;

public interface ContactCallback {
    boolean onContact(Contact contact);
}
