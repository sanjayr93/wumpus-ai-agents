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

import java.util.HashMap;

class AgentFunction {
	
	// string to store the agent's name
	// do not remove this variable
	private String agentName = "Agent Carter";
	
	private Model envModel;

	public AgentFunction() {
		envModel = new Model();
	}

	/*
	Percepts tuple - <glitter, stench, scream, bump, breeze>
	*/
    public int process(TransferPercept tp) {
		// read in the current percepts
		// if it's glitter then grab right away
		if(tp.getGlitter()){
		    return Action.GRAB;
        }

        //updating the model based on last action
		envModel.updateModelOnAction();
		//updating the model based on current percepts
		envModel.updateModelOnPercept(tp.getBreeze(), tp.getStench(), tp.getBump(), tp.getScream(), tp.getGlitter());

		//Condition - Action Rules
		if (tp.getBump()) {
			//save the current action
			envModel.setPrevAction(Action.TURN_LEFT);
			return Action.TURN_LEFT;
		}

		//Resolve any conflicting markings of pits and wumpus
		resolveCellStatus(tp);
		//Get the location of the next safe cell
		int[] loc = envModel.nextOkCell();

		//If no safe cell is found, then do NO_OP
		if (loc[0] == -1) {
			envModel.setPrevAction(Action.NO_OP);
			return Action.NO_OP;
		}

		//Check if forward action takes the agent to the desired safe cell
		if (isFwdCell(loc)) {
			envModel.resetNextLoc();
			envModel.setPrevAction(Action.GO_FORWARD);
			return Action.GO_FORWARD;
		}

		//If not, then perform a turn operation..
		envModel.setPrevAction(Action.TURN_LEFT);
		return Action.TURN_LEFT;
	}

	private boolean isFwdCell(int[] loc) {
    	/* Based on the direction the agent is currently facing, check if the forward action takes the
			agent to the loc cell
		*/
		switch (envModel.getAgentDirection()){
			case "E":
				return envModel.getAgLoc()[0] == loc[0] && envModel.getAgLoc()[1] + 1 == loc[1];
			case "W":
				return envModel.getAgLoc()[0] == loc[0] && envModel.getAgLoc()[1] - 1 == loc[1];
			case "N":
				return envModel.getAgLoc()[0] - 1 == loc[0] && envModel.getAgLoc()[1] == loc[1];
			case "S":
				return envModel.getAgLoc()[0] + 1 == loc[0] && envModel.getAgLoc()[1] == loc[1];
		}
		return false;
    }

	private void resolveCellStatus(TransferPercept tp) {
		int[] aLoc = envModel.getAgLoc();

		//Pass the location of all adjacent cells to the current agent location..
		if(aLoc[1] + 1 < envModel.getWorld()[0].length){
			resolve(aLoc[0], aLoc[1] + 1, tp);
		}

		if(aLoc[1] - 1 >= 0){
			resolve(aLoc[0], aLoc[1] - 1, tp);
		}

		if(aLoc[0] - 1 >= 0){
			resolve(aLoc[0] - 1, aLoc[1], tp);
		}

		if(aLoc[0] + 1 < envModel.getWorld().length){
			resolve(aLoc[0] + 1, aLoc[1], tp);
		}
	}

	private void resolve(int r, int c, TransferPercept tp) {
    	//Get the pit and the wumpus flags set for the cell at (r, c) position
    	HashMap<String, String> flags = envModel.getWorld()[r][c].getFlags();

    	//If ok then do nothing..
    	if(envModel.getWorld()[r][c].isCellOK())
			return;

    	/*
    	Three conditions to nullify a pit/wumpus possibility..
    	- If there is a pit and wumpus flags in one of the adjacent cells but the agent doesn't sense stench
    	or breeze in current cell then, clear the flags and the cell is ok.
    	- If there is a pit flag but no breeze, then clear the flag and the cell is ok if there is no stench and wumpus flag
    	- If there is a wumpus flag but no stench, then clear the flag and the cell is ok if there is no breeze and pit flag
    	*/
    	if(flags.get(Utils.pit).equals("?") &&
				flags.get(Utils.wumpus).equals("?")) {
			if (!(tp.getBreeze() || tp.getStench())) {
				envModel.getWorld()[r][c].setCellOK(true);
				flags.put(Utils.pit, "");
				flags.put(Utils.wumpus, "");
			}
		}

		if(flags.get(Utils.pit).equals("?") && !flags.get(Utils.wumpus).equals("?")){
    		if(!tp.getBreeze()){
    			envModel.getWorld()[r][c].setCellOK(true);
    			flags.put(Utils.pit, "");
			}
		}

		if(!flags.get(Utils.pit).equals("?") && flags.get(Utils.wumpus).equals("?")){
			if(!tp.getStench()){
				envModel.getWorld()[r][c].setCellOK(true);
				flags.put(Utils.wumpus, "");
			}
		}
	}

	// public method to return the agent's name
	// do not remove this method
	public String getAgentName() {
		return agentName;
	}
}