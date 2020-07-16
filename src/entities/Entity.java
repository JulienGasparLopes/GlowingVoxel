package entities;

import pinzen.utils.glow.ShaderProgram;
import pinzen.utils.mathsfog.Vertex3f;
import world.Cube;
import world.World;

public abstract class Entity {

	private Vertex3f position, rotation, force;
	private boolean isOnGround;
	
	private Vertex3f bounds;
	
	private boolean isGravityAffected;
	
	public Entity(Vertex3f bounds) {
		this.position = new Vertex3f();
		this.rotation = new Vertex3f();
		this.force = new Vertex3f();
		this.isGravityAffected = true;
		
		this.isOnGround = false;
		this.bounds = bounds.clone();
	}
	
	private boolean intersectCube(World world, float x, float y, float z) {
		float ceilX = (float)Math.ceil(bounds.x);
		float ceilY = (float)Math.ceil(bounds.y);
		float ceilZ = (float)Math.ceil(bounds.z);
		
		Cube[] cubes = new Cube[(int)(ceilX+1) * (int)(ceilY+1) * (int)(ceilZ+1)];
		int id = 0;
		
		for(int idx = 0; idx <= ceilX; idx++){
			for(int idy = 0; idy <= ceilY; idy++){
				for(int idz = 0; idz <= ceilZ; idz++){
					cubes[id] = world.getCube(
							x + (idx/ceilX)*bounds.x,
							y + (idy/ceilY)*bounds.y,
							z + (idz/ceilZ)*bounds.z);
					id++;
				}
			}
		}
		
		boolean doNotIntersects = true;
		for(Cube cube : cubes) {
			doNotIntersects = doNotIntersects && (cube == null || cube.isEmpty());
		}
		return !doNotIntersects;
	}
	
	public void update(World world, long delta) {
		//Apply gravity
		if(this.isGravityAffected)
			this.force.y += World.GRAVITY;
		
		
		Vertex3f nextPos = Vertex3f.translate(position, force);
		
		if(world != null) {
			if(!this.intersectCube(world, nextPos.x, nextPos.y, nextPos.z)) {
				this.isOnGround = false;
			}
			else {
				if(this.intersectCube(world, position.x, nextPos.y, position.z)) {				
					if(this.force.y < 0)
						this.isOnGround = true;
					
					this.force.y = 0;
				}
				else {
					this.isOnGround = false;
				}
				/*
				if(!this.intersectCube(world, position.x, nextPos.y, position.z)) {
					this.isOnGround = false;
				}
				else {
					this.force.y = 0;
					this.position.y = (int)nextPos.y+1;
					this.isOnGround = true;
				}
				*/
				
				if(this.intersectCube(world, nextPos.x, position.y, position.z)) {
					this.force.x = 0;
				}
				
				if(this.intersectCube(world, position.x, position.y, nextPos.z)) {
					this.force.z = 0;
				}
			}
		}
		
		this.position = Vertex3f.translate(position, force);
		
		this.force.x *= 0.8f;
		this.force.z *= 0.8f;
		
		if(!this.isGravityAffected)
			this.force.y *= 0.8f;
	}
	
	public abstract void render(ShaderProgram s);

	public void jump() {
		if(this.isOnGround)
			this.addForce(new Vertex3f(0, 0.2f, 0));
	}
	
	public void addForce(Vertex3f f) {
		this.force = Vertex3f.translate(force, f);
	}
	
	public Vertex3f getLookDirection() {
		double angleToRad = Math.PI / 180f;
		double lookX = -Math.cos(rotation.x * angleToRad) * Math.sin(rotation.y * angleToRad);
		double lookY = -Math.sin(rotation.x * angleToRad);
		double lookZ = Math.cos(rotation.x * angleToRad) * Math.cos(rotation.y * angleToRad);
		
		return Vertex3f.normalize(new Vertex3f((float)lookX, (float)lookY, (float)lookZ));
	}
	
	public void translate(Vertex3f trans) {
		this.position = Vertex3f.translate(position, trans);
	}
	
	public void rotate(Vertex3f rot) {
		this.rotation = Vertex3f.translate(rotation, rot);
	}
	
	public void setPosition(Vertex3f pos) {
		this.position = pos.clone();
	}
	
	public void setGravityEffect(boolean shouldBeAffected) {
		this.isGravityAffected = shouldBeAffected;
	}
	
	public boolean isGravityAffected() {
		return this.isGravityAffected;
	}
	
	public void setRotation(Vertex3f rot) {
		this.rotation = rot;
	}
	
	public Vertex3f getPosition() {
		return this.position.clone();
	}
	
	public Vertex3f getRotation() {
		return this.rotation.clone();
	}
	
	public boolean isOnGround() {
		return this.isOnGround;
	}
	
	public Vertex3f getBounds() {
		return this.bounds;
	}
}
