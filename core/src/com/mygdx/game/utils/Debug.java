package com.mygdx.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;

public class Debug {
    private static final String FPS_TAG = "FPS";
    private static final String BINDS_TAG = "binds";
    private static final String DRAWS_TAG = "draws";
    private static final Stage stage;
    private static final Table table;
    private static final Label.LabelStyle LABEL_STYLE;
    private static final Skin skin;
    private static final TextButton.TextButtonStyle BUTTON_STYLE;
    private static final GLProfiler profiler;
    private static final HashMap<String, Label> logs;
    private static final HashMap<String, Rectangle> rects;
    private static final HashMap<String, Vector2> points;
    private static final HashMap<String, Line> lines;
    private static final Runtime runtime;
    private static final ShapeRenderer shapeRenderer;
    private static float timeBuffer = 0;
    private static Camera gameCamera;
    private static int frames;


    static {
        runtime = Runtime.getRuntime();

        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();

        stage = new Stage(new ScreenViewport());
        LABEL_STYLE = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        skin = new Skin();

        // Generate a 1x1 white texture and store it in the skin named "white".
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        // Store the default libGDX font under the name "default".
        skin.add("default", new BitmapFont());

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        BUTTON_STYLE = new TextButton.TextButtonStyle();
        BUTTON_STYLE.up = skin.newDrawable("white", Color.DARK_GRAY);
        BUTTON_STYLE.down = skin.newDrawable("white", Color.DARK_GRAY);
        BUTTON_STYLE.checked = skin.newDrawable("white", Color.BLUE);
        BUTTON_STYLE.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        BUTTON_STYLE.font = skin.getFont("default");
        skin.add("default", BUTTON_STYLE);

        table = new Table();
        table.top().left();
        table.setFillParent(true);

        logs = new HashMap<>();
        rects = new HashMap<>();
        points = new HashMap<>();
        lines = new HashMap<>();
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

    public static void drawLine(String tag, Vector2 s, Vector2 e) {
        Line line = lines.get(tag);
        if (line == null) {
            line = new Line(s, e);
        } else {
            line.start.set(s);
            line.end.set(e);
        }
        lines.put(tag, line);
    }

    public static void log(String tag, Object info) {
        Label label = logs.get(tag);
        if (label == null) {
            label = new Label("", LABEL_STYLE);
            addLabel(tag, label);
        }
        label.setText(tag + ": " + info);
    }

    private static void addLabel(String tag, Label label) {
        table.add(label).left();
        table.row();
        logs.put(tag, label);
    }

    public static void addButton(String tag, Callback callback) {
        TextButton button = new TextButton(tag, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.run();
            }
        });
        table.add(button).left();
        table.row();
    }

    public static void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public static void setCamera(Camera camera) {
        gameCamera = camera;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void render(float delta) {
        frames++;
        timeBuffer += delta;
        if (timeBuffer >= 1) {
            log("Memory", ""
                    + (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + "MB / "
                    + runtime.maxMemory() / (1024 * 1024) + "MB");
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

            for (Line line : lines.values()) {
                shapeRenderer.line(line.start, line.end);
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

    public static interface Callback {
        void run();
    }

    private static class Line {
        Vector2 start;
        Vector2 end;

        public Line(Vector2 start, Vector2 end) {
            this.start = start;
            this.end = end;
        }
    }
}
