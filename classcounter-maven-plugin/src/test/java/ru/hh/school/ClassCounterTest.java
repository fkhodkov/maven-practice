package ru.hh.school;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

public class ClassCounterTest {
    private final Path target = Paths.get("target/test-classes/project-to-test/");
    private final Path classesDir = target.resolve("target/classes/");
    private final Path dir1 = classesDir.resolve("dir1");
    private final Path dir2 = classesDir.resolve("dir2");

    private ClassCounter classCounter;

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
            assertNotNull(target);
            assertTrue(Files.exists(target));
            Files.createDirectories(classesDir);
            assertTrue(Files.exists(classesDir));
            classCounter = (ClassCounter)
                rule.lookupConfiguredMojo(target.toFile(), "countClasses");
            assertNotNull(classCounter);
            classCounter.setThrowIfFailed(true);
            classCounter.setMinClassNumber(5);
        }

        @Override
        protected void after() {
            try {
                Files.delete(classesDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void doFiles(Path dir, int outer, Map<Integer,Integer> inners, boolean create)
        throws IOException {
        for (int i = 0; i < outer; i++) {
            if (create) Files.createFile(dir.resolve(String.format("class%d.class", i)));
            else Files.delete(dir.resolve(String.format("class%d.class", i)));
            if (inners == null) continue;
            Integer inner = inners.get(i);
            if (inner == null) continue;
            for (int j = 0; j < inner; j++)
                if (create) Files.createFile(dir.resolve(String.format("class%d$%d.class", i, j)));
                else Files.delete(dir.resolve(String.format("class%d$%d.class", i, j)));
        }
    }

    protected void testClasses(Path dir, int outer, Map<Integer, Integer> inners)
        throws MojoExecutionException, IOException {
        try {
            doFiles(dir, outer, inners, true);
            classCounter.execute();
        } finally {
            doFiles(dir, outer, inners, false);            
        }
    }

    protected void testClasses(Path dir, int outer) throws MojoExecutionException, IOException {
        testClasses(dir, outer, null);
    }

    @Test(expected = MojoExecutionException.class)
    public void testEmpty() throws MojoExecutionException, IOException {
        testClasses(classesDir, 0);
    }

    @Test(expected = MojoExecutionException.class)
    public void testNotEnough() throws MojoExecutionException, IOException {
        testClasses(classesDir, 4);
    }

    @Test(expected = MojoExecutionException.class)
    public void testNotEnoughWithInners() throws MojoExecutionException, IOException {
        Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
        inners.put(1, 2);
        inners.put(3, 4);
        testClasses(classesDir, 4, inners);
    }

    @Test(expected = MojoExecutionException.class)
    public void testNotEnoughWithInnersMultiDir() throws MojoExecutionException, IOException {
        Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
        inners.put(0, 2);
        inners.put(1, 4);
        try {
            Files.createDirectory(dir1);
            Files.createDirectory(dir2);
            doFiles(dir1, 2, inners, true);
            doFiles(dir2, 2, inners, true);
            classCounter.execute();
        } finally {
            doFiles(dir1, 2, inners, false);
            doFiles(dir2, 2, inners, false);
            Files.delete(dir1);
            Files.delete(dir2); 
        }
    }

    @Test
    public void testEnough() throws MojoExecutionException, IOException {
        testClasses(classesDir, 5);
    }

    @Test
    public void testEnoughWithInners() throws MojoExecutionException, IOException {
        Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
        inners.put(1, 2);
        inners.put(3, 4);
        testClasses(classesDir, 5, inners);
    }

    @Test
    public void testEnoughWithInnersMultiDir() throws MojoExecutionException, IOException {
        Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
        inners.put(0, 2);
        inners.put(1, 4);
        try {
            Files.createDirectory(dir1);
            Files.createDirectory(dir2);
            doFiles(dir1, 2, inners, true);
            doFiles(dir2, 3, inners, true);
            classCounter.execute();
        } finally {
            doFiles(dir1, 2, inners, false);
            doFiles(dir2, 3, inners, false);
            Files.delete(dir1);
            Files.delete(dir2);
        }
    }
}
