import com.intellij.psi.PsiStatement;
import com.intellij.psi.controlFlow.SimpleInstruction;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Based on {@link com.intellij.psi.controlFlow.ControlFlowImpl}.
 *
 * @author andrewhead
 */
public class ControlFlowGraph {

    private List<ControlFlowNode> mNodes = new ArrayList<>();
    private Map<ControlFlowNode, Set<ControlFlowNode>> mNext = new HashMap<>();
    private Map<ControlFlowNode, Set<ControlFlowNode>> mPrevious = new HashMap<>();

    public void addNode(@NotNull ControlFlowNode node) {
        mNodes.add(node);
    }

    public void addEdge(@NotNull ControlFlowNode from, @NotNull ControlFlowNode to) {
        if (!mNext.containsKey(from)) {
            mNext.put(from, new HashSet<>());
        }
        mNext.get(from).add(to);

        if (!mPrevious.containsKey(to)) {
            mPrevious.put(to, new HashSet<>());
        }
        mPrevious.get(to).add(from);
    }

    private int getIndexOfFirstMatchingNode(@NotNull Set<ControlFlowNode> nodes) {
        for (int i = 0; i < mNodes.size(); i++) {
            if (nodes.contains(mNodes.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public void removeNode(@NotNull ControlFlowNode node) {
        mNodes.remove(node);
    }

    public void removeEdge(@NotNull ControlFlowNode from, @NotNull ControlFlowNode to) {
        if (mNext.containsKey(from)) {
            mNext.get(from).remove(to);
        }
        if (mPrevious.containsKey(to)) {
            mPrevious.get(to).remove(from);
        }
    }

    private class StatementNodeMap {
        Map<PsiStatement, Set<ControlFlowNode>> mStatementNodes = new HashMap<>();

        public void addNode(@NotNull PsiStatement statement, @NotNull ControlFlowNode node) {
            if (!mStatementNodes.containsKey(statement)) {
                mStatementNodes.put(statement, new HashSet<>());
            }
            mStatementNodes.get(statement).add(node);
        }

        @Nullable
        public Set<ControlFlowNode> getNodes(PsiStatement statement) {
            return mStatementNodes.get(statement);
        }

        public Set<PsiStatement> getStatements() {
            return mStatementNodes.keySet();
        }
    }

    private StatementNodeMap groupNodesByStatement() {
        StatementNodeMap statementNodes = new StatementNodeMap();
        for (ControlFlowNode node : mNodes) {
            if (node.getInstruction() instanceof SimpleInstruction) {
                PsiStatement statement;
                if (node.getElement() instanceof PsiStatement) {
                    statement = (PsiStatement) node.getElement();
                } else {
                    statement = PsiTreeUtil.getParentOfType(node.getElement(), PsiStatement.class);
                }
                if (statement != null) {
                    statementNodes.addNode(statement, node);
                }
            }
        }
        return statementNodes;
    }

    public void mergeSimpleInstructionsByStatement() {
        StatementNodeMap statementNodes = groupNodesByStatement();
        for (PsiStatement statement : statementNodes.getStatements()) {
            Set<ControlFlowNode> nodesToMerge = statementNodes.getNodes(statement);
            if (nodesToMerge != null) {
                ControlFlowNode newNode = new ControlFlowNode(new StatementInstruction(), statement);
                merge(nodesToMerge, newNode);
            }
        }
    }

    private void redirectPreviousEdges(ControlFlowNode oldTo, ControlFlowNode newTo) {
        Set<ControlFlowNode> predecessors = mPrevious.get(oldTo);
        if (predecessors != null) {
            List<ControlFlowNode> predecessorsCopy = new ArrayList<>(predecessors);
            for (ControlFlowNode predecessor : predecessorsCopy) {
                removeEdge(predecessor, oldTo);
                if (predecessor != newTo) {
                    addEdge(predecessor, newTo);
                }
            }
        }
    }

    private void redirectNextEdges(ControlFlowNode oldFrom, ControlFlowNode newFrom) {
        Set<ControlFlowNode> successors = mNext.get(oldFrom);
        if (successors != null) {
            List<ControlFlowNode> successorsCopy = new ArrayList<>(successors);
            for (ControlFlowNode successor : successorsCopy) {
                removeEdge(oldFrom, successor);
                if (successor != newFrom) {
                    addEdge(newFrom, successor);
                }
            }
        }
    }

    private void merge(@NotNull Set<ControlFlowNode> oldNodes, @NotNull ControlFlowNode newNode) {
        int insertIndex = getIndexOfFirstMatchingNode(oldNodes);
        mNodes.add(insertIndex, newNode);

        for (ControlFlowNode oldNode : oldNodes) {
            redirectPreviousEdges(oldNode, newNode);
            redirectNextEdges(oldNode, newNode);
            removeNode(oldNode);
        }
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
            Set<ControlFlowNode> successors = this.mNext.get(node);
            if (successors != null) {
                buffer.append(" ->");
                List<Integer> nextIndexes = new ArrayList<>();
                for (ControlFlowNode successor : successors) {
                    nextIndexes.add(nodeIndexes.get(successor));
                }
                nextIndexes.sort(Comparator.comparingInt(index -> index));
                for (int nextIndex : nextIndexes) {
                    buffer.append(" " + nextIndex);
                }
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
