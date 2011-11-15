package edu.washington.cs.games.ktuite.pointcraft;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import org.lwjgl.util.vector.Vector3f;

public class DataStore implements Serializable {

	private static final long serialVersionUID = 6554769726907063951L;
	class pellet {
		int pellet_id;
		Vector3f pos;
		float radius;
		Main.GunMode type;
	}
	
	class line {
		Vector3f pt_1;
		Vector3f pt_2;
	}
	
	class plane {
		Vector3f mean;
		float a,b,c,d;
	}
	
	class polygon {
		int polygon_id;
		int[] pellets;
		String texture_url;
	}
	
	class action {
		int action_id;
		int pellet_id;
		String action;
	}
	
	List<pellet> pellets;
	List<line> lines;
	List<plane> planes;
	List<polygon> polygons;
	List<action> actions;
	
	public DataStore(){
		pellets = new Stack<pellet>();
		lines = new Stack<line>();
		planes = new Stack<plane>();
		polygons = new Stack<polygon>();
		actions = new Stack<action>();
	}
	
	public void showData(){
		System.out.println("Here's what's currently in the data store:");
		
		System.out.println("Pellets:\n---------");
		for (pellet p : pellets)
			System.out.println("\t" + p);
		
		System.out.println("Lines:\n---------");
		for (line p : lines)
			System.out.println("\t" + p);
		
		System.out.println("Planes:\n---------");
		for (plane p : planes)
			System.out.println("\t" + p);
		
		System.out.println("Polygons:\n---------");
		for (polygon p : polygons)
			System.out.println("\t" + p);
		
		System.out.println("Actions:\n---------");
		for (action p : actions)
			System.out.println("\t" + p);
	}
}
