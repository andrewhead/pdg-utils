import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ShortestPostdominatorPaths {

    private final Map<ControlFlowNode, Postdominator> mPostdominators = new HashMap<>();

    public boolean updateIfShortestPath(@NotNull ControlFlowNode postDominatorNode,
                                        @NotNull Postdominator newPostdominator) {
        if (!mPostdominators.containsKey(postDominatorNode)) {
            mPostdominators.put(postDominatorNode, newPostdominator);
            return true;
        }
        Postdominator currentNearestPostdominator = mPostdominators.get(postDominatorNode);
        if (newPostdominator.getPathLength() == -1) {
            return false;
        }
        if (currentNearestPostdominator.getPathLength() == -1 ||
                (newPostdominator.getPathLength() < currentNearestPostdominator.getPathLength())) {
            mPostdominators.put(postDominatorNode, newPostdominator);
            return true;
        }
        return false;
    }

    @Nullable
    public Postdominator getPostdominatorWithShortestPath(@NotNull ControlFlowNode node) {
        return mPostdominators.get(node);
    }
}
