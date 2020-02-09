package com.rileywalls.mariobros;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.*;
import com.rileywalls.mariobros.sprites.Enemies.Enemy;
import com.rileywalls.mariobros.sprites.InteractiveTileObject;
import com.rileywalls.mariobros.sprites.Items.Item;
import com.rileywalls.mariobros.sprites.Mario;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = (fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits);



        switch(cDef){
            case MarioBros.MARIO_HEAD_BIT | MarioBros.BRICK_BIT:
            case MarioBros.MARIO_HEAD_BIT | MarioBros.COIN_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.MARIO_HEAD_BIT){
                    ((InteractiveTileObject)fixB.getUserData()).onHeadHit((Mario)fixA.getUserData());
                } else {
                    ((InteractiveTileObject)fixA.getUserData()).onHeadHit((Mario)fixB.getUserData());
                }
                break;
            case MarioBros.ENEMY_HEAD_BIT | MarioBros.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT){
                    ((Enemy)fixA.getUserData()).hitOnHead((Mario)fixB.getUserData());
                } else if(fixB.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT){
                    ((Enemy)fixB.getUserData()).hitOnHead((Mario)fixB.getUserData());
                }
                MarioBros.manager.get("audio/sounds/stomp.wav", Sound.class).play();
                break;
            case MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_BIT){
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                } else if (fixB.getFilterData().categoryBits == MarioBros.ENEMY_BIT){
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case MarioBros.ENEMY_BIT | MarioBros.ENEMY_BIT:
                ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                break;
            case MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT){
                    ((Item)fixA.getUserData()).reverseVelocity(true, false);
                } else if (fixB.getFilterData().categoryBits == MarioBros.ITEM_BIT){
                    ((Item)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case MarioBros.ITEM_BIT | MarioBros.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT){
                    ((Item)fixA.getUserData()).use((Mario)fixB.getUserData());
                } else if (fixB.getFilterData().categoryBits == MarioBros.ITEM_BIT){
                    ((Item)fixB.getUserData()).use((Mario)fixA.getUserData());
                }
                break;
            case MarioBros.MARIO_BIT | MarioBros.ENEMY_BIT:
            case MarioBros.MARIO_BIT | MarioBros.SPINNING_SHELL_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.MARIO_BIT){
                    ((Mario)fixA.getUserData()).hit();
                } else {
                    ((Mario) fixB.getUserData()).hit();
                }
                break;

            case MarioBros.SPINNING_SHELL_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.SPINNING_SHELL_BIT){
                    Gdx.app.log("collision", (String.valueOf(((Enemy)fixA.getUserData()).velocity)));
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                } else if (fixB.getFilterData().categoryBits == MarioBros.SPINNING_SHELL_BIT){
                    Gdx.app.log("collision", (String.valueOf(((Enemy)fixB.getUserData()).velocity)));
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                }
                break;

            case MarioBros.SPINNING_SHELL_BIT | MarioBros.ENEMY_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.SPINNING_SHELL_BIT){
                    ((Enemy)fixB.getUserData()).hitWithShell();
                } else if (fixB.getFilterData().categoryBits == MarioBros.SPINNING_SHELL_BIT){
                    ((Enemy)fixA.getUserData()).hitWithShell();
                }
                break;

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
