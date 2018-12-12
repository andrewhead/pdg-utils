import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Based on {@link com.intellij.psi.controlFlow.ControlFlowImpl}.
 *
 * @author andrewhead
 */
public class ControlFlowGraph {

    private List<ControlFlowNode> mNodes = new ArrayList<>();
    private Map<ControlFlowNode, Set<ControlFlowEdge>> mNext = new HashMap<>();
    private Map<ControlFlowNode, Set<ControlFlowEdge>> mPrevious = new HashMap<>();

    public void addNode(@NotNull ControlFlowNode node) {
        mNodes.add(node);
    }

    public void addEdge(@NotNull ControlFlowNode from, @NotNull ControlFlowNode to, @NotNull ControlFlowEdgeType type) {
        ControlFlowEdge edge = new ControlFlowEdge(from, to, type);

        if (!mNext.containsKey(from)) {
            mNext.put(from, new HashSet<>());
        }
        mNext.get(from).add(edge);

        if (!mPrevious.containsKey(to)) {
            mPrevious.put(to, new HashSet<>());
        }
        mPrevious.get(to).add(edge);
    }

    public void removeNode(@NotNull ControlFlowNode node) {
        mNodes.remove(node);
    }

    public int size() {
        return this.mNodes.size();
    }

    public String toString() {

        Map<ControlFlowNode, Integer> nodeIndexes = new HashMap<>();
        for (int i = 0; i < mNodes.size(); i++) {
            nodeIndexes.put(mNodes.get(i), i);
        }

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < mNodes.size(); i++) {
            ControlFlowNode node = mNodes.get(i);
            buffer.append(Integer.toString(i));
            buffer.append(": ");
            buffer.append(node.getInstruction().getClass().getSimpleName().replace("Instruction", ""));
            Set<ControlFlowEdge> successors = this.mNext.get(node);
            buffer.append(" ->");
            if (successors != null) {
                List<ControlFlowEdge> nextEdges = new ArrayList<>(successors);
                nextEdges.sort(Comparator.comparingInt((edge) -> nodeIndexes.get(edge.getTo())));
                for (ControlFlowEdge nextEdge : nextEdges) {
                    buffer.append(" ").append(nodeIndexes.get(nextEdge.getTo())).append(".").append(nextEdge.getType());
                }
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
