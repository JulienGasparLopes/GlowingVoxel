package menus;

import main.Main;
import pinzen.utils.glow.Color;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.components.CString;
import pinzen.utils.glow.components.Component;
import pinzen.utils.glow.graphics.GRectangle;
import pinzen.utils.glow.inputs.InputManager;
import pinzen.utils.glow.inputs.InputManager.MouseButton;
import pinzen.utils.glow.inputs.InputManager.MouseEvent;
import pinzen.utils.glow.textures.Texture;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex2f;

public class CSlider extends Component{

	private Vertex2f position;
	
	private float max, min;
	private float currentValuePercent;
	private boolean isHolding;
	
	private CString strMax, strMin, strCurrent;
	private GRectangle slider, cursor;
		
	public CSlider(float min, float max) {
		this.min = min;
		this.max = max;
		
		if(this.max < this.min) {
			this.max = min;
		}
		
		this.currentValuePercent = 0.5f;
		this.isHolding = false;
		
		this.position = new Vertex2f(100, 100);
		
		this.strMin = new CString(""+min, 15, position.x, position.y+20);
		this.strMax = new CString(""+max, 15, position.x+100, position.y+20);
		this.strCurrent = new CString(""+getValue(), 15, position.x+50, position.y+20);
		this.strMin.center(true, false);
		this.strMax.center(true, false);
		this.strCurrent.center(true, false);
		
		this.slider = new GRectangle(100, 4, new Color(1, 0, 0, 1));
		this.cursor = new GRectangle(20, 20, new Color(1, 0, 0, 1));
	}
	
	public void setPosition(Vertex2f pos) {
		this.strMin.setPosition(pos.x, pos.y + 20);
		this.strMax.setPosition(pos.x+100, pos.y+20);
		this.strCurrent.setPosition(pos.x+50, pos.y+20);
		
		this.position = pos.clone();
	}
	
	@Override
	public void update(InputManager inputs, long delta) {
		if(this.isHolding) {
			float perc = (inputs.getcursorPos().x - this.position.x - this.cursor.getDimension().x/2f) / 100f;
			if(perc < 0)
				perc = 0f;
			if(perc > 1f)
				perc = 1f;
			
			this.currentValuePercent = perc;
			this.strCurrent.setText(""+getValue());
		}
	}

	@Override
	public void render(ShaderProgram s) {
		s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(position));
		Main.textureManager.bindDefaultTexture();
		this.slider.render();
		
		s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(Vertex2f.translate(position, new Vertex2f(currentValuePercent*100, -8))));
		Main.textureManager.bindDefaultTexture();
		this.cursor.render();
		
		this.strMin.render(s);
		this.strMax.render(s);
		this.strCurrent.render(s);
	}

	@Override
	public boolean onMouseEvent(Vertex2f pos, MouseButton button, MouseEvent event) {
		Vertex2f bl = position;
		Vertex2f tr = Vertex2f.translate(position, new Vertex2f(slider.getDimension().x, cursor.getDimension().y));
		
		if(event == MouseEvent.RELEASED) {
			this.isHolding = false;
			return false;
		}
		else if(event == MouseEvent.PRESSED && pos.x >= bl.x && pos.x <= tr.x && pos.y >= bl.y && pos.y <= tr.y) {
			this.isHolding = true;
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setValue(float v) {
		if(v >= min && v <= max) {
			this.currentValuePercent = (v-min) / (max-min);
			this.strCurrent.setText(""+getValue());
		}
	}
	
	public void setPercentage(float perc) {
		if(perc >= 0f && perc <= 1f) {
			this.currentValuePercent = perc;
			this.strCurrent.setText(""+getValue());
		}
	}
	
	public float getValue() {
		return min + (max-min)*this.currentValuePercent;
	}

}
