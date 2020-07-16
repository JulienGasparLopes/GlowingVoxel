package menus;

import entities.Player;
import main.Utils;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.components.CString;
import pinzen.utils.glow.inputs.Key;
import pinzen.utils.glow.inputs.InputManager.MouseButton;
import pinzen.utils.glow.inputs.InputManager.MouseEvent;
import pinzen.utils.glow.logic.ApplicationWindow;
import pinzen.utils.glow.logic.GUI;
import pinzen.utils.glow.logic.GUISimple;
import pinzen.utils.glow.logic.Menu;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex2f;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;
import world.Chunk;
import world.Cube;
import world.CubeFace;
import world.World;

public class MenuGame extends Menu{

	private World world;
	private Player player;
	private boolean firstPerson;
	private Matrix4f view;
	
	private Cube selectedCube;
	private Chunk selectedCubeChunk;
	private CubeFace selectedFace;
	private Vertex3f selectedCubeCoord;
	
	private boolean cursorBound;
	
	private GUI guiDebug;
	private CString guiPosition, guiLookDirection, guiCubeSelected, guiFaceSelected, guiChunk, fakeSight;
	
	public MenuGame(ApplicationWindow win) {
		super(win);
		
		this.firstPerson = false;
		this.view = new Matrix4f();
		
		this.selectedCube = null;
		this.selectedCubeChunk = null;
		this.selectedFace = CubeFace.NONE;
		this.selectedCubeCoord = null;
		
		this.init();
	}

	@Override
	public void onMouseEvent(Vertex2f pos, MouseButton button, MouseEvent event) {
		if(event == MouseEvent.RELEASED && this.selectedCube != null) {
			if(button == MouseButton.LEFT) {
				this.selectedCube.remove();
				this.selectedCubeChunk.updateVAO();
			}
			else if(button == MouseButton.RIGHT) {
				Vertex3f cubePos = Vertex3f.translate(selectedCubeCoord, this.selectedFace.getDirection());

				Cube cube = this.world.getCube(cubePos);
				if(cube != null) {
					cube.setColor(new Vertex4f(0, 0.4f, 0.8f, 1f));
					this.selectedCubeChunk.updateVAO();
				}
			}
		}
	}

	public void init() {				
		guiDebug = new GUISimple(this);
		
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
		
		fakeSight = new CString("+", 25, this.getWidth()/2, this.getHeight()/2);
		fakeSight.setPosition(Vertex2f.scale(Vertex2f.difference(fakeSight.getBounds(), this.getBounds()), 0.5f));
		
		guiDebug.addComponent(guiPosition);
		guiDebug.addComponent(guiChunk);
		guiDebug.addComponent(guiLookDirection);
		guiDebug.addComponent(guiCubeSelected);
		guiDebug.addComponent(guiFaceSelected);
		guiDebug.addComponent(fakeSight);
		
		this.addGUI(guiDebug);
	}

	@Override
	public void onShow() {
		player = new Player();
		player.setPosition(new Vertex3f(0f, 40f, 0f));
		
		this.world = new World();
		int centerChunkX  = (int)this.player.getPosition().x / Chunk.CHUNK_WIDTH;
		int centerChunkZ  = (int)this.player.getPosition().z / Chunk.CHUNK_DEPTH;
		this.world.setCenterChunk(centerChunkX, centerChunkZ);
		
		this.world.addEntity(player);
	}

	@Override
	public void update(long delta) {
		this.world.update(delta);
		
		this.updateInputs(delta);

		
		Vertex3f rotation = player.getRotation();
		Vertex3f position = player.getPosition();
		Vertex3f lookAt = player.getLookDirection();

		//Update rotation
		Matrix4f rotX = Matrix4f.getRotationMatrix(new Vertex3f(1, 0, 0), - rotation.x);
		Matrix4f rotY = Matrix4f.getRotationMatrix(new Vertex3f(0, 1, 0), rotation.y);
		Matrix4f trans = Matrix4f.getTranslationMatrix(Vertex3f.scale(Vertex3f.translate(position, new Vertex3f(0.5f)), -1f));
		
		Vertex3f camOff = Vertex3f.scale(lookAt, firstPerson ? 0f : 4f);
		Matrix4f cameraOffset = Matrix4f.getTranslationMatrix(camOff);
		
		this.view = Matrix4f.mult(Matrix4f.mult(Matrix4f.mult(rotX, rotY), trans), cameraOffset);
		
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
		
		if(this.selectedCube != null) {
			this.selectedCube.deselect();
			this.selectedCubeChunk.updateVAO();
		}
		
		this.selectedCube = null;
		this.selectedCubeCoord = null;
		this.selectedCubeChunk = null;
		
		int newCenterChunkX  = (int)this.player.getPosition().x / Chunk.CHUNK_WIDTH;
		int newCenterChunkZ  = (int)this.player.getPosition().z / Chunk.CHUNK_DEPTH;
		
		this.world.setCenterChunk(newCenterChunkX, newCenterChunkZ);
		
		Cube selected = null;
		Vertex3f cubePos = null;
		Vertex3f center = Vertex3f.translate(position, new Vertex3f(0.5f));
		float maxRange = 10f, i = 0f;
		while(selected == null && i < maxRange){
			i += 0.01f;
			cubePos = Vertex3f.translate(center, Vertex3f.scale(lookAt, i));
			Cube current = this.world.getCube(cubePos.x, cubePos.y, cubePos.z);
			if(current != null && !current.isEmpty() ) {
				selected = current;
				
				this.selectedCube = current;
				this.selectedCubeChunk = this.world.getChunk(cubePos.x, cubePos.z);
				this.selectedCubeCoord = new Vertex3f(cubePos.x, cubePos.y, cubePos.z);
				
				this.selectedCube.select();
				this.selectedCubeChunk.updateVAO();
				
				
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
			float invert = this.firstPerson ? 1f : -1f;
			this.player.rotate(new Vertex3f(invert * mouseDelta.y*delta*0.002f, mouseDelta.x*delta*0.002f, 0));
			
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
		if(inputs().consumeKey(Key.F2)) {
			this.firstPerson = !this.firstPerson;
		}
		if(inputs().consumeKey(Key.F5)) {
			System.out.println("Saving ...");
			this.world.save();
			System.out.println("Saved !");
		}
	}

	@Override
	public void render(ShaderProgram s) {
		s.setUniformMatrix4f("view", view);
		this.world.render(s);
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
