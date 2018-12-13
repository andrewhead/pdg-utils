import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Postdominator {

    private ControlFlowNode mNode;
    private List<ControlFlowNode> mPath;

    public Postdominator(@NotNull ControlFlowNode node, @Nullable List<ControlFlowNode> path) {
        mNode = node;
        mPath = path;
    }

    private static void addAllOtherNodesAsPostdominators(@NotNull PostdominatorIndex postdominators,
                                                         @NotNull ControlFlowNode node,
                                                         @NotNull List<ControlFlowNode> nodes) {
        for (ControlFlowNode otherNode : nodes) {
            if (node != otherNode) {
                postdominators.addNullPath(node, otherNode);
            }
        }
    }

    private static PostdominatorIndex initializePostdominators(@NotNull ControlFlowGraph controlFlowGraph) {
        PostdominatorIndex postdominators = new PostdominatorIndex();
        ControlFlowNode exitNode = controlFlowGraph.getExitNode();
        for (ControlFlowNode node : controlFlowGraph.getNodes()) {
            postdominators.addSelfAsPostdominator(node);
            if (node != exitNode) {
                addAllOtherNodesAsPostdominators(postdominators, node, controlFlowGraph.getNodes());
            }
        }
        return postdominators;
    }

    public static PostdominatorIndex getPostdominators(@NotNull ControlFlowGraph controlFlowGraph) {
        PostdominatorIndex postdominators = initializePostdominators(controlFlowGraph);
        boolean postdominatorsUpdated = true;
        while (postdominatorsUpdated) {
            postdominatorsUpdated = false;
            for (ControlFlowNode node : controlFlowGraph.getNodes()) {
                Set<Postdominator> nodePostdominatorsBefore = postdominators.getPostdominators(node);
                Set<ControlFlowNode> successors = controlFlowGraph.getSuccessors(node);
                Set<Postdominator> successorPostdominators = postdominators.intersectPostdominators(successors);
                postdominators.setPostdominatorsForNode(node, successorPostdominators);
                postdominators.addSelfAsPostdominator(node);
                Set<Postdominator> nodePostdominatorsAfter = postdominators.getPostdominators(node);
                if (!nodePostdominatorsBefore.equals(nodePostdominatorsAfter)) {
                    postdominatorsUpdated = true;
                }
            }
        }
        return postdominators;
    }

    @Nullable
    public List<ControlFlowNode> getPath() {
        return mPath;
    }

    public int getPathLength() {
        if (mPath != null) {
            return mPath.size();
        }
        return -1;
    }

    @NotNull
    public Postdominator clone() {
        Postdominator clone;
        List<ControlFlowNode> newPath = null;
        if (mPath != null) newPath = new ArrayList<>(mPath);
        try {
            clone = (Postdominator) super.clone();
            clone.mNode = mNode;
            clone.mPath = newPath;
        } catch (CloneNotSupportedException e) {
            clone = new Postdominator(mNode, newPath);
        }
        return clone;
    }

    public void insertInPath(ControlFlowNode newNode) {
        if (mPath != null) {
            mPath.add(0, newNode);
        }
    }

    @NotNull
    public ControlFlowNode getNode() {
        return mNode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Postdominator)) return false;
        Postdominator other = (Postdominator) obj;
        if (other.getNode() == getNode()) {
            if (other.getPath() == null && getPath() == null) return true;
            else if (getPath() != null) {
                return getPath().equals(other.getPath());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mNode, mPath);
    }
}
