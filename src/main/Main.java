package main;

import java.io.File;

import menus.MenuEditor;
import menus.MenuGame;
import menus.MenuPrincipal;
import pinzen.utils.glow.Color;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.logic.ApplicationWindow3D;
import pinzen.utils.glow.logic.Menu;
import pinzen.utils.glow.textures.TextureManager;

public class Main {

	public static TextureManager textureManager;
	public static Menu menuPrincipal, menuGame, menuEditor;
	
	public static void main(String[] args) {		
		ApplicationWindow3D win = new ApplicationWindow3D("Voxel", 1000, 700, true, 60, 1f, 100f);
		ShaderProgram shaderMain = new ShaderProgram(
				new File("res/shaders/mainShader.vert"),
				new File("res/shaders/mainShader.frag"));
		ShaderProgram shaderGUI = new ShaderProgram(
				new File("res/shaders/guiShader.vert"),
				new File("res/shaders/guiShader.frag"));
		
		win.setGUIShader(shaderGUI);
		win.setMainShader(shaderMain);
		
		textureManager = new TextureManager("res");

		menuGame = new MenuGame(win);
		
		menuEditor = new MenuEditor(win);
		
		menuPrincipal = new MenuPrincipal(win);
		menuPrincipal.setCurrent();
				
		win.setBackground(new Color(0, 0.4f, 0.7f, 0.6f));
		win.showFPSOnTitle(true);
		
		while(!win.isDisposed()) {
			win.update();
		}
	}
}
