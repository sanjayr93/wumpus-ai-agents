
public class Model {

    private Cell[][] world;
    private String agentDirection;
    private int[] agLoc, nextLoc;
    private int prevAction;

    public Model(){
        //Initializing the 4x4 world
        world = new Cell[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                world[i][j] = new Cell();
            }
        }
        //Assigning the initial conditions
        agentDirection = "E";
        agLoc = new int[]{world.length - 1, 0}; //starts in the world at (0, 0)
        world[agLoc[0]][agLoc[1]].setVisited(true);
        nextLoc = new int[]{-1, -1};
        prevAction = Action.NO_OP;
    }

    //this has to happen first to update the agent location especially...
    public void updateModelOnAction(){
        switch (prevAction){
            case 1: //Fwd
                updateAgentLoc();
                break;
            case 2: //right
                updateAgentDirection(0);
                break;
            case 3: //left
                updateAgentDirection(1);
                break;
            case 4: //grab
                //game ends
                break;
            case 5: //shoot
                //do nothing..
                break;
            case 6: //no-op
                //do nothing
                break;
        }
    }

    public void updateModelOnPercept(boolean breeze, boolean stench, boolean bump, boolean scream, boolean glitter){
        //Update the world based on the current percepts..
        if(breeze){
            //assign the pit flag to all adjacent cells..
            assignFlags(Utils.pit, "?", false);
        }
        if(stench) {
            //clear unwanted wumpus flags in the model
            int[] wLoc = resolveWumpus();
            //if no overlapping wumpus location is returned, then assign wumpus flag to all adjacent cells..
            if(wLoc[0] == -1) {
                assignFlags(Utils.wumpus, "?", false);
            }else{
                //Else assign Ok flags to all adjacent cells other than the wumpus loc, if no pit flag is there
                assignClearFlags(wLoc);
            }
        }

        //if no stench or breeze, then the cell is ok and also the adjacent cells
        world[agLoc[0]][agLoc[1]].setCellOK(true);
        if(!breeze && !stench)
            assignFlags(null, null, true);
    }

    private void assignClearFlags(int[] wLoc) {

        //Assign the adjacent cells other than wLoc location as safe cells if no other flags are set
        if(agLoc[0] + 1 < world.length && !world[agLoc[0] + 1][agLoc[1]].isCellOK()
                && (agLoc[0] + 1 != wLoc[0] && agLoc[1] != wLoc[1])){
            if(!(world[agLoc[0] + 1][agLoc[1]].getFlags().get(Utils.pit).equals("?") ||
                    world[agLoc[0] + 1][agLoc[1]].getFlags().get(Utils.wumpus).equals("?")))
                world[agLoc[0] + 1][agLoc[1]].setCellOK(true);
        }
        if(agLoc[0] - 1 >= 0 && !world[agLoc[0] - 1][agLoc[1]].isCellOK() && (agLoc[0] - 1 != wLoc[0] && agLoc[1] != wLoc[1])) {
            if(!(world[agLoc[0] - 1][agLoc[1]].getFlags().get(Utils.pit).equals("?") ||
                    world[agLoc[0] - 1][agLoc[1]].getFlags().get(Utils.wumpus).equals("?")))
                world[agLoc[0] - 1][agLoc[1]].setCellOK(true);
        }
        if(agLoc[1] + 1 < world[0].length && !world[agLoc[0]][agLoc[1] + 1].isCellOK() && (agLoc[0] != wLoc[0] && agLoc[1] + 1 != wLoc[1])){
            if(!(world[agLoc[0]][agLoc[1] + 1].getFlags().get(Utils.pit).equals("?") ||
                    world[agLoc[0]][agLoc[1] + 1].getFlags().get(Utils.wumpus).equals("?")))
                world[agLoc[0]][agLoc[1] + 1].setCellOK(true);
        }
        if(agLoc[1] - 1 >= 0 && !world[agLoc[0]][agLoc[1] - 1].isCellOK() && (agLoc[0] != wLoc[0] && agLoc[1] - 1 != wLoc[1])){
            if(!(world[agLoc[0]][agLoc[1] - 1].getFlags().get(Utils.pit).equals("?") ||
                    world[agLoc[0]][agLoc[1] + 1].getFlags().get(Utils.wumpus).equals("?")))
                world[agLoc[0]][agLoc[1] - 1].setCellOK(true);
        }
    }

    private int[] resolveWumpus() {
        /*If a wumpus flag is set for a cell in the model but the cell is not adjacent to current
        position where there was stench, then clear the flag..
        */

        int[] loc = new int[]{-1, -1};
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
//                if(true || world[i][j].isVisited()){
                    if(world[i][j].getFlags().get(Utils.wumpus).equals("?")){
                        if(checkIfNeighborCell(i, j)){
                            loc[0] = i;
                            loc[1] = j;
                        }else{
                            world[i][j].getFlags().put(Utils.wumpus, "");
                            if(!world[i][j].getFlags().get(Utils.pit).equals("?")){
                                world[i][j].setCellOK(true);
                            }
                        }
                    }
//                }
            }
        }

        return loc;
    }

    private boolean checkIfNeighborCell(int r, int c) {
        if((agLoc[0] + 1 == r && agLoc[1] == c) || (agLoc[0] - 1 == r && agLoc[1] == c) ||
                (agLoc[0] == r && agLoc[1] + 1 == c) || (agLoc[0]== r && agLoc[1] - 1 == c))
            return true;
        return false;
    }


    private void updateAgentDirection(int direction) {
        //Update the agent direction, based on the Turn action..
        switch (agentDirection){
            case "E":
                if(direction == 0){ //right
                    agentDirection = "S";
                }else{ //left
                    agentDirection = "N";
                }
                break;
            case "W":
                if(direction == 0){ //right
                    agentDirection = "N";
                }else{ //left
                    agentDirection = "S";
                }
                break;
            case "N":
                if(direction == 0){ //right
                    agentDirection = "E";
                }else{ //left
                    agentDirection = "W";
                }
                break;
            case "S":
                if(direction == 0){ //right
                    agentDirection = "W";
                }else{ //left
                    agentDirection = "E";
                }
                break;
        }

    }

    private void updateAgentLoc() {
        //Update the agent location for the Forward action..
        switch (agentDirection){
            case "E":
                if(agLoc[1] + 1 < world[0].length)
                    agLoc[1]++;
                break;
            case "W":
                if(agLoc[1] - 1 >= 0)
                    agLoc[1]--;
                break;
            case "N":
                if(agLoc[0] - 1 >= 0) //moves up the matrix
                    agLoc[0]--;
                break;
            case "S":
                if(agLoc[0] + 1 < world.length) //moves down the matrix
                    agLoc[0]++;
                break;
        }

        if(!world[agLoc[0]][agLoc[1]].isVisited()){
            world[agLoc[0]][agLoc[1]].setVisited(true);
        }
    }

    private void assignFlags(String obj, String flag, boolean cellOk){
        //Assign flags or ok flag for all the cells adjacent to the current location..
        if(agLoc[0] + 1 < world.length && !world[agLoc[0] + 1][agLoc[1]].isCellOK()){
            if(cellOk) {
                world[agLoc[0] + 1][agLoc[1]].setCellOK(cellOk);
                world[agLoc[0] + 1][agLoc[1]].getFlags().put(Utils.wumpus, "");
                world[agLoc[0] + 1][agLoc[1]].getFlags().put(Utils.pit, "");
            }else
                world[agLoc[0] + 1][agLoc[1]].updateFlags(obj, flag);
        }
        if(agLoc[0] - 1 >= 0 && !world[agLoc[0] - 1][agLoc[1]].isCellOK()) {
            if(cellOk) {
                world[agLoc[0] - 1][agLoc[1]].setCellOK(cellOk);
                world[agLoc[0] - 1][agLoc[1]].getFlags().put(Utils.wumpus, "");
                world[agLoc[0] - 1][agLoc[1]].getFlags().put(Utils.pit, "");
            }else
                world[agLoc[0] - 1][agLoc[1]].updateFlags(obj, flag);
        }
        if(agLoc[1] + 1 < world[0].length && !world[agLoc[0]][agLoc[1] + 1].isCellOK()){
            if(cellOk) {
                world[agLoc[0]][agLoc[1] + 1].setCellOK(cellOk);
                world[agLoc[0]][agLoc[1] + 1].getFlags().put(Utils.wumpus, "");
                world[agLoc[0]][agLoc[1] + 1].getFlags().put(Utils.pit, "");
            }else
                world[agLoc[0]][agLoc[1] + 1].updateFlags(obj, flag);
        }
        if(agLoc[1] - 1 >= 0 && !world[agLoc[0]][agLoc[1] - 1].isCellOK()){
            if(cellOk) {
                world[agLoc[0]][agLoc[1] - 1].setCellOK(cellOk);
                world[agLoc[0]][agLoc[1] - 1].getFlags().put(Utils.wumpus, "");
                world[agLoc[0]][agLoc[1] - 1].getFlags().put(Utils.pit, "");
            }else
                world[agLoc[0]][agLoc[1] - 1].updateFlags(obj, flag);
        }
    }

    public int[] nextOkCell() {

        /*
        If a ok cell calculation has not been used, then return the values in nextLoc[].
        If an adjacent cell to the agent has not been visited and is marked Ok, then it is given priority..
        If all adjacent cells have been visited then, to retrace a path one of the adjacent cell that is
        marked safe will be returned..
         */

        if(nextLoc[0] != -1){
            return nextLoc;
        }

        boolean flag = false;

        if(agLoc[1] + 1 < world[0].length && world[agLoc[0]][agLoc[1] + 1].isCellOK()){
            if(!world[agLoc[0]][agLoc[1] + 1].isVisited())
                flag = true;

            nextLoc[0] = agLoc[0];
            nextLoc[1] = agLoc[1] + 1;
        }
        if(!flag && agLoc[0] - 1 >= 0 && world[agLoc[0] - 1][agLoc[1]].isCellOK()) {
            if(!world[agLoc[0] - 1][agLoc[1]].isVisited())
                flag = true;

            nextLoc[0] = agLoc[0] - 1;
            nextLoc[1] = agLoc[1];
        }
        if(!flag && agLoc[0] + 1 < world.length && world[agLoc[0] + 1][agLoc[1]].isCellOK()){
            if(!world[agLoc[0] + 1][agLoc[1]].isVisited())
                flag = true;

            nextLoc[0] = agLoc[0] + 1;
            nextLoc[1] = agLoc[1];
        }
        if(!flag && agLoc[1] - 1 >= 0 && world[agLoc[0]][agLoc[1] - 1].isCellOK()){
            nextLoc[0] = agLoc[0];
            nextLoc[1] = agLoc[1] - 1;
        }

        return nextLoc;
    }

    public void resetNextLoc() {
        nextLoc[0] = -1;
        nextLoc[1] = -1;
    }



    public int[] getAgLoc() {
        return agLoc;
    }

    public void setAgLoc(int[] agLoc) {
        this.agLoc = agLoc;
    }

    public Cell[][] getWorld() {
        return world;
    }

    public void setWorld(Cell[][] world) {
        this.world = world;
    }

    public String getAgentDirection() {
        return agentDirection;
    }

    public void setAgentDirection(String agentDirection) {
        this.agentDirection = agentDirection;
    }

    public int getPrevAction() {
        return prevAction;
    }

    public void setPrevAction(int prevAction) {
        this.prevAction = prevAction;
    }
}
