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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
	public String[][] envArray = new String[4][4];
	private char dir = 'e';
	private int prevAction = -1;
	private int row = 0;
	private int column = 0;
	private boolean arrow = true;
	private int startCount = 0;
	private int steps = 0;

	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your
		// own intelligent agent

		// this integer array will store the agent actions
		actionTable = new int[8];

		actionTable[0] = Action.NO_OP;
		actionTable[1] = Action.GO_FORWARD;
		actionTable[2] = Action.GO_FORWARD;
		actionTable[3] = Action.GO_FORWARD;
		actionTable[5] = Action.TURN_RIGHT;
		actionTable[4] = Action.TURN_LEFT;
		actionTable[6] = Action.GRAB;
		actionTable[7] = Action.SHOOT;

		//envArray = new int[]{0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				envArray[i][j] = "";
			}
		}
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
		rand = new Random();
		int[] a = new int[]{ 4,5,1,1,1 };
		int[] b = new int[]{ 7,7,7,7,4,5 };
		int[] c = new int[]{ 4,1};
		int action = 0;

		envArray[row][column] = "s";
		if(row == 0 && column == 0)
			startCount += 1;
		steps += 1;
		if(steps > 45)	return 0;

		if (!(bump == true || glitter == true || breeze == true || stench == true || scream == true))
		{
			if(row == 0 && column == 0)
			{
				if(prevAction == 4)		action = 1;
				else	action = c[rand.nextInt(2)];
			}
			else
			{
				if(prevAction == 4 || prevAction == 5)	action = 1;
				else	action = a[rand.nextInt(5)];
			}
		}
		else if (glitter == true) action = 6;
		else if (scream == true && breeze == false) action = 1;
		else if (stench == true)
		{
			//if(arrow == false)	action = 0;
			if(arrow == false && breeze == false)	action = 0;
			else if(prevAction == 7 && breeze == false && arrow == true)
			{
				action = 1;
				arrow = false;
			}
			else if(arrow == true)
			{
				if((dir == 'e' && column == 2)||(dir == 'w' && column == 0)||(dir == 'n' && row == 3)|| (dir == 's' && row == 0))
					action = b[rand.nextInt(6)];
				else
					action = 0;
					//action = a[rand.nextInt(2)];
			}
			else
			{
				arrow = false;
				action = 7;
			}
		}
		else if (bump == true) action = a[rand.nextInt(2)];
		else if (breeze ==true)
		{
			if(row == 0 && column == 0)	action = 0;
			else	action = breezeAction();
			//action = 0;
		}
		dir = updateDirection(action);
		//if(action == 7)	startCount += 10;
		if (action == 1)
			updateLoc();
		//System.out.println(row);
		//System.out.println(column);
		//System.out.println(Arrays.deepToString(envArray));
		// return action to be performed
		prevAction = action;
		//System.out.println(dir);
	    return actionTable[action];
	}

	public char updateDirection(int action)
	{
		char d = dir;
		if(dir == 'e' && action == 4)	d = 'n';
		if(dir == 'e' && action == 5)	d = 's';
		if(dir == 'n' && action == 4)	d = 'w';
		if(dir == 'n' && action == 5)	d = 'e';
		if(dir == 'w' && action == 4)	d = 's';
		if(dir == 'w' && action == 5)	d = 'n';
		if(dir == 's' && action == 4)	d = 'e';
		if(dir == 's' && action == 5)	d = 'w';
		return d;
	}

	public void updateLoc()
	{
		if(dir == 'e' && column == 3) {}
		else if(dir == 'w' && column == 0) {}
		else if(dir == 'n' && row == 3) {}
		else if(dir == 's' && row == 0) {}
		else if(row < 3 && column < 3)
		{
			if (dir == 'e') column += 1;
			else if (dir == 'w') column -= 1;
			else if (dir == 'n') row += 1;
			else if (dir == 's') row -= 1;
		}
	}

	public List neighbors(int r, int c)
	{
		//ArrayList[] arr = new ArrayList[0];
		List<String> arr = new ArrayList<>();
		if (r > 0 && c > 0 && r < 3 && c < 3)
		{
			arr.add(envArray[r][c+1]);
			arr.add(envArray[r+1][c]);
			arr.add(envArray[r][c-1]);
			arr.add(envArray[r-1][c-1]);
		}
		else if (r > 0 && r < 3 && c == 0)
		{
			arr.add(envArray[r][c+1]);
			arr.add(envArray[r+1][c]);
			arr.add(envArray[r-1][c]);
		}
		else if (r > 0 && r < 3 && c == 2)
		{
			arr.add(envArray[r+1][c]);
			arr.add(envArray[r][c-1]);
			arr.add(envArray[r-1][c]);
		}
		else if (c > 0 && c < 3 && r == 0)
		{
			arr.add(envArray[r][c+1]);
			arr.add(envArray[r+1][c]);
			arr.add(envArray[r][c-1]);
		}
		else if (c > 0 && c < 3 && r == 2)
		{
			arr.add(envArray[r][c+1]);
			arr.add(envArray[r][c-1]);
			arr.add(envArray[r-1][c]);
		}
		else if (r == 0 && c == 0)
		{
			arr.add(envArray[r][c+1]);
			arr.add(envArray[r+1][c]);
		}
		else if (r == 0 && c == 2)
		{
			arr.add(envArray[r+1][c]);
			arr.add(envArray[r][c-1]);
		}
		else if (r == 2 && c == 2)
		{
			arr.add(envArray[r][c-1]);
			arr.add(envArray[r-1][c]);
		}
		else if (r == 2 && c == 0)
		{
			arr.add(envArray[r][c+1]);
			arr.add(envArray[r-1][c]);
		}
		return  arr;
	}

	public int breezeAction()
	{
		int action = 0;
		int r = row;
		int c = column;
		List arr = neighbors(row,column);
		//System.out.println(arr);
		if (r > 0 && c > 0 && r < 3 && c < 3)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 1;
			else if(arr.get(0) == "s" && dir == 'n')	action = 5;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 4;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 1;
			else if(arr.get(1) == "s" && dir == 'w')	action = 5;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else if(arr.get(2) == "s" && dir == 'e')	action = 4;
			else if(arr.get(2) == "s" && dir == 'n')	action = 4;
			else if(arr.get(2) == "s" && dir == 'w')	action = 1;
			else if(arr.get(2) == "s" && dir == 's')	action = 5;

			else if(arr.get(3) == "s" && dir == 'e')	action = 5;
			else if(arr.get(3) == "s" && dir == 'n')	action = 5;
			else if(arr.get(3) == "s" && dir == 'w')	action = 4;
			else if(arr.get(3) == "s" && dir == 's')	action = 1;

			else action = 0;
		}
		else if (r > 0 && r < 3 && c == 0)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 1;
			else if(arr.get(0) == "s" && dir == 'n')	action = 5;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 4;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 1;
			else if(arr.get(1) == "s" && dir == 'w')	action = 5;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else if(arr.get(2) == "s" && dir == 'e')	action = 5;
			else if(arr.get(2) == "s" && dir == 'n')	action = 5;
			else if(arr.get(2) == "s" && dir == 'w')	action = 4;
			else if(arr.get(2) == "s" && dir == 's')	action = 1;

			else action = 0;
		}
		else if (r > 0 && r < 3 && c == 2)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 4;
			else if(arr.get(0) == "s" && dir == 'n')	action = 1;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 5;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 4;
			else if(arr.get(1) == "s" && dir == 'w')	action = 1;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else if(arr.get(2) == "s" && dir == 'e')	action = 5;
			else if(arr.get(2) == "s" && dir == 'n')	action = 5;
			else if(arr.get(2) == "s" && dir == 'w')	action = 4;
			else if(arr.get(2) == "s" && dir == 's')	action = 1;

			else action = 0;
		}
		else if (c > 0 && c < 3 && r == 0)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 1;
			else if(arr.get(0) == "s" && dir == 'n')	action = 5;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 4;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 1;
			else if(arr.get(1) == "s" && dir == 'w')	action = 5;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else if(arr.get(2) == "s" && dir == 'e')	action = 4;
			else if(arr.get(2) == "s" && dir == 'n')	action = 4;
			else if(arr.get(2) == "s" && dir == 'w')	action = 1;
			else if(arr.get(2) == "s" && dir == 's')	action = 5;

			else action = 0;
		}
		else if (c > 0 && c < 3 && r == 2)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 1;
			else if(arr.get(0) == "s" && dir == 'n')	action = 5;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 4;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 1;
			else if(arr.get(1) == "s" && dir == 'w')	action = 5;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else if(arr.get(2) == "s" && dir == 'e')	action = 5;
			else if(arr.get(2) == "s" && dir == 'n')	action = 5;
			else if(arr.get(2) == "s" && dir == 'w')	action = 4;
			else if(arr.get(2) == "s" && dir == 's')	action = 1;

			else action = 0;
		}
		else if (r == 0 && c == 0)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 1;
			else if(arr.get(0) == "s" && dir == 'n')	action = 5;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 4;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 1;
			else if(arr.get(1) == "s" && dir == 'w')	action = 5;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else action = 0;
		}
		else if (r == 0 && c == 2)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 4;
			else if(arr.get(0) == "s" && dir == 'n')	action = 1;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 5;

			else if(arr.get(1) == "s" && dir == 'e')	action = 4;
			else if(arr.get(1) == "s" && dir == 'n')	action = 4;
			else if(arr.get(1) == "s" && dir == 'w')	action = 1;
			else if(arr.get(1) == "s" && dir == 's')	action = 5;

			else action = 0;
		}
		else if (r == 2 && c == 2)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 4;
			else if(arr.get(0) == "s" && dir == 'n')	action = 4;
			else if(arr.get(0) == "s" && dir == 'w')	action = 1;
			else if(arr.get(0) == "s" && dir == 's')	action = 5;

			else if(arr.get(1) == "s" && dir == 'e')	action = 5;
			else if(arr.get(1) == "s" && dir == 'n')	action = 5;
			else if(arr.get(1) == "s" && dir == 'w')	action = 4;
			else if(arr.get(1) == "s" && dir == 's')	action = 1;

			else action = 0;
		}
		else if (r == 2 && c == 0)
		{
			if(arr.get(0) == "s" && dir == 'e')	action = 1;
			else if(arr.get(0) == "s" && dir == 'n')	action = 5;
			else if(arr.get(0) == "s" && dir == 'w')	action = 5;
			else if(arr.get(0) == "s" && dir == 's')	action = 4;

			else if(arr.get(1) == "s" && dir == 'e')	action = 5;
			else if(arr.get(1) == "s" && dir == 'n')	action = 5;
			else if(arr.get(1) == "s" && dir == 'w')	action = 4;
			else if(arr.get(1) == "s" && dir == 's')	action = 1;

			else action = 0;
		}
		return action;
	}

	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
}