import com.intellij.psi.controlFlow.*;

public enum ControlFlowEdgeType {
    UNKNOWN("unknown"),
    NORMAL("normal"),
    BRANCH("branch"),
    EXCEPTION("exception");

    private String mReadableName;

    ControlFlowEdgeType(String readableName) {
        mReadableName = readableName;
    }

    public String toString() {
        return mReadableName;
    }

    public static ControlFlowEdgeType getControlFlowEdgeType(Instruction instruction, int index, int nextNumber) {
        if (instruction instanceof ConditionalThrowToInstruction) {
            if (nextNumber == 0) {
                return EXCEPTION;
            } else if (nextNumber == 1) {
                return NORMAL;
            }
        } else if (instruction instanceof ConditionalGoToInstruction) {
            return BRANCH;
        } else if (instruction instanceof SimpleInstruction ||
                instruction instanceof GoToInstruction ||
                instruction instanceof ThrowToInstruction) {
            return ControlFlowEdgeType.NORMAL;
        }
        return UNKNOWN;
    }
}
