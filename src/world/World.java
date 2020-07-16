package world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entities.Entity;
import entities.Player;
import main.SaveManager;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.inputs.IMouseListener;
import pinzen.utils.glow.inputs.InputManager;
import pinzen.utils.glow.inputs.InputManager.MouseButton;
import pinzen.utils.glow.inputs.InputManager.MouseEvent;
import pinzen.utils.glow.inputs.Key;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex2f;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;

public class World implements IMouseListener{
	
	public static final boolean DEBUG_BOUNDS = false;
	public static final float GRAVITY = -0.0089f;
		
	private OpenSimplexNoise noise;
	
	private String name;
	private int viewChunkDistance;
	private Vertex2f centerChunkPosition;
	
	private HashMap<String, Chunk> chunks;
	
	private List<Entity> entities, entitiesToAdd, entitiesToRemove;
		
	public World() {	
		this.noise = new OpenSimplexNoise();
		this.name = "TestWorld";
		this.viewChunkDistance = 3;
				
		chunks = new HashMap<String, Chunk>();
		entities = new ArrayList<Entity>();
		entitiesToAdd = new ArrayList<Entity>();
		entitiesToRemove = new ArrayList<Entity>();
		
		this.loadMap(new Vertex2f(0, 0));
	}
	
	private void loadMap(Vertex2f centerChunkPosition) {
		this.centerChunkPosition = centerChunkPosition.clone();
		
		for(int x = (int)centerChunkPosition.x - this.viewChunkDistance; x <= centerChunkPosition.x + this.viewChunkDistance; x++) {
			for(int z = (int)centerChunkPosition.y - this.viewChunkDistance; z <= centerChunkPosition.y + this.viewChunkDistance; z++){	
				this.loadChunk(x, z);
			}
		}
	}
	
	private void loadChunk(int x, int z) {
		try {
			List<String> save = SaveManager.load(name + "\\" + Chunk.getChunkUid(x, z));
			Chunk c = new Chunk(save);
			this.chunks.put(c.getUid(), c);
		}
		catch(IOException e) {
			int maxTotalHeight = Chunk.CHUNK_HEIGHT;
			int minFloorHeight = (int)(0.9f * maxTotalHeight);
			
			int[][] heightMap = new int[Chunk.CHUNK_WIDTH][Chunk.CHUNK_DEPTH];
			int minHeight = maxTotalHeight;
			int maxHeight = 0;
			
			for(int cx = 0; cx<Chunk.CHUNK_WIDTH; cx++) {
				for(int cz = 0; cz<Chunk.CHUNK_DEPTH; cz++) {
					float rand = ((float)noise.eval(x + (float)cx/Chunk.CHUNK_WIDTH, z + (float)cz/Chunk.CHUNK_DEPTH) + 1f) / 2f;
					int value = minFloorHeight + (int) (rand*(maxTotalHeight-minFloorHeight));
					
					if(value > maxHeight)
						maxHeight = value;
					if(value < minHeight)
						minHeight = value;
						
					heightMap[cx][cz] = value;
				}
			}
			
			Chunk c = new Chunk(x, z, new Vertex4f(1, 0, 0, 1), heightMap);

			chunks.put(c.getUid(), c);
		}
	}
	
	public void save() {
		for(Chunk c : chunks.values()) {
			c.save(name);
		}
	}
	
	public void update(long delta) {
		this.entities.addAll(entitiesToAdd);
		this.entities.removeAll(entitiesToRemove);
		this.entitiesToAdd.clear();
		this.entitiesToRemove.clear();
		
		for(Entity e : entities) {
			e.update(this, delta);
		}
	}

	public void setCenterChunk(int newCenterChunkX, int newCenterChunkZ) {		
		//guiChunk.setText("current chunk : (" + newCenterChunkX  + ", " + newCenterChunkZ + ")");
		
		if(newCenterChunkX > this.centerChunkPosition.x) {
			for(int z = (int)centerChunkPosition.y - this.viewChunkDistance; z <= centerChunkPosition.y + this.viewChunkDistance; z++) {
				Chunk oldChunk = this.getChunk((this.centerChunkPosition.x - this.viewChunkDistance) * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_DEPTH);
				oldChunk.save(name);
				this.chunks.remove(oldChunk.getUid());
				
				this.loadChunk(newCenterChunkX + this.viewChunkDistance, z);
			}
			this.centerChunkPosition.x = newCenterChunkX;
		}
		else if(newCenterChunkX < this.centerChunkPosition.x) {
			for(int z = (int)centerChunkPosition.y - this.viewChunkDistance; z <= centerChunkPosition.y + this.viewChunkDistance; z++) {
				Chunk oldChunk = this.getChunk((this.centerChunkPosition.x + this.viewChunkDistance) * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_DEPTH);
				oldChunk.save(name);
				this.chunks.remove(oldChunk.getUid());
				
				this.loadChunk(newCenterChunkX - this.viewChunkDistance, z);
			}
			this.centerChunkPosition.x = newCenterChunkX; 
		}
		else if(newCenterChunkZ > this.centerChunkPosition.y) {
			for(int x = (int)centerChunkPosition.x - this.viewChunkDistance; x <= centerChunkPosition.x + this.viewChunkDistance; x++) {
				Chunk oldChunk = this.getChunk(x * Chunk.CHUNK_WIDTH, (this.centerChunkPosition.y - this.viewChunkDistance) * Chunk.CHUNK_DEPTH);
				oldChunk.save(name);
				this.chunks.remove(oldChunk.getUid());
				
				this.loadChunk(x, newCenterChunkZ + this.viewChunkDistance);
			}
			this.centerChunkPosition.y = newCenterChunkZ;
		}
		else if(newCenterChunkZ < this.centerChunkPosition.y) {
			for(int x = (int)centerChunkPosition.x - this.viewChunkDistance; x <= centerChunkPosition.x + this.viewChunkDistance; x++) {
				Chunk oldChunk = this.getChunk(x * Chunk.CHUNK_WIDTH, (this.centerChunkPosition.y + this.viewChunkDistance) * Chunk.CHUNK_DEPTH);
				oldChunk.save(name);
				this.chunks.remove(oldChunk.getUid());
				
				this.loadChunk(x, newCenterChunkZ - this.viewChunkDistance);
			}
			this.centerChunkPosition.y = newCenterChunkZ;
		}
	}
	
	public Chunk getChunk(float x, float z) {
		int chunkX = (int)Math.floor(x / Chunk.CHUNK_WIDTH);
		int chunkZ = (int)Math.floor(z / Chunk.CHUNK_DEPTH);
		
		try {
			return chunks.get(Chunk.getChunkUid(chunkX, chunkZ));
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public Cube getCube(float x, float y, float z) {
		Chunk chunk = getChunk(x, z);
		
		int cubeX = (int)Math.floor((Math.floor(x % Chunk.CHUNK_WIDTH) + Chunk.CHUNK_WIDTH) % Chunk.CHUNK_WIDTH);
		int cubeY = (int)Math.floor(y);
		int cubeZ = (int)Math.floor((Math.floor(z % Chunk.CHUNK_DEPTH) + Chunk.CHUNK_DEPTH) % Chunk.CHUNK_DEPTH);
		
		try {
			return chunk.getCube(cubeX, cubeY, cubeZ);
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public Cube getCube(Vertex3f pos) {
		return this.getCube(pos.x, pos.y, pos.z);
	}
	
	public void addEntity(Entity e) {
		this.entitiesToAdd.add(e);
	}
	
	public void removeEntity(Entity e) {
		this.entitiesToRemove.add(e);
	}
	
	public void render(ShaderProgram s) {
		for(Chunk c : chunks.values()) {
			Matrix4f model = Matrix4f.getTranslationMatrix(new Vertex3f(c.getX()*Chunk.CHUNK_WIDTH, 0, c.getZ()*Chunk.CHUNK_DEPTH));
			s.setUniformMatrix4f("model", model);
			c.render(s);
		}
		
		for(Entity e : entities) {
			e.render(s);
		}
	}
	
	/*
	public void renderGUI(ShaderProgram s) {
		if(this.showDebugGui) {
			int offset = guiPosition.getHeight();
			s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(new Vertex2f(0, window.getHeight() - offset)));
			this.guiPosition.render();
			
			offset += guiChunk.getHeight();
			s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(new Vertex2f(0, window.getHeight() - offset)));
			this.guiChunk.render();
			
			offset += guiLookDirection.getHeight();
			s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(new Vertex2f(0, window.getHeight() - offset)));
			this.guiLookDirection.render();
			
			offset +=  guiCubeSelected.getHeight();
			s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(new Vertex2f(0, window.getHeight() - offset)));
			this.guiCubeSelected.render();
			
			offset +=  guiFaceSelected.getHeight();
			s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(new Vertex2f(0, window.getHeight() - offset)));
			this.guiFaceSelected.render();
		}
		Vertex2f bl = new Vertex2f((window.getWidth() - this.fakeSight.getWidth()) / 2f, (window.getHeight() - this.fakeSight.getHeight()) / 2f);
		s.setUniformMatrix4f("model", Matrix4f.getTranslationMatrix(bl));
		this.fakeSight.render();
	}
	*/

	@Override
	public void onMouseEvent(Vertex2f pos, MouseButton button, MouseEvent event) {

	}
}
