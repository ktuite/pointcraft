package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
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
	
	public boolean isReady(){
		return false;
	}

	@Override
	public String toJSONString() {
		// line and plane scaffolding methods should get called here instead
		return null;
	}

	public static void loadFromJSONv2(JSONObject obj) throws JSONException {
		String scaffold_type = obj.getString("scaffold_type");
		if (scaffold_type.contains("line")){
			LineScaffold.loadFromJSONv2(obj);
		}
		else if (scaffold_type.contains("plane")){
			PlaneScaffold.loadFromJSONv2(obj);
		}
	}
	
	public static void loadFromJSONv3(JSONObject obj) throws JSONException {
		String scaffold_type = obj.getString("scaffold_type");
		if (scaffold_type.contains("line")){
			LineScaffold.loadFromJSONv3(obj);
		}
		else if (scaffold_type.contains("plane")){
			PlaneScaffold.loadFromJSONv3(obj);
		}
	}
}
