package gamejam;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;

class Person extends Sprite { 
    static int countPersons = 0;
    static int countDead = 0;

    static BitmapFont font = new BitmapFont();
    static {
        font.setColor(0,0,0,1);
    }
    static final float INFECTED_TO_SICK = 7;
    static final float SICK_TO_DEAD = 15;
    static final float DEAD_TO_REMOVED = 150;

    static float spreadOnDeath = 0.60f;

    float timer;
    static Texture personTexture;

    float maxSpeed = 200;
    float speedX;
    float speedY;
    Group group;
    boolean isSick;
    State state;

    enum State {
        NORMAL, INFECTED, SICK, DEAD
    }

    Person(Group group) {
        super(personTexture);
        this.group = group;
        speedX = MathUtils.random(-maxSpeed, maxSpeed);
        speedY = MathUtils.random(-maxSpeed, maxSpeed);
        setCenter(group.rectangle.x + group.rectangle.width/2,
                  group.rectangle.y + group.rectangle.height/2);

        countPersons ++;
        state = State.NORMAL;
        if(MathUtils.randomBoolean(0.15f)) {
            setInfected();
        }
    }

    void setInfected() {
        timer = INFECTED_TO_SICK;
        state = State.INFECTED;
    }
    void setSick() {
        timer = SICK_TO_DEAD;
        state = State.SICK;
        setColor(1, 0.2f, 0.2f, 1);
        GameScreen.playSound(GameScreen.sndSneeze);
    }
    void setDead() {
        state = State.DEAD;
        GameScreen.playSound(GameScreen.sndDeath);
        setColor(0.2f, 0.1f, 0.1f, 0.8f);
        for(int i = 0; i < group.members.size; i++) {
            Person p = group.members.get(i);
            if (p.state == State.NORMAL) {
                if(MathUtils.randomBoolean(spreadOnDeath)) {
                    p.setInfected();
                }
            }
        }
        countPersons--;
        countDead++;
        timer = DEAD_TO_REMOVED;
    }

    static String strNormal = "N";
    static String strInfected = "I";
    static String strSick = "S";
    static String strDead = "D";
    
    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        if(GameScreen.debug) {
            String s = "";
            if(state == State.NORMAL) s = strNormal;
            else if(state == State.INFECTED) s = strInfected;
            else if(state == State.SICK) s = strSick;
            else s = strDead;
            font.draw(batch, s, getX()+12, getY()+22); 
        }
    }

    float testAlpha = 1;
    public void update(float delta) {
        timer -= delta;
        switch (state) {
        case INFECTED:
            if (timer < 0) {
                setSick();
            }
            break;
        case SICK:
            if (timer < 0) {
                setDead();
                return;
            }
            break;
        case DEAD:
            if(testAlpha > 0.1) {
                testAlpha -= 0.01f * delta;
                if(testAlpha < 0.1) {
                    testAlpha = 0.1f;
                }
            }
            setAlpha(testAlpha);
            return;
        }
        Rectangle r = getBoundingRectangle();
        if (!r.overlaps(group.rectangle)) {
            // x,y inside group
            float randomX = MathUtils.random(group.rectangle.x, group.rectangle.x+group.rectangle.width);
            float randomY = MathUtils.random(group.rectangle.y, group.rectangle.y+group.rectangle.height);
            speedX = randomX - getX();
            speedY = randomY - getY();
        }
        translate((speedX + group.speedX) * delta, (speedY + group.speedY)*delta);
    }
}
