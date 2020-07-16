package editor;

import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;
import world.Cube;

public class ModelAnchor extends Cube{

	private static int NEXT_ANCHOR_ID = 1;
	public static int getNextAnchorId() {
		int next = NEXT_ANCHOR_ID;
		NEXT_ANCHOR_ID++;
		return next;
	}
	
	private static float COLOR = 0;
	private static boolean ASCENDING = true;
	
	public static void updateColorCounter() {
		COLOR += ASCENDING ? 0.04f : -0.04f;
		if(COLOR >= 1f)
			ASCENDING = false;
		else if(COLOR <= 0)
			ASCENDING = true;
	}
	
	private Vertex3f position;
	private ModelPivot linkedPivot;
	
	public ModelAnchor(Vertex3f position) {
		super(new Vertex4f(0, 0, 0, 1f));
		
		int cx = (int)Math.floor(position.x);
		int cy = (int)Math.floor(position.y);
		int cz = (int)Math.floor(position.z);
		
		this.position = new Vertex3f(cx, cy, cz);
   	}
	
	public void setPivot(ModelPivot pivot) {
		if(!this.hasPivot()) {
			this.linkedPivot = pivot;
		}
	}
	
	public void removePivot() {
		this.linkedPivot = null;
	}
	
	public Vertex3f getPosition() {
		return this.position.clone();
	}
	
	public void update(long delta) {
		this.setColor(new Vertex4f(COLOR, COLOR, COLOR, 1f));
		
		if(this.linkedPivot != null)
			this.linkedPivot.update(delta);
	}
	
	public void render(ShaderProgram s) {
		//s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(position));
		if(this.linkedPivot != null)
			this.linkedPivot.render(s, this.position);
	}
	
	public boolean hasPivot() {
		return this.linkedPivot != null;
	}
	
	public ModelPivot getPivot() {
		return this.linkedPivot;
	}
}
