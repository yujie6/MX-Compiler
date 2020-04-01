package Compiler;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;

import static Compiler.AppTest.PrepareTestFile;
import static org.junit.Assert.assertEquals;

public class DebugSingle {
    private App mxcc;

    public int RunTestFile(String name) throws IOException, InterruptedException {
        String rootDir = "/home/yujie6/Documents/Compiler/MX-Compiler/src/test/resources/";
        String fileName = "codegen/" + name;
        System.out.println("Ready to compile \'" + name + "\'");
        int exitCode = PrepareTestFile(rootDir + fileName);
        mxcc.compile(rootDir + fileName);
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(rootDir));
        builder.command("bash", "test.sh");
        Process process = builder.start();
        return process.waitFor();
    }

    @Before
    public void initialize() {
        this.mxcc = new App();
    }

    @Test
    public void debugCustomTest() throws IOException, InterruptedException {
        mxcc = new App();
        int diffResult = RunTestFile("t59.mx");
        assertEquals(diffResult, 0);
    }

}
