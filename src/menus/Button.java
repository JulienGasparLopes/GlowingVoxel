package menus;

import main.Main;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.components.CButton;
import pinzen.utils.glow.graphics.GRectangle;
import pinzen.utils.glow.graphics.GString;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex2f;

public class Button extends CButton{

	private GRectangle rectangle;
	private GString text;
	private Vertex2f txtPos;
	
	public Button(float x, float y, float width, float height, String txt) {
		super(x, y, width, height);
		rectangle = new GRectangle(width, height);
		text = new GString(txt, 22);
		
		txtPos = Vertex2f.scale(bounds.dimension, .5f);
		txtPos = Vertex2f.translate(bounds.position, txtPos);
		Vertex2f txtOffset = Vertex2f.scale(text.getDimension(), .5f);
		txtPos = Vertex2f.difference(txtOffset, txtPos);
	}

	@Override
	public void render(ShaderProgram s) {
		s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(this.txtPos));
		this.text.render();
		
		s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(this.bounds.position));
		
		if(this.getState() == ButtonState.IDDLE)
			Main.textureManager.bindTexture("buttons", "buttonIddle");
		else if(this.getState() == ButtonState.HOVERED)
			Main.textureManager.bindTexture("buttons", "buttonHovered");
		else if(this.getState() == ButtonState.PRESSED)
			Main.textureManager.bindTexture("buttons", "buttonPressed");
		else
			Main.textureManager.bindTexture("buttons", "buttonDisabled");
		
		rectangle.render();
		

	}
	
	public void setText(String txt) {
		this.text.dispose();
		this.text = new GString(txt, 22);
		
		txtPos = Vertex2f.scale(bounds.dimension, .5f);
		txtPos = Vertex2f.translate(bounds.position, txtPos);
		Vertex2f txtOffset = Vertex2f.scale(text.getDimension(), .5f);
		txtPos = Vertex2f.difference(txtOffset, txtPos);
	}

}
