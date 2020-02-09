package com.rileywalls.mariobros.sprites.Enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.rileywalls.mariobros.MarioBros;
import com.rileywalls.mariobros.screens.PlayScreen;
import com.rileywalls.mariobros.sprites.Mario;

public class Turtle extends Enemy{
    public enum State {WALKING, SHELL, SPINNING}
    public State currentState;
    public State previousState;
    private float stateTimer;

    private Animation<TextureRegion> walkAnimation;
    private Array<TextureRegion> frames;
    private TextureRegion shell;

    private boolean setToDestroy = false;
    private boolean destroyed = false;
    private PlayScreen screen;

    private boolean setToSpinningShell = false;

    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        this.screen = screen;
        Array<TextureRegion> frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24);
        walkAnimation = new Animation(0.2f, frames);
        currentState = previousState = State.WALKING;



        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);


    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fdef.filter.maskBits =
                MarioBros.GROUND_BIT
                        | MarioBros.COIN_BIT
                        | MarioBros.BRICK_BIT
                        | MarioBros.ENEMY_BIT
                        | MarioBros.OBJECT_BIT
                        | MarioBros.MARIO_BIT;


        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5, 8).scl(1 / MarioBros.PPM);
        vertice[1] = new Vector2(5, 8).scl(1 / MarioBros.PPM);
        vertice[2] = new Vector2(-3, 3).scl(1 / MarioBros.PPM);
        vertice[3] = new Vector2(3, 3).scl(1 / MarioBros.PPM);
        head.set(vertice);


        fdef.shape = head;
        fdef.restitution = 0.5f;
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);

    }

    private void defineSpinningShell(){

        Filter filter = new Filter();
        filter.categoryBits = MarioBros.SPINNING_SHELL_BIT;
        filter.maskBits =
                MarioBros.GROUND_BIT
                        | MarioBros.COIN_BIT
                        | MarioBros.BRICK_BIT
                        | MarioBros.ENEMY_BIT
                        | MarioBros.OBJECT_BIT
                        | MarioBros.MARIO_BIT;


        b2body.getFixtureList().get(0).setFilterData(filter);

        filter.categoryBits = MarioBros.NOTHING_BIT;
        filter.maskBits =
                MarioBros.NOTHING_BIT;

        b2body.getFixtureList().get(1).setFilterData(filter);


        setToSpinningShell = false;
        Gdx.app.log(String.valueOf(b2body.getFixtureList().get(0).getFilterData().categoryBits), String.valueOf(b2body.getFixtureList().get(1).getFilterData().categoryBits));
    }

    @Override
    public void hitOnHead(Mario mario) {
        if(currentState != State.SHELL && currentState != State.SPINNING){
            currentState = State.SHELL;
            velocity.x = 0;
        } else if (currentState == State.SHELL){
            currentState = State.SPINNING;
            setToSpinningShell = true;
            if(mario.getBodyPosition().x > b2body.getPosition().x)
                velocity.x = -2;
            else
                velocity.x = 2;
        }
    }


    public TextureRegion getFrame(float dt){
        TextureRegion region;

        switch(currentState){
            case SPINNING:
            case SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTimer, true);
        }

        if(velocity.x > 0 && region.isFlipX() == false){
            region.flip(true, false);
        }
        if(velocity.x < 0 && region.isFlipX() == true){
            region.flip(true, false);
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;

        return region;

    }

    @Override
    public void update(float dt) {
        setRegion(getFrame(dt));

        if(setToSpinningShell == true){
            this.defineSpinningShell();
        }


        if(currentState == State.SHELL && stateTimer > 5){
            currentState = State.WALKING;
            velocity.x = 1;
        }

        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 8 / MarioBros.PPM);
        b2body.setLinearVelocity(velocity);
    }

    @Override
    public void draw(Batch batch){
        if(!destroyed){
            super.draw(batch);
        }
    }

    public State getCurrentState(){
        return currentState;
    }



}
