import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ControlDependenceEdge {

    private ControlFlowNode mFrom;
    private ControlFlowNode mTo;
    private ControlFlowEdgeType mControlFlowEdgeType;

    public ControlDependenceEdge(ControlFlowNode from, ControlFlowNode to, ControlFlowEdgeType type) {
        mFrom = from;
        mTo = to;
        mControlFlowEdgeType = type;
    }

    @NotNull
    public static Map<ControlFlowNode, Set<ControlDependenceEdge>> getControlDependences(
            @NotNull ControlFlowGraph controlFlowGraph,
            @NotNull ImmediatePostdominatorTree immediatePostdominatorTree,
            @NotNull PostdominatorIndex postdominators) {
        Set<ControlFlowNode> visited = new HashSet<>();
        Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences = new HashMap<>();
        for (ControlFlowNode node : controlFlowGraph.getNodes()) {
            getControlDependences(node, controlFlowGraph, immediatePostdominatorTree, postdominators, dependences,
                    visited);
        }
        return dependences;
    }

    private static void saveDependence(Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences,
                                       ControlFlowNode node, ControlDependenceEdge dependence) {
        if (!dependences.containsKey(node)) {
            dependences.put(node, new HashSet<>());
        }
        dependences.get(node).add(dependence);
    }

    private static void getControlDependences(
            @NotNull ControlFlowNode node, @NotNull ControlFlowGraph controlFlowGraph,
            @NotNull ImmediatePostdominatorTree immediatePostdominatorTree,
            @NotNull PostdominatorIndex postdominatorIndex,
            @NotNull Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences,
            @NotNull Set<ControlFlowNode> visitedNodes) {
        if (visitedNodes.contains(node)) {
            return;
        }
        visitedNodes.add(node);
        getDependencesOnPredecessors(node, controlFlowGraph, immediatePostdominatorTree, dependences);
        Set<ControlFlowNode> children = immediatePostdominatorTree.getChildren(node);
        if (children != null) {
            getChildDependences(children, controlFlowGraph, immediatePostdominatorTree, postdominatorIndex, dependences,
                    visitedNodes);
            getDependencesFromChildren(node, children, postdominatorIndex, dependences);
        }
    }

    private static void getDependencesFromChildren(
            @NotNull ControlFlowNode node, Set<ControlFlowNode> children,
            @NotNull PostdominatorIndex postdominatorIndex,
            @NotNull Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences
    ) {
        for (ControlFlowNode child : children) {
            Set<ControlDependenceEdge> childDependences = dependences.get(child);
            if (childDependences != null) {
                for (ControlDependenceEdge childDependence : childDependences) {
                    if (!postdominatorIndex.hasPostdominator(childDependence.getTo(), node)) {
                        ControlDependenceEdge dependence = childDependence.clone();
                        childDependence.setFrom(node);
                        saveDependence(dependences, node, dependence);
                    }
                }
            }
        }
    }

    private static void getChildDependences(
            @NotNull Set<ControlFlowNode> children, @NotNull ControlFlowGraph controlFlowGraph,
            @NotNull ImmediatePostdominatorTree immediatePostdominatorTree,
            @NotNull PostdominatorIndex postdominatorIndex,
            @NotNull Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences,
            @NotNull Set<ControlFlowNode> visitedNodes) {
        for (ControlFlowNode child : children) {
            getControlDependences(child, controlFlowGraph, immediatePostdominatorTree, postdominatorIndex,
                    dependences, visitedNodes);
        }
    }

    private static void getDependencesOnPredecessors(
            @NotNull ControlFlowNode node, @NotNull ControlFlowGraph controlFlowGraph,
            @NotNull ImmediatePostdominatorTree immediatePostdominatorTree,
            @NotNull Map<ControlFlowNode, Set<ControlDependenceEdge>> dependences) {
        Set<ControlFlowEdge> edgesTo = controlFlowGraph.getEdgesTo(node);
        if (edgesTo != null) {
            for (ControlFlowEdge edge : edgesTo) {
                ControlFlowNode predecessor = edge.getFrom();
                if (immediatePostdominatorTree.getParent(predecessor) != node) {
                    ControlDependenceEdge dependence = new ControlDependenceEdge(node, predecessor, edge.getType());
                    saveDependence(dependences, node, dependence);
                }
            }
        }
    }

    public ControlFlowNode getFrom() {
        return mFrom;
    }

    public void setFrom(ControlFlowNode from) {
        this.mFrom = from;
    }

    public ControlFlowNode getTo() {
        return mTo;
    }

    public ControlFlowEdgeType getControlFlowEdgeType() {
        return mControlFlowEdgeType;
    }

    @Override
    public ControlDependenceEdge clone() {
        ControlDependenceEdge clone;
        try {
            clone = (ControlDependenceEdge) super.clone();
            clone.mFrom = mFrom;
            clone.mTo = mTo;
            clone.mControlFlowEdgeType = mControlFlowEdgeType;
        } catch (CloneNotSupportedException e) {
            clone = new ControlDependenceEdge(getFrom(), getTo(), getControlFlowEdgeType());
        }
        return clone;
    }
}
