package gamejam;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;

public class GameScreen extends ScreenAdapter {
	SpriteBatch batch;
    SpriteBatch guiBatch;
    Array<Group> persons;
    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;
    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();

    Sprite red = new Sprite(new Texture("red.png"), screenWidth, screenHeight);
    float deltaModifier = 1;

    static boolean debug = false;
    BitmapFont bigFont = new BitmapFont();
    
    enum GameState {
        Menu, Playing, Over
    }

    GameState gameState = GameState.Menu;

    static float GameWidth = 1600;
    static float GameHeight = 900;
    static Rectangle gameBoard = new Rectangle(0,0, 1600, 900);
    static BitmapFont font;

    boolean drawLine = false;
    Vector3 startTouchPos = new Vector3();
    Vector3 endTouchPos = new Vector3();

    int largestGroupEver = 0;
    Spawner spawner = new Spawner();

    static Sound sndSneeze;
    static Sound sndMerge;
    static Sound sndDeath;

    static float soundTimer = 0;
    public static void playSound(Sound s) {
        if(soundTimer > 0.5f) {
            s.play();
            soundTimer = 0;
        }
    }

    public void show() {
        sndSneeze = Gdx.audio.newSound(Gdx.files.internal("sneeze.wav"));
        sndMerge = Gdx.audio.newSound(Gdx.files.internal("merge.wav"));
        sndDeath = Gdx.audio.newSound(Gdx.files.internal("death.wav"));

        bigFont.scale(4);
        batch = new SpriteBatch();
        guiBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        Gdx.input.setCursorPosition(screenWidth / 2, screenHeight / 2);
        Gdx.input.setInputProcessor(new GameInputAdapter());
        Person.personTexture = new Texture("person.png");
        font = new BitmapFont();
        font.setColor(1, 1, 1, 1);
        red.setAlpha(0);
    }

    class Spawner {
        static final float startInterval = 1;
        float interval = startInterval;
        float time = interval;
        float incTimer = 0;
        float count = 1;
        
        void spawn(float delta) {
            incTimer += delta;
            if(incTimer > 40) {
                count ++;
            }
            if(interval > 0.1) {
                interval -= 0.01 * delta;
            }
            time += delta;
            for(int i = 0; i < count; i++) {
                float test = MathUtils.random(360);
                if (time >= interval) {
                    float x = (float)Math.cos(test);
                    float y = (float)Math.sin(test);
                    Group g = new Group(x * 3000 + gameBoard.width / 2, y * 3000 + gameBoard.height/2,
                           MathUtils.random(-200, 200), MathUtils.random(-200, 200));
                    g.update(0);
                    for(Person p: g.members) {
                        p.setPosition(g.rectangle.x + x * 100, g.rectangle.y + y * 100);
                    }
                    time = 0;
                }
            }
        }
    }

    static String menuText = "Limit Virus";
    static String tellText = "Click to play";
    static String credit = "Team Øyvind:\nØyvind J.Amundrud";
    void stateMenu() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        bigFont.draw(batch, menuText,Gdx.graphics.getWidth()/2 - (menuText.length()/2) * 30,
                Gdx.graphics.getHeight() - 300);
        font.drawMultiLine(batch, tellText, Gdx.graphics.getWidth() /2 - 19/2 * 15,
                Gdx.graphics.getHeight() - 450);
        font.drawMultiLine(batch, credit, Gdx.graphics.getWidth() /2 - 19/2 * 15,
                Gdx.graphics.getHeight() - 600);
        batch.end();
    }
    void statePlaying(float delta) {
        soundTimer += delta;

        mousePosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePosition);

        camera.position.y += (mousePosition.y - camera.position.y) * delta;
        camera.position.x += (mousePosition.x - camera.position.x) * delta; 
        if(!debug) {
            if(camera.position.y < Gdx.graphics.getHeight() / 2) camera.position.y = Gdx.graphics.getHeight() / 2;
            if(camera.position.y > gameBoard.height - Gdx.graphics.getHeight()/2)
                camera.position.y = gameBoard.height - Gdx.graphics.getHeight()/2;
            
            if(camera.position.x < Gdx.graphics.getWidth()/2) camera.position.x = Gdx.graphics.getWidth()/2;
            if(camera.position.x > gameBoard.width - Gdx.graphics.getWidth()/2) 
                camera.position.x = gameBoard.width - Gdx.graphics.getWidth()/2;
        }
        camera.update();
        delta = delta * deltaModifier;

        spawner.spawn(delta);
        Group.updateAll(delta);

        red.setCenter(camera.position.x, camera.position.y);
        if(Person.countPersons != 0 && Group.deadPersons.size != 0) {
            float alpha = (float)Group.deadPersons.size / Person.countPersons;
            red.setAlpha(alpha);
        }
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        red.draw(batch);
        Group.drawAll(batch);
        batch.end();


        guiBatch.begin();
        font.setColor(0,1,0,1);
        font.draw(guiBatch, "Alive: " + Person.countPersons, 20, Gdx.graphics.getHeight() - 32);
        font.setColor(1,0,0,1);
        font.draw(guiBatch, "Dead: " + Group.deadPersons.size, 20, Gdx.graphics.getHeight() - 64);
        font.setColor(1,1,1,1);
        font.draw(guiBatch, "Current largest group: " + Group.currentLargestGroup, 20, Gdx.graphics.getHeight() - 96);
        font.draw(guiBatch, "Largest group ever: " + Group.largestGroupEver, 20, Gdx.graphics.getHeight() - 128);
        guiBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        if (debug) {
            shapeRenderer.setColor(1,0,0,1);
            shapeRenderer.rect(gameBoard.x, gameBoard.y, gameBoard.width, gameBoard.height);
            shapeRenderer.setColor(0,1,0,1);
            Group.drawAllDebug(shapeRenderer);
        }
        shapeRenderer.end();
        if (drawLine) {
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(0.1f,0.1f,1,0);
            shapeRenderer.rectLine(startTouchPos.x, startTouchPos.y, mousePosition.x, mousePosition.y, 3);
            shapeRenderer.end();
        }

        if(Group.deadPersons.size > Person.countPersons) {
            gameState = GameState.Over;
        }
    }

    static String gameOverTekst = "Game Over";
    static String gameOverInfo = "Press 'R' to restart.";
    void stateOver() {
        statePlaying(0);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        bigFont.draw(batch, gameOverTekst,camera.position.x - (menuText.length()/2) * 30,
                camera.position.y + 200);
        font.draw(batch, gameOverInfo, camera.position.x, camera.position.y - 100); 
        batch.end();
    }
    Vector3 mousePosition = new Vector3();
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        switch(gameState) {
        case Menu:
            stateMenu();
            break;
        case Playing:
            statePlaying(delta);
            break;
        case Over:
            stateOver();
            break;
        }

        
    }

   class GameInputAdapter extends InputAdapter {

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Keys.ESCAPE) {
                Gdx.app.exit();
            }
            if (keycode == Keys.D) {
                debug = !debug;
            }
            if (keycode == Keys.R) {
                mousePosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            }
            if (keycode == Keys.LEFT) {
                deltaModifier -= 0.25;
                if (deltaModifier < 0) {
                    deltaModifier = 0;
                }
            }
            if (keycode == Keys.RIGHT) {
                deltaModifier += 0.25;
            }
            if (keycode == Keys.DOWN) {
                deltaModifier = 1;
            }
            if (keycode == Keys.P) {
                if(deltaModifier != 0) 
                    deltaModifier = 0;
                else 
                    deltaModifier = 1;
            }

            if (keycode == Keys.R) {
                // reset
                gameState = GameState.Playing;
                Group.allGroups = new Array<Group>();
                Group.deadPersons = new Array<Person>(true, 100);
                Group.currentLargestGroup = 0;
                Group.largestGroupEver = 0;
                Person.countPersons = 0;
                Person.countDead = 0;
                spawner.interval = Spawner.startInterval;
                red.setAlpha(0);
            }
            return true;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if(gameState == GameState.Menu) {
                gameState = GameState.Playing;
            }
            if(button == Buttons.LEFT) {
                startTouchPos.set(screenX, screenY, 0);
                camera.unproject(startTouchPos);
                drawLine = true;
            }

            return true;
        }
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if(button == Buttons.LEFT) {
                drawLine = false;
                endTouchPos.set(screenX, screenY, 0);
                camera.unproject(endTouchPos);
                Group.splitLine(startTouchPos.x, startTouchPos.y, endTouchPos.x, endTouchPos.y);
            }
            return true;
        }
    }
}
