/*
 * Class that defines the agent function.
 * 
 * Written by James P. Biagioni (jbiagi1@uic.edu)
 * for CS511 Artificial Intelligence II
 * at The University of Illinois at Chicago
 * 
 * Last modified 2/19/07 
 * 
 * DISCLAIMER:
 * Elements of this application were borrowed from
 * the client-server implementation of the Wumpus
 * World Simulator written by Kruti Mehta at
 * The University of Texas at Arlington.
 * 
 */

import javax.naming.PartialResultException;
import java.lang.reflect.Array;
import java.util.*;

// Used for representation of the world model
class Location {
	public Presence pit;
	public Presence wumpus;
	public Presence gold;
	public int row;
	public int col;
	public int util;

	public Location(int i,int j){
		pit = Presence.U;
		wumpus = Presence.U;
		gold = Presence.U;
		row = i;
		col = j;
		util = 50;
	}
}

enum Presence {
	F,	// False
	T,	// True
	P,	// Probable
	U	// Unknown
}

enum Direction {
	UP,
	DOWN,
	LEFT,
	RIGHT
}

enum DT {
	PIT,
	WUMPUS
}

// Used for representation of the agent
class AgentModel {
	public int[] location;
	public Direction dir;
	public boolean arrow;

	public AgentModel (){
		location = new int[]{3,0};
		dir = Direction.RIGHT;
		arrow = true;
	}
}

@SuppressWarnings("unchecked")
class AgentFunction {
	
	// string to store the agent's name
	// do not remove this variable
	private String agentName = "Agent Smith";
	
	// all of these variables are created and used
	// for illustration purposes; you may delete them
	// when implementing your own intelligent agent
	private int[] actionTable;
	private boolean bump;
	private boolean glitter;
	private boolean breeze;
	private boolean stench;
	private boolean scream;
	private Random rand;
	private Location[][] model = new Location[4][4];
	private AgentModel agent = new AgentModel();
	private int n = 4;
	private HashMap<Direction,int[]> move = new HashMap<Direction,int[]>();
	private Direction[] lmove = new Direction[]{Direction.UP,Direction.LEFT,Direction.RIGHT,Direction.DOWN};
	private HashMap<Direction,Direction []> turn = new HashMap<Direction,Direction []>(); // [0]: : Left, [1] : Right
	private int knownPitCount = 0;
	private int	steps = 0;

	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your 
		// own intelligent agent

		// this integer array will store the agent actions

		initialize();

		actionTable = new int[8];
				  
		actionTable[0] = Action.GO_FORWARD;
		actionTable[1] = Action.GO_FORWARD;
		actionTable[2] = Action.GO_FORWARD;
		actionTable[3] = Action.GO_FORWARD;
		actionTable[4] = Action.TURN_RIGHT;
		actionTable[5] = Action.TURN_LEFT;
		actionTable[6] = Action.GRAB;
		actionTable[7] = Action.SHOOT;

		//printModel();
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
	}

	public int process(TransferPercept tp)
	{
		// To build your own intelligent agent, replace
		// all code below this comment block. You have
		// access to all percepts through the object
		// 'tp' as illustrated here:
		
		// read in the current percepts
		bump = tp.getBump();
		glitter = tp.getGlitter();
		breeze = tp.getBreeze();
		stench = tp.getStench();
		scream = tp.getScream();
		steps += 1;
		int nextAction = Action.GO_FORWARD;

		updateState();
		nextAction = action();
		updateAction(nextAction);

		// return action to be performed
	    return nextAction;	    
	}

	// Based on the incoming percepts update the current world Model
	public void updateState(){
		int row = agent.location[0];
		int col = agent.location[1];

		model[row][col].util -= 1;

		model[row][col].pit = Presence.F;
		model[row][col].wumpus = Presence.F;

		if (glitter == true){
			model[row][col].gold = Presence.T;
		}else{
			model[row][col].gold = Presence.F;
		}

		if (breeze == false){
			updateLocation(DT.PIT,Presence.F);
		} else {
			updateLocation(DT.PIT,Presence.P);
		}

		if (stench == false){
			updateLocation(DT.WUMPUS,Presence.F);
		}else {
			updateLocation(DT.WUMPUS,Presence.P);
		}

		if (scream == true) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					model[i][j].wumpus = Presence.F;
				}
			}
		}

	}

	// Based on current state of the world, returns the next action to be performed
	public int action(){
		int nextAction = Action.NO_OP;
		int row = agent.location[0];
		int col = agent.location[1];

		if (model[row][col].gold == Presence.T){
			return Action.GRAB;
		}

		ArrayList<Location>[] neighbours = getNeighbours();
		ArrayList<Location> safe = neighbours[0];
		ArrayList<Location> unsafe = neighbours[1];
		ArrayList<Location> unknown = neighbours[2];
		ArrayList<Location> wumpus = neighbours[3];
		ArrayList<Location> pit = neighbours[4];
		int totalNeighbours = safe.size() + unsafe.size() + unknown.size();

		// Surrounded by unsafe locations
		if (unsafe.size() == totalNeighbours){
			if (agent.arrow && !breeze){
				return Action.SHOOT;
			}else {
				return Action.NO_OP;
			}
		}

		// Wumpus in one or more locations and agent has arrow
		if (wumpus.size() == 1 && agent.arrow) {
			// Wumpus location known
			//nextAction = locationAction(wumpus.get(0),Action.SHOOT);
			return locationAction(wumpus.get(0),Action.SHOOT);
		}else if (unsafe.size() >= 1 && agent.arrow && stench){
			// Wumpus location unknown but in one of neighbouring squares
			for (Location l : unsafe){
				if (l.pit == Presence.T)
					continue;
				else
					//nextAction = locationAction(l,Action.SHOOT);
					return locationAction(l,Action.SHOOT);
			}
		}

		// Only one location is safe
		if (safe.size() == 1){
			//nextAction = locationAction(safe.get(0),Action.GO_FORWARD);
			return locationAction(safe.get(0),Action.GO_FORWARD);
		}

		// More than one safe locations to move towards
		if (safe.size() > 1){
			for (Location l : safe){
				if (goldInDirection(l.row,l.col) == Presence.U){
					//nextAction = locationAction(l,Action.GO_FORWARD);
					return locationAction(l,Action.GO_FORWARD);
				}
			}
			// None of the direction have a gold
			// Move in direction which does not have a pit

			Location l1 = safe.get(0);
			for (Location l : safe){
				//if (!pitWumpusInDirection(l.row,l.col)){
					//System.out.println(l.util);
					if (l.util == 50)
						return locationAction(l,Action.GO_FORWARD);
				//}
			}
			return locationAction(l1,Action.GO_FORWARD);
		}

		return nextAction;
	}

	// Based on the next action to be taken update the current world Model
	public void updateAction(int nextAction){
		int row = agent.location[0];
		int col = agent.location[1];
		if (nextAction == Action.GO_FORWARD){
			updatePosition();
		}else if (nextAction == Action.TURN_LEFT){
			agent.dir = turn.get(agent.dir)[0];
		}else if (nextAction == Action.TURN_RIGHT){
			agent.dir = turn.get(agent.dir)[1];
		}else if (nextAction == Action.SHOOT){
			agent.arrow = false;
			int[] shift = new int[]{move.get(agent.dir)[0],move.get(agent.dir)[1]};
			int [] x = new int[]{shift[0],shift[1]};
			while(validLocation(shift)){
				model[agent.location[0]+shift[0]][agent.location[1]+shift[1]].wumpus = Presence.F;
				shift[0] = x[0] + shift[0]; shift[1] = x[1] + shift[1];
			}


		}
	}

	/// Returns True - If any location ahead has a pit or wumpus
	private boolean pitWumpusInDirection(int row,int col){
		int shift[] = new int[2];
		shift[0] = row - agent.location[0];
		shift[1] = col - agent.location[1];
		int x[] = new int[]{shift[0],shift[1]};

		while (validLocation(shift)){
			Location l = model[row][col];
			if (l.pit == Presence.P || l.pit == Presence.T || l.wumpus ==Presence.P || l.wumpus == Presence.T){
				return true;
			}
			shift [0] += x[0]; shift[1] += x[1];
			row = agent.location[0] + shift[0]; col = agent.location[1] + shift[1];
		}
		return false;
	}

	// Returns True - If any location ahead has Gold and it is not
	// CoLocated with a pit or wumpus
	private Presence goldInDirection(int row,int col){
		int shift[] = new int[2];
		int x[] = new int[2];
		x[0] = row - agent.location[0];
		x[1] = col - agent.location[1];
		shift[0] = row - agent.location[0];
		shift[1] = col - agent.location[1];

		while (validLocation(shift)){
			Location l = model[row][col];
			if (l.gold == Presence.U){
				if (l.pit == Presence.P || l.pit == Presence.T){
					return Presence.F;
				}
				return Presence.U;
			}
			shift [0] += x[0]; shift[1] += x[1];
			row = agent.location[0] + shift[0]; col = agent.location[1] + shift[1];
		}
		return Presence.F;
	}

	// Returns Action - TURN_LEFT or TURN_RIGHT if agent not facing the direction to move in
	// Returns Action - nextAction if agent facing the direction to take action in
	private int locationAction(Location neighbour,int nextAction){
		int rowShift = neighbour.row - agent.location[0];
		int colShift = neighbour.col - agent.location[1];

		for (Direction key: lmove){
			int[] shift = move.get(key);
			if (rowShift ==  shift[0] &&  colShift == shift[1]){
				if (key == agent.dir){
					return nextAction;
				}else{
					return turnAction(agent.dir,key);
				}
			}
		}
		return Action.NO_OP;
	}

	// Based on current direction and next direction, returns action to perform
	// i.e. TURN_LEFT or TURN_RIGHT
	private int turnAction(Direction current,Direction next){

		if (current == Direction.UP || current == Direction.DOWN){
			int[] x = move.get(current);
			int[] y = move.get(next);

			if (x[0] == y[1])
				return Action.TURN_LEFT;
			else if (x[0] == -1*y[0])
				return Action.TURN_LEFT;
			else if (x[0] == -1*y[1])
				return Action.TURN_RIGHT;
		}else if (current == Direction.LEFT || current == Direction.RIGHT){
			int[] x = move.get(current);
			int[] y = move.get(next);
			if (x[1] == y[0])
				return Action.TURN_RIGHT;
			else if (x[1] == -1*y[1])
				return Action.TURN_LEFT;
			else if (x[1] == -1*y[0])
				return Action.TURN_LEFT;
		}
		return Action.NO_OP;
	}

	// Based on incoming percepts update the locations
	private void updateLocation(DT typ,Presence p){

		ArrayList<int[]> squares = new ArrayList<int[]>();
		ArrayList<int[]> squaresTrue = new ArrayList<int[]>();

		for (Direction key : lmove){
			int [] shift = move.get(key);
			if (validLocation(shift)){
				Presence rp = updatePitWumpus(shift,typ,p);

				if (rp == Presence.U || rp == Presence.P){
					squares.add(shift);
				}
				if (rp == Presence.T){
					squaresTrue.add(shift);
				}
			}
		}

		if (squares.size() == 1){
			int [] shift = squares.get(0);
			int row = agent.location[0]+shift[0];
			int col = agent.location[1]+shift[1];
			if (typ == DT.WUMPUS) {
				model[row][col].wumpus = Presence.T;
				updateToFalse((DT.WUMPUS));
			}else if (typ == DT.PIT) {
				if (squaresTrue.size() == 0) {
					model[row][col].pit = Presence.T;
					knownPitCount += 1;

					if (knownPitCount == 2){
						updateToFalse(DT.PIT);
					}

				}
			}
		}
	}

	// Based on incoming percepts update pit and wumpus
	private Presence updatePitWumpus(int [] shift,DT typ,Presence p) {
		int row = agent.location[0] + shift[0];
		int col = agent.location[1] + shift[1];

		if (typ == DT.PIT){
			if (model[row][col].pit == Presence.U || model[row][col].pit == Presence.P) {
				model[row][col].pit = p;
			}

			return model[row][col].pit;
		}else if (typ == DT.WUMPUS){
			if (model[row][col].wumpus == Presence.U || model[row][col].wumpus == Presence.P) {
				model[row][col].wumpus = p;
			}
			return model[row][col].wumpus;
		}

		return Presence.U;
	}

	// Update Pit or Wumpus to False in all locations
	// If all Pits or Wumpus are discovered
	private void updateToFalse(DT typ){

		if(typ == DT.PIT){
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					if (model[i][j].pit != Presence.T)
						model[i][j].pit = Presence.F;
				}
			}
		}else if (typ == DT.WUMPUS){
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					if (model[i][j].wumpus != Presence.T)
						model[i][j].wumpus = Presence.F;
				}
			}
		}

	}

	// Returns all neighbours for the current location
	private ArrayList<Location>[] getNeighbours(){
		ArrayList<Location> safe = new ArrayList<Location>();	// Locations which have no pit or wumpus
		ArrayList<Location> unsafe = new ArrayList<Location>(); // Locations which could have pit or wumpus
		ArrayList<Location> unknown = new ArrayList<Location>(); // Locations which are unknown
		ArrayList<Location> wumpus = new ArrayList<Location>(); // Location which have wumpus
		ArrayList<Location> pit = new ArrayList<Location>();	// Locations which have pits

		ArrayList<Direction> lst = new ArrayList<Direction>();
		lst.add(agent.dir);

		for (Direction key : lmove){
			if (key != agent.dir)
				lst.add(key);
		}

		for (Direction key : lst){
			int [] shift = move.get(key);
			if (validLocation(shift)){
				int row =  agent.location[0]+shift[0];
				int col = agent.location[1]+shift[1];
				Location neighbour = model[row][col];

				if (neighbour.pit == Presence.F && neighbour.wumpus == Presence.F){
					safe.add(neighbour);
				}else if (neighbour.pit == Presence.P || neighbour.wumpus == Presence.P ||
						  neighbour.pit == Presence.T || neighbour.wumpus == Presence.T){
					unsafe.add(neighbour);
					if (neighbour.wumpus == Presence.T){
						wumpus.add(neighbour);
					}else if (neighbour.pit == Presence.T){
						pit.add(neighbour);
					}
				}else {
					unknown.add(neighbour);
				}
			}
		}
		ArrayList<Location>[] neighbours = new ArrayList[]{safe,unsafe,unknown,wumpus,pit};
		return neighbours;
	}

	// Returns True - If location reached on moving in the direction of shift
	// From current location of agent is present inside grid
	private boolean validLocation(int[] shift){
		int row = agent.location[0] + shift[0];
		int col = agent.location[1] + shift[1];

		if (row >= 0 && row < n && col >= 0 && col < n) {
			return true;
		}
		return false;
	}

	// Update the current location of the agent based on the action to be performed
	private void updatePosition(){

		int[] shift = move.get(agent.dir);
		if (validLocation((shift))) {
			agent.location[0] += shift[0];
			agent.location[1] += shift[1];
		}
	}

	// Initialize static dictionaries
	private void initialize(){

		move.put(Direction.UP,new int[]{-1,0});
		move.put(Direction.DOWN,new int[]{1,0});
		move.put(Direction.LEFT,new int[]{0,-1});
		move.put(Direction.RIGHT,new int[]{0,1});

		turn.put(Direction.UP,new Direction[]{Direction.LEFT,Direction.RIGHT});
		turn.put(Direction.DOWN,new Direction[]{Direction.RIGHT,Direction.LEFT});
		turn.put(Direction.LEFT,new Direction[]{Direction.DOWN,Direction.UP});
		turn.put(Direction.RIGHT,new Direction[]{Direction.UP,Direction.DOWN});

		for (int i=0;i<4;i++){
			for (int j=0;j<4;j++){
				model[i][j] = new Location(i,j);
			}
		}
	}

	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
}