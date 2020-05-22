package Compiler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static Compiler.LLVMTest.PrepareTestFile;
import static org.junit.Assert.assertEquals;

public class DebugSingle {
    private MXCC mxcc;

    public int RunTestFile(String name) throws IOException, InterruptedException {
        String rootDir = "/home/yujie6/Documents/Compiler/MX-Compiler/src/test/resources/";
        String fileName = "codegen/" + name;
        System.out.println("Ready to compile \'" + name + "\'");
        int exitCode = PrepareTestFile(rootDir + fileName);
        mxcc.compile(rootDir + fileName, 1);
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(rootDir));
        builder.command("bash", "test.sh");
        Process process = builder.start();
        return process.waitFor();
    }

    @Before
    public void initialize() {
        this.mxcc = new MXCC();
    }

    @Test
    public void debugCustomTest() throws IOException, InterruptedException {
        mxcc = new MXCC();
        int diffResult = RunTestFile("t58.mx");
        assertEquals(diffResult, 0);
    }

}
