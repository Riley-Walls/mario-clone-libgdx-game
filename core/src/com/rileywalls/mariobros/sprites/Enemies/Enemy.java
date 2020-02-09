package com.rileywalls.mariobros.sprites.Enemies;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.rileywalls.mariobros.MarioBros;
import com.rileywalls.mariobros.screens.PlayScreen;
import com.rileywalls.mariobros.sprites.Mario;

public abstract class Enemy extends Sprite{
    protected World world;
    protected PlayScreen screen;
    protected boolean setToDestroy = false;
    public Body b2body;
    public Vector2 velocity;


    public Enemy(PlayScreen screen, float x, float y){

        this.world = screen.getWorld();
        this.screen = screen;
        setPosition(x, y);
        defineEnemy();
        velocity = new Vector2(1, 0);
        b2body.setActive(false);
    }

    protected abstract void defineEnemy();

    public abstract void hitOnHead(Mario mario);

    public abstract void update(float dt);

    public void reverseVelocity(boolean x, boolean y){
        if(x){
            velocity.x = -velocity.x;
        }
        if(y){
            velocity.y = -velocity.y;
        }
    }

    public void hitWithShell(){
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;
        for(Fixture fixture : b2body.getFixtureList()){
            fixture.setFilterData(filter);
        }

        b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);

    }


}
