import java.util.HashMap;

public class Cell {
    private boolean cellOK;
    private boolean visited;
    private HashMap<String, String> flags; //pit - ?/!, wumpus - ?/!

    public Cell(){
        cellOK = false;
        visited = false;
        flags = new HashMap<>();
        flags.put(Utils.pit, "");
        flags.put(Utils.wumpus, "");
    }

    public void updateFlags(String obj, String flag){
        flags.put(obj, flag);
    }

    public boolean isCellOK() {
        return cellOK;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void setCellOK(boolean cellOK) {
        this.cellOK = cellOK;
    }

    public HashMap<String, String> getFlags() {
        return flags;
    }

    public void setFlags(HashMap<String, String> flags) {
        this.flags = flags;
    }
}
