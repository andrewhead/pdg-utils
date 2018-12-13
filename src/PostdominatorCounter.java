import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PostdominatorCounter {

    private final Map<ControlFlowNode, Integer> mCounts = new HashMap<>();

    public void increment(ControlFlowNode node) {
        if (!mCounts.containsKey(node)) {
            mCounts.put(node, 0);
        }
        mCounts.put(node, mCounts.get(node) + 1);
    }

    public Set<ControlFlowNode> getPostdominatorNodes() {
        return mCounts.keySet();
    }

    public int getCount(ControlFlowNode node) {
        return mCounts.get(node);
    }
}
