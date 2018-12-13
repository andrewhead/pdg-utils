import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PostdominatorIndex {

    private final Map<ControlFlowNode, Map<ControlFlowNode, Postdominator>> mIndex = new HashMap<>();

    private void registerNode(@NotNull ControlFlowNode node) {
        if (!mIndex.containsKey(node)) {
            mIndex.put(node, new HashMap<>());
        }
    }

    @NotNull
    public Set<ControlFlowNode> getNodes() {
        return mIndex.keySet();
    }

    public void setPostdominator(@NotNull ControlFlowNode node, @NotNull ControlFlowNode postdominatorNode,
                                 @NotNull Postdominator postdominator) {
        registerNode(node);
        mIndex.get(node).put(postdominatorNode, postdominator);
    }

    public void addNullPath(@NotNull ControlFlowNode node, @NotNull ControlFlowNode postdominatorNode) {
        setPostdominator(node, postdominatorNode, new Postdominator(postdominatorNode, null));
    }

    public void addSelfAsPostdominator(@NotNull ControlFlowNode node) {
        setPostdominator(node, node, new Postdominator(node, new ArrayList<>()));
    }

    public boolean hasPostdominator(@NotNull ControlFlowNode node, @NotNull ControlFlowNode postdominatorNode) {
        return mIndex.containsKey(node) && mIndex.get(node).containsKey(postdominatorNode);
    }

    public void setPostdominatorsForNode(@NotNull ControlFlowNode node,
                                         @NotNull Collection<Postdominator> postdominators) {
        registerNode(node);
        mIndex.get(node).clear();
        for (Postdominator postdominator : postdominators) {
            mIndex.get(node).put(postdominator.getNode(), postdominator);
        }
    }

    @NotNull
    Set<Postdominator> getPostdominators(@NotNull ControlFlowNode node) {
        Set<Postdominator> postdominators = new HashSet<>();
        if (mIndex.containsKey(node)) {
            postdominators.addAll(mIndex.get(node).values());
        }
        return postdominators;
    }

    @NotNull
    private Set<Postdominator> getSharedPostdominators(@NotNull Collection<ControlFlowNode> nodes,
                                                       PostdominatorCounter postdominatorCounts,
                                                       ShortestPostdominatorPaths shortestPostdominatorPaths) {
        Set<Postdominator> intersectedPostdominators = new HashSet<>();
        for (ControlFlowNode postdominatorNode : postdominatorCounts.getPostdominatorNodes()) {
            if (postdominatorCounts.getCount(postdominatorNode) == nodes.size()) {
                Postdominator shortestPathPostdominator =
                        shortestPostdominatorPaths.getPostdominatorWithShortestPath(postdominatorNode);
                if (shortestPathPostdominator != null) {
                    intersectedPostdominators.add(shortestPathPostdominator);
                }
            }
        }
        return intersectedPostdominators;
    }

    @NotNull
    public Set<Postdominator> intersectPostdominators(@NotNull Collection<ControlFlowNode> nodes) {
        PostdominatorCounter postdominatorCounts = new PostdominatorCounter();
        ShortestPostdominatorPaths shortestPostdominatorPaths = new ShortestPostdominatorPaths();
        for (ControlFlowNode node : nodes) {
            if (mIndex.containsKey(node)) {
                for (ControlFlowNode postdominatorNode : mIndex.get(node).keySet()) {
                    postdominatorCounts.increment(postdominatorNode);
                    Postdominator postdominator = mIndex.get(node).get(postdominatorNode).clone();
                    postdominator.insertInPath(node);
                    shortestPostdominatorPaths.updateIfShortestPath(postdominatorNode, postdominator);
                }
            }
        }
        return getSharedPostdominators(nodes, postdominatorCounts, shortestPostdominatorPaths);
    }
}
