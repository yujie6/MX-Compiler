package Compiler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;

import static Compiler.LLVMTest.PrepareTestFile;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class CodegenTest {

    private int TestNumber;
    private String testClass;
    private MXCC mxcc;

    @Before
    public void initialize() {
        this.mxcc = new MXCC();
    }

    public CodegenTest(String testClass, int testNumber) {
        this.testClass = testClass;
        this.TestNumber = testNumber;
    }

    public int RunTestFile(String name) throws IOException, InterruptedException {
        String rootDir = "/home/yujie6/Documents/Compiler/MX-Compiler/src/test/resources/";
        String fileName = "codegen/" + name;
        System.out.println("Ready to compile \'" + name + "\'");
        int exitCode = PrepareTestFile(rootDir + fileName);
        mxcc.compile(rootDir + fileName, 0);
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(rootDir));
        builder.command("bash", "codegen.sh");
        Process process = builder.start();
        return process.waitFor();
    }


    @Test(timeout=1000)
    public void testCodegen() throws IOException, InterruptedException {
        int diffResult = RunTestFile(this.testClass + this.TestNumber + ".mx");
        assertEquals(diffResult, 0);
    }
}
