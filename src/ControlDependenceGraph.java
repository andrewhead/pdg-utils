import java.util.*;

public class ControlDependenceGraph {

    private final List<ControlFlowNode> mNodes;
    private final Map<ControlFlowNode, Set<ControlDependenceEdge>> mDependences;

    public ControlDependenceGraph(List<ControlFlowNode> nodes,
                                  Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences) {
        mNodes = nodes;
        mDependences = dependences;
    }

    public static ControlDependenceGraph from(ControlFlowGraph controlFlowGraph) {
        /* Based on control dependence algorithm described in "Modern Compiler Implementation in Java", see pages 415
         * 443, and 465. */
        PostdominatorIndex postdominatorIndex = Postdominator.getPostdominators(controlFlowGraph);
        ImmediatePostdominatorTree immediatePostdominatorTree = new ImmediatePostdominatorTree(postdominatorIndex);
        Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences = ControlDependenceEdge
                .getControlDependences(controlFlowGraph, immediatePostdominatorTree, postdominatorIndex);
        return new ControlDependenceGraph(controlFlowGraph.getNodes(), dependences);
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
            Set<ControlDependenceEdge> dependences = this.mDependences.get(node);
            buffer.append(" ->");
            if (dependences != null) {
                List<ControlDependenceEdge> dependenceList = new ArrayList<>(dependences);
                dependenceList.sort(Comparator.comparingInt((dependence) -> nodeIndexes.get(dependence.getTo())));
                for (ControlDependenceEdge dependence : dependenceList) {
                    buffer.append(" ").append(nodeIndexes.get(dependence.getTo())).append(".")
                            .append(dependence.getControlFlowEdgeType());
                }
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
