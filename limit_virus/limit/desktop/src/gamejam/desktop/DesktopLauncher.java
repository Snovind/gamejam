package gamejam.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import gamejam.MainGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.fullscreen = false;
        config.resizable = false;
        config.vSyncEnabled = true;
        config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
        config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;

        //config.width = 800;
        //config.height = 640;
        System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		new LwjglApplication(new MainGame(), config);
	}
}
