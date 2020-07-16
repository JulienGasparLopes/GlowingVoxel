package editor;

import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;
import world.Cube;

public class ModelPivot extends Cube{
	
	private static float COLOR = 0;
	private static boolean ASCENDING = true;
	
	public static void updateColorCounter() {
		COLOR += ASCENDING ? 0.04f : -0.04f;
		if(COLOR >= 1f)
			ASCENDING = false;
		else if(COLOR <= 0)
			ASCENDING = true;
	}
	
	private Vertex3f position, rotation;
	private ModelEdit linkedModelEdit;
	private Vertex4f colorSave;
	
	public ModelPivot(ModelEdit modelEdit, Vertex4f color, Vertex3f position) {
		super(color);
		
		this.colorSave = color.clone();
		this.linkedModelEdit = modelEdit;
		
		int cx = (int)Math.floor(position.x);
		int cy = (int)Math.floor(position.y);
		int cz = (int)Math.floor(position.z);
		
		this.position = new Vertex3f(cx, cy, cz);
		this.rotation = new Vertex3f();
	}
	
	public ModelPivot(ModelEdit modelEdit, Vertex3f position) {
		this(modelEdit, new Vertex4f(0, 0, 0, 1f), position);
	}
	
	public Vertex3f getPosition() {
		return this.position.clone();
	}
	
	public void rotate(Vertex3f rot) {
		this.rotation = Vertex3f.translate(rotation, rot);
	}
	
	public void update(long delta) {
		Vertex4f newColor = new Vertex4f();
		if(COLOR >= 0.75f) {
			for(int i = 1; i<=3; i++) {
				float diff = 1f - colorSave.get(i);
				newColor.set(i, colorSave.get(i) + diff * (4*COLOR - 3f));
			}
			newColor.w = 1f;
		}
		else {
			newColor = colorSave;
		}
				
		this.setColor(newColor);
		this.linkedModelEdit.updateVAO();
	}

	public void render(ShaderProgram s, Vertex3f anchorPos) {
		//Update rotation
		Matrix4f rotX = Matrix4f.getRotationMatrix(new Vertex3f(1, 0, 0), - rotation.x);
		Matrix4f rotY = Matrix4f.getRotationMatrix(new Vertex3f(0, 1, 0), rotation.y);
		Matrix4f rotZ = Matrix4f.getRotationMatrix(new Vertex3f(0, 0, 1), rotation.z);
		Matrix4f trans = Matrix4f.getTranslationMatrix(Vertex3f.translate(anchorPos, Vertex3f.scale(position, -1f)));
		Matrix4f transToCenter = Matrix4f.getTranslationMatrix(Vertex3f.translate(position, new Vertex3f(0.5f)));
		Matrix4f transToCenterBack = Matrix4f.getTranslationMatrix(Vertex3f.scale(Vertex3f.translate(position, new Vertex3f(0.5f)), -1f));

		Matrix4f rot = Matrix4f.mult(Matrix4f.mult(rotX, rotY), rotZ);
		
		//Matrix4f model = Matrix4f.mult(Matrix4f.mult(Matrix4f.mult(transToCenter, rot), transToCenterBack), trans);
		Matrix4f model = Matrix4f.mult(trans, Matrix4f.mult(Matrix4f.mult(transToCenter, rot), transToCenterBack));
		
		s.setUniformMatrix4f("model", model);
		this.linkedModelEdit.render(s);
	}
	
	public ModelPivot clone() {
		return new ModelPivot(this.linkedModelEdit, this.colorSave, this.position);
	}
}
