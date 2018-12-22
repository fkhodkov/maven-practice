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

public final class ClassCounterTest {
  private final Path target = Paths.get("target/test-classes/project-to-test/");
  private final Path classesDir = target.resolve("target/classes/");
  private final Path dir1 = classesDir.resolve("dir1");
  private final Path dir2 = classesDir.resolve("dir2");

  private static final int GENERATOR = 2;
  private static final int MAX_NOT_ENOUGH = 2 * GENERATOR;
  private static final int MIN_ENOUGH = MAX_NOT_ENOUGH + 1;
  private static final int INNER_CLASSES = 3;

  private ClassCounter classCounter;

  @Rule
  public MojoRule rule = new MojoRule() {
    @Override
    protected void before() throws Throwable {
      assertNotNull(target);
      assertTrue(Files.exists(target));
      Files.createDirectories(classesDir);
      assertTrue(Files.exists(classesDir));
      classCounter = (ClassCounter) rule.lookupConfiguredMojo(target.toFile(), "countClasses");
      assertNotNull(classCounter);
      classCounter.setThrowIfFailed(true);
      classCounter.setMinClassNumber(MIN_ENOUGH);
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

  private void doFiles(final Path dir, final int outer, final Map<Integer, Integer> inners,
                       final boolean create) throws IOException {
    for (int i = 0; i < outer; i++) {
      if (create) {
        Files.createFile(dir.resolve(String.format("class%d.class", i)));
      } else {
        Files.delete(dir.resolve(String.format("class%d.class", i)));
      }
      if (inners == null) {
        continue;
      }
      Integer inner = inners.get(i);
      if (inner == null) {
        continue;
      }
      for (int j = 0; j < inner; j++) {
        if (create) {
          Files.createFile(dir.resolve(String.format("class%d$inner%d.class", i, j)));
        } else {
          Files.delete(dir.resolve(String.format("class%d$inner%d.class", i, j)));
        }
      }
    }
  }

  protected void testClasses(final Path dir, final int outer, final Map<Integer, Integer> inners)
      throws MojoExecutionException, IOException {
    try {
      doFiles(dir, outer, inners, true);
      classCounter.execute();
    } finally {
      doFiles(dir, outer, inners, false);
    }
  }

  protected void testClasses(final Path dir, final int outer) throws MojoExecutionException,
                                                                     IOException {
    testClasses(dir, outer, null);
  }

  /**
   * When we have no pseudo-class files, exception should be thrown.
   *
   * @throws IOException             Actually, it should never be thrown as no files actually being
   *                                 created.
   * @throws MojoExecutionException  Expected to throw as the number of classes is less than
   *                                 required.
   */
  @Test(expected = MojoExecutionException.class)
  public void testEmpty() throws MojoExecutionException, IOException {
    testClasses(classesDir, 0);
  }

  /**
   * When we have less than MIN_ENOUGH pseudo-class files, exception should be thrown.
   *
   * @throws IOException             If fail to create or delete pseudo-class files.
   * @throws MojoExecutionException  Expected to throw as the number of classes is less than
   *                                 required.
   */
  @Test(expected = MojoExecutionException.class)
  public void testNotEnough() throws MojoExecutionException, IOException {
    testClasses(classesDir, MAX_NOT_ENOUGH);
  }

  /**
   * When we have less than MIN_ENOUGH pseudo-class files, exception should be thrown.
   * Only outer "classes" is counted.
   *
   * @throws IOException             If fail to create or delete pseudo-class files.
   * @throws MojoExecutionException  Expected to throw as the number of classes is less than
   *                                 required.
   */
  @Test(expected = MojoExecutionException.class)
  public void testNotEnoughWithInners() throws MojoExecutionException, IOException {
    Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
    inners.put(0, INNER_CLASSES - 1);
    inners.put(1, INNER_CLASSES + 1);
    testClasses(classesDir, MAX_NOT_ENOUGH, inners);
  }

  /**
   * When we have less than MIN_ENOUGH pseudo-class files, exception should be thrown.
   * This should be true if pseudo-class files is in several subdirectories.
   * Only outer classes is counted.
   *
   * @throws IOException             If fail to create or delete pseudo-class files.
   * @throws MojoExecutionException  Expected to throw as the number of classes is less than
   *                                 required.
   */
  @Test(expected = MojoExecutionException.class)
  public void testNotEnoughWithInnersMultiDir() throws MojoExecutionException, IOException {
    Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
    inners.put(0, INNER_CLASSES - 1);
    inners.put(1, INNER_CLASSES + 1);
    try {
      Files.createDirectory(dir1);
      Files.createDirectory(dir2);
      doFiles(dir1, GENERATOR, inners, true);
      doFiles(dir2, GENERATOR, inners, true);
      classCounter.execute();
    } finally {
      doFiles(dir1, GENERATOR, inners, false);
      doFiles(dir2, GENERATOR, inners, false);
      Files.delete(dir1);
      Files.delete(dir2);
    }
  }

  /**
   * When we have MIN_ENOUGH pseudo-class files, exception should not be thrown.
   *
   * @throws IOException             If fail to create or delete pseudo-class files.
   * @throws MojoExecutionException  Not expected to throw as we have the required number of
   *                                 classes.
   */
  @Test
  public void testEnough() throws MojoExecutionException, IOException {
    testClasses(classesDir, MIN_ENOUGH);
  }

  /**
   * When we have MIN_ENOUGH pseudo-class files, exception should not be thrown.
   * Only outer classes is counted.
   *
   * @throws IOException             If fail to create or delete pseudo-class files.
   * @throws MojoExecutionException  Not expected to throw as we have the required number of
   *                                 classes.
   */
  @Test
  public void testEnoughWithInners() throws MojoExecutionException, IOException {
    Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
    inners.put(0, INNER_CLASSES - 1);
    inners.put(1, INNER_CLASSES + 1);
    testClasses(classesDir, MIN_ENOUGH, inners);
  }

  /**
   * When we have MIN_ENOUGH pseudo-class files, exception should not be thrown.
   * This should be true if pseudo-class files is in several subdirectories.
   * Only outer classes is counted.
   *
   * @throws IOException             If fail to create or delete pseudo-class files.
   * @throws MojoExecutionException  Not expected to throw as we have the required number of
   *                                 classes.
   */
  @Test
  public void testEnoughWithInnersMultiDir() throws MojoExecutionException, IOException {
    Map<Integer, Integer> inners = new HashMap<Integer, Integer>();
    inners.put(0, INNER_CLASSES - 1);
    inners.put(1, INNER_CLASSES + 1);
    try {
      Files.createDirectory(dir1);
      Files.createDirectory(dir2);
      doFiles(dir1, GENERATOR, inners, true);
      doFiles(dir2, GENERATOR + 1, inners, true);
      classCounter.execute();
    } finally {
      doFiles(dir1, GENERATOR, inners, false);
      doFiles(dir2, GENERATOR + 1, inners, false);
      Files.delete(dir1);
      Files.delete(dir2);
    }
  }
}
