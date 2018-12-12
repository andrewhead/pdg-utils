public class ControlFlowEdge {

    private ControlFlowNode mFrom;
    private ControlFlowNode mTo;
    private ControlFlowEdgeType mType;

    public ControlFlowEdge(ControlFlowNode from, ControlFlowNode to, ControlFlowEdgeType type) {
        mFrom = from;
        mTo = to;
        mType = type;
    }

    public ControlFlowNode getFrom() {
        return mFrom;
    }

    public ControlFlowNode getTo() {
        return mTo;
    }

    public ControlFlowEdgeType getType() {
        return mType;
    }
}
