import java.util.ArrayList;

public class GameStats {
    public ArrayList<ArrayList<Integer>> branchingFactors;
    public ArrayList<ArrayList<Integer>> naiveDepths;
    public ArrayList<ArrayList<Integer>> smartDepths;
    public ArrayList<ArrayList<Integer>> smartSims;
    public ArrayList<ArrayList<Integer>> naiveSims;
    public ArrayList<ArrayList<Double>> confidence;

    public GameStats() {
        branchingFactors = new ArrayList<>();
        naiveDepths = new ArrayList<>();
        smartDepths = new ArrayList<>();
        smartSims = new ArrayList<>();
        naiveSims = new ArrayList<>();
        confidence = new ArrayList<>();
    }

    public void addNewList() {
        branchingFactors.add(new ArrayList<>());
        naiveDepths.add(new ArrayList<>());
        smartDepths.add(new ArrayList<>());
        smartSims.add(new ArrayList<>());
        naiveSims.add(new ArrayList<>());
        confidence.add(new ArrayList<>());
    }
}
