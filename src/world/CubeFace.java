package world;

import pinzen.utils.mathsfog.Vertex3f;

public enum CubeFace {
	NONE(-1, new Vertex3f()),
	TOP(0, new Vertex3f(0f, 1f, 0f)),
	BOTTOM(1, new Vertex3f(0f, -1f, 0f)),
	LEFT(2, new Vertex3f(-1f, 0f, 0f)),
	RIGHT(3, new Vertex3f(1f, 0f, 0f)),
	BACK(4, new Vertex3f(0f, 0f, 1f)),
	FRONT(5, new Vertex3f(0f, 0f, -1f));
	
	private int id;
	private Vertex3f direction;
	
	CubeFace(int index, Vertex3f dir) {
		this.id = index;
		this.direction = dir;
	}
	
	public static CubeFace getFaceFromDirection(Vertex3f dir) {
		for(CubeFace c : CubeFace.values()) {
			if(Vertex3f.difference(dir, c.direction).norm() == 0) {
				return c;
			}
		}
		return NONE;
	}
	
	public static CubeFace getFaceFromIndex(int index) {
		for(CubeFace c : CubeFace.values()) {
			if(c.id == index) {
				return c;
			}
		}
		return NONE;
	}
	
	public Vertex3f getDirection() {
		return this.direction.clone();
	}
}
