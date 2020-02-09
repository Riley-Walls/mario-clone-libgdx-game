package com.rileywalls.mariobros.sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.rileywalls.mariobros.MarioBros;
import com.rileywalls.mariobros.screens.PlayScreen;

/*
To create a sprite, you create a "Body". A body needs to be created by a world and uses a Body Def when it's created
(b2body = world.createBody(bdef);).

A bodydef can have a type, a position, etc

After creation, a body also needs a FixtureDefinition (FixtureDef fdef = new FixtureDef();,
then assign with "b2body.createFixture(fdef))", and a fixture needs a shape (fdef.shape = shape;)
 */

public class Mario extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD}

    ;
    public State currentState;
    public State previousState;

    public World world;
    public Body b2body;
    // TextureRegion works with TextureAtlas
    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private Animation<TextureRegion> marioRun;
    private Animation<TextureRegion> marioJump;

    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion> bigMarioRun;
    private Animation<TextureRegion> growMario;

    private float stateTimer;
    private boolean runningRight;
    private boolean marioIsBig;
    private boolean runGrowAnimation;
    private boolean marioIsDead;

    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;

    public Mario(PlayScreen screen) {
        //texture can now be grabbed with getTexture() after being set in the super constructor
        super(screen.getAtlas().findRegion("little_mario"));

        //this.screen = screen;
        this.world = screen.getWorld();

        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(getTexture(), 1 + i * 16, 11, 16, 16));
        }
        marioRun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        frames = new Array<TextureRegion>();
        for (int i = 4; i < 6; i++) {
            frames.add(new TextureRegion(getTexture(), 1 + i * 16, 11, 16, 16));
        }
        marioJump = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();


        defineMario();
        marioStand = new TextureRegion(getTexture(), 1, 11, 16, 16);
        setBounds(0, 0, 16 / MarioBros.PPM, 16 / MarioBros.PPM);


        //big mario texture
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80, 0, 16, 32);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation<TextureRegion>(0.2f, frames);

        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96, 0, 16, 16);

        //just setting the initial texture region
        setRegion(marioStand);

    }


    public void update(float dt) {

        //matches the texture position to the 2dbody position
        if(marioIsBig)
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2 - 6 / MarioBros.PPM);
        else
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);

        setRegion(getFrame(dt));
        if(timeToDefineBigMario){
            defineBigMario();
        }
        if(timeToRedefineMario){
            redefineMario();
        }

        if(b2body.getPosition().y < -5 / MarioBros.PPM && currentState != State.DEAD){

            MarioBros.manager.get("audio/music/mario_music.ogg", Music.class).stop();
            MarioBros.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
            marioIsDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;
            for(Fixture fixture : b2body.getFixtureList()){
                fixture.setFilterData(filter);
            }

        }


    }

    public TextureRegion getFrame(float dt) {

        TextureRegion region;
        switch (currentState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer, false);
                if(growMario.isAnimationFinished(stateTimer))
                    runGrowAnimation = false;
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump.getKeyFrame(stateTimer);
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;

        }

        if ((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if ((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }


        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        currentState = getState();
        return region;
    }

    public State getState() {
        if(marioIsDead){
            return State.DEAD;
        } else if(runGrowAnimation)
            return State.GROWING;
        else if (b2body.getLinearVelocity().y > 0) {
            return State.JUMPING;
        } else if (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING) {
            return State.FALLING;
        } else if (b2body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }

    public void redefineMario(){
        Vector2 position = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits =
                MarioBros.GROUND_BIT
                        | MarioBros.COIN_BIT
                        | MarioBros.BRICK_BIT
                        | MarioBros.OBJECT_BIT
                        | MarioBros.ENEMY_BIT
                        | MarioBros.ENEMY_HEAD_BIT
                        | MarioBros.ITEM_BIT
                        | MarioBros.SPINNING_SHELL_BIT;
        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);


        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.shape = head;
        fdef.isSensor = true;
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);

        timeToRedefineMario = false;
    }

    public void grow(){
        runGrowAnimation = true;
        marioIsBig = true;
        timeToDefineBigMario = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        MarioBros.manager.get("audio/sounds/powerup.wav", Sound.class).play();
    }


    public void defineMario() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / MarioBros.PPM, 32 / MarioBros.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits =
                        MarioBros.GROUND_BIT
                        | MarioBros.COIN_BIT
                        | MarioBros.BRICK_BIT
                        | MarioBros.OBJECT_BIT
                        | MarioBros.ENEMY_BIT
                        | MarioBros.ENEMY_HEAD_BIT
                        | MarioBros.ITEM_BIT
                        | MarioBros.SPINNING_SHELL_BIT;
        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);


        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.shape = head;
        fdef.isSensor = true;
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void defineBigMario() {
        Vector2 currentPosition = b2body.getPosition();
        world.destroyBody(b2body);


        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPosition.add(0, 10/MarioBros.PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits =
                MarioBros.GROUND_BIT
                        | MarioBros.COIN_BIT
                        | MarioBros.BRICK_BIT
                        | MarioBros.OBJECT_BIT
                        | MarioBros.ENEMY_BIT
                        | MarioBros.ENEMY_HEAD_BIT
                        | MarioBros.ITEM_BIT
                        | MarioBros.SPINNING_SHELL_BIT;
        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
        shape.setPosition(new Vector2(0, -14 / MarioBros.PPM));
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.shape = head;
        fdef.isSensor = true;
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);

        timeToDefineBigMario = false;


    }

    public boolean isBig(){
        return marioIsBig;
    }

    public boolean isDead(){
        return marioIsDead;
    }

    public float getStateTimer(){
        return stateTimer;
    }

    public Vector2 getBodyPosition(){
        return b2body.getPosition();
    }


    public void hit(){
        if(marioIsBig){
            marioIsBig = false;
            timeToRedefineMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() / 2);
            MarioBros.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
        } else {
            MarioBros.manager.get("audio/music/mario_music.ogg", Music.class).stop();
            MarioBros.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
            marioIsDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;
            for(Fixture fixture : b2body.getFixtureList()){
                fixture.setFilterData(filter);
            }
            b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
        }
    }
}
