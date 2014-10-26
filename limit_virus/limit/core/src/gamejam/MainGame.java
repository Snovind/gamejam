package gamejam;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

public class MainGame extends Game {

	@Override
	public void create () {
        Gdx.graphics.setTitle("Limit Virus");
        setScreen(new GameScreen());
	}

	@Override
	public void render () {
        super.render();
	}
}
