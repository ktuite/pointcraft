package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;
import java.util.Stack;

import org.lwjgl.util.vector.Vector3f;

/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Scaffold implements org.json.JSONString{
	public Stack<Pellet> pellets;
	public List<Pellet> intersection_pellets; 
	
	public Scaffold(){
		pellets = new Stack<Pellet>();
	}
	
	public float distanceToPoint(Vector3f pos) {
		return Float.MAX_VALUE;
	}
	
	public Vector3f closestPoint(Vector3f pos) {
		return null;
	}
	
	public void draw() {
		// to be overwritten
	}
	
	public void addNewPellet(Pellet p){
		
	}
	
	public void add(Pellet p) {
		pellets.add(p);
	}

	@Override
	public String toJSONString() {
		// line and plane scaffolding methods should get called here instead
		return null;
	}
}
