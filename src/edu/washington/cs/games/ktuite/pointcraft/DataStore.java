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
		
		public pellet(Pellet p){
			pellet_id = p.id;
			pos = p.pos;
			radius = p.radius;
			if (p instanceof ScaffoldPellet)
				type = Main.GunMode.PELLET;
			else if (p instanceof LinePellet)
				type = Main.GunMode.LINE;
			else if (p instanceof PlanePellet)
				type = Main.GunMode.PLANE;
			else if (p instanceof PolygonPellet)
				type = Main.GunMode.POLYGON;
		}
		
		@Override
		public String toString(){
			String s = "Pellet ID: " + pellet_id;
			s += " (" + type + ") ";
			s += pos; 
			return s;
		}
	}
	
	class line {
		Vector3f pt_1;
		Vector3f pt_2;
		
		public line(PrimitiveVertex v){
			pt_1 = v.pt_1;
			pt_2 = v.pt_2;
		}
	}
	
	class plane {
		Vector3f mean;
		float a,b,c,d;
		
		public plane(PrimitiveVertex v){
			a = v.a;
			b = v.b;
			c = v.c;
			d = v.d;
		}
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
	
	public void putThingsInDataStoreFromMain(){
		for (Pellet p : Main.all_pellets_in_world){
			pellets.add(new pellet(p));
		}
		
		for (PrimitiveVertex v : Main.geometry_v){
			if (v.isLine())
				lines.add(new line(v));
		}
		
		//for (Primitive p : Main.geometry){
			// do nothing for now
		//}
		
		showData();
	}
}
