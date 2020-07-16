package entities;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
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

import org.lwjgl.BufferUtils;

import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.glow.inputs.InputManager;
import pinzen.utils.glow.inputs.Key;
import pinzen.utils.mathsfog.Matrix4f;
import pinzen.utils.mathsfog.Vertex2f;
import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;
import world.Cube;
import world.World;

public class Player extends Entity{

	private int vao;
	
	public Player() {
		super(new Vertex3f(0.8f, 1.8f, 0.8f));
		
		initVAO();
	}
	
	public void updateInputs(InputManager inputs, long delta) {
		Vertex3f rotation = this.getRotation();
		Vertex3f force = new Vertex3f();
		
		float speed = 0.03f;
		
		if(inputs.isLeftDown()) {
			force.x -= Math.cos(rotation.y * Math.PI / 180) * speed;
			force.z -= Math.sin(rotation.y * Math.PI / 180) * speed;
		}
		if(inputs.isRightDown()) {
			force.x += Math.cos(rotation.y * Math.PI / 180) * speed;
			force.z += Math.sin(rotation.y * Math.PI / 180) * speed;
		}
		if(inputs.isUpDown()) {
			force.x -= Math.sin(rotation.y * Math.PI / 180) * speed;
			force.z += Math.cos(rotation.y * Math.PI / 180) * speed;
		}
		if(inputs.isDownDown()) {
			force.x += Math.sin(rotation.y * Math.PI / 180) * speed;
			force.z -= Math.cos(rotation.y * Math.PI / 180) * speed;
		}
		
		if(inputs.isKeyDown(Key.SPACE)) {
			if(this.isGravityAffected())
				this.jump();
			else
				force.y += speed;
		}
		
		if(inputs.isKeyDown(Key.LSHIFT)) {
			if(!this.isGravityAffected())
				force.y -= speed;
		}
				
		this.addForce(force);
	}
	
	private void initVAO() {
		vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //Generate VBO to store data
        int vboVertices = glGenBuffers();
        int vboNormals = glGenBuffers();
        int vboColors = glGenBuffers();
        
        Cube cube = new Cube(new Vertex4f(0, 1, 1, 1));
        
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(3 * 48);
        FloatBuffer normalsBuffer  = BufferUtils.createFloatBuffer(3 * 48);
        FloatBuffer colorsBuffer   = BufferUtils.createFloatBuffer(4 * 48);
        IntBuffer showFaces = BufferUtils.createIntBuffer(6);
        showFaces.put(new int[] {0,1,2,3,4,5});
        showFaces.flip();
        
        cube.addRenderInfo(verticesBuffer, normalsBuffer, colorsBuffer, showFaces, new Vertex3f(), true);
		
        verticesBuffer.flip();
		normalsBuffer.flip();
		colorsBuffer.flip();
        
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
	
	@Override
	public void render(ShaderProgram s) {
		glBindVertexArray(vao);
		Matrix4f trans = Matrix4f.getTranslationMatrix(getPosition());
		Matrix4f scale = Matrix4f.getScalingMatrix(this.getBounds());
		s.setUniformMatrix4f("model", Matrix4f.mult(trans, scale));
		glDrawArrays(GL_LINES, 0, 48);
	}

}
