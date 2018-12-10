import com.intellij.psi.controlFlow.Instruction;
import com.intellij.psi.PsiElement;

class ControlFlowNode {

    private Instruction mInstruction;
    private PsiElement mElement;

    ControlFlowNode(Instruction instruction, PsiElement element) {
        mInstruction = instruction;
        mElement = element;
    }

    Instruction getInstruction() {
        return mInstruction;
    }

    PsiElement getElement() {
        return mElement;
    }
}
