import com.intellij.psi.PsiElement;
import com.intellij.psi.controlFlow.ControlFlow;
import com.intellij.psi.controlFlow.EmptyInstruction;
import com.intellij.psi.controlFlow.Instruction;

import java.util.HashMap;
import java.util.Map;

public class ControlFlowGraphFactory {
    /**
     * Get control flow graph for control flow. Consolidates statements in a control flow.
     */
    public static ControlFlowGraph getControlFlowGraph(ControlFlow controlFlow) {
        ControlFlowGraph controlFlowGraph = new ControlFlowGraph();
        Map<Integer, ControlFlowNode> nodes = addNodes(controlFlowGraph, controlFlow);
        addEdges(controlFlowGraph, controlFlow, nodes);
        return controlFlowGraph;
    }

    /**
     * @return map from control flow instruction index to control flow graph node.
     */
    private static Map<Integer, ControlFlowNode> addNodes(ControlFlowGraph controlFlowGraph, ControlFlow controlFlow) {
        Map<Integer, ControlFlowNode> nodes = new HashMap<>();
        for (int i = 0; i < controlFlow.getInstructions().size(); i++) {
            Instruction instruction = controlFlow.getInstructions().get(i);
            PsiElement element = controlFlow.getElement(i);
            ControlFlowNode node = new ControlFlowNode(instruction, element);
            controlFlowGraph.addNode(node);
            nodes.put(i, node);
        }
        addExitNode(controlFlowGraph, nodes);
        return nodes;
    }

    private static void addExitNode(ControlFlowGraph controlFlowGraph, Map<Integer, ControlFlowNode> nodes) {
        ControlFlowNode node = new ControlFlowNode(EmptyInstruction.INSTANCE, null);
        controlFlowGraph.addNode(node);
        controlFlowGraph.setExitNode(node);
        nodes.put(controlFlowGraph.size() - 1, node);
    }

    private static void addEdges(ControlFlowGraph controlFlowGraph, ControlFlow controlFlow, Map<Integer, ControlFlowNode> nodes) {
        for (int i = 0; i < controlFlow.getInstructions().size(); i++) {
            ControlFlowNode from = nodes.get(i);
            if (from != null) {
                for (int offset = 0; offset < from.getInstruction().nNext(); offset++) {
                    int nextI = from.getInstruction().getNext(i, offset);
                    ControlFlowNode to = nodes.get(nextI);
                    if (to != null) {
                        ControlFlowEdgeType edgeType = ControlFlowEdgeType.getControlFlowEdgeType(from.getInstruction(), i, offset);
                        controlFlowGraph.addEdge(from, to, edgeType);
                    }
                }
            }
        }
    }
}
