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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.BaseProject;
import rife.bld.extension.checkstyle.OutputFormat;
import rife.bld.extension.tools.CollectionTools;
import rife.bld.extension.tools.ObjectTools;
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
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional and documented")
public class CheckstyleOperation extends AbstractProcessOperation<CheckstyleOperation> {

    private static final String EXCLUDE_NOT_VALID = "exclude values must not be empty or null";
    private static final String REGEX_NOT_VALID = "exclude regex values must not be empty or null";
    private static final String SOURCE_DIR_NOT_VALID = "source dir values must not be empty or null";
    private static final Logger logger = Logger.getLogger(CheckstyleOperation.class.getName());
    private final List<String> excludeRegex_ = new ArrayList<>();
    private final List<File> exclude_ = new ArrayList<>();
    private final Map<String, String> options_ = new HashMap<>();
    private final Set<File> sourceDir_ = new LinkedHashSet<>();

    private BaseProject project_;

    /**
     * Part of the {@link #execute} operation, constructs the command list to use for building the process.
     *
     * @return the list of command arguments
     * @throws NullPointerException if {@link #fromProject(BaseProject)} was not called
     */
    @Override
    @NonNull
    protected List<String> executeConstructProcessCommandList() {
        Objects.requireNonNull(project_, "project must not be null");

        List<String> args = new ArrayList<>();

        args.add(javaTool());

        args.add("-cp");
        var classpath = String.join(File.pathSeparator,
                new File(project_.libTestDirectory(), "*").getPath(),
                new File(project_.libCompileDirectory(), "*").getPath(),
                project_.buildMainDirectory().getPath(),
                project_.buildTestDirectory().getPath()
        );
        args.add(classpath);
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

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.join(" ", args));
        }

        return args;
    }

    /**
     * Configures the operation from a {@link BaseProject}.
     * <p>
     * Sets the {@link #sourceDir() source directories} to the project's
     * {@link BaseProject#srcMainJavaDirectory() main} and
     * {@link BaseProject#srcTestJavaDirectory() test} Java source directories.
     *
     * @param project the project to configure from, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code project} is {@code null}
     */
    @Override
    @NonNull
    public CheckstyleOperation fromProject(@NonNull BaseProject project) {
        project_ = Objects.requireNonNull(project, "project must not be null");
        sourceDir_.clear();
        sourceDir_.addAll(List.of(project_.srcMainJavaDirectory(), project_.srcTestJavaDirectory()));
        return this;
    }

    /**
     * Shows Abstract Syntax Tree (AST) branches that match the given XPath query.
     * Corresponds to the {@code -b} option.
     *
     * @param xPathQuery the XPath query, must not be {@code null} or empty
     * @return this operation instance
     * @throws IllegalArgumentException if {@code xPathQuery} is {@code null} or empty
     */
    @NonNull
    public CheckstyleOperation branchMatchingXpath(@NonNull String xPathQuery) {
        return opt("branch matching xpath", "-b", xPathQuery);
    }

    /**
     * Specifies the location of the Checkstyle configuration file.
     * <p>
     * The location can be a filesystem path or a name passed to
     * {@link ClassLoader#getResource(String) ClassLoader.getResource()}.
     * Corresponds to the {@code -c} option.
     *
     * @param file the configuration file path, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public CheckstyleOperation configurationFile(@NonNull String file) {
        Objects.requireNonNull(file, "configuration file must not be null");
        return optFile("configuration file", "-c", new File(file));
    }

    /**
     * Specifies the location of the Checkstyle configuration file.
     * Corresponds to the {@code -c} option.
     *
     * @param file the configuration file, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    public CheckstyleOperation configurationFile(@NonNull File file) {
        Objects.requireNonNull(file, "configuration file must not be null");
        return optFile("configuration file", "-c", file);
    }

    /**
     * Specifies the location of the Checkstyle configuration file.
     * Corresponds to the {@code -c} option.
     *
     * @param file the configuration file path, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    public CheckstyleOperation configurationFile(@NonNull Path file) {
        Objects.requireNonNull(file, "configuration file must not be null");
        return optFile("configuration file", "-c", file.toFile());
    }

    /**
     * Enables or disables debug logging for the Checkstyle utility.
     * Corresponds to the {@code -d} option.
     *
     * @param isDebug {@code true} to enable debug logging, {@code false} to disable
     * @return this operation instance
     */
    @NonNull
    public CheckstyleOperation debug(boolean isDebug) {
        return optBool("-d", isDebug);
    }

    /**
     * Retrieves the list of files and directories excluded from analysis.
     *
     * @return a mutable list of excluded files
     * @since 1.1.0
     */
    @NonNull
    public List<File> exclude() {
        return exclude_;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     * Corresponds to the {@code -e} option. Replaces any previously set excludes.
     *
     * @param paths the paths to exclude, must not contain {@code null} or empty values
     * @return this operation instance
     * @throws IllegalArgumentException if {@code paths} is {@code null} or contains {@code null} or empty values
     */
    @NonNull
    public CheckstyleOperation exclude(@NonNull String... paths) {
        ObjectTools.requireAllNotEmpty(paths, EXCLUDE_NOT_VALID);
        exclude_.clear();
        exclude_.addAll(CollectionTools.combineStringsToFiles(paths));
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     * Corresponds to the {@code -e} option. Replaces any previously set excludes.
     *
     * @param paths the files to exclude, must not contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code paths} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation exclude(@NonNull File... paths) {
        ObjectTools.requireAllNotEmpty(paths, EXCLUDE_NOT_VALID);
        exclude_.clear();
        exclude_.addAll(List.of(paths));
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     * Corresponds to the {@code -e} option. Replaces any previously set excludes.
     *
     * @param paths the paths to exclude, must not contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code paths} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation exclude(@NonNull Path... paths) {
        ObjectTools.requireAllNotEmpty(paths, EXCLUDE_NOT_VALID);
        exclude_.clear();
        exclude_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     * Corresponds to the {@code -e} option. Replaces any previously set excludes.
     *
     * @param paths the collection of files to exclude, must not be {@code null} or contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code paths} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation exclude(@NonNull Collection<File> paths) {
        ObjectTools.requireAllNotEmpty(paths, EXCLUDE_NOT_VALID);
        exclude_.clear();
        exclude_.addAll(paths);
        return this;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     * Corresponds to the {@code -e} option. Replaces any previously set excludes.
     *
     * @param paths the collection of paths to exclude, must not be {@code null} or contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code paths} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation excludePaths(@NonNull Collection<Path> paths) {
        ObjectTools.requireAllNotEmpty(paths, EXCLUDE_NOT_VALID);
        exclude_.clear();
        exclude_.addAll(CollectionTools.combinePathsToFiles(paths));
        return this;
    }

    /**
     * Specifies directory or file patterns to exclude from Checkstyle analysis.
     * Corresponds to the {@code -x} option. Replaces any previously set exclude patterns.
     *
     * @param patterns the regex patterns to exclude, must not contain {@code null} or empty values
     * @return this operation instance
     * @throws IllegalArgumentException if {@code patterns} is {@code null} or contains {@code null} or empty values
     */
    @NonNull
    public CheckstyleOperation excludeRegex(@NonNull String... patterns) {
        ObjectTools.requireAllNotEmpty(patterns, REGEX_NOT_VALID);
        excludeRegex_.clear();
        excludeRegex_.addAll(List.of(patterns));
        return this;
    }

    /**
     * Specifies directory or file patterns to exclude from Checkstyle analysis.
     * Corresponds to the {@code -x} option. Replaces any previously set exclude patterns.
     *
     * @param patterns the collection of regex patterns to exclude, must not be {@code null} or contain {@code null} or empty values
     * @return this operation instance
     * @throws IllegalArgumentException if {@code patterns} is {@code null} or contains {@code null} or empty values
     */
    @NonNull
    public CheckstyleOperation excludeRegex(@NonNull Collection<String> patterns) {
        ObjectTools.requireAllNotEmpty(patterns, REGEX_NOT_VALID);
        excludeRegex_.clear();
        excludeRegex_.addAll(patterns);
        return this;
    }

    /**
     * Retrieves the list of regex patterns excluded from analysis.
     *
     * @return a mutable list of exclude regex patterns
     */
    @NonNull
    public List<String> excludeRegex() {
        return excludeRegex_;
    }

    /**
     * Specifies directories or files to exclude from Checkstyle analysis.
     * Corresponds to the {@code -e} option. Replaces any previously set excludes.
     *
     * @param paths the collection of path strings to exclude, must not be {@code null} or contain {@code null} or empty values
     * @return this operation instance
     * @throws IllegalArgumentException if {@code paths} is {@code null} or contains {@code null} or empty values
     */
    @NonNull
    public CheckstyleOperation excludeStrings(@NonNull Collection<String> paths) {
        ObjectTools.requireAllNotEmpty(paths, EXCLUDE_NOT_VALID);
        exclude_.clear();
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
    @NonNull
    public CheckstyleOperation executeIgnoredModules(boolean isAllowIgnoreModules) {
        return optBool("-E", isAllowIgnoreModules);
    }

    /**
     * Specifies the output format for Checkstyle results.
     * Corresponds to the {@code -f} option.
     *
     * @param format the output format, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code format} is {@code null}
     */
    @NonNull
    @SuppressFBWarnings("STT_TOSTRING_STORED_IN_FIELD")
    public CheckstyleOperation format(@NonNull OutputFormat format) {
        Objects.requireNonNull(format, "format must not be null");
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
    @NonNull
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
    @NonNull
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
    @NonNull
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
    @NonNull
    public Map<String, String> options() {
        return options_;
    }

    /**
     * Sets the output file for Checkstyle results.
     * Corresponds to the {@code -o} option.
     *
     * @param file the output file path, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public CheckstyleOperation outputPath(@NonNull String file) {
        Objects.requireNonNull(file, "output path must not be null");
        return optFile("output path", "-o", new File(file));
    }

    /**
     * Sets the output file for Checkstyle results.
     * Corresponds to the {@code -o} option.
     *
     * @param file the output file, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    public CheckstyleOperation outputPath(@NonNull File file) {
        Objects.requireNonNull(file, "output file must not be null");
        return optFile("output path", "-o", file);
    }

    /**
     * Sets the output file for Checkstyle results.
     * Corresponds to the {@code -o} option.
     *
     * @param file the output file path, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    public CheckstyleOperation outputPath(@NonNull Path file) {
        Objects.requireNonNull(file, "output path must not be null");
        return optFile("output path", "-o", file.toFile());
    }

    /**
     * Sets the properties file for Checkstyle.
     * Corresponds to the {@code -p} option.
     *
     * @param file the properties file path, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public CheckstyleOperation propertiesFile(@NonNull String file) {
        Objects.requireNonNull(file, "property file must not be null");
        return optFile("properties file", "-p", new File(file));
    }

    /**
     * Sets the properties file for Checkstyle.
     * Corresponds to the {@code -p} option.
     *
     * @param file the properties file, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    public CheckstyleOperation propertiesFile(@NonNull File file) {
        Objects.requireNonNull(file, "property file must not be null");
        return optFile("properties file", "-p", file);
    }

    /**
     * Sets the properties file for Checkstyle.
     * Corresponds to the {@code -p} option.
     *
     * @param file the properties file path, must not be {@code null}
     * @return this operation instance
     * @throws NullPointerException if {@code file} is {@code null}
     */
    @NonNull
    public CheckstyleOperation propertiesFile(@NonNull Path file) {
        Objects.requireNonNull(file, "property file must not be null");
        return optFile("properties file", "-p", file.toFile());
    }

    /**
     * Specifies source directories to analyze. Replaces any previously set source directories.
     *
     * @param dirs the directory paths, must not contain {@code null} or empty values
     * @return this operation instance
     * @throws IllegalArgumentException if {@code dirs} is {@code null} or contains {@code null} or empty values
     */
    @NonNull
    public CheckstyleOperation sourceDir(@NonNull String... dirs) {
        ObjectTools.requireAllNotEmpty(dirs, SOURCE_DIR_NOT_VALID);
        sourceDir_.clear();
        sourceDir_.addAll(CollectionTools.combineStringsToFiles(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze. Replaces any previously set source directories.
     *
     * @param dirs the directories, must not contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code dirs} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation sourceDir(@NonNull File... dirs) {
        ObjectTools.requireAllNotEmpty(dirs, SOURCE_DIR_NOT_VALID);
        sourceDir_.clear();
        sourceDir_.addAll(List.of(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze. Replaces any previously set source directories.
     *
     * @param dirs the directory paths, must not contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code dirs} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation sourceDir(@NonNull Path... dirs) {
        ObjectTools.requireAllNotEmpty(dirs, SOURCE_DIR_NOT_VALID);
        sourceDir_.clear();
        sourceDir_.addAll(CollectionTools.combinePathsToFiles(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze. Replaces any previously set source directories.
     *
     * @param dirs the collection of directories, must not be {@code null} or contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code dirs} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation sourceDir(@NonNull Collection<File> dirs) {
        ObjectTools.requireAllNotEmpty(dirs, SOURCE_DIR_NOT_VALID);
        sourceDir_.clear();
        sourceDir_.addAll(dirs);
        return this;
    }

    /**
     * Returns the set of source directories to analyze.
     *
     * @return a mutable set of source directories
     */
    @NonNull
    public Set<File> sourceDir() {
        return sourceDir_;
    }

    /**
     * Specifies source directories to analyze. Replaces any previously set source directories.
     *
     * @param dirs the collection of directory paths, must not be {@code null} or contain {@code null}
     * @return this operation instance
     * @throws IllegalArgumentException if {@code dirs} is {@code null} or contains {@code null}
     */
    @NonNull
    public CheckstyleOperation sourceDirPaths(@NonNull Collection<Path> dirs) {
        ObjectTools.requireAllNotEmpty(dirs, SOURCE_DIR_NOT_VALID);
        sourceDir_.clear();
        sourceDir_.addAll(CollectionTools.combinePathsToFiles(dirs));
        return this;
    }

    /**
     * Specifies source directories to analyze. Replaces any previously set source directories.
     *
     * @param dirs the collection of directory path strings, must not be {@code null} or contain {@code null} or empty values
     * @return this operation instance
     * @throws IllegalArgumentException if {@code dirs} is {@code null} or contains {@code null} or empty values
     */
    @NonNull
    public CheckstyleOperation sourceDirStrings(@NonNull Collection<String> dirs) {
        ObjectTools.requireAllNotEmpty(dirs, SOURCE_DIR_NOT_VALID);
        sourceDir_.clear();
        sourceDir_.addAll(CollectionTools.combineStringsToFiles(dirs));
        return this;
    }

    /**
     * Prints XPath suppressions for a specific line and column.
     * Corresponds to the {@code -s} option.
     *
     * @param line   the line number, must not be negative
     * @param column the column number, must not be negative
     * @return this operation instance
     * @throws IllegalArgumentException if {@code line} or {@code column} is negative
     */
    @NonNull
    public CheckstyleOperation suppressionLineColumnNumber(int line, int column) {
        requireNonNegative(line, "suppression line number");
        requireNonNegative(column, "suppression column number");
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
    @NonNull
    public CheckstyleOperation tabWidth(int length) {
        if (length >= 0) {
            options_.put("-w", String.valueOf(length));
        } else {
            throw new IllegalArgumentException("tab width must be greater than or equal to 0");
        }
        return this;
    }

    /**
     * Prints the Abstract Syntax Tree without comments.
     * Corresponds to the {@code -t} option.
     *
     * @param isTree {@code true} to print the AST, {@code false} otherwise
     * @return this operation instance
     */
    @NonNull
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
    @NonNull
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
    @NonNull
    public CheckstyleOperation treeWithJavadoc(boolean isTree) {
        return optBool("-J", isTree);
    }

    private CheckstyleOperation opt(String label, String key, String value) {
        ObjectTools.requireNotEmpty(value, label + " must not be null or empty");
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
        Objects.requireNonNull(file, label + " must not be null");
        return opt(label, key, file.getAbsolutePath());
    }

    private void requireNonNegative(int value, String label) {
        if (value < 0) {
            throw new IllegalArgumentException(label + " must not be negative");
        }
    }
}