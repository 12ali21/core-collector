package com.mygdx.game.world;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

public class MyContactListener implements ContactListener {
    Array<ContactCallback> callbacks = new Array<>();

    public void registerCallback(ContactCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void beginContact(Contact contact) {
        for (ContactCallback callback : callbacks) {
            if (callback.onContact(contact)) return;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
