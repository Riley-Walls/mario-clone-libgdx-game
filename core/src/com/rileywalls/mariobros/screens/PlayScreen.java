package com.rileywalls.mariobros.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import com.rileywalls.mariobros.MarioBros;
import com.rileywalls.mariobros.WorldContactListener;
import com.rileywalls.mariobros.scenes.*;
import com.rileywalls.mariobros.sprites.Enemies.Enemy;
import com.rileywalls.mariobros.sprites.Items.Item;
import com.rileywalls.mariobros.sprites.Items.ItemDef;
import com.rileywalls.mariobros.sprites.Items.Mushroom;
import com.rileywalls.mariobros.sprites.Mario;
import com.rileywalls.mariobros.tools.B2WorldCreator;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Classes that implement screen are given render(), resize(), show(), etc
// Game classes use these Screen classes to take over their rendering by "setScreen" in the Game class

// To have actions happen upon resizing (resize()), you must have something to update when the player resizes the window.
// That thing is a Viewport. A viewport is like a "virtual window" being rendered within the actual system window.
// To create a viewport, we need to create a camera.

/* SCALING-------------------------

The PPM in MarioBros is used for scaling in this class by...

gameport = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT/ MarioBros.PPM, gamecam);
renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);
shape.setAsBox((rect.getWidth() / 2) / MarioBros.PPM, (rect.getHeight() / 2) / MarioBros.PPM);
bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);
shape.setAsBox((rect.getWidth() / 2) / MarioBros.PPM, (rect.getHeight() / 2) / MarioBros.PPM);
bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);


 ---------------------------------*/


public class PlayScreen implements Screen {



    private MarioBros game;
    private TextureAtlas atlas;

    private OrthographicCamera gamecam;
    private Viewport gameport;
    private Hud hud;

    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    //sprites
    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;
    private Mario player;

    private Music music;

    private B2WorldCreator creator;


    //Box2d vars
    private World world;
    private Box2DDebugRenderer b2dr;

    public PlayScreen(MarioBros game){

        //AssetManager would be better for large amounts of texture
        atlas = new TextureAtlas("Mario_and_Enemies.pack");
        this.game = game;

        hud = new Hud(game.batch);

        gamecam = new OrthographicCamera();
        gameport = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT/ MarioBros.PPM, gamecam);

        //create map loader
        maploader = new TmxMapLoader();
        //maploader creates/loads map
        map = maploader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);
        gamecam.position.set(gameport.getWorldWidth() / 2, gameport.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0,-10), true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioBros.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        //music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();


    }

    public void spawnItem(ItemDef idef){
        itemsToSpawn.add(idef);
    }

    public void handleSpawningItems(){
        if (!itemsToSpawn.isEmpty()){
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class){
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }

    public TextureAtlas getAtlas(){
        return atlas;
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt){
        if(player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                // the true here is "will it wake the body up" in the physics simulation

                if(player.b2body.getLinearVelocity().y == 0){
                    player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2) {
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2) {
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
            }
        }
        // "&& player.b2body.getLinearVelocity() <= 2" gives the player a max speed

    }

    public void update(float dt){
        handleInput(dt);
        handleSpawningItems();

        player.update(dt);

        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if (enemy.getX() < player.getX() + 260 / MarioBros.PPM) {
                enemy.b2body.setActive(true);
            }
        }

        for(Item item : items){
            item.update(dt);
        }

        hud.update(dt);

        world.step(1/60f, 6, 2);

        if(player.currentState != Mario.State.DEAD) {
            gamecam.position.x = player.b2body.getPosition().x;
        }

        gamecam.update();
        renderer.setView(gamecam);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render the game map
        renderer.render();

        //render the Box2dDebugLines
        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();
        player.draw(game.batch);

        for(Enemy enemy : creator.getEnemies())
            enemy.draw(game.batch);

        for(Item item : items){
            item.draw(game.batch);
        }

        game.batch.end();

        // Set the batch to now draw what the Hud camera sees.
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);

        hud.stage.draw();

        if(gameOver()){
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    public boolean gameOver() {
        if (player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
            return true;
        } else{
            return false;
        }
    }


    @Override
    public void resize(int width, int height) {
        gameport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public TiledMap getMap(){
        return map;
    }

    public World getWorld(){
        return world;
    }
}
