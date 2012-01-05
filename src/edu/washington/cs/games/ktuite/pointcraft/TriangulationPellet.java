package edu.washington.cs.games.ktuite.pointcraft;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import megamu.mesh.Delaunay;

import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

public class TriangulationPellet extends Pellet {

	public class TriVert {
		TriangulationPellet pellet;
		List<TriEdge> adjacent_edges;

		public TriVert(TriangulationPellet p) {
			pellet = p;
			adjacent_edges = new LinkedList<TriEdge>();
		}
	}

	public class TriEdge {
		Primitive graphic_line;
		TriVert start;
		TriVert end;
		boolean fringe;

		public TriEdge(TriVert v1, TriVert v2) {
			LinkedList<Pellet> pellets = new LinkedList<Pellet>();
			pellets.add(v1.pellet);
			pellets.add(v2.pellet);
			graphic_line = new Primitive(GL_LINES, pellets, 3);
			
			fringe = true;
			
			start = v1;
			end = v2;
		}
	}

	public static List<TriVert> current_vertices = new LinkedList<TriVert>();
	public static List<TriEdge> current_edges = new LinkedList<TriEdge>();
	public static List<Primitive> edges_to_display = new LinkedList<Primitive>();

	public static Stack<TriangulationPellet> current_cycle = new Stack<TriangulationPellet>();
	private boolean first_in_cycle = false;
	private static PlaneScaffold plane = new PlaneScaffold();

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public TriangulationPellet(List<Pellet> _pellets) {
		super(_pellets);
		pellet_type = Main.GunMode.TRIANGULATION;
	}

	public TriangulationPellet(Pellet lp) {
		super(lp.main_pellets);
		pos.set(lp.pos);
		radius = lp.radius;
		max_radius = lp.max_radius;
		constructing = lp.constructing;
		pellet_type = Main.GunMode.TRIANGULATION;
	}

	@Override
	public void update() {
		// constructing means the pellet has triggered something to be built at
		// its sticking location
		if (!constructing) {
			// not constructing means the pellet is still traveling through
			// space

			// move the pellet
			Vector3f.add(pos, vel, pos);

			// if it's too old, kill it
			if (Main.timer.getTime() - birthday > 5) {
				alive = false;
			} else {
				// if the pellet is not dead yet, see if it intersected anything

				// did it hit another pellet?
				Pellet neighbor_pellet = queryOtherPellets();

				// did it hit a line or plane?
				Vector3f closest_point = queryScaffoldGeometry();

				if (neighbor_pellet != null) {
					alive = false;
					// TODO: implement this
				} else if (closest_point != null) {
					// TODO: implement this
				} else if (Main.draw_points) {
					// if it's not dead yet and also didn't hit a
					// neighboring pellet, look for nearby points in model
					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						constructing = true;
						setInPlace();
						snapToCenterOfPoints();

						addNewPelletToTriMesh();
					}
				}
			}
		} else {
			// the pellet has stuck... here we just give it a nice growing
			// bubble animation
			if (radius < max_radius) {
				radius *= 1.1;
			}
		}
	}

	private void addNewPelletToTriMesh() {
		// add new tri very to list of vertices
		TriVert vert = new TriVert(this);
		current_vertices.add(vert);
		Main.new_pellets_to_add_to_world.add(vert.pellet);
		
		// do delaunay triangulation (projecting points into viewing plane)
		float[][] points = new float[current_vertices.size()][2];
		for (int i = 0; i < current_vertices.size(); i++){
			// use other x's and y's with other type of plane
			points[i][0] = current_vertices.get(i).pellet.pos.x;
			points[i][1] = current_vertices.get(i).pellet.pos.y;
		}
		
		Delaunay myDelaunay = new Delaunay(points);
		
		// form edges
		current_edges.clear();
		edges_to_display.clear();
		int[][] myLinks = myDelaunay.getLinks();

		for(int i=0; i<myLinks.length; i++)
		{
			int startIndex = myLinks[i][0];
			int endIndex = myLinks[i][1];

			TriEdge edge = new TriEdge(current_vertices.get(startIndex), current_vertices.get(endIndex));
			edges_to_display.add(edge.graphic_line);
		}
	}
	
	@SuppressWarnings("unused")
	private void connectToAllOtherVertices() {
		TriVert vert = new TriVert(this);
		for (TriVert other_vert : current_vertices){
			TriEdge edge = new TriEdge(vert, other_vert);
			// TODO: check that the edge is acceptable to add 
			current_edges.add(edge);
			Main.geometry.add(edge.graphic_line);
		}
		current_vertices.add(vert);
		Main.new_pellets_to_add_to_world.add(vert.pellet);	
	}

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.1f, .7f, .4f, alpha);
			if (first_in_cycle)
				glColor4f(0f, .5f, .3f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.1f, .7f, .4f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}

	public static void startNewTriMesh() {
		current_cycle.clear();
		current_edges.clear();
		current_vertices.clear();
		System.out.println("making new tri mesh");
	}

	public void delete() {
		// TODO: fill this in
	}
}
