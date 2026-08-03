/*
 * Copyright 2023-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import rife.bld.BaseProject;
import rife.bld.dependencies.Scope;
import rife.bld.extension.checkstyle.OutputFormat;
import rife.bld.extension.tools.CollectionTools;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.PathTools;
import rife.bld.extension.tools.TextTools;
import rife.bld.operations.AbstractProcessOperation;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static code analysis operation using <a href="https://checkstyle.sourceforge.io/">Checkstyle</a>.
 * <p>
 * Provides a fluent API to configure and execute Checkstyle against Java source files.
 * <p>
 * Example usage:
 * <pre>
 * new CheckstyleOperation()
 *     .fromProject(project)
 *     .configurationFile("config/checkstyle.xml")
 *     .format(OutputFormat.XML)
 *     .outputPath("build/checkstyle.xml")
 *     .execute();
 * </pre>
 *
 * @author <a href="https://erik.thauvin.net">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Builder pattern intentionally exposes mutable collections"
)
@NullMarked
public class CheckstyleOperation extends AbstractProcessOperation<CheckstyleOperation> {

    private static final String EXCLUDE = "exclude";
    private static final String EXCLUDE_REGEX = "excludeRegex";
    private static final String SOURCE_DIR = "sourceDir";
    private static final Logger logger = Logger.getLogger(CheckstyleOperation.class.getName());
    private final List<String> excludeRegex_ = new ArrayList<>();
    private final List<File> exclude_ = new ArrayList<>();
    private final Map<String, String> options_ = new HashMap<>();
    private final Set<File> sourceDir_ = new LinkedHashSet<>();

    private @Nullable BaseProject project_;

    /**
     * Part of the {@link #execute} operation, constructs the command list to use for building the process.
     *
     * @return the list of command arguments
     * @throws NullPointerException if {@code project_} is {@code null}
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        ObjectTools.requireNonNull(project_, "project");

        List<String> args = new ArrayList<>();

        args.add(javaTool());

        args.add("-cp");
        args.add(PathTools.joinClasspath(
                project_.dependencyClasspathJars(Scope.test, "com.puppycrawl.tools", "checkstyle")));
        args.add("com.puppycrawl.tools.checkstyle.Main");

        options_.forEach((k, v) -> {
            args.add(k);
            if (TextTools.isNotEmpty(v)) {
                args.add(v);
            }
        });

        if (!exclude_.isEmpty()) {
            for (var e : exclude_) {
                args.add("-e");
                args.add(e.getAbsolutePath());
            }
        }

        if (!excludeRegex_.isEmpty()) {
            excludeRegex_.forEach(e -> {
                args.add("-x");
                args.add(e);
            });
        }

        if (!sourceDir_.isEmpty()) {
            args.addAll(sourceDir_.stream().map(File::getAbsolutePath).toList());
        }

        if (!silent() && logger.isLoggable(Level.FINE)) {
            logger.fine(PathTools.formatCommandLine(args));
        }

        return args;
    }

    /**
     * Configures the operation from a {@link BaseProject}.
     * <p>
     * Sets the {@link #sourceDir() source directories} to the project's
     * {@link BaseProject#srcMainJavaDirectory() main} and
     * {@link BaseProject#srcTestJavaDirectory() test} Java source directories, if not already set.
     *
     * @param project the project to configure from
     * @return this operation instance
     * @throws NullPointerException if {@code project} is {@code null}
     */
    @Override
    public CheckstyleOperation fromProject(BaseProject project) {
        project_ = ObjectTools.requireNonNull(project, "fromProject");
        if (sourceDir_.isEmpty()) {
            sourceDir_.addAll(List.of(project_.srcMainJavaDirectory(), project_.srcTestJavaDirectory()));
        }
        return this;
    }

    /**
     * Shows Abstract Syntax Tree (AST) branches that match the given XPath query.
     * Corresponds to the {@code -b} option.
     *
     * @param xPathQuery the XPath query or empty
     * @return this operation instance
     * @throws NullPointerException     if {@code xPathQuery} is null
     * @throws IllegalArgumentException if {@code xPathQuery} is blank
     */

    public CheckstyleOperation branchMatchingXpath(String xPathQuery) {
        return opt("branch matching xpath", "-b", xPathQuery);
    }

    /**
     * Specifies the location of the Checkstyle configuration file.
     * <p>
     * The location can be a filesystem path or a name passed to
     * {@link ClassLoader#getResource(String) ClassLoader.getResource()}.
     * Corresponds to the {@code -c} option.
     *
     * @param file the configuration file path
     * @return this operation instance
     * @throws NullPointerException     if {@code file} is {@code null}
     * @throws IllegalArgumentException if {@code file} is blank
     */

    public CheckstyleOperation configurationFile(String file) {
        TextTools.requireNotBlank(file, "configurationFile");
        return optFile("configuration file", "-c", new File(file));
    }

    /**
     * Specifies the location of the Checkstyle configuration file.
     * Corresponds to the {@code -c} option.
     *
     * @param file the configuration file
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */

    public CheckstyleOperation configurationFile(File file) {
        ObjectTools.requireNonNull(file, "configurationFile");
        return optFile("configuration file", "-c", file);
    }

    /**
     * Specifies the location of the Checkstyle configuration file.
     * Corresponds to the {@code -c} option.
     *
     * @param file the configuration file path
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */

    public CheckstyleOperation configurationFile(Path file) {
        ObjectTools.requireNonNull(file, "configurationFile");
        return optFile("configuration file", "-c", file.toFile());
    }

    /**
     * Enables or disables debug logging for the Checkstyle utility.
     * Corresponds to the {@code -d} option.
     *
     * @param isDebug {@code true} to enable debug logging, {@code false} to disable
     * @return this operation instance
     */

    public CheckstyleOperation debug(boolean isDebug) {
        return optBool("-d", isDebug);
    }

    /**
     * Retrieves the list of files and directories excluded from analysis.
     *
     * @return a mutable list of excluded files
     * @since 1.1.0
     */

    public List<File> exclude() {
        return exclude_;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     *
     * @param paths the paths to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is blank, or contains blank elements
     */

    public CheckstyleOperation exclude(String... paths) {
        TextTools.requireNotBlank(EXCLUDE, paths);
        exclude_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     *
     * @param paths the files to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     */

    public CheckstyleOperation exclude(File... paths) {
        ObjectTools.requireNotEmpty(paths, EXCLUDE);
        exclude_.addAll(List.of(paths));
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     *
     * @param paths the paths to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     */

    public CheckstyleOperation exclude(Path... paths) {
        ObjectTools.requireNotEmpty(paths, EXCLUDE);
        exclude_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     *
     * @param paths the collection of files to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     */

    public CheckstyleOperation exclude(Collection<File> paths) {
        ObjectTools.requireNotEmpty(paths, EXCLUDE);
        exclude_.addAll(paths);
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     *
     * @param paths the collection of paths to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty
     */

    public CheckstyleOperation excludePaths(Collection<Path> paths) {
        ObjectTools.requireNotEmpty(paths, EXCLUDE);
        exclude_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * Specifies directory or file patterns to exclude from Checkstyle analysis.
     *
     * @param patterns the regex patterns to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code patterns} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code patterns} is empty, or contains empty elements
     */

    public CheckstyleOperation excludeRegex(String... patterns) {
        TextTools.requireNotBlank(EXCLUDE_REGEX, patterns);
        excludeRegex_.addAll(List.of(patterns));
        return this;
    }

    /**
     * Specifies directory or file patterns to exclude from Checkstyle analysis.
     *
     * @param patterns the collection of regex patterns to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code patterns} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code patterns} is empty, or contains empty elements
     */

    public CheckstyleOperation excludeRegex(Collection<String> patterns) {
        TextTools.requireNotBlank(patterns, EXCLUDE_REGEX);
        excludeRegex_.addAll(patterns);
        return this;
    }

    /**
     * Retrieves the list of regex patterns excluded from analysis.
     *
     * @return a mutable list of exclude regex patterns
     */

    public List<String> excludeRegex() {
        return excludeRegex_;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     *
     * @param paths the collection of path strings to exclude
     * @return this operation instance
     * @throws NullPointerException     if {@code paths} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code paths} is empty, or contains empty elements
     */

    public CheckstyleOperation excludeStrings(Collection<String> paths) {
        TextTools.requireNotBlank(paths, EXCLUDE);
        exclude_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * Allows ignored modules to be executed.
     * Corresponds to the {@code -E} option.
     *
     * @param isAllowIgnoreModules {@code true} to execute ignored modules, {@code false} to skip them
     * @return this operation instance
     */

    public CheckstyleOperation executeIgnoredModules(boolean isAllowIgnoreModules) {
        return optBool("-E", isAllowIgnoreModules);
    }

    /**
     * Specifies the output format for Checkstyle results.
     * Corresponds to the {@code -f} option.
     *
     * @param format the output format
     * @return this operation instance
     * @throws NullPointerException if {@code format} is {@code null}
     */

    public CheckstyleOperation format(OutputFormat format) {
        ObjectTools.requireNonNull(format, "format");
        options_.put("-f", format.toString());
        return this;
    }

    /**
     * Generates suppression XML files for checks and files.
     * Corresponds to the {@code -G} option.
     *
     * @param generateChecksAndFileSuppression {@code true} to generate suppression XML, {@code false} otherwise
     * @return this operation instance
     */

    public CheckstyleOperation generateChecksAndFileSuppression(boolean generateChecksAndFileSuppression) {
        return optBool("-G", generateChecksAndFileSuppression);
    }

    /**
     * Generates XPath suppression XML.
     * Corresponds to the {@code -g} option.
     *
     * @param xPathSuppression {@code true} to generate XPath suppression XML, {@code false} otherwise
     * @return this operation instance
     */

    public CheckstyleOperation generateXpathSuppression(boolean xPathSuppression) {
        return optBool("-g", xPathSuppression);
    }

    /**
     * Prints the Javadoc parse tree.
     * Corresponds to the {@code -j} option.
     *
     * @param isTree {@code true} to print the Javadoc tree, {@code false} otherwise
     * @return this operation instance
     */

    public CheckstyleOperation javadocTree(boolean isTree) {
        return optBool("-j", isTree);
    }

    /**
     * Returns the mutable map of command line options.
     * <p>
     * Modifying this map bypasses validation. Use with care. Intended for advanced
     * configuration and passing options not exposed by the fluent API.
     *
     * @return the mutable options map
     */

    public Map<String, String> options() {
        return options_;
    }

    /**
     * Sets the output file for Checkstyle results.
     * Corresponds to the {@code -o} option.
     *
     * @param file the output file path
     * @return this operation instance
     * @throws NullPointerException     if {@code file} is {@code null}
     * @throws IllegalArgumentException if {@code file} is blank
     */

    public CheckstyleOperation outputPath(String file) {
        TextTools.requireNotBlank(file, "outputPath");
        return optFile("output path", "-o", new File(file));
    }

    /**
     * Sets the output file for Checkstyle results.
     * Corresponds to the {@code -o} option.
     *
     * @param file the output file
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */

    public CheckstyleOperation outputPath(File file) {
        ObjectTools.requireNonNull(file, "outputPath");
        return optFile("output path", "-o", file);
    }

    /**
     * Sets the output file for Checkstyle results.
     * Corresponds to the {@code -o} option.
     *
     * @param file the output file path
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */

    public CheckstyleOperation outputPath(Path file) {
        ObjectTools.requireNonNull(file, "outputPath");
        return optFile("output path", "-o", file.toFile());
    }

    /**
     * Sets the properties file for Checkstyle.
     * Corresponds to the {@code -p} option.
     *
     * @param file the properties file path
     * @return this operation instance
     * @throws NullPointerException     if {@code file} is {@code null}
     * @throws IllegalArgumentException if {@code file} is blank
     */

    public CheckstyleOperation propertiesFile(String file) {
        TextTools.requireNotBlank(file, "propertiesFiles");
        return optFile("properties file", "-p", new File(file));
    }

    /**
     * Sets the properties file for Checkstyle.
     * Corresponds to the {@code -p} option.
     *
     * @param file the properties file
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */

    public CheckstyleOperation propertiesFile(File file) {
        ObjectTools.requireNonNull(file, "propertiesFile");
        return optFile("properties file", "-p", file);
    }

    /**
     * Sets the properties file for Checkstyle.
     * Corresponds to the {@code -p} option.
     *
     * @param file the properties file path
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */

    public CheckstyleOperation propertiesFile(Path file) {
        ObjectTools.requireNonNull(file, "propertiesFile");
        return optFile("properties file", "-p", file.toFile());
    }

    /**
     * Specifies source directories to analyze.
     *
     * @param dirs the directory paths
     * @return this operation instance
     * @throws NullPointerException     if {@code dirs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code dirs} is blank, or contains blank elements
     */

    public CheckstyleOperation sourceDir(String... dirs) {
        TextTools.requireNotBlank(SOURCE_DIR, dirs);
        sourceDir_.addAll(CollectionTools.combineStringsToFiles(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze.
     *
     * @param dirs the directories
     * @return this operation instance
     * @throws NullPointerException     if {@code dirs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code dirs} is empty
     */

    public CheckstyleOperation sourceDir(File... dirs) {
        ObjectTools.requireNotEmpty(dirs, SOURCE_DIR);
        sourceDir_.addAll(List.of(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze.
     *
     * @param dirs the directory paths
     * @return this operation instance
     * @throws NullPointerException     if {@code dirs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code dirs} is empty
     */

    public CheckstyleOperation sourceDir(Path... dirs) {
        ObjectTools.requireNotEmpty(dirs, SOURCE_DIR);
        sourceDir_.addAll(CollectionTools.combinePathsToFiles(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze.
     *
     * @param dirs the collection of directories
     * @return this operation instance
     * @throws NullPointerException     if {@code dirs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code dirs} is empty
     */

    public CheckstyleOperation sourceDir(Collection<File> dirs) {
        ObjectTools.requireNotEmpty(dirs, SOURCE_DIR);
        sourceDir_.addAll(dirs);
        return this;
    }

    /**
     * Returns the set of source directories to analyze.
     *
     * @return a mutable set of source directories
     */

    public Set<File> sourceDir() {
        return sourceDir_;
    }

    /**
     * Specifies source directories to analyze.
     *
     * @param dirs the collection of directory paths
     * @return this operation instance
     * @throws NullPointerException     if {@code dirs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code dirs} is empty
     */

    public CheckstyleOperation sourceDirPaths(Collection<Path> dirs) {
        ObjectTools.requireNotEmpty(dirs, SOURCE_DIR);
        sourceDir_.addAll(CollectionTools.combinePathsToFiles(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze.
     *
     * @param dirs the collection of directory path strings
     * @return this operation instance
     * @throws NullPointerException     if {@code dirs} is {@code null} or contains {@code null} elements
     * @throws IllegalArgumentException if {@code dirs} is empty, or contains empty elements
     */

    public CheckstyleOperation sourceDirStrings(Collection<String> dirs) {
        TextTools.requireNotBlank(dirs, SOURCE_DIR);
        sourceDir_.addAll(CollectionTools.combineStringsToFiles(dirs));
        return this;
    }

    /**
     * Prints XPath suppressions for a specific line and column.
     *
     * @param line   the line number
     * @param column the column number
     * @return this operation instance
     * @throws IllegalArgumentException if {@code line} or {@code column} is negative
     */

    public CheckstyleOperation suppressionLineColumnNumber(int line, int column) {
        ObjectTools.requireNonNegative(line, "suppression line");
        ObjectTools.requireNonNegative(column, "suppression column");
        options_.put("-s", line + ":" + column);
        return this;
    }

    /**
     * Sets the tab width for column reporting.
     * <p>
     * A value of {@code 0} disables tab expansion, causing tabs to be counted as a single character.
     * Corresponds to the {@code -w} option. Default is 8 if not set.
     *
     * @param length the tab width, must not be negative
     * @return this operation instance
     * @throws IllegalArgumentException if {@code length} is negative
     */

    public CheckstyleOperation tabWidth(int length) {
        ObjectTools.requireNonNegative(length, "tabWidth");
        options_.put("-w", String.valueOf(length));
        return this;
    }

    /**
     * Prints the Abstract Syntax Tree without comments.
     * Corresponds to the {@code -t} option.
     *
     * @param isTree {@code true} to print the AST, {@code false} otherwise
     * @return this operation instance
     */

    public CheckstyleOperation tree(boolean isTree) {
        return optBool("-t", isTree);
    }

    /**
     * Prints the Abstract Syntax Tree with comment nodes included.
     * Corresponds to the {@code -T} option.
     *
     * @param isTree {@code true} to print the AST with comments, {@code false} otherwise
     * @return this operation instance
     */

    public CheckstyleOperation treeWithComments(boolean isTree) {
        return optBool("-T", isTree);
    }

    /**
     * Prints the Abstract Syntax Tree with Javadoc nodes included.
     * Corresponds to the {@code -J} option.
     *
     * @param isTree {@code true} to print the AST with Javadoc, {@code false} otherwise
     * @return this operation instance
     */

    public CheckstyleOperation treeWithJavadoc(boolean isTree) {
        return optBool("-J", isTree);
    }

    private CheckstyleOperation opt(String label, String key, String value) {
        TextTools.requireNotBlank(value, label);
        options_.put(key, value);
        return this;
    }

    private CheckstyleOperation optBool(String key, boolean enable) {
        if (enable) {
            options_.put(key, "");
        } else {
            options_.remove(key);
        }
        return this;
    }

    private CheckstyleOperation optFile(String label, String key, File file) {
        ObjectTools.requireNonNull(file, label);
        return opt(label, key, file.getAbsolutePath());
    }
}