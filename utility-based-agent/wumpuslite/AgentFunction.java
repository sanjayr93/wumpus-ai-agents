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
import java.util.List;
import java.util.Random;

class AgentFunction {

	// string to store the agent's name
	// do not remove this variable
	private String agentName = "Agent AorSenjiruven";
	int agentXPos;
	int agentYPos;
	int initBlock;
	char direction;
	WumpusWorld wumpusWorld;
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

	public AgentFunction()
	{
		// for illustration purposes; you may delete all code
		// inside this constructor when implementing your
		// own intelligent agent

		// this integer array will store the agent actions
		agentXPos = 0;
		agentYPos = 0;
		direction = 'E';
		initBlock = 0;
		actionTable = new int[6];
		actionTable[0] = Action.GO_FORWARD;
		actionTable[1] = Action.TURN_RIGHT;
		actionTable[2] = Action.TURN_LEFT;
		actionTable[3] = Action.SHOOT;
		actionTable[4] = Action.GRAB;
		actionTable[5] = Action.NO_OP;
		wumpusWorld = new WumpusWorld();
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
		// new random number generator, for
		// randomly picking actions to execute
		//rand = new Random();
	}

	public int process(TransferPercept tp) {
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

		if(agentXPos == 0 && agentYPos == 0 && stench == true && breeze == false && glitter == false) {
			if(direction == 'E' && initBlock == 0) {
				initBlock +=1;
				return actionTable[3];
			}
		}
		if (glitter == true) {
			return actionTable[4];
		} else if (stench == false && breeze == false && scream == false) {
			wumpusWorld.updateWumpusWorld(agentXPos, agentYPos, 0);
			return actionTable[nextmove(wumpusWorld)];
		} else if (stench == true && breeze == true) {
			wumpusWorld.updateWumpusWorld(agentXPos, agentYPos, 1);
			return actionTable[nextmove(wumpusWorld)];
		} else if (stench == true && breeze == false) {
			wumpusWorld.updateWumpusWorld(agentXPos, agentYPos, 2);
			return actionTable[nextmove(wumpusWorld)];
		} else if (stench == false && breeze == true && scream == false) {
			wumpusWorld.updateWumpusWorld(agentXPos, agentYPos, 3);
			return actionTable[nextmove(wumpusWorld)];
		} else if (breeze == true && scream == true) {
			wumpusWorld.updateWumpusWorld(agentXPos, agentYPos, 4);
			return actionTable[nextmove(wumpusWorld)];
		} else if (breeze == false && scream == true) {
			wumpusWorld.updateWumpusWorld(agentXPos, agentYPos, 5);
			return actionTable[nextmove(wumpusWorld)];
		} else {
			return actionTable[nextmove(wumpusWorld)];
		}
	}

    // Based on the current values of the model cells - determine the most viable cell destination
	// Based on the destination cell chosen - output the corresponding action
	int nextmove(WumpusWorld wumpusWorld) {
		List<NewWumpusClass> possibleCells = new ArrayList();
		List<NewWumpusClass> possibleUnexploredCells = new ArrayList();
		List<NewWumpusClass> possibleExploredCells = new ArrayList();
		List<Integer> result = new ArrayList<>();
		int destX = agentXPos;
		int destY = agentYPos;

		if(agentXPos != 0) {
			if (wumpusWorld.cells[agentXPos - 1][agentYPos].safe != -1
				&& (wumpusWorld.cells[agentXPos - 1][agentYPos].safe == 1
					|| (wumpusWorld.cells[agentXPos - 1][agentYPos].wumpus == 0
					&& wumpusWorld.cells[agentXPos - 1][agentYPos].pit == 0) )) {
				NewWumpusClass wumpus = new NewWumpusClass();
				wumpus.cell = wumpusWorld.cells[agentXPos - 1][agentYPos];
				wumpus.x = agentXPos - 1;
				wumpus.y = agentYPos;
				possibleCells.add(wumpus);
			}
		}
		if (agentXPos != 3) {
			if (wumpusWorld.cells[agentXPos + 1][agentYPos].safe != -1
					&& (wumpusWorld.cells[agentXPos + 1][agentYPos].safe == 1
					|| (wumpusWorld.cells[agentXPos + 1][agentYPos].wumpus == 0
					&& wumpusWorld.cells[agentXPos + 1][agentYPos].pit == 0) )) {
				NewWumpusClass wumpus = new NewWumpusClass();
				wumpus.cell = wumpusWorld.cells[agentXPos + 1][agentYPos];
				wumpus.x = agentXPos + 1;
				wumpus.y = agentYPos;
				possibleCells.add(wumpus);
			}
		}
		if(agentYPos != 0) {
			if (wumpusWorld.cells[agentXPos][agentYPos - 1].safe != -1
					&& (wumpusWorld.cells[agentXPos][agentYPos - 1].safe == 1
					|| (wumpusWorld.cells[agentXPos][agentYPos - 1].wumpus == 0
					&& wumpusWorld.cells[agentXPos][agentYPos - 1].pit == 0) )) {
				NewWumpusClass wumpus = new NewWumpusClass();
				wumpus.cell = wumpusWorld.cells[agentXPos][agentYPos - 1];
				wumpus.x = agentXPos;
				wumpus.y = agentYPos - 1;
				possibleCells.add(wumpus);
			}
		}
		if (agentYPos != 3) {
			if (wumpusWorld.cells[agentXPos][agentYPos + 1].safe != -1
					&& (wumpusWorld.cells[agentXPos][agentYPos + 1].safe == 1
					|| (wumpusWorld.cells[agentXPos][agentYPos + 1].wumpus == 0
					&& wumpusWorld.cells[agentXPos][agentYPos + 1].pit == 0) )) {
				NewWumpusClass wumpus = new NewWumpusClass();
				wumpus.cell = wumpusWorld.cells[agentXPos][agentYPos + 1];
				wumpus.x = agentXPos;
				wumpus.y = agentYPos + 1;
				possibleCells.add(wumpus);
			}
		}


		if (possibleCells.size() == 0) {
				destX = agentXPos;
				destY = agentYPos;
		} else if (possibleCells.size() == 1) {
			for (NewWumpusClass possiblecell : possibleCells) {
				destX = possiblecell.x;
				destY = possiblecell.y;
			}
		} else {
			for (NewWumpusClass possiblecell : possibleCells) {
				if (possiblecell.cell.visited == 1) {
					possibleExploredCells.add(possiblecell);
				} else {
					possibleUnexploredCells.add(possiblecell);
				}
			}
			ForwardSearch search = new ForwardSearch();
			result = search.fwdSearch(possibleExploredCells, possibleUnexploredCells, agentXPos, agentYPos, direction, wumpusWorld);
			destX = result.get(0);
			destY = result.get(1);
		}

		int move = nextMovement(destX, destY, wumpusWorld);
		return move;
	}

	int nextMovement(int destinationX, int destinationY, WumpusWorld wumpusWorld) {
		if(agentXPos < destinationX) {
			if (direction == 'E') {
				agentXPos = destinationX;
				wumpusWorld.cells[agentXPos][agentYPos].visited = 1;
				wumpusWorld.cells[agentXPos][agentYPos].safe = 1;
				return 0;
			} else if (direction == 'N') {
				direction = 'E';
				return 1;
			} else if (direction == 'S') {
				direction = 'E';
				return 2;
			} else {
				if(rand.nextInt(2) + 1 == 1) {
					direction = 'N';
					return 1;
				} else {
					direction = 'S';
					return 2;
				}
			}
		} else if(agentXPos > destinationX) {
			if (direction == 'W') {
				agentXPos = destinationX;
				wumpusWorld.cells[agentXPos][agentYPos].visited = 1;
				wumpusWorld.cells[agentXPos][agentYPos].safe = 1;
				return 0;
			} else if (direction == 'N') {
				direction = 'W';
				return 2;
			} else if (direction == 'S') {
				direction = 'W';
				return 1;
			} else {
				if(rand.nextInt(2) + 1 == 1) {
					direction = 'S';
					return 1;
				} else {
					direction = 'N';
					return 2;
				}
			}
		} else {
			if(agentYPos < destinationY) {
				if (direction == 'N') {
					agentYPos = destinationY;
					wumpusWorld.cells[agentXPos][agentYPos].visited = 1;
					wumpusWorld.cells[agentXPos][agentYPos].safe = 1;
					return 0;
				} else if (direction == 'W') {
					direction = 'N';
					return 1;
				} else if (direction == 'E') {
					direction = 'N';
					return 2;
				} else {
					if(rand.nextInt(2) + 1 == 1) {
						direction = 'W';
						return 1;
					} else {
						direction = 'E';
						return 2;
					}
				}
			} else if (agentYPos > destinationY) {
				if (direction == 'S') {
					agentYPos = destinationY;
					wumpusWorld.cells[agentXPos][agentYPos].visited = 1;
					wumpusWorld.cells[agentXPos][agentYPos].safe = 1;
					return 0;
				} else if (direction == 'W') {
					direction = 'S';
					return 2;
				} else if (direction == 'E') {
					direction = 'S';
					return 1;
				} else {
					if(rand.nextInt(2) + 1 == 1) {
						direction = 'E';
						return 1;
					} else {
						direction = 'W';
						return 2;
					}
				}
			} else {
				return 5;
			}
		}
	}

	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
}


class NewWumpusClass {
	WumpusCell cell;
	int x;
	int y;
}