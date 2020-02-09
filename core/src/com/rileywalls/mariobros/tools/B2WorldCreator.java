package com.rileywalls.mariobros.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.rileywalls.mariobros.MarioBros;
import com.rileywalls.mariobros.screens.PlayScreen;
import com.rileywalls.mariobros.sprites.Brick;
import com.rileywalls.mariobros.sprites.Coin;
import com.rileywalls.mariobros.sprites.Enemies.Enemy;
import com.rileywalls.mariobros.sprites.Enemies.Goomba;
import com.rileywalls.mariobros.sprites.Enemies.Turtle;

public class B2WorldCreator {



    Array<Goomba> goombas;
    private Array<Turtle> turtles;

    public B2WorldCreator(PlayScreen screen){
        World world = screen.getWorld();
        TiledMap map = screen.getMap();
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;




        // CREATE GROUND
        /* a map (map = maploader.load("level1.tmx")) has layers which have objects in them
         */
        for(MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            //here, we can downcast the objects to rectangles because we know that's what they all are
            //this rect we grab is only ever used in this case for creating a BodyDef and FixtureDef
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            //bdef should be given a type and position
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            // world + a bdef creates a body which adds a body to the world and can also returns a ref to it
            body = world.createBody(bdef);

            //FixtureDef should be given a shape
            shape.setAsBox((rect.getWidth() / 2) / MarioBros.PPM, (rect.getHeight() / 2) / MarioBros.PPM);
            fdef.shape = shape;

            // body + fdef creates a fixture and puts it into the body
            body.createFixture(fdef);
        }

        // CREATE PIPES

        for(MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            //here, we can downcast the objects to rectangles because we know that's what they all are
            //this rect we grab is only ever used in this case for creating a BodyDef and FixtureDef
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            //bdef should be given a type and position
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            // world + a bdef creates a body
            body = world.createBody(bdef);

            //FixtureDef should be given a shape
            shape.setAsBox((rect.getWidth() / 2) / MarioBros.PPM, (rect.getHeight() / 2) / MarioBros.PPM);
            fdef.shape = shape;
            fdef.filter.categoryBits = MarioBros.OBJECT_BIT;
            // body + fdef creates a fixture and puts it into the body
            body.createFixture(fdef);
        }

        // CREATE BRICKS

        for(MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class))
        {
            new Brick(screen, object);
        }

        // CREATE COINS

        for(MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class))
        {
            new Coin(screen, object);
        }

        // CREATE GOOMBAS
        goombas = new Array<Goomba>();
        for(MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class))
        {
            Rectangle rect = ((RectangleMapObject)object).getRectangle();
            goombas.add(new Goomba(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
        }

        // CREATE TURTLES
        turtles = new Array<Turtle>();
        for(MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class))
        {
            Rectangle rect = ((RectangleMapObject)object).getRectangle();
            turtles.add(new Turtle(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
        }


    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }
    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }

}
