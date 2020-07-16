package editor;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;
import world.Chunk;
import world.Cube;

public class ModelEdit {

	private HashMap<String, Cube> cubes, cubesTransparent;
	
	private HashMap<String, ModelAnchor> anchors;
	
	private String modelPivotId;
	
	private FloatBuffer verticesBuffer, normalsBuffer, colorsBuffer;
	
	private int vao, vboVertices, vboNormals, vboColors;
	private int verticesCount;
	
	private Matrix4f model;
	
	public ModelEdit() {
		cubes = new HashMap<String, Cube>();
		cubesTransparent = new HashMap<String, Cube>();
		model = new Matrix4f();
		
		this.anchors = new HashMap<String, ModelAnchor>();
		this.modelPivotId = "";
		
		this.setCube(0, 0, 0, new Vertex4f(1f, .4f, .2f, 1f));
		
		//Generate VAO
		vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //Generate VBO to store data
        vboVertices = glGenBuffers();
        vboNormals = glGenBuffers();
        vboColors = glGenBuffers();
        
        this.updateVAO();
	}
	
	public void updateVAO() {
		verticesBuffer = BufferUtils.createFloatBuffer(3 * 36 * (cubes.size() + cubesTransparent.size()));
		normalsBuffer  = BufferUtils.createFloatBuffer(3 * 36 * (cubes.size() + cubesTransparent.size()));
		colorsBuffer   = BufferUtils.createFloatBuffer(4 * 36 * (cubes.size() + cubesTransparent.size()));

		IntBuffer showFaces = BufferUtils.createIntBuffer(6);
		
		Object[] cubeLists = new Object[] {cubes, cubesTransparent};
		for(Object cubeList : cubeLists) {
			HashMap<String, Cube> cubesToRender = (HashMap<String, Cube>)cubeList;
	        for(String uid : cubesToRender.keySet()) {
	            showFaces.clear();
	        	Cube c = cubesToRender.get(uid);
	        	
	        	if(!c.isEmpty() && (!(c instanceof ModelAnchor) || !((ModelAnchor)c).hasPivot())) {
		        	
		        	String[] toParse = uid.replace("cubeX", "").replace("Z", "Y").split("Y");
		        	int x = Integer.parseInt(toParse[0]);
		        	int y = Integer.parseInt(toParse[1]);
		        	int z = Integer.parseInt(toParse[2]);
		        	
		        	//Transparent cube
		        	if(c.getColor().w < 1f) {
		        		int faceIndex = 0;
						for(int[] n : Chunk.neighbours) {
							try {
								if(this.getCube(x + n[0], y + n[1], z + n[2]) == null) {
									showFaces.put(faceIndex);
								}
							}
							catch(Exception e) {
								showFaces.put(faceIndex);
							}
							faceIndex++;
						}
		        	}
		        	else {
		        		showFaces.put(new int[] {0,1,2,3,4,5});
		        	}
		        	
		        	showFaces.flip();
		        	c.addRenderInfo(verticesBuffer, normalsBuffer, colorsBuffer, showFaces, new Vertex3f(x, y, z), false);
	        	}
	        }
		}
        
		verticesBuffer.flip();
		normalsBuffer.flip();
		colorsBuffer.flip();
		
		verticesCount = verticesBuffer.limit()/3;
				
		glBindVertexArray(vao);
		
		//layout = 0 for position of vertices
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        //layout = 1 for normals
        glBindBuffer(GL_ARRAY_BUFFER, vboNormals);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
        
        
        //layout = 2 for colors
        glBindBuffer(GL_ARRAY_BUFFER, vboColors);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
        glBufferData(GL_ARRAY_BUFFER, colorsBuffer, GL_STATIC_DRAW);
        

        //Free the current buffer
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void render(ShaderProgram s) {
		//s.setUniformMatrix4f("model", model);
		
		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, 0, verticesCount);
		
		for(ModelAnchor anchor : anchors.values()) {
			anchor.render(s);
		}
	}
	
	public void update(long delta) {
		for(ModelAnchor anch : anchors.values()) {
			anch.update(delta);;
		}
		
		Cube pivot = this.cubes.get(modelPivotId);
		if(pivot != null) {
			((ModelPivot)pivot).update(delta);
		}
		
		this.updateVAO();
	}
	
	public Cube getCube(float x, float y, float z) {
		int cx = (int)Math.floor(x);
		int cy = (int)Math.floor(y);
		int cz = (int)Math.floor(z);
		
		Cube toRet = this.cubes.get(getCubeUid(cx, cy, cz));
		if(toRet == null)
			toRet = this.cubesTransparent.get(getCubeUid(cx, cy, cz));
		
		return toRet;
	}
	
	private String getCubeUid(float x, float y, float z) {
		int cx = (int)Math.floor(x);
		int cy = (int)Math.floor(y);
		int cz = (int)Math.floor(z);
		
		return "cubeX" + cx + "Y" + cy + "Z" + cz;
	}
	
	private String getCubeUid(Vertex3f pos) {
		return this.getCubeUid(pos.x, pos.y, pos.z);
	}
	
	public void setCube(float x, float y, float z, Vertex4f color) {
		int cx = (int)Math.floor(x);
		int cy = (int)Math.floor(y);
		int cz = (int)Math.floor(z);
		
		Cube c = new Cube(color);
		
		if(color.w == 1f)
			cubes.put(getCubeUid(cx, cy, cz), c);
		else
			cubesTransparent.put(getCubeUid(cx, cy, cz), c);
		
		this.updateVAO();
	}
	
	public void setCube(Vertex3f pos, Vertex4f color) {
		this.setCube(pos.x, pos.y, pos.z, color);
	}
	
	public void removeCube(float x, float y, float z) {
		Cube deleted = null;
		int totalCubes = this.cubes.size() + this.cubesTransparent.size();
		String id = getCubeUid(x, y, z);
		
		if(totalCubes > 1) {
			deleted = this.cubes.remove(id);
		}
		
		if(totalCubes > 1 && deleted == null) {
			deleted = this.cubesTransparent.remove(id);
		}
		
		this.anchors.remove(id);
		
		if(this.modelPivotId.equals(id))
			this.modelPivotId = "";
		
		if(deleted != null)
			this.updateVAO();
	}
	
	public void removeCube(Vertex3f pos) {
		this.removeCube(pos.x, pos.y, pos.z);
	}
	
	public void addAnchor(Vertex3f pos) {
		String id = this.getCubeUid(pos);
		ModelAnchor anchor = new ModelAnchor(pos);
		
		this.anchors.put(id, anchor);
		this.cubes.put(id, anchor);
	}
	
	public void setPivot(Vertex4f color, Vertex3f pos) {
		if(this.modelPivotId == "") {
			String id = this.getCubeUid(pos);
			ModelPivot pivot = new ModelPivot(this, color, pos);
			this.modelPivotId = id;
			this.cubes.put(id, pivot);
		}
	}
	
	public ModelPivot getPivot() {
		return (ModelPivot) this.cubes.get(modelPivotId);
	}
	
	public boolean hasPivot() {
		return this.modelPivotId != "";
	}
}
