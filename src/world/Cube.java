package world;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import pinzen.utils.mathsfog.Vertex3f;
import pinzen.utils.mathsfog.Vertex4f;

public class Cube {
	
	public static final int FACE_TOP = 0, FACE_BOTTOM = 1, FACE_LEFT = 2, FACE_RIGHT = 3, FACE_BACK = 4, FACE_FRONT = 5;
	
	private static final float s = 1f;
	private static final int[] boundsVertexIndices = new int[] {0, 1, 1, 2, 2, 5, 5, 0};
	private static final float[][] faceVerticesData = new float[][] {
		//FACE_TOP
		new float[] { 0,  s,  0,
					  0,  s,  s,
					  s,  s,  s,
					  0,  s,  0,
					  s,  s,  s,
					  s,  s,  0,},
		//FACE_BOTTOM
		new float[] { 0,  0,  s,
					  0,  0,  0,
					  s,  0,  0,
					  0,  0,  s,
					  s,  0,  0,
					  s,  0,  s,},
		//FACE_LEFT
		new float[] { 0,  0,  s,
					  0,  s,  s,
					  0,  s,  0,
					  0,  0,  s,
					  0,  s,  0,
					  0,  0,  0,
		},
		//FACE_RIGHT
		new float[] { s,  0,  0,
					  s,  s,  0,
					  s,  s,  s,
					  s,  0,  0,
					  s,  s,  s,
					  s,  0,  s,
		},
		//FACE_BACK
		new float[] { s,  0,  s,
					  s,  s,  s,
					  0,  s,  s,
					  s,  0,  s,
					  0,  s,  s,
					  0,  0,  s,
		},
		//FACE_FRONT
		new float[] { 0,  0,  0,
					  0,  s,  0,
					  s,  s,  0,
					  0,  0,  0,
					  s,  s,  0,
					  s,  0,  0,
		}
	};
	private static final float[][] faceNormalData = new float[][] {
		//FACE_TOP
		new float[] { 0,  1,  0,},
		//FACE_BOTTOM
		new float[] { 0, -1,  0,},
		//FACE_LEFT
		new float[] {-1,  0,  0,},
		//FACE_RIGHT
		new float[] { 1,  0,  0,},
		//FACE_BACK
		new float[] { 0,  0, -1,},
		//FACE_FRONT
		new float[] { 0,  0,  1,},
	};
	
	private boolean isEmpty, isSelected;
	private Vertex4f color;
	
	public Cube() {
		this.isEmpty = true;
		this.isSelected = false;
	}
	
	public Cube(Vertex4f color) {
		this();
		this.isEmpty = false;
		this.color = color.clone();
	}
	
	public void addRenderInfo(FloatBuffer vertices, FloatBuffer normals, FloatBuffer colors, IntBuffer facesToRender, Vertex3f pos, boolean showBounds) {
		while(facesToRender.hasRemaining()) {
			int faceId = facesToRender.get();
			
			float[] dataVertices = faceVerticesData[faceId];
			float[] dataNormal = faceNormalData[faceId];
			
			if(!showBounds) {
				for(int i = 0; i<dataVertices.length; i+=3) {
					for(int j = 0; j<3; j++) {
						vertices.put(dataVertices[i+j] + pos.get(1+j));
						normals.put(dataNormal[j]);
						if(isSelected)
							colors.put(this.color.get(1+j)*0.75f);
						else
							colors.put(this.color.get(1+j));
					}
					//Alpha
					colors.put(this.color.w);
				}
			}
			else {
				for(int index : boundsVertexIndices) {
					for(int j = 0; j<3; j++) {
						vertices.put(dataVertices[index*3+j] + pos.get(1+j));
						normals.put(dataNormal[j]);
						colors.put(this.color.get(1+j));
					}
					//Alpha
					colors.put(this.color.w);
				}
			}
		}
	}
	
	public boolean isEmpty() {
		return this.isEmpty;
	}
	
	public void remove() {
		this.isEmpty = true;
		this.isSelected = false;
	}
	
	public void setColor(Vertex4f color) {
		this.color = color;
		this.isEmpty = false;
	}
	
	public Vertex4f getColor() {
		return this.isEmpty ? new Vertex4f(-1f, -1f, -1f, -1f) : this.color.clone();
	}
	
	public void select() {
		this.isSelected = true;
	}
	
	public void deselect() {
		this.isSelected = false;
	}
}