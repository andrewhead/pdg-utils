// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.controlFlow.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightCodeInsightTestCase;
import com.intellij.util.containers.IntArrayList;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a regression test for the IntelliJ control flow functionality. Test code was originally copied from the
 * IntelliJ community edition test code.
 * <p>
 * If you are seeing some of these tests fail and the generated control flow graphs look similar to but not exactly the
 * same as the expected control flow graphs, you may need to configure your JUnit test case template to have arguments
 * like these:
 * <p>
 * -ea -Xbootclasspath/p:../out/classes/production/boot
 * -XX:+HeapDumpOnOutOfMemoryError -Xmx512m -XX:MaxPermSize=320m
 * -Didea.system.path=__PATH-TO-INTELLIJ-SOURCE-CODE__/system
 * -Didea.home.path=__PATH-TO-INTELLIJ-SOURCE-CODE__ -Didea.config.path=__PATH-TO-INTELLIJ-SOURCE-CODE__/config
 * -Didea.test.group=ALL_EXCLUDE_DEFINED
 * <p>
 * Without this configuration, control flow graphs may be produced incorrectly because classes used in the example
 * programs can't be resolved to types. See here for more details:
 * <p>
 * http://www.jetbrains.org/intellij/sdk/docs/tutorials/writing_tests_for_plugins/tests_prerequisites.html
 *
 * @author cdr, andrewhead
 */
public class ControlFlowTest extends LightCodeInsightTestCase {
    @NonNls
    private static final String BASE_PATH = "testData/psi/controlFlow";

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
        assertTrue("Selected element: " + element, element instanceof PsiCodeBlock);

        ControlFlow controlFlow = ControlFlowFactory.getInstance(getProject()).getControlFlow(element, policy);
        String result = controlFlow.toString().trim();

        final String expectedFullPath = StringUtil.trimEnd(file.getPath(), ".java") + ".txt";
        VirtualFile expectedFile = LocalFileSystem.getInstance().findFileByPath(expectedFullPath);
        String expected = LoadTextUtil.loadText(expectedFile).toString().trim();
        expected = expected.replaceAll("\r", "");
        assertEquals("Text mismatch (in file " + expectedFullPath + "):\n", expected, result);
    }

    // Not sure why this is failing on some simple tests (like flow3). It looks like the branching, reading, and
    // writing structure is correctly captured. So maybe we should just update the test output.
    private static void doAllTests() throws Exception {
        final String testDirPath = BASE_PATH;
        File testDir = new File(testDirPath);
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

    public void testMethodWithOnlyDoWhileStatementHasExitPoints() throws Exception {
        configureFromFileText("a.java", "public class Foo {\n" +
                "  public void foo() {\n" +
                "    boolean f;\n" +
                "    do {\n" +
                "      f = something();\n" +
                "    } while (f);\n" +
                "  }\n" +
                "}");
        final PsiCodeBlock body = ((PsiJavaFile) getFile()).getClasses()[0].getMethods()[0].getBody();
        ControlFlow flow = ControlFlowFactory.getInstance(getProject()).getControlFlow(body, new LocalsControlFlowPolicy(body), false);
        IntArrayList exitPoints = new IntArrayList();
        ControlFlowUtil.findExitPointsAndStatements(flow, 0, flow.getSize() - 1, exitPoints, ControlFlowUtil.DEFAULT_EXIT_STATEMENTS_CLASSES);
        assertEquals(1, exitPoints.size());
    }
}
