package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

public class Debug {
    private static final String FPS_TAG = "FPS";
    private static final String BINDS_TAG = "binds";
    private static final String DRAWS_TAG = "draws";
    private static final Stage stage;
    private static final Table table;
    private static final Label.LabelStyle style;
    private static float timeBuffer = 0;
    private static final GLProfiler profiler;
    private static final HashMap<String, Label> logs;
    private static final HashMap<String, Rectangle> rects;
    private static final HashMap<String, Vector2> points;
    private static final Runtime runtime;
    private static Camera gameCamera;
    private static final ShapeRenderer shapeRenderer;
    private static int frames;

    static {
        runtime = Runtime.getRuntime();

        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();

        stage = new Stage(new ScreenViewport());
        style = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        table = new Table();
        table.top().left();
        table.setFillParent(true);

        logs = new HashMap<>();
        rects = new HashMap<>();
        points = new HashMap<>();
        shapeRenderer = new ShapeRenderer();

        log(FPS_TAG, "");
        log(BINDS_TAG, "");
        log(DRAWS_TAG, "");

        stage.addActor(table);
    }
    public static void drawPoint(String tag, Vector2 v) {
        points.put(tag, v);
    }
    public static void drawPoint(String tag, float x, float y) {
        drawPoint(tag, new Vector2(x, y));
    }

    public static void drawRect(String tag, int x, int y, int width, int height) {
        drawRect(tag, new Rectangle(x, y, width, height));
    }

    public static void drawRect(String tag, Rectangle rectangle) {
        Rectangle rect = rects.get(tag);
        if (rect == null) {
            rect = new Rectangle(rectangle);
        } else {
            rect.set(rectangle);
        }
        rects.put(tag, rect);
    }

    public static void log(String tag, Object info) {
        Label label = logs.get(tag);
        if (label == null) {
            label = new Label("", style);
            addLabel(tag, label);
        }
        label.setText(tag + ": " + info);
    }

    private static void addLabel(String tag, Label label) {
        table.add(label).left();
        table.row();
        logs.put(tag, label);
    }

    public static void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public static void setCamera(Camera camera) {
        gameCamera = camera;
    }

    public static void render(float delta) {
        frames++;
        timeBuffer += delta;
        if (timeBuffer >= 1) {
            log("Memory", "" + (runtime.totalMemory() - runtime.freeMemory()) / 1048576 + "MB / " + runtime.maxMemory() / 1048576 + "MB");
            log(FPS_TAG, "" + frames / timeBuffer);
            timeBuffer = 0;
            frames = 0;
            log(BINDS_TAG, "" + profiler.getTextureBindings());
            log(DRAWS_TAG, "" + profiler.getDrawCalls());
            profiler.reset();
        }

        stage.act(delta);
        stage.draw();

        if (gameCamera != null) {
            shapeRenderer.setProjectionMatrix(gameCamera.combined);
            shapeRenderer.setAutoShapeType(true);
            shapeRenderer.begin();
            shapeRenderer.setColor(Color.GREEN);
            for (Rectangle rect : rects.values()) {
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
            }

            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);
            for (Vector2 point : points.values()) {
                shapeRenderer.circle(point.x, point.y, 0.1f, 90);
            }
            shapeRenderer.end();

            rects.clear();
        }
    }

    public static void dispose() {
        stage.dispose();
    }
}
