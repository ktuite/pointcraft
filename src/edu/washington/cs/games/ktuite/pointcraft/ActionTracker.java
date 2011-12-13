package edu.washington.cs.games.ktuite.pointcraft;

import java.util.Stack;

public class ActionTracker {
	
	// The different types of actions
	public enum ActionType {
		NEW_PELLET, PARTIAL_POLYGON, POLYGON_LINE, COMPLETED_POLYGON, PARTIAL_LINE, COMPLETED_LINE, 
		PARTIAL_PLANE, COMPLETED_PLANE, VERTICAL_HEIGHT_SET, NEW_VERTICAL_LINE_PELLET, NEW_VERTICAL_WALL, 
		DELETED_PELLET, DELETED_LINE, DELETED_PLANE, DELETED_POYLGON, EXTENDED_LINE, EXTENDED_PLANE,
		LINE_PLANE_INTERSECTION
	}
	
	// A little holder-class for the actions
	// has a type and pointers to the objects it uses
	public static class Action {
		public ActionType action_type;
		public Pellet pellet;
		public Primitive primitive;
		public Stack<PolygonPellet> current_poly;
		public Scaffold scaffold;
		
		public Action(ActionType at){
			action_type = at;
		}
		
		public Action(ActionType at, Pellet p){
			pellet = p;
			action_type = at;
		}
		
		public Action(ActionType at, Primitive p){
			primitive = p;
			action_type = at;
		}
		
		public Action(ActionType at, Primitive p, Stack<PolygonPellet> curr){
			primitive = p;
			action_type = at;
			current_poly = curr;
		}
		
		public Action(ActionType at, Scaffold s){
			scaffold = s;
			action_type = at;
		}
	}

	private static Stack<Action> undo_stack = new Stack<Action>();
	private static Stack<Action> redo_stack = new Stack<Action>();

	public static String showLatestAction(){
		if (undo_stack.size() == 0)
			return "No actions yet";
		else 
			return undo_stack.peek().action_type.toString().replace("_", " ");
	}
	
	public static void printStack(){
		System.out.println("Undo stack: ");
		for (Action a : undo_stack){
			System.out.println("\t" + a.action_type.toString());
		}
	}
	
	public static void undo(){
		if (undo_stack.size() == 0){
			System.out.println("Nothing to undo");
			return;
		}
		
		System.out.println("Undoing last action: " + showLatestAction());
		
		Action last_action = undo_stack.pop();
		redo_stack.push(last_action);
		
		// here we deal with each possible type of action!!
		if (last_action.action_type == ActionType.NEW_PELLET){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		}
		else if (last_action.action_type == ActionType.PARTIAL_POLYGON){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
			PolygonPellet.current_cycle.pop();
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.POLYGON_LINE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.POLYGON_LINE){
			if (last_action.primitive != null){
				Main.geometry.remove(last_action.primitive);
			}
		}
		else if (last_action.action_type == ActionType.COMPLETED_POLYGON){
			if (last_action.primitive != null){
				Main.geometry.remove(last_action.primitive);
			}
			PolygonPellet.current_cycle = last_action.current_poly;
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.POLYGON_LINE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.PARTIAL_LINE){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
			if (LinePellet.current_line.pellets.size() > 0)
				LinePellet.current_line.pellets.pop();
			else if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_LINE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.COMPLETED_LINE){
			if (last_action.scaffold != null){
				((LineScaffold) last_action.scaffold).nullifyLine();
			}
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_LINE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.EXTENDED_LINE){
			if (last_action.scaffold != null){
				((LineScaffold) last_action.scaffold).removeLastPointAndRefit();
			}
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_LINE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.PARTIAL_PLANE){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
			if (PlanePellet.current_plane.pellets.size() > 0)
				PlanePellet.current_plane.pellets.pop();
			else if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_PLANE){
				undo();
				if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_PLANE){
					undo();
				}
			}
		}
		else if (last_action.action_type == ActionType.COMPLETED_PLANE){
			if (last_action.scaffold != null){
				((PlaneScaffold) last_action.scaffold).nullifyPlane();
			}
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_PLANE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.EXTENDED_PLANE){
			if (last_action.scaffold != null){
				((PlaneScaffold) last_action.scaffold).removeLastPointAndRefit();
			}
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.PARTIAL_PLANE){
				undo();
			}
		}
		else if (last_action.action_type == ActionType.LINE_PLANE_INTERSECTION){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		}
		else if (last_action.action_type == ActionType.NEW_VERTICAL_LINE_PELLET){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		}
		else if (last_action.action_type == ActionType.VERTICAL_HEIGHT_SET){
			VerticalLinePellet.clearAllVerticalLines();
			if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.NEW_VERTICAL_LINE_PELLET){
				undo();
				undo();
			}
		}
	}
	
	// Here are the new actions to be tracked and undone!
	public static void newScaffoldPellet(Pellet p){
		undo_stack.add(new Action(ActionType.NEW_PELLET, p));
	}
	
	public static void newPolygonPellet(Pellet p){
		undo_stack.add(new Action(ActionType.PARTIAL_POLYGON, p));
	}
	
	public static void newPolygonLine(Primitive p){
		undo_stack.add(new Action(ActionType.POLYGON_LINE, p));
	} 
	
	public static void newPolygon(Primitive p, Stack<PolygonPellet> curr){
		curr.pop();
		undo_stack.add(new Action(ActionType.COMPLETED_POLYGON, p, curr));
	}
	
	public static void newLinePellet(Pellet p){
		undo_stack.add(new Action(ActionType.PARTIAL_LINE, p));
	}
	
	public static void newLine(Scaffold s){
		undo_stack.add(new Action(ActionType.COMPLETED_LINE, s));
	}
	
	public static void extendedLine(Scaffold s){
		undo_stack.add(new Action(ActionType.EXTENDED_LINE, s));
	}
	
	public static void newPlanePellet(Pellet p){
		undo_stack.add(new Action(ActionType.PARTIAL_PLANE, p));
	}
	
	public static void newPlane(Scaffold s){
		undo_stack.add(new Action(ActionType.COMPLETED_PLANE, s));
	}
	
	public static void extendedPlane(Scaffold s){
		undo_stack.add(new Action(ActionType.EXTENDED_PLANE, s));
	}
	
	public static void newLinePlaneIntersection(Pellet p){
		undo_stack.add(new Action(ActionType.LINE_PLANE_INTERSECTION, p));
	}
	
	public static void newVerticalLinePellet(Pellet p){
		undo_stack.add(new Action(ActionType.NEW_VERTICAL_LINE_PELLET, p));
	}
	
	public static void newVerticalHeightSet(){
		undo_stack.add(new Action(ActionType.VERTICAL_HEIGHT_SET));
	}
}
