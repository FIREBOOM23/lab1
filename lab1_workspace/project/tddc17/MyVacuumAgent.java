package tddc17;


import aima.core.environment.liuvacuum.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.Random;


class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;
	
	public boolean[][] visitedBFS;
    public Queue<int[]> bfsQueue = new LinkedList<int[]>();
    public boolean bfsInitialized = false;
    
    public static final int[][] DIRS = {
            {-1, 0}, {0, 1}, {1, 0}, {0, -1}
    };
	
	
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
		visitedBFS = new boolean[world.length][world[0].length];
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }
		
	}
	
	public void initBFS() {
        // 重置标志
        for (int i = 0; i < visitedBFS.length; i++)
            for (int j = 0; j < visitedBFS[i].length; j++)
                visitedBFS[i][j] = false;

        bfsQueue.clear();
        bfsInitialized = true;

        // 从当前 Agent 位置开始
        int sx = agent_x_position, sy = agent_y_position;
        visitedBFS[sx][sy] = true;
        bfsQueue.offer(new int[]{sx, sy});

        // 标准 BFS 循环
        while (!bfsQueue.isEmpty()) {
            int[] cur = bfsQueue.poll();
            int x = cur[0], y = cur[1];

            // 遍历四个方向
            for (int[] d : DIRS) {
                int nx = x + d[0], ny = y + d[1];
                if (nx < 0 || nx >= world.length 
                 || ny < 0 || ny >= world[0].length)
                    continue;
                if (visitedBFS[nx][ny] 
                 || world[nx][ny] == WALL)
                    continue;

                visitedBFS[nx][ny] = true;
                bfsQueue.offer(new int[]{nx, ny});
            }
        }
    }

	
	
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
	
	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
			}
			System.out.println("");
		}
	}
}

class MyAgentProgram implements AgentProgram {
	


	private int initnialRandomActions = 10;
	private Random random_generator = new Random();

	
	// Here you can define your variables!
	private Queue<int[]> frontier = new LinkedList<>();
    private Set<String> visited = new HashSet<>();

	public int iterationCounter = 1000;
	public MyAgentState state = new MyAgentState();
	
	
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	
	@Override
	public Action execute(Percept percept) {
		
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
		
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!
    	    	
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);
    	
		
	    iterationCounter--;
	    
	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;

	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);
	    
	    // State update based on the percept value and the last action
	    state.updatePosition((DynamicPercept)percept);
	    if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
	    }else if (state.world[state.agent_x_position][state.agent_y_position] == state.UNKNOWN) {
	        state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
	    }
	    
//	   
	    if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    } else {
            state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
        }
	 // check the left dirt
	    boolean dirtLeft = false;
	    for (int i = 0; i < state.world.length; i++) {
	        for (int j = 0; j < state.world[i].length; j++) {
	            if (state.world[i][j] == state.DIRT) {
	                dirtLeft = true;
	                break;
	            }
	        }
	        if (dirtLeft) break;
	    }
	    
	    if (dirt) {
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    }
	    else {
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);
	    }
	    state.printWorldDebug();
	    
//	  

	    // Next action selection based on the percept value
	    
	    
	    String posKey = state.agent_x_position + "," + state.agent_y_position;
        visited.add(posKey);

        
        
        if (bump) {
            switch (state.agent_direction) {
                case MyAgentState.NORTH:
                    state.updateWorld(state.agent_x_position, state.agent_y_position - 1, state.WALL);
                    break;
                case MyAgentState.EAST:
                    state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
                    break;
                case MyAgentState.SOUTH:
                    state.updateWorld(state.agent_x_position, state.agent_y_position + 1, state.WALL);
                    break;
                case MyAgentState.WEST:
                    state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
                    break;
            }
        } else {
            // put neighbor to the bfs
            addNeighbor(state.agent_x_position + 1, state.agent_y_position);
            addNeighbor(state.agent_x_position - 1, state.agent_y_position);
            addNeighbor(state.agent_x_position, state.agent_y_position + 1);
            addNeighbor(state.agent_x_position, state.agent_y_position - 1);
        }

        // if nonewhere, go home
        if (frontier.isEmpty()) {
            if (home) {
                return NoOpAction.NO_OP;
            } else {                
                return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
            }
        }

        // next target
        int[] target = frontier.peek();
        if (state.agent_x_position == target[0] && state.agent_y_position == target[1]) {
            frontier.poll();
            return LIUVacuumEnvironment.ACTION_MOVE_FORWARD; // 继续走到下一个
        }

        // move to the destination
        return moveToward(target[0], target[1]);
    }

    private void addNeighbor(int x, int y) {
        String key = x + "," + y;
        if (!visited.contains(key) && state.world[x][y] != state.WALL) {
        	
            frontier.add(new int[]{x, y});
        }
    }

    private Action moveToward(int targetX, int targetY) {
        int dx = targetX - state.agent_x_position;
        int dy = targetY - state.agent_y_position;

        // judge the direction
        if (dx == 1 && state.agent_direction != MyAgentState.EAST) {
            return turnTo(MyAgentState.EAST);
        } else if (dx == -1 && state.agent_direction != MyAgentState.WEST) {
            return turnTo(MyAgentState.WEST);
        } else if (dy == 1 && state.agent_direction != MyAgentState.SOUTH) {
            return turnTo(MyAgentState.SOUTH);
        } else if (dy == -1 && state.agent_direction != MyAgentState.NORTH) {
            return turnTo(MyAgentState.NORTH);
        }

        // forward
        state.agent_last_action = state.ACTION_MOVE_FORWARD;
        return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    }

    private Action turnTo(int dir) {
        if ((state.agent_direction + 1) % 4 == dir) {
            state.agent_direction = dir;
            state.agent_last_action = state.ACTION_TURN_RIGHT;
            return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
        } else {
            state.agent_direction = (state.agent_direction + 3) % 4;
            state.agent_last_action = state.ACTION_TURN_LEFT;
            return LIUVacuumEnvironment.ACTION_TURN_LEFT;
        }
    }
}
public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
    	super(new MyAgentProgram());
	}
}

