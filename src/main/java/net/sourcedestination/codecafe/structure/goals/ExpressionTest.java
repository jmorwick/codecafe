package net.sourcedestination.codecafe.structure.goals;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import net.sourcedestination.codecafe.structure.exercises.MethodlikeExpressionExercise;
import net.sourcedestination.funcles.tuple.Tuple2;

import java.util.logging.Logger;

import static net.sourcedestination.funcles.tuple.Tuple.makeTuple;

public class ExpressionTest extends MethodUnitTest {
    private static final Logger logger = Logger.getLogger(MethodUnitTest.class.getCanonicalName());

    private final boolean hiddenTest;
    private final String shortDescription;
    private final Object expectedOutput;
    private final String initializationCode;

    public ExpressionTest(String id,
                          String shortDescription,
                          String initializationCode,
                          Object expectedOutput,
                          boolean hiddenTest
                          ) {
        super(id,
                "expressionTestMethod",
                hiddenTest,
                "()" + MethodlikeExpressionExercise.determineType(expectedOutput),
                ""+expectedOutput,
                "");
        this.shortDescription = shortDescription;
        this.hiddenTest = hiddenTest;
        this.initializationCode = initializationCode;
        this.expectedOutput = expectedOutput;
    }

    @Override
    public String getType() { return "expression-test"; }

    @Override
    public String getDescription() { return hiddenTest ? "<hidden test>" : shortDescription; }

    @Override  public String getLongDescription() {
        return hiddenTest ?
            "<hidden test>" :
            "expected output: " + expectedOutput;
    }

    @Override
    public Tuple2<Double,String> completionPercentage(JShellExerciseTool tool) {
        tool.silentlyExecuteCodeSnippet(initializationCode);
        var expectedOutputType = MethodlikeExpressionExercise.determineType(expectedOutput);
        var signature = "()" + expectedOutputType;
        var js = tool.getShell();
        // check method has correct signature
        if(!js.methods().anyMatch(m -> m.name().equals("expressionTestMethod") &&
                m.signature().equals(signature)))
            return makeTuple(0.0, "Expression had the wrong type, should be " + expectedOutputType);

        return super.completionPercentage(tool);

    }
}
