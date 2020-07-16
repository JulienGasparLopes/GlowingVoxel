package menus;

import main.Main;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.inputs.InputManager.MouseButton;
import pinzen.utils.glow.inputs.InputManager.MouseEvent;
import pinzen.utils.glow.logic.ApplicationWindow;
import pinzen.utils.glow.logic.MenuSimple;
import pinzen.utils.mathsfog.Vertex2f;

public class MenuPrincipal extends MenuSimple{
	
	private Button bPlay, bEditor, bExit;
		
	public MenuPrincipal(ApplicationWindow win) {
		super(win);
		
		this.init();
	}

	public void init() {
		float width = this.getBounds().x;
		float height = this.getBounds().y;
		
		this.bPlay = new Button(width/2 - 300, height - 100, 600, 80, "Play");
		this.bEditor = new Button(width/2 - 300, height - 300, 600, 80, "Editor");
		this.bExit = new Button(width/2 - 300, height - 500, 600, 80, "Exit");
		
		this.bPlay.setCallback(this::callbackPlay);
		this.bEditor.setCallback(this::callbackEditor);
		this.bExit.setCallback(this::callbackExit);
		
		this.addComponent(bPlay);
		this.addComponent(bEditor);
		this.addComponent(bExit);
	}

	@Override
	public void onShow() {
		
	}

	@Override
	public void update(long delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(ShaderProgram s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onMouseEvent(Vertex2f pos, MouseButton button, MouseEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	private void callbackPlay() {
		Main.menuGame.setCurrent();
	}
	
	private void callbackEditor() {
		Main.menuEditor.setCurrent();
	}
	
	private void callbackExit() {
		System.out.println("exit");
	}
}
