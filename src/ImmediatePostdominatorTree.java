import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ImmediatePostdominatorTree {

    private final Map<ControlFlowNode, ControlFlowNode> mParents = new HashMap<>();
    private final Map<ControlFlowNode, Set<ControlFlowNode>> mChildren = new HashMap<>();

    public ImmediatePostdominatorTree(@NotNull PostdominatorIndex postdominatorIndex) {
        for (ControlFlowNode node : postdominatorIndex.getNodes()) {
            ControlFlowNode immediatePostdominator = getImmediatePostdominator(node, postdominatorIndex);
            saveImmediatePostdominator(node, immediatePostdominator);
        }
    }

    private void saveImmediatePostdominator(@NotNull ControlFlowNode node,
                                            @Nullable ControlFlowNode immediatePostdominator) {
        mParents.put(node, immediatePostdominator);
        if (!mChildren.containsKey(immediatePostdominator)) {
            mChildren.put(immediatePostdominator, new HashSet<>());
        }
        mChildren.get(immediatePostdominator).add(node);
    }

    @Nullable
    private ControlFlowNode getImmediatePostdominator(@NotNull ControlFlowNode node,
                                                      @NotNull PostdominatorIndex postdominatorIndex) {
        Set<Postdominator> nodePostdominators = postdominatorIndex.getPostdominators(node);
        nodePostdominators.removeIf(postdominator -> postdominator.getNode() == node);
        try {
            Postdominator immediatePostdominator = Collections.min(nodePostdominators,
                    Comparator.comparingInt(Postdominator::getPathLength));
            return immediatePostdominator.getNode();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Nullable
    public ControlFlowNode getParent(@NotNull ControlFlowNode node) {
        return this.mParents.get(node);
    }

    @Nullable
    public Set<ControlFlowNode> getChildren(@NotNull ControlFlowNode node) {
        return this.mChildren.get(node);
    }
}
