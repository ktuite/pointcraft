package edu.washington.cs.games.ktuite.pointcraft;

import java.util.Stack;

public class ActionTracker {
	
	// The different types of actions
	public enum ActionType {
		NEW_PELLET, PARTIAL_POLYGON, COMPLETED_POLYGON, PARTIAL_LINE, COMPLETED_LINE, 
		PARTIAL_PLANE, COMPLETED_PLANE, NEW_VERTICAL_LINE, NEW_VERTICAL_WALL, 
		DELETED_PELLET, DELETED_LINE, DELETED_PLANE, DELETED_POYLGON
	}
	
	// A little holder-class for the actions
	// has a type and pointers to the objects it uses
	public static class Action {
		public ActionType action_type;
		public Pellet pellet;
		
		public Action(ActionType at, Pellet p){
			pellet = p;
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
	
	public static void undo(){
		if (undo_stack.size() == 0){
			System.out.println("Nothing to undo");
			return;
		}
		
		System.out.println("UNDOING LAST ACTION: " + showLatestAction());
		
		Action last_action = undo_stack.pop();
		redo_stack.push(last_action);
		
		if (last_action.action_type == ActionType.NEW_PELLET){
			Main.all_dead_pellets_in_world.add(last_action.pellet);
		}
	}
	
	public static void newScaffoldPellet(Pellet p){
		undo_stack.add(new Action(ActionType.NEW_PELLET, p));
	}
}
