package Compiler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static Compiler.LLVMTest.PrepareTestFile;
import static org.junit.Assert.assertEquals;

public class CodegenSingle {
    private MXCC mxcc;

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

    @Before
    public void initialize() {
        this.mxcc = new MXCC();
    }

    @Test
    public void debugCustomTest() throws IOException, InterruptedException {
        mxcc = new MXCC();
        int diffResult = RunTestFile("t8.mx");
        assertEquals(0, diffResult);
    }

}
