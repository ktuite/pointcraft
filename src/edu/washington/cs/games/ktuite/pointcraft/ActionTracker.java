package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONStringer;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.geometry.LineScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.PlaneScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
import edu.washington.cs.games.ktuite.pointcraft.tools.HoverPellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.LinePellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.PlanePellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.PolygonPellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.TriangulationPellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.VerticalLinePellet;

public class ActionTracker {

	// The different types of actions
	public enum ActionType {
		NEW_PELLET, PARTIAL_POLYGON, POLYGON_LINE, COMPLETED_POLYGON, PARTIAL_LINE, COMPLETED_LINE, PARTIAL_PLANE, COMPLETED_PLANE, 
		VERTICAL_HEIGHT_SET, NEW_VERTICAL_LINE_PELLET, NEW_VERTICAL_WALL, PELLET_DELETED, SCAFFOLDING_DELETED, POLYGON_DELETED, 
		EXTENDED_LINE, EXTENDED_PLANE, LINE_PLANE_INTERSECTION, COMBINED_PELLETS, MOVED_PELLET, PELLET_HIDDEN, 
		PARTIAL_TRIANGLE_MESH, COMPLETED_TRIANGLE_MESH;
	}

	// A little holder-class for the actions
	// has a type and pointers to the objects it uses
	public static class Action {
		public ActionType action_type;
		public Pellet pellet;
		public Pellet pellet2;
		public Primitive primitive;
		public Stack<Pellet> current_poly;
		public Scaffold scaffold;
		public Vector3f old_pos;
		public List<Primitive> current_edges;

		public String toString() {
			JSONStringer s = new JSONStringer();
			try {
				s.object();
				s.key("action_type");
				s.value(action_type);
				if (pellet != null) {
					s.key("pellet");
					s.value(pellet);
				}
				if (pellet2 != null) {
					s.key("pellet2");
					s.value(pellet2);
				}
				if (primitive != null) {
					s.key("primitive");
					s.value(primitive);
				}
				if (scaffold != null) {
					s.key("scaffold");
					s.value(scaffold);
				}
				if (old_pos != null) {
					s.key("old_pos");
					s.value(Pellet.JSONVector3f(old_pos));
				}

				s.key("json_version");
				s.value("1");

				s.endObject();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return s.toString();
		}

		private void notifyServer() {
			Main.server.actionUpdate(this);
		}

		public Action(ActionType at) {
			action_type = at;
			notifyServer();
		}

		public Action(ActionType at, Pellet p) {
			pellet = p;
			action_type = at;
			notifyServer();
		}

		public Action(ActionType at, Pellet p, Pellet p2) {
			pellet = p;
			pellet2 = p2;
			action_type = at;
			notifyServer();
		}

		public Action(ActionType at, Primitive p) {
			primitive = p;
			action_type = at;
			notifyServer();
		}

		public Action(ActionType at, Primitive p, Stack<Pellet> curr) {
			primitive = p;
			action_type = at;
			current_poly = curr;
			notifyServer();
		}

		public Action(ActionType at, Scaffold s) {
			scaffold = s;
			action_type = at;
			notifyServer();
		}

		public Action(ActionType at, Pellet p, Vector3f pos) {
			pellet = p;
			old_pos = pos;
			action_type = at;
			notifyServer();
		}

		public Action(ActionType at, Primitive p, Stack<Pellet> curr,
				List<Primitive> list) {
			primitive = p;
			action_type = at;
			current_poly = curr;
			current_edges = list;
			notifyServer();
		}
		
		public Action(ActionType at, Stack<Pellet> curr,
				List<Primitive> list) {
			action_type = at;
			current_poly = curr;
			current_edges = list;
			notifyServer();
		}
	}

	private static Stack<Action> undo_stack = new Stack<Action>();
	private static Stack<Action> redo_stack = new Stack<Action>();

	public static String showLatestAction() {
		if (undo_stack.size() == 0)
			return "No actions yet";
		else
			return undo_stack.peek().action_type.toString().replace("_", " ");
	}

	public static void printStack() {
		System.out.println("Undo stack: ");
		for (Action a : undo_stack) {
			System.out.println("\t" + a.action_type.toString());
		}
	}

	public static void undo() {
		if (undo_stack.size() == 0) {
			System.out.println("Nothing to undo");
			return;
		}

		Main.server.undoUpdate();

		System.out.println("Undoing last action: " + showLatestAction());

		Action last_action = undo_stack.pop();
		redo_stack.push(last_action);

		// here we deal with each possible type of action!!
		if (last_action.action_type == ActionType.NEW_PELLET) {
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		} else if (last_action.action_type == ActionType.PARTIAL_POLYGON) {
			if (last_action.pellet.refCountZero())
				Main.all_dead_pellets_in_world.add(last_action.pellet);
			else
				last_action.pellet.ref_count--;
			PolygonPellet.current_cycle.pop();
			if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.POLYGON_LINE) {
				undo();
			}
		} else if (last_action.action_type == ActionType.POLYGON_LINE) {
			if (last_action.primitive != null) {
				PolygonPellet.edges_to_display.remove(last_action.primitive);
			}
		} else if (last_action.action_type == ActionType.COMPLETED_POLYGON) {
			if (last_action.primitive != null) {
				Main.geometry.remove(last_action.primitive);
			}
			if (last_action.current_poly != null) {
				PolygonPellet.current_cycle = last_action.current_poly;
			}
			if (last_action.current_edges != null) {
				PolygonPellet.edges_to_display = (Stack<Primitive>) last_action.current_edges;
			}
			if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.POLYGON_LINE) {
				undo();
			}
			else if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.COMPLETED_TRIANGLE_MESH) {
				undo();
			}
		} else if (last_action.action_type == ActionType.PARTIAL_LINE) {
			Main.all_dead_pellets_in_world.add(last_action.pellet);
			if (LinePellet.current_line.pellets.size() > 0)
				LinePellet.current_line.pellets.pop();
			else if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.PARTIAL_LINE) {
				undo();
			}
		} else if (last_action.action_type == ActionType.COMPLETED_LINE) {
			if (last_action.scaffold != null) {
				((LineScaffold) last_action.scaffold).nullifyLine();
			}
			if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.PARTIAL_LINE) {
				undo();
			}
		} else if (last_action.action_type == ActionType.EXTENDED_LINE) {
			if (last_action.scaffold != null) {
				((LineScaffold) last_action.scaffold).removeLastPointAndRefit();
			}
		} else if (last_action.action_type == ActionType.PARTIAL_PLANE) {
			Main.all_dead_pellets_in_world.add(last_action.pellet);
			if (PlanePellet.current_plane.pellets.size() > 0)
				PlanePellet.current_plane.pellets.pop();
			else if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.PARTIAL_PLANE) {
				undo();
				if (undo_stack.size() > 0
						&& undo_stack.peek().action_type == ActionType.PARTIAL_PLANE) {
					undo();
				}
			}
		} else if (last_action.action_type == ActionType.COMPLETED_PLANE) {
			if (last_action.scaffold != null) {
				((PlaneScaffold) last_action.scaffold).nullifyPlane();
			}
			if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.PARTIAL_PLANE) {
				undo();
			}
		} else if (last_action.action_type == ActionType.EXTENDED_PLANE) {
			if (last_action.scaffold != null) {
				((PlaneScaffold) last_action.scaffold)
						.removeLastPointAndRefit();
			}
		} else if (last_action.action_type == ActionType.LINE_PLANE_INTERSECTION) {
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		} else if (last_action.action_type == ActionType.NEW_VERTICAL_LINE_PELLET) {
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		} else if (last_action.action_type == ActionType.VERTICAL_HEIGHT_SET) {
			VerticalLinePellet.clearAllVerticalLines();
			if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.NEW_VERTICAL_LINE_PELLET) {
				undo();
				undo();
			}
		} else if (last_action.action_type == ActionType.NEW_VERTICAL_WALL) {
			Main.all_dead_pellets_in_world.add(VerticalLinePellet.top_pellet);
			Main.all_dead_pellets_in_world
					.add(VerticalLinePellet.bottom_pellet);
			VerticalLinePellet.top_pellet = (VerticalLinePellet) last_action.pellet;
			VerticalLinePellet.bottom_pellet = (VerticalLinePellet) last_action.pellet2;
			if (undo_stack.size() > 0
					&& undo_stack.peek().action_type == ActionType.COMPLETED_POLYGON) {
				undo();
			}
		} else if (last_action.action_type == ActionType.PELLET_DELETED) {
			if (last_action.pellet != null) {
				last_action.pellet.alive = true;
				Main.new_pellets_to_add_to_world.add(last_action.pellet);
			}
		} else if (last_action.action_type == ActionType.SCAFFOLDING_DELETED) {
			if (last_action.scaffold != null) {
				Main.geometry_v.add(last_action.scaffold);
			}
		} else if (last_action.action_type == ActionType.POLYGON_DELETED) {
			if (last_action.primitive != null) {
				Primitive.addBackDeletedPrimitive(last_action.primitive);
			}
		} else if (last_action.action_type == ActionType.COMBINED_PELLETS) {
			if (last_action.pellet != null && last_action.old_pos != null) {
				last_action.pellet.visible = true;
				last_action.pellet.pos.set(last_action.old_pos);
				HoverPellet.fixPolygonOfPellet(last_action.pellet);
			}
		} else if (last_action.action_type == ActionType.MOVED_PELLET) {
			if (last_action.pellet != null && last_action.old_pos != null) {
				last_action.pellet.visible = true;
				last_action.pellet.pos.set(last_action.old_pos);
				HoverPellet.fixPolygonOfPellet(last_action.pellet);
			}
		} else if (last_action.action_type == ActionType.PELLET_HIDDEN) {
			if (last_action.pellet != null)
				last_action.pellet.visible = true;

		} else if (last_action.action_type == ActionType.PARTIAL_TRIANGLE_MESH) {
			if (last_action.pellet.refCountZero())
				Main.all_dead_pellets_in_world.add(last_action.pellet);
			else
				last_action.pellet.ref_count--;
			if (TriangulationPellet.current_vertices.size() > 0){
				TriangulationPellet.current_vertices.pop();
				TriangulationPellet.computeTriangulation();
			}
		}
		else if (last_action.action_type == ActionType.COMPLETED_TRIANGLE_MESH) {
			if (last_action.current_poly != null) {
				TriangulationPellet.current_vertices = last_action.current_poly;
			}
			if (last_action.current_edges != null) {
				TriangulationPellet.edges_to_display = (Stack<Primitive>) last_action.current_edges;
			}
		}

		// always unhide pellets
		if (undo_stack.size() > 0
				&& undo_stack.peek().action_type == ActionType.PELLET_HIDDEN) {
			undo();
		}
	}

	// Here are the new actions to be tracked and undone!
	public static void newScaffoldPellet(Pellet p) {
		undo_stack.add(new Action(ActionType.NEW_PELLET, p));
	}

	public static void newPolygonPellet(Pellet p) {
		undo_stack.add(new Action(ActionType.PARTIAL_POLYGON, p));
	}

	public static void newPolygonLine(Primitive p) {
		undo_stack.add(new Action(ActionType.POLYGON_LINE, p));
	}

	public static void newPolygon(Primitive p, Stack<Pellet> curr,
			List<Primitive> list) {
		if (curr != null && curr.size() > 0)
			curr.pop();
		undo_stack.add(new Action(ActionType.COMPLETED_POLYGON, p, curr, list));
	}

	public static void newLinePellet(Pellet p) {
		undo_stack.add(new Action(ActionType.PARTIAL_LINE, p));
	}

	public static void newLine(Scaffold s) {
		undo_stack.add(new Action(ActionType.COMPLETED_LINE, s));
	}

	public static void extendedLine(Scaffold s) {
		undo_stack.add(new Action(ActionType.EXTENDED_LINE, s));
	}

	public static void newPlanePellet(Pellet p) {
		undo_stack.add(new Action(ActionType.PARTIAL_PLANE, p));
	}

	public static void newPlane(Scaffold s) {
		undo_stack.add(new Action(ActionType.COMPLETED_PLANE, s));
	}

	public static void extendedPlane(Scaffold s) {
		undo_stack.add(new Action(ActionType.EXTENDED_PLANE, s));
	}

	public static void newLinePlaneIntersection(Pellet p) {
		undo_stack.add(new Action(ActionType.LINE_PLANE_INTERSECTION, p));
	}

	public static void newVerticalLinePellet(Pellet p) {
		undo_stack.add(new Action(ActionType.NEW_VERTICAL_LINE_PELLET, p));
	}

	public static void newVerticalHeightSet() {
		undo_stack.add(new Action(ActionType.VERTICAL_HEIGHT_SET));
	}

	public static void newVerticalWall(Pellet p1, Pellet p2) {
		undo_stack.add(new Action(ActionType.NEW_VERTICAL_WALL, p1, p2));
	}

	public static void deletedPellet(Pellet p) {
		undo_stack.add(new Action(ActionType.PELLET_DELETED, p));
	}

	public static void deletedScaffolding(Scaffold s) {
		undo_stack.add(new Action(ActionType.SCAFFOLDING_DELETED, s));
	}

	public static void deletedPrimitive(Primitive p) {
		undo_stack.add(new Action(ActionType.POLYGON_DELETED, p));
	}

	public static void combinedPellet(Pellet p, Vector3f old_pos) {
		undo_stack.add(new Action(ActionType.COMBINED_PELLETS, p, old_pos));
	}

	public static void movedPellet(Pellet p, Vector3f old_pos) {
		undo_stack.add(new Action(ActionType.MOVED_PELLET, p, old_pos));
	}

	public static void hiddenPellet(Pellet p) {
		undo_stack.add(new Action(ActionType.PELLET_HIDDEN, p));
	}

	public static void newTriangleMeshPellet(Pellet p) {
		undo_stack.add(new Action(ActionType.PARTIAL_TRIANGLE_MESH, p));
	}
	
	public static void newTriangulation(Stack<Pellet> pellets, Stack<Primitive> edges) {
		undo_stack.add(new Action(ActionType.COMPLETED_TRIANGLE_MESH, pellets, edges));

	}
}
