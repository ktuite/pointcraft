package edu.washington.cs.games.ktuite.pointcraft;

import java.util.Stack;

public class ActionTracker {
	
	// The different types of actions
	public enum ActionType {
		NEW_PELLET, PARTIAL_POLYGON, POLYGON_LINE, COMPLETED_POLYGON, PARTIAL_LINE, COMPLETED_LINE, 
		PARTIAL_PLANE, COMPLETED_PLANE, NEW_VERTICAL_LINE, NEW_VERTICAL_WALL, 
		DELETED_PELLET, DELETED_LINE, DELETED_PLANE, DELETED_POYLGON, PELLET_TYPE_CHANGED
	}
	
	// A little holder-class for the actions
	// has a type and pointers to the objects it uses
	public static class Action {
		public ActionType action_type;
		public Pellet pellet;
		public Pellet pellet2;
		public Primitive primitive;
		public Stack<PolygonPellet> current_poly;
		
		public Action(ActionType at, Pellet p){
			pellet = p;
			action_type = at;
		}
		
		public Action(ActionType at, Pellet p, Pellet p2){
			pellet = p;
			pellet2 = p2;
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
	}

	private static Stack<Action> undo_stack = new Stack<Action>();
	private static Stack<Action> redo_stack = new Stack<Action>();

	public static String showLatestAction(){
		if (undo_stack.size() == 0)
			return "No actions yet";
		else 
			return undo_stack.peek().action_type.toString().replace("_", " ");
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
			//if (undo_stack.size() > 0 && undo_stack.peek().action_type == ActionType.COMPLETED_POLYGON){
			//	undo();
			//}
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
		else if (last_action.action_type == ActionType.PELLET_TYPE_CHANGED){
			last_action.pellet.visible = true;
			last_action.pellet2.alive = false;
			undo(); // this pellet changing action doesn't happen on its own...
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
	
	public static void pelletChanged(Pellet old_p, Pellet new_p){
		undo_stack.add(new Action(ActionType.PELLET_TYPE_CHANGED, old_p, new_p));
	}
}
