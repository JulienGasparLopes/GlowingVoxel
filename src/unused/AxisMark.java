package unused;

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

public class AxisMark {

	private int vao;
	
	public AxisMark() {
		vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //Generate VBO to store data
        int vboVertices = glGenBuffers();
        int vboNormals = glGenBuffers();
        int vboColors = glGenBuffers();
        
        float s = 0.04f;
        
        //layout = 0 for position of vertices
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
        		0, 0, 0,
        		s, 0, 0,
        		0, 0, 0,
        		0, s, 0,
        		0, 0, 0, 
        		0, 0, s }, GL_STATIC_DRAW);

        //layout = 1 for normals
        glBindBuffer(GL_ARRAY_BUFFER, vboNormals);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
        		0, 1, 0,
        		0, 1, 0,
        		0, 1, 0,
        		0, 1, 0,
        		0, 1, 0,
        		0, 1, 0 }, GL_STATIC_DRAW);
        
        
        //layout = 2 for colors
        glBindBuffer(GL_ARRAY_BUFFER, vboColors);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
        glBufferData(GL_ARRAY_BUFFER, new float[] {
        		1, 0, 0, 1,
        		1, 0, 0, 1,
        		0, 1, 0, 1,
        		0, 1, 0, 1, 
        		0, 0, 1, 1, 
        		0, 0, 1, 1 }, GL_STATIC_DRAW);
        

        //Free the current buffer
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void render() {
		glBindVertexArray(vao);
		glDrawArrays(GL_LINES, 0, 6);
	}
}
