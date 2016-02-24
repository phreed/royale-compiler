package org.apache.flex.compiler.tools;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by christoferdutz on 20.02.16.
 */
public abstract class BaseProblemGeneratorMojo extends AbstractMojo
{

    private ImmutableSet<String> nonProblemClassesToFilter =
            new ImmutableSet.Builder<String>()
                    .add("DefaultSeverity.java")
                    .add("CompositeProblemFilter.java")
                    .add("FilteredIterator.java")
                    .add("CompilerProblemSeverity.java")
                    .build();

    abstract protected File getInputDirectory();
    abstract protected File getOutputDirectory();
    abstract protected String getOutputFile();

    public void execute() throws MojoExecutionException
    {
        File generatedFile = new File(getOutputDirectory(), getOutputFile());

        if(!generatedFile.getParentFile().exists()) {
            if(!generatedFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Could not create output directory: " + generatedFile.getParentFile());
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(generatedFile, true));
            try {
                printHeader(writer);

                Collection<File> problemClassSourceFiles = getProblemClassSourceFiles(getInputDirectory());
                Iterator<File> problemClassSourceFileIterator = problemClassSourceFiles.iterator();
                while (problemClassSourceFileIterator.hasNext()) {
                    File problemClassSourceFile = problemClassSourceFileIterator.next();
                    printEntry(writer, problemClassSourceFile, !problemClassSourceFileIterator.hasNext());
                }

                printFooter(writer);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Caught Exception while trying to create output file: " + generatedFile);
        }
    }


    protected void printHeader(PrintWriter writer) {
        // Optional.
    }

    abstract protected void printEntry(PrintWriter writer, File source, boolean last);

    protected void printFooter(PrintWriter writer) {
        // Optional.
    }

    /**
     * Recursively search a directory and its sub-directories for .class files.
     * Use the file name to determine what equivalent class name should be
     * available to the current classloader.
     *
     * @param dir - the directory to search
     * @return list of source files found
     */
    protected Collection<File> getProblemClassSourceFiles(File dir)
    {
        Collection<File> problemFiles = FileUtils.listFiles(dir, new String[] { "java" }, true);
        return Collections2.filter(problemFiles,
                new Predicate<File>() {
                    @Override
                    public boolean apply(File input)
                    {
                        return isProblemClass(input);
                    }
                });
    }

    private boolean isProblemClass(File javaFile)
    {
        if (nonProblemClassesToFilter.contains(javaFile.getName()))
            return false;
        final String javaClassName = FilenameUtils.getBaseName(javaFile.getAbsolutePath());
        try
        {
            Collection<String> lines =  Files.readLines(javaFile, Charset.forName("UTF8"));
            for (String line : lines)
            {
                if (line.matches("^\\s*public\\s+final\\s+class\\s+" + javaClassName + "\\s+extends\\s+.*"))
                    return true;
                if (line.matches("^\\s*final\\s+public\\s+class\\s+" + javaClassName + "\\s+extends\\s+.*"))
                    return true;
                if (line.matches("^\\s*public\\s+class\\s+" + javaClassName + "\\s+extends\\s+.*"))
                    return true;
            }
            return false;
        }
        catch (IOException e)
        {
            return false;
        }

    }

}
