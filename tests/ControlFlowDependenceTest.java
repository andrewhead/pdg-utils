// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be
// found in the LICENSE file.

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.controlFlow.ControlFlow;
import com.intellij.psi.controlFlow.ControlFlowFactory;
import com.intellij.psi.controlFlow.ControlFlowPolicy;
import com.intellij.psi.controlFlow.LocalsOrMyInstanceFieldsControlFlowPolicy;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightCodeInsightTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author andrewhead
 */
public class ControlFlowDependenceTest extends LightCodeInsightTestCase {
    @NonNls
    private static final String TEST_DIR_PATH = "testData/psi/controlDependenceGraph";

    private static void doTestFor(final File file) throws Exception {
        String contents = StringUtil.convertLineSeparators(FileUtil.loadFile(file));
        configureFromFileText(file.getName(), contents);
        // extract factory policy class name
        Pattern pattern = Pattern.compile("^// (\\S*).*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents);
        assertTrue(matcher.matches());
        final String policyClassName = matcher.group(1);
        final ControlFlowPolicy policy;
        if ("LocalsOrMyInstanceFieldsControlFlowPolicy".equals(policyClassName)) {
            policy = LocalsOrMyInstanceFieldsControlFlowPolicy.getInstance();
        } else {
            policy = null;
        }

        final int offset = getEditor().getCaretModel().getOffset();
        PsiElement element = getFile().findElementAt(offset);
        element = PsiTreeUtil.getParentOfType(element, PsiCodeBlock.class, false);
        assertNotNull("Selected element: " + element, element);

        ControlFlow controlFlow = ControlFlowFactory.getInstance(getProject()).getControlFlow(element, policy);
        ControlFlowGraph controlFlowGraph = ControlFlowGraphFactory.getControlFlowGraph(controlFlow);
        ControlDependenceGraph controlDependenceGraph = ControlDependenceGraph.from(controlFlowGraph);
        String result = controlDependenceGraph.toString().trim();

        final String expectedFullPath = StringUtil.trimEnd(file.getPath(), ".java") + ".txt";
        VirtualFile expectedFile = LocalFileSystem.getInstance().findFileByPath(expectedFullPath);
        String expected = LoadTextUtil.loadText(expectedFile).toString().trim();
        expected = expected.replaceAll("\r", "");
        assertEquals("Text mismatch (in file " + expectedFullPath + "):\n", expected, result);
    }

    // Not sure why this is failing on some simple tests (like flow3). It looks like the branching, reading, and
    // writing structure is correctly captured. So maybe we should just update the test output.
    private static void doAllTests() throws Exception {
        File testDir = new File(TEST_DIR_PATH);
        final File[] files = testDir.listFiles((dir, name) -> name.endsWith(".java"));
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            doTestFor(file);

            System.out.print((i + 1) + " ");
        }
    }

    public void test() throws Exception {
        doAllTests();
    }
}
