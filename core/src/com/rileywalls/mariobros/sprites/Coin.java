package com.rileywalls.mariobros.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.rileywalls.mariobros.MarioBros;
import com.rileywalls.mariobros.scenes.Hud;
import com.rileywalls.mariobros.screens.PlayScreen;
import com.rileywalls.mariobros.sprites.Items.Item;
import com.rileywalls.mariobros.sprites.Items.ItemDef;
import com.rileywalls.mariobros.sprites.Items.Mushroom;

public class Coin extends InteractiveTileObject {
    public static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28;
    public Coin(PlayScreen screen, MapObject object){
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");

        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {

        if(getCell().getTile().getId() == BLANK_COIN){
            MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
        } else {

            if(!object.getProperties().containsKey("mushroom")) {
                MarioBros.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Mushroom.class));
            } else {
                MarioBros.manager.get("audio/sounds/coin.wav", Sound.class).play();
            }
        }

        Gdx.app.log("Coin", "Collision");
        getCell().setTile(tileSet.getTile(BLANK_COIN));
        Hud.addScore(100);


    }


}
