package world;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
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
import java.util.List;

import org.lwjgl.BufferUtils;

import main.SaveManager;
import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;

public class Chunk {
	
	public static final int CHUNK_WIDTH = 16, CHUNK_HEIGHT = 32, CHUNK_DEPTH = 16;
	//6 vertices by face when rendering triangles, 8 vertices by face when rendering lines : 48 vertices max
	private static final FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(3 * 48 * CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_DEPTH),
									 normalsBuffer  = BufferUtils.createFloatBuffer(3 * 48 * CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_DEPTH),
									 colorsBuffer   = BufferUtils.createFloatBuffer(4 * 48 * CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_DEPTH);
	
	
	public static final int[][] neighbours= new int[][] {
		new int[] { 0,  1,  0},
		new int[] { 0, -1,  0},
		new int[] {-1,  0,  0},
		new int[] { 1,  0,  0},
		new int[] { 0,  0,  1},
		new int[] { 0,  0, -1},
	};
	
	private int positionX, positionZ;
	
	private boolean isEmpty;
	private Cube[][][] cubes;
	
	private int vao, vboVertices, vboNormals, vboColors;
	private int verticesCount;
	
	public Chunk(int posX, int posZ, Vertex4f color, int width, int height, int depth) {
		this.positionX = posX;
		this.positionZ = posZ;
		
		isEmpty = color == null;
		cubes = new Cube[width][height][depth];
		
		for(int x= 0; x<width; x++) {
			for(int z= 0; z<depth; z++) {
				for(int y= 0; y<height; y++) {
					if(color == null)
						cubes[x][y][z] = new Cube();
					else
						cubes[x][y][z] = new Cube(color);
				}
			}
		}
		
		this.initialize();
	}
	
	public Chunk(int posX, int posZ, Vertex4f color) {
		this(posX, posZ, color, CHUNK_WIDTH, CHUNK_HEIGHT, CHUNK_DEPTH);
	}
	
	public Chunk(int posX, int posZ, int width, int height, int depth) {
		this(posX, posZ, null, width, height, depth);
	}
	
	public Chunk(int posX, int posZ) {
		this(posX, posZ, null);
	}
	
	public Chunk(List<String> save) {
		this.positionX = Integer.parseInt(save.remove(0));
		this.positionZ = Integer.parseInt(save.remove(0));
		
		isEmpty = false;
		cubes = new Cube[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_DEPTH];
		
		for(int x= 0; x<CHUNK_WIDTH; x++) {
			for(int z= 0; z<CHUNK_DEPTH; z++) {
				for(int y= 0; y<CHUNK_HEIGHT; y++) {
					String[] sColors = save.remove(0).split(",");
					float[] c = new float[4];
					for(int i = 0; i<sColors.length; i++) {
						c[i] = Float.parseFloat(sColors[i]);
					}
					this.cubes[x][y][z] = c[0] < 0 ? new Cube() : new Cube(new Vertex4f(c[0], c[1], c[2], c[3]));
				}
			}
		}
		
		this.initialize();
	}
	
	public Chunk(int posX, int posZ, Vertex4f color, int[][] heightMap) {
		this.positionX = posX;
		this.positionZ = posZ;
		
		isEmpty = true;
		cubes = new Cube[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_DEPTH];
				
		for(int x= 0; x<CHUNK_WIDTH; x++) {
			for(int z= 0; z<CHUNK_DEPTH; z++) {
				for(int y= 0; y<CHUNK_HEIGHT; y++) {
					if(y <= heightMap[x][z]) {
						cubes[x][y][z] = (y == heightMap[x][z]) ? new Cube(new Vertex4f(0, 1, 0, 1)) : new Cube(color);
						this.isEmpty = false;
					}
					else {
						cubes[x][y][z] = new Cube();
					}
				}
			}
		}
		
		this.initialize();
	}
	
	private void initialize() {
		//Generate VAO
		vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //Generate VBO to store data
        vboVertices = glGenBuffers();
        vboNormals = glGenBuffers();
        vboColors = glGenBuffers();
        
        this.updateVAO();
	}
	
	public void selectCube(int x, int y, int z) {
		this.cubes[x][y][z].select();
		this.updateVAO();
	}
	
	public void setBlock(int x, int y, int z, Vertex4f color) {
		this.cubes[x][y][z] = new Cube(color);
		this.updateVAO();
	}
	
	public Cube getCube(int x, int y, int z) {
		return this.cubes[x][y][z];
	}
	
	public void updateVAO() {
		verticesBuffer.clear();
		normalsBuffer.clear();
		colorsBuffer.clear();
						
		IntBuffer showFaces = BufferUtils.createIntBuffer(6);
		
		for(int x= 0; x<CHUNK_WIDTH; x++) {
			for(int z= 0; z<CHUNK_DEPTH; z++) {
				for(int y= 0; y<CHUNK_HEIGHT; y++) {
					if(!cubes[x][y][z].isEmpty()) {
						//Check if faces should be rendered
						showFaces.clear();
						int faceIndex = 0;
						for(int[] n : neighbours) {
							try {
								if(cubes[x + n[0]][y + n[1]][z + n[2]].isEmpty()) {
									showFaces.put(faceIndex);
								}
							}
							catch(Exception e) {
								showFaces.put(faceIndex);
							}
							faceIndex++;
						}
						showFaces.flip();
						cubes[x][y][z].addRenderInfo(verticesBuffer, normalsBuffer, colorsBuffer, showFaces, new Vertex3f(x, y, z), World.DEBUG_BOUNDS);
					}
				}
			}
		}

		verticesBuffer.flip();
		normalsBuffer.flip();
		colorsBuffer.flip();
		
		verticesCount = verticesBuffer.limit();
				
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
		if(!this.isEmpty) {
			glBindVertexArray(vao);
			//glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
			if(!World.DEBUG_BOUNDS)
				glDrawArrays(GL_TRIANGLES, 0, verticesCount);
			else
				glDrawArrays(GL_LINES, 0, verticesCount);
		}
	}
	
	public void save(String worldName) {
		List<String> lines = new ArrayList<String>();
		lines.add("" + getX());
		lines.add("" + getZ());
		
		for(int x= 0; x<CHUNK_WIDTH; x++) {
			for(int z= 0; z<CHUNK_DEPTH; z++) {
				for(int y= 0; y<CHUNK_HEIGHT; y++) {
					Vertex4f color = this.cubes[x][y][z].getColor();
					lines.add(color.x + "," + color.y + "," + color.z + "," + color.w);
				}
			}
		}
		SaveManager.save(worldName + "\\" + this.getUid(), lines);
	}
	
	public int getX() {
		return this.positionX;
	}
	
	public int getZ() {
		return this.positionZ;
	}
	
	public String getUid() {
		return "CX" + positionX + "Z" + positionZ;
	}
	
	public static String getChunkUid(int x, int z) {
		return "CX" + x + "Z" + z;
	}
}
