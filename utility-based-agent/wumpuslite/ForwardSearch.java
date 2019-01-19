import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;

public class ForwardSearch {
    private List<Utility> utilities = new ArrayList<>();
    private List<String> goals = new ArrayList<>();
    private int destX;
    private int destY;
    private WumpusWorld wumpusWorld;
    private int flag = 0;

    public List<Integer> fwdSearch(List<NewWumpusClass> possExpCells, List<NewWumpusClass> possUnexpCells, int agentPosX, int agentPosY, char direction, WumpusWorld wumpWorld) {
        this.wumpusWorld = wumpWorld;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((wumpusWorld.cells[i][j].safe == 1 && wumpusWorld.cells[i][j].visited != 1)
                    || (wumpusWorld.cells[i][j].safe != -1 && wumpusWorld.cells[i][j].visited != 1
                        && wumpusWorld.cells[i][j].wumpus == 0 && wumpusWorld.cells[i][j].pit == 0)) {

                        goals.add(String.valueOf(i) + "-" + String.valueOf(j));
                }
            }
        }

        if(possUnexpCells.size() != 0){
            for (NewWumpusClass cell : possUnexpCells) {
                utilities.add(new Utility(cell.x, cell.y));
            }
        } else {
            for (NewWumpusClass cell : possExpCells) {
                utilities.add(new Utility(cell.x, cell.y));
            }
        }

        int ctr = 0;
        if(possUnexpCells.size() != 0) {
            for (NewWumpusClass cell : possUnexpCells) {
                if (direction == 'N' && cell.y == agentPosY + 1 && cell.x == agentPosX) {
                    destX = cell.x;
                    destY = cell.y;
                    ctr += 1;
                    break;
                } else if (direction == 'E' && cell.y == agentPosY && cell.x == agentPosX + 1) {
                    destX = cell.x;
                    destY = cell.y;
                    ctr += 1;
                    break;
                } else if (direction == 'S' && cell.y == agentPosY - 1 && cell.x == agentPosX) {
                    destX = cell.x;
                    destY = cell.y;
                    ctr += 1;
                    break;
                } else if (direction == 'W' && cell.y == agentPosY && cell.x == agentPosX - 1) {
                    destX = cell.x;
                    destY = cell.y;
                    ctr += 1;
                    break;
                }
            }

            if (ctr == 0) {
                Utility finalCell = getUtilityValue();
                destX = finalCell.x;
                destY = finalCell.y;
            }
        } else {
                Utility finalCell = getUtilityValue();
                destX = finalCell.x;
                destY = finalCell.y;
        }

        List<Integer> result = new ArrayList<>();

        if(flag == 0) {
            result.add(destX);
            result.add(destY);
        } else {
            result.add(agentPosX);
            result.add(agentPosY);
        }

        return result;
    }

    private Utility getUtilityValue() {
        List<Goal> allPossibleCells = new ArrayList<>();

        for(int j = 0; j < 4; j++)
            for(int k = 0; k < 4; k++)
                allPossibleCells.add(new Goal(j,k));

        for (Utility cell : utilities) {
            List<String>  reachableCell = new ArrayList<>();

            if(wumpusWorld.cells[cell.x][cell.y].visited == 0) {
                cell.utility += 1;
            }

            for(Goal goals : allPossibleCells) {
                goals.dist = abs(goals.x - cell.x) +  abs(goals.y - cell.y);
            }

            Collections.sort(allPossibleCells, new GoalCellComparator());

            for(Goal goal : allPossibleCells){

                if(goal.dist == 0) {
                    reachableCell.add(String.valueOf(goal.x)+"-"+String.valueOf(goal.y));
                } else {
                    int x1 = goal.x;
                    int y1 = goal.y;

                    if(reachableCell.contains(String.valueOf(x1-1)+"-"+String.valueOf(y1))
                        || reachableCell.contains(String.valueOf(x1+1)+"-"+String.valueOf(y1))
                        || reachableCell.contains(String.valueOf(x1)+"-"+String.valueOf(y1-1))
                        || reachableCell.contains(String.valueOf(x1)+"-"+String.valueOf(y1+1))) {
                        if (this.goals.contains(String.valueOf(x1)+"-"+String.valueOf(y1))) {
                            reachableCell.add(String.valueOf(goal.x)+"-"+String.valueOf(goal.y));
                            cell.utility += 1 / (double)goal.dist;
                        } else {
                            if((wumpusWorld.cells[x1][y1].pit == 0 && wumpusWorld.cells[x1][y1].wumpus == 0
                                    && (wumpusWorld.cells[x1][y1].safe != -1 || wumpusWorld.cells[x1][y1].wumpusExists == 1 ))
                                || (wumpusWorld.cells[x1][y1].safe == 1)) {
                                reachableCell.add(String.valueOf(goal.x)+"-"+String.valueOf(goal.y));
                            }
                        }
                    }
                }
            }
        }

        Comparator<Utility> cmp = Comparator.comparing(Utility::getUtility);
        Utility finalCell = Collections.max(utilities,cmp);
        if(finalCell.utility == 0) {
            flag = 1;
        }
        return finalCell;
    }

}

class Utility {
    int x,y;
    double utility = 0.0;
    Utility(int x, int y){
        this.x = x;
        this.y = y;
    }
    double getUtility() {
        return this.utility;
    }
}

class Goal {
    int x,y;
    int dist=0;
    Goal(int x, int y){
        this.x = x;
        this.y = y;
    }
    int getDist() {
        return this.dist;
    }
}

class GoalCellComparator implements Comparator<Goal> {
    public int compare(Goal goal1, Goal goal2) {
        return goal1.getDist() - goal2.getDist();
    }
}