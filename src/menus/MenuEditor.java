package menus;

import java.util.ArrayList;
import java.util.List;

import editor.ModelAnchor;
import editor.ModelEdit;
import editor.ModelPivot;
import entities.Player;
import main.Utils;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.components.CString;
import pinzen.utils.glow.inputs.InputManager.MouseButton;
import pinzen.utils.glow.inputs.InputManager.MouseEvent;
import pinzen.utils.glow.inputs.Key;
import pinzen.utils.glow.logic.ApplicationWindow;
import pinzen.utils.glow.logic.GUI;
import pinzen.utils.glow.logic.GUISimple;
import pinzen.utils.glow.logic.Menu;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex2f;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;
import world.Cube;
import world.CubeFace;

public class MenuEditor extends Menu{

	private Player player;
	private Matrix4f view;
	
	private boolean cursorBound;
	
	private Cube selectedCube;
	private CubeFace selectedFace;
	private Vertex3f selectedCubeCoord;
	
	private List<ModelEdit> modelEditList;
	private ModelEdit currentModelEdit;
	
	private GUI guiDebug;
	private CString guiPosition, guiLookDirection, guiCubeSelected, guiFaceSelected, guiChunk, fakeSight;
	
	private CSlider sliderRed, sliderGreen, sliderBlue, sliderAlpha;
	
	public MenuEditor(ApplicationWindow win) {
		super(win);
		
		this.cursorBound = false;
		
		this.selectedCube = null;
		this.selectedFace = CubeFace.NONE;
		this.selectedCubeCoord = null;
		
		this.init();
	}

	public void init() {
		player = new Player();
		player.setPosition(new Vertex3f(-5f, 0f, -5f));
		player.setGravityEffect(false);
		
		this.modelEditList = new ArrayList<ModelEdit>();
		currentModelEdit = new ModelEdit();
		this.modelEditList.add(currentModelEdit);
		
		//Create some empty model edits
		for(int i = 0; i<2; i++) {
			ModelEdit m = new ModelEdit();
			this.modelEditList.add(m);
		}
		
		//Create some cubes, pivot and anchor on model 3
		ModelEdit m = this.modelEditList.get(2);
		m.setCube(0, 1, 1, new Vertex4f(1,0,0,1));
		m.setPivot(new Vertex4f(1,1,0,1), new Vertex3f(1,1,0));
		
		ModelEdit m1 = this.modelEditList.get(0);
		m1.addAnchor(new Vertex3f(-1,0,0));
		
		guiDebug = new GUISimple(this);
		fakeSight = new CString("+", 25, this.getWidth()/2, this.getHeight()/2);
		fakeSight.setPosition(Vertex2f.scale(Vertex2f.difference(fakeSight.getBounds(), this.getBounds()), 0.5f));
		
		int h = this.getHeight();
		int offset = 20;
		
		guiPosition = new CString("position : (None, None, None)", 15, 10, h-offset);
		offset += guiPosition.getBounds().y;
		
		guiChunk = new CString("current chunk : None", 15, 10, h-offset);
		offset += guiChunk.getBounds().y;
		
		guiLookDirection = new CString("look dir : None", 15, 10, h-offset);
		offset += guiLookDirection.getBounds().y;
		
		guiCubeSelected = new CString("cube selected : None", 15, 10, h-offset);
		offset += guiCubeSelected.getBounds().y;
		
		guiFaceSelected = new CString("face selected : None", 15, 10, h-offset);
		
		guiDebug.addComponent(guiPosition);
		guiDebug.addComponent(guiChunk);
		guiDebug.addComponent(guiLookDirection);
		guiDebug.addComponent(guiCubeSelected);
		guiDebug.addComponent(guiFaceSelected);
		guiDebug.addComponent(fakeSight);
		
		this.sliderRed = new CSlider(0, 1f);
		this.sliderGreen = new CSlider(0, 1f);
		this.sliderBlue = new CSlider(0, 1f);
		this.sliderAlpha = new CSlider(.4f, 1f); this.sliderAlpha.setPercentage(1f);
		
		this.sliderRed.setPosition(new Vertex2f(20, 20));
		this.sliderGreen.setPosition(new Vertex2f(20, 70));
		this.sliderBlue.setPosition(new Vertex2f(20, 120));
		this.sliderAlpha.setPosition(new Vertex2f(20, 170));
		
		guiDebug.addComponent(sliderRed);
		guiDebug.addComponent(sliderGreen);
		guiDebug.addComponent(sliderBlue);
		guiDebug.addComponent(sliderAlpha);
		
		this.addGUI(guiDebug);
	}

	@Override
	public void onShow() {
		
	}

	@Override
	public void update(long delta) {
		ModelAnchor.updateColorCounter();
		ModelPivot.updateColorCounter();
		
		this.updateInputs(delta);
		
		this.currentModelEdit.update(delta);

		Vertex3f rotation = player.getRotation();
		Vertex3f position = player.getPosition();
		Vertex3f lookAt = player.getLookDirection();

		//Update rotation
		Matrix4f rotX = Matrix4f.getRotationMatrix(new Vertex3f(1, 0, 0), - rotation.x);
		Matrix4f rotY = Matrix4f.getRotationMatrix(new Vertex3f(0, 1, 0), rotation.y);
		Matrix4f trans = Matrix4f.getTranslationMatrix(Vertex3f.scale(Vertex3f.translate(position, new Vertex3f(0.5f)), -1f));
		
		this.view = Matrix4f.mult(Matrix4f.mult(rotX, rotY), trans);
		
		if(this.guiDebug.isVisible()) {
			guiPosition.setText("position : (" + Utils.format(position.x, 2) + "," + Utils.format(position.y, 2) + "," + Utils.format(position.z, 2) + ")");
			guiLookDirection.setText("look dir : (" + Utils.format(lookAt.x, 2) + "," + Utils.format(lookAt.y, 2) + "," + Utils.format(lookAt.z, 2) + ")");
			if(this.selectedCubeCoord == null) {
				guiCubeSelected.setText("cube selected : None");
				guiFaceSelected.setText("face selected : None");
			}
			else {
				guiCubeSelected.setText("cube selected : (" + (int)selectedCubeCoord.x + "," + (int)selectedCubeCoord.y + "," + (int)selectedCubeCoord.z + ")" );
				String face = this.selectedFace.name();
				guiFaceSelected.setText("face selected : " + face);
			}
		}
		
		this.player.update(null, delta);
		
		if(this.selectedCube != null) {
			this.selectedCube.deselect();
			this.currentModelEdit.updateVAO();
		}
		
		this.selectedCube = null;
		this.selectedCubeCoord = null;
		
		Cube selected = null;
		Vertex3f cubePos = null;
		Vertex3f center = Vertex3f.translate(position, new Vertex3f(0.5f));
		float maxRange = 40f, i = 0f;
		while(selected == null && i < maxRange){
			i += 0.01f;
			cubePos = Vertex3f.translate(center, Vertex3f.scale(lookAt, i));
			Cube current = this.currentModelEdit.getCube(cubePos.x, cubePos.y, cubePos.z);
			if(current != null && !current.isEmpty() ) {
				selected = current;
				
				this.selectedCube = current;
				this.selectedCubeCoord = new Vertex3f(cubePos.x, cubePos.y, cubePos.z);
				
				this.selectedCube.select();
				//this.modelEdit.updateVAO();
				
				//Find selected face
				float dx = cubePos.x - (int)Math.floor(cubePos.x);
				float dy = cubePos.y - (int)Math.floor(cubePos.y);
				float dz = cubePos.z - (int)Math.floor(cubePos.z);
				float[] distToFace = new float[] {
						1f - dy,
						dy,
						dx,
						1f - dx,
						1f - dz,
						dz
				};
				float min = 1f;
				int face = -1;
				for(int faceId = 0; faceId<distToFace.length; faceId++) {
					if(distToFace[faceId] < min) {
						min = distToFace[faceId];
						face = faceId;
					}
				}
				this.selectedFace = CubeFace.getFaceFromIndex(face);
			}
		}
		
	}
	
	private void updateInputs(long delta) {
		this.player.updateInputs(inputs(), delta);

		if(this.cursorBound) {
			Vertex2f newCursorPos = inputs().getcursorPos();
			Vertex2f cursorPosMid = inputs().centerCursor();
			
			Vertex2f mouseDelta = Vertex2f.difference(newCursorPos, cursorPosMid);
			this.player.rotate(new Vertex3f(mouseDelta.y*delta*0.002f, mouseDelta.x*delta*0.002f, 0));
			
			Vertex3f camRot = this.player.getRotation();
			if(camRot.x > 90)
				this.player.setRotation(new Vertex3f(90, camRot.y, 0));
			
			if(camRot.x <= -75)
				this.player.setRotation(new Vertex3f(-75, camRot.y, 0));
		}
		
		if(inputs().consumeKey(Key.ESCAPE)) {
			inputs().centerCursor();
			this.cursorBound = !this.cursorBound;
			if(this.cursorBound)
				inputs().hideCursor();
			else
				inputs().showCursor();
		}
		if(inputs().consumeKey(Key.F1)) {
			this.guiDebug.toggleVisibility();
		}
		
		//Switch current model edit
		if(inputs().consumeKey(Key.ONE))
			this.currentModelEdit = this.modelEditList.get(0);
		else if(inputs().consumeKey(Key.TWO))
			this.currentModelEdit = this.modelEditList.get(1);
		else if(inputs().consumeKey(Key.THREE))
			this.currentModelEdit = this.modelEditList.get(2);
		
		//Attach modelEdit to anchor
		if(this.selectedCube instanceof ModelAnchor) {
			ModelAnchor anchor = ((ModelAnchor)this.selectedCube);
			if(!anchor.hasPivot()) {
				if(inputs().consumeKey(Key.FOUR) && this.currentModelEdit != this.modelEditList.get(1)) {
					anchor.setPivot(this.modelEditList.get(1).getPivot().clone());
				}
				if(inputs().consumeKey(Key.FIVE) && this.currentModelEdit != this.modelEditList.get(2)) {
					anchor.setPivot(this.modelEditList.get(2).getPivot().clone());
				}
			}
			else {
				float speed = 2f;
				if(inputs().isKeyDown(Key.W)) {
					anchor.getPivot().rotate(new Vertex3f(speed, 0, 0));
				}
				else if(inputs().isKeyDown(Key.X)) {
					anchor.getPivot().rotate(new Vertex3f(0, speed, 0));
				}
				else if(inputs().isKeyDown(Key.C)) {
					anchor.getPivot().rotate(new Vertex3f(0 , 0, speed));
				}
			}
		}
	}

	@Override
	public void render(ShaderProgram s) {
		s.setUniformMatrix4f("view", view);
		s.setUniformMatrix4f("model", new Matrix4f());
		this.currentModelEdit.render(s);
	}

	@Override
	public void onHide() {
		
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void onMouseEvent(Vertex2f pos, MouseButton button, MouseEvent event) {
		if(this.cursorBound && event == MouseEvent.CLICKED && this.selectedCube != null) {
			if(button == MouseButton.LEFT) {
				this.currentModelEdit.removeCube(this.selectedCubeCoord);
				this.selectedCube = null;
				this.selectedCubeCoord = null;
			}
			else if(button == MouseButton.RIGHT) {
				Vertex3f cubePos = Vertex3f.translate(selectedCubeCoord, this.selectedFace.getDirection());
				if(inputs().isKeyDown(Key.A))
					this.currentModelEdit.addAnchor(cubePos);
				else if(inputs().isKeyDown(Key.E)) {
					if(this.modelEditList.get(0) != this.currentModelEdit)
						this.currentModelEdit.setPivot(new Vertex4f(sliderRed.getValue(), sliderGreen.getValue(), sliderBlue.getValue(), sliderAlpha.getValue()), cubePos);
				}
				else
					this.currentModelEdit.setCube(cubePos, new Vertex4f(sliderRed.getValue(), sliderGreen.getValue(), sliderBlue.getValue(), sliderAlpha.getValue()));
			}
		}
	}
}
