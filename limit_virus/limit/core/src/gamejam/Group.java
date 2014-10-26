package gamejam;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

class Group {
    static Array<Group> allGroups = new Array<Group>();
    static Array<Person> deadPersons = new Array<Person>();
    static int currentLargestGroup = 0;
    static int largestGroupEver = 0;

    Array<Person> members = new Array<Person>();
    Rectangle rectangle = new Rectangle(64, 64, 64, 64);
    float speedX;
    float speedY;

    void updateSize() {
        int count = members.size;
        if (count <= 1) {
            rectangle.width = 64;
            rectangle.height = 64;
        }
        else {
            rectangle.width = 64 + (int)Math.sqrt(64 * count-1);
            rectangle.height = rectangle.width;
        }
    }
    Group(Array<Person> persons) {
        members = persons;
        for(Person p : members) {
            p.group = this;
        }
        allGroups.add(this);
    }
    Group(float x, float y, float speedX, float speedY) {
        rectangle.x = x;
        rectangle.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        members.add(new Person(this));
        allGroups.add(this);
    }

    void update(float delta) {
        rectangle.x += speedX * delta;
        rectangle.y += speedY * delta;
        if(rectangle.x < 0) {
            rectangle.x = 0;
            speedX = -speedX;
        }
        if(rectangle.x > GameScreen.gameBoard.width - rectangle.width) {
            rectangle.x = GameScreen.gameBoard.width - rectangle.width;
            speedX = -speedX;
        }
        if(rectangle.y < 0) {
            rectangle.y = 0;
            speedY = -speedY;
        }
        if(rectangle.y > GameScreen.gameBoard.height - rectangle.height) {
            rectangle.y = GameScreen.gameBoard.height - rectangle.height;
            speedY = -speedY;
        }

        for(Person p: members) {
            p.update(delta);
            if (p.state == Person.State.DEAD) {
                deadPersons.add(p);
                members.removeValue(p, true);
                if(members.size == 0) {
                    allGroups.removeValue(this, true);
                }
            }
        }
        for(Person p: deadPersons) {
            p.update(delta);
            if(p.timer <= 0) {
                deadPersons.removeValue(p, true);
            }
        }
        updateSize();
    }

    static void checkGroupCollision() {
        for(int i = 0; i < allGroups.size; i++) {
            for(int j = i+1; j < allGroups.size; j++) {
                if(allGroups.get(i).collideWith(allGroups.get(j))) {
                    GameScreen.playSound(GameScreen.sndMerge);
                    Group gI = allGroups.get(i);
                    Group gJ = allGroups.get(j);
                    Group newGroup = gI.merge(gJ);
                    allGroups.removeValue(gI, true);
                    allGroups.removeValue(gJ, true);
                    allGroups.add(newGroup);
                    i = 0;
                    j = 1;
                }
            } 
        }
    }

    static void updateAll(float delta) {
        checkGroupCollision();

        currentLargestGroup = 0;
        for(Group g: allGroups) {
            g.update(delta);

            if(currentLargestGroup < g.members.size) {
                currentLargestGroup = g.members.size;
            }
            if(largestGroupEver < g.members.size) {
                largestGroupEver = g.members.size;
            }
        }
    }

    static void drawAll(SpriteBatch batch) {
        for(Person p: deadPersons) {
            p.draw(batch);
        }
        for(Group g: allGroups) {
            g.draw(batch);
        }
    }
    static void drawAllDebug(ShapeRenderer sr) {
        for(Group g: allGroups) {
            g.drawDebug(sr);
        }
    }

    boolean collideWith(Group g) {
        return g.rectangle.overlaps(rectangle);
    }

    Group merge(Group g) {
        if(g.members.size > members.size) {
            addToGroup(members, g);
            return g;
        } else {
            addToGroup(g.members, this);
            return this;
        }
    }

    void addToGroup(Array<Person> persons, Group g) {
        for(Person p: persons) {
            p.group = g;
        }
        g.members.addAll(persons);
    }

    void draw(SpriteBatch batch) {
        for(Person p: members) {
            p.draw(batch);
        }
    }

    static void splitLine(float startX, float startY, float endX, float endY) {
        for(Group g: allGroups) {
            if(g.members.size > 1) {
                // Horizontal split
                if (((startX < g.rectangle.x && endX > g.rectangle.x + g.rectangle.width) ||
                        (endX < g.rectangle.x && startX > g.rectangle.x + g.rectangle.width)) &&
                        (startY > g.rectangle.y && startY < g.rectangle.y+g.rectangle.height) && 
                        (endY > g.rectangle.y && endY < g.rectangle.y+g.rectangle.height)) {

                    float avarage = (startY + endY) / 2;
                    Array<Person> top = new Array<Person>();
                    for(Person p : g.members) {
                        if(p.getY() - p.getHeight() / 2 > avarage) {
                            top.add(p);
                        }
                    }
                    if(top.size > 0 && top.size < g.members.size) {
                        for(Person p : top) {
                            g.members.removeValue(p, true);
                        }
                        Group topGroup = new Group(top);
                        topGroup.rectangle.x = g.rectangle.x;
                        topGroup.rectangle.y = g.rectangle.y + g.rectangle.height + 10;

                        if(Math.abs(g.speedX) < 100) {
                            g.speedX *= 2;
                        }
                        if(Math.abs(g.speedY) < 100) {
                            g.speedY *= 2;
                        }

                        topGroup.speedX = g.speedX;
                        topGroup.speedY = Math.abs(g.speedY);
                        g.speedY = -Math.abs(g.speedY);  
                    }
                    return;
                }
                // Vertical split
                else if (startX > g.rectangle.x && startX < g.rectangle.x + g.rectangle.width &&
                        endX > g.rectangle.x && endX < g.rectangle.x + g.rectangle.width &&
                        ((startY < g.rectangle.y + g.rectangle.height && endY > g.rectangle.y) ||
                         (endY < g.rectangle.y + g.rectangle.height && startY > g.rectangle.y))) {
                    
                    float avarage = (startX+ endX) / 2;
                    Array<Person> right = new Array<Person>();
                    for(Person p : g.members) {
                        if(p.getX() + p.getWidth() / 2 > avarage) {
                            right.add(p);
                        }
                    }
                    if(right.size > 0 && right.size < g.members.size) {
                        for(Person p : right) {
                            g.members.removeValue(p, true);
                        }
                        Group rightGroup = new Group(right);
                        rightGroup.rectangle.x = g.rectangle.x + g.rectangle.width + 10;
                        rightGroup.rectangle.y = g.rectangle.y;
                        rightGroup.speedX = Math.abs(g.speedX);
                        if(Math.abs(g.speedX) < 100) {
                            g.speedX *= 2;
                        }
                        if(Math.abs(g.speedY) < 100) {
                            g.speedY *= 2;
                        }
                        rightGroup.speedY = g.speedY;
                        g.speedX = -Math.abs(g.speedX);
                    }
                    return;
                }
            }
        }
    }
    
    void drawDebug(ShapeRenderer sr) {
        sr.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
}
