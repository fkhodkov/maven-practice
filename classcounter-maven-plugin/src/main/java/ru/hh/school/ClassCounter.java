package ru.hh.school;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Formatter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Counts compiled classes.  Checks that the number is not less than specified
 * by minClassNumber parameter.  Throws an exception if previous condition is
 * violated and throwIfFailed parameter is set to true.
 */
@Mojo(name = "countClasses", defaultPhase = LifecyclePhase.VALIDATE)
public class ClassCounter extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project}", property="targetProject", required = true)
    private MavenProject project;

    private Path dir;
    protected void setDir (FileSystem fs) {
        dir = fs.getPath(project.getBuild().getOutputDirectory());
    }

    @Parameter(defaultValue = "10", property = "minClassNumber", required = true)
    private int minClassNumber;
    protected void setMinClassNumber(int newMinClassNumber) {
        minClassNumber = newMinClassNumber;
    }

    @Parameter(defaultValue = "false", property = "throwIfFailed", required = true)
    private boolean throwIfFailed;
    protected void setThrowIfFailed(boolean newThrowIfFailed) {
        throwIfFailed = newThrowIfFailed;
    }

    private int outerClasses = 0;
    private int innerClasses = 0;
    private int nonClasses = 0;

    private final Log log = getLog();

    public void execute() throws MojoExecutionException {
        if (dir == null)
            setDir(FileSystems.getDefault());
        try {
            countClasses(dir);
            reportClassInfo();
            if (outerClasses < minClassNumber)
                reportNotEnoughClasses();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot process classes dir!", e);
        }
    }

    private void countClasses(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    countClasses(path);
                } else {
                    String pathString = path.toString();
                    if (pathString.endsWith(".class")) {
                        if (pathString.toString().contains("$")) {
                            innerClasses++;
                        } else {
                            outerClasses++;
                        }
                    } else {
                        log.warn("Non-class file in class directory: " + path.toString());
                        nonClasses++;
                    }
                }
            }
        }
    }

    private void reportClassInfo() {
        int totalClassNumber = outerClasses + innerClasses;
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("Found %d classes", totalClassNumber);
        if (innerClasses > 0)
            fmt.format(", including %d outer and %d inner", outerClasses, innerClasses);
        if (nonClasses > 0)
            fmt.format("\nAlso found %d non-class files in class directory", nonClasses);
        log.info(sb.toString());
    }

    private void reportNotEnoughClasses () throws MojoExecutionException {
        String msg = String.format
            ("Not enought classes: only %d top-level classes found while %d is required",
             outerClasses, minClassNumber);
        if (throwIfFailed) {
            getLog().error(msg);
            throw new MojoExecutionException(msg);
        } else {
            getLog().warn(msg);
        }
    }
}
