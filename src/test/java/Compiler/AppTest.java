/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Compiler;

import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class AppTest {

    public int PrepareTestFile(String path) throws IOException {
        InputStream is = new FileInputStream(path);

        String text = IOUtils.toString(is, StandardCharsets.UTF_8);

        String meta = text.substring(text.indexOf("/*"), text.indexOf("*/"));
        int start = meta.lastIndexOf("=== input ===");
        int end = meta.indexOf("=== end ===");
        assertNotEquals(start, -1);
        assertNotEquals(end, -1);
        String input = meta.substring(start + "=== input ===".length() + 1, end);
        BufferedWriter bufw1 = new BufferedWriter(new FileWriter("/tmp/test.in"));
        bufw1.write(input);
        bufw1.flush();

        start = meta.lastIndexOf("=== output ===");
        end = meta.lastIndexOf("=== end ===");
        assertNotEquals(start, -1);
        assertNotEquals(end, -1);
        String output = meta.substring(start + "=== output ===".length() + 1, end);
        BufferedWriter bufw2 = new BufferedWriter(new FileWriter("/tmp/test.out"));
        bufw2.write(output);
        bufw2.flush();

        start = meta.indexOf("ExitCode:") + "ExitCode:".length() + 1;
        end = meta.indexOf("InstLimit:") - 1;
        String exit = meta.substring(start, end);
        return Integer.parseInt(exit);
    }

    public int RunTestFile(String name) throws IOException, InterruptedException {
        App mxcc = new App();
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

    @Test
    public void test2dArray() throws IOException, InterruptedException {
        int diffResult = RunTestFile("t3.mx");
        assertEquals(diffResult, 0);
    }

    @Test
    public void testArrayInit() throws IOException, InterruptedException {
        int diffResult = RunTestFile("t7.mx");
        assertEquals(diffResult, 0);
    }

    @Test
    public void testExpr() throws IOException, InterruptedException {
        int diffResult = RunTestFile("t6.mx");
        assertEquals(diffResult, 0);
    }

    @Test
    public void testBasic_e1_to_e5() throws IOException, InterruptedException {
        for (int i = 1; i <= 5; i++) {
            String name = "e" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_e6_to_e10() throws IOException, InterruptedException {
        for (int i = 6; i <= 10; i++) {
            String name = "e" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t11_to_t15() throws IOException, InterruptedException {
        for (int i = 11; i <= 15; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t16_to_t20() throws IOException, InterruptedException {
        for (int i = 16; i <= 20; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t21_to_t25() throws IOException, InterruptedException {
        for (int i = 21; i <= 25; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t26_to_t30() throws IOException, InterruptedException {
        for (int i = 26; i <= 30; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t31_to_t35() throws IOException, InterruptedException {
        for (int i = 31; i <= 35; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t36_to_t40() throws IOException, InterruptedException {
        for (int i = 36; i <= 40; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t41_to_t45() throws IOException, InterruptedException {
        for (int i = 41; i <= 45; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t46_to_t50() throws IOException, InterruptedException {
        for (int i = 46; i <= 50; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t51_to_t55() throws IOException, InterruptedException {
        for (int i = 51; i <= 55; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t56_to_t60() throws IOException, InterruptedException {
        for (int i = 56; i <= 60; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t61_to_t65() throws IOException, InterruptedException {
        for (int i = 61; i <= 65; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }

    @Test
    public void testBasic_t66_to_t68() throws IOException, InterruptedException {
        for (int i = 66; i <= 68; i++) {
            String name = "t" + i + ".mx";
            int diffResult = RunTestFile(name);
            assertEquals(diffResult, 0);
        }
    }
}
