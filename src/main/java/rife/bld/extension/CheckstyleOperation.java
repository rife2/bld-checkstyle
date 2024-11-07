/*
 * Copyright 2023-2024 the original author or authors.
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

import rife.bld.BaseProject;
import rife.bld.extension.checkstyle.OutputFormat;
import rife.bld.operations.AbstractProcessOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static code analysis using <a href="https://checkstyle.sourceforge.io/">Checkstyle</a>.
 *
 * @author <a href="https://erik.thauvin.net">Erik C. Thauvin</a>
 * @since 1.0
 */
public class CheckstyleOperation extends AbstractProcessOperation<CheckstyleOperation> {
    private static final Logger LOGGER = Logger.getLogger(CheckstyleOperation.class.getName());
    private final Collection<String> excludeRegex_ = new ArrayList<>();
    private final Collection<File> exclude_ = new ArrayList<>();
    private final Map<String, String> options_ = new ConcurrentHashMap<>();
    private final Set<File> sourceDir_ = new TreeSet<>();

    private BaseProject project_;

    /**
     * Shows Abstract Syntax Tree(AST) branches that match given XPath query.
     *
     * @param xPathQuery the xPath query
     * @return the checkstyle operation
     */
    public CheckstyleOperation branchMatchingXpath(String xPathQuery) {
        if (isNotBlank(xPathQuery)) {
            options_.put("-b", xPathQuery);
        }
        return this;
    }

    /**
     * Specifies the location of the file that defines the configuration modules. The location can either be a
     * filesystem location, or a name passed to the {@link ClassLoader#getResource(String) ClassLoader.getResource() }
     * method. A configuration file is required.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation configurationFile(String file) {
        if (isNotBlank(file)) {
            options_.put("-c", file);
        }
        return this;
    }

    /**
     * Specifies the location of the file that defines the configuration modules. The location can either be a
     * filesystem location, or a name passed to the {@link ClassLoader#getResource(String) ClassLoader.getResource() }
     * method. A configuration file is required.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation configurationFile(File file) {
        return configurationFile(file.getAbsolutePath());
    }

    /**
     * Specifies the location of the file that defines the configuration modules. The location can either be a
     * filesystem location, or a name passed to the {@link ClassLoader#getResource(String) ClassLoader.getResource() }
     * method. A configuration file is required.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation configurationFile(Path file) {
        return configurationFile(file.toFile().getAbsolutePath());
    }

    /**
     * Prints all debug logging of Checkstyle utility.
     *
     * @param isDebug {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation debug(boolean isDebug) {
        if (isDebug) {
            options_.put("-d", "");
        } else {
            options_.remove("-d");
        }
        return this;
    }

    /**
     * Directory/file to exclude from Checkstyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param path one or more paths
     * @return the checkstyle operation
     * @see #excludeStrings(Collection)
     */
    public CheckstyleOperation exclude(String... path) {
        return excludeStrings(List.of(path));
    }

    /**
     * Directory/file to exclude from Checkstyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param path one or more paths
     * @return the checkstyle operation
     * @see #exclude(Collection)
     */
    public CheckstyleOperation exclude(File... path) {
        return exclude(List.of(path));
    }

    /**
     * Directory/file to exclude from Checkstyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param path one or more paths
     * @return the checkstyle operation
     * @see #excludePaths(Collection)
     */
    public CheckstyleOperation exclude(Path... path) {
        return excludePaths(List.of(path));
    }

    /**
     * Directory/file to exclude from Checkstyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param paths the paths
     * @return the checkstyle operation
     * @see #exclude(File...)
     */
    public CheckstyleOperation exclude(Collection<File> paths) {
        exclude_.addAll(paths);
        return this;
    }

    /**
     * Directory/file to exclude from Checkstyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param paths the paths
     * @return the checkstyle operation
     * @see #exclude(Path...)
     */
    public CheckstyleOperation excludePaths(Collection<Path> paths) {
        return exclude(paths.stream().map(Path::toFile).toList());
    }

    /**
     * Directory/file pattern to exclude from Checkstyle. Multiple exclude are allowed.
     *
     * @param regex the pattern to exclude
     * @return the checkstyle operation
     * @see #excludeRegex(Collection)
     */
    public CheckstyleOperation excludeRegex(String... regex) {
        return excludeRegex(List.of(regex));
    }

    /**
     * Directory/file pattern to exclude from Checkstyle. Multiple exclude are allowed.
     *
     * @param regex the patterns to exclude
     * @return the checkstyle operation
     * @see #excludeRegex(String...)
     */
    public CheckstyleOperation excludeRegex(Collection<String> regex) {
        excludeRegex_.addAll(regex);
        return this;
    }

    /**
     * Directory/file to exclude from Checkstyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param paths the paths
     * @return the checkstyle operation
     * @see #exclude(String...)
     */
    public CheckstyleOperation excludeStrings(Collection<String> paths) {
        return exclude(paths.stream().map(File::new).toList());
    }

    @Override
    public void execute() throws IOException, InterruptedException, ExitStatusException {
        if (project_ == null) {
            if (LOGGER.isLoggable(Level.SEVERE) && !silent()) {
                LOGGER.severe("A project must be specified.");
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        } else {
            super.execute();
        }
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        final List<String> args = new ArrayList<>();

        if (project_ != null) {
            if (sourceDir_.isEmpty()) {
                sourceDir_.add(project_.srcMainJavaDirectory());
                sourceDir_.add(project_.srcTestJavaDirectory());
            }
            args.add(javaTool());

            args.add("-cp");
            args.add(String.format("%s:%s:%s:%s", new File(project_.libTestDirectory(), "*"),
                    new File(project_.libCompileDirectory(), "*"), project_.buildMainDirectory(),
                    project_.buildTestDirectory()));
            args.add("com.puppycrawl.tools.checkstyle.Main");

            options_.forEach((k, v) -> {
                args.add(k);
                if (!v.isEmpty()) {
                    args.add(v);
                }
            });

            if (!exclude_.isEmpty()) {
                for (var e : exclude_) {
                    if (e.exists()) {
                        args.add("-e " + e.getAbsolutePath());
                    }
                }
            }

            if (!excludeRegex_.isEmpty()) {
                for (var e : excludeRegex_) {
                    if (isNotBlank(e)) {
                        args.add("-x " + e);
                    }
                }
            }

            args.addAll(sourceDir_.stream().map(File::getAbsolutePath).toList());

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.join(" ", args));
            }
        }

        return args;
    }

    /**
     * Configures the {@link BaseProject}.
     */
    @Override
    public CheckstyleOperation fromProject(BaseProject project) {
        project_ = project;
        return this;
    }

    /**
     * Allows ignored modules to be run.
     *
     * @param isAllowIgnoreModules {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation executeIgnoredModules(boolean isAllowIgnoreModules) {
        if (isAllowIgnoreModules) {
            options_.put("-E", "");
        } else {
            options_.remove("-E");
        }
        return this;
    }

    /**
     * Specifies the output format. Valid values: {@link OutputFormat#XML},
     * {@link OutputFormat#SARIF}, {@link OutputFormat#PLAIN} for the XML, sarif and default logger
     * respectively.
     * <p>
     * Defaults to {@link OutputFormat#PLAIN}.
     *
     * @param format the output format
     * @return the checkstyle operation
     */
    public CheckstyleOperation format(OutputFormat format) {
        options_.put("-f", format.label.toLowerCase());
        return this;
    }

    /**
     * Generates to output a suppression xml to use to suppress all violations from user's config. Instead of printing
     * every violation, all violations will be caught and single suppressions xml file will be printed out.
     * Used only with the {@link #configurationFile(String) configurationFile} option. Output location can be specified
     * with the {@link #outputPath(String) output} option.
     *
     * @param xPathSuppression {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation generateXpathSuppression(boolean xPathSuppression) {
        if (xPathSuppression) {
            options_.put("-g", "");
        } else {
            options_.remove("-g");
        }
        return this;
    }

    /*
     * Determines if a string is not blank.
     */
    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * This option is used to print the Parse Tree of the Javadoc comment. The file has to contain only Javadoc comment
     * content excluding '&#47;**' and '*&#47;' at the beginning and at the end respectively. It can only be used on a
     * single file and cannot be combined with other options.
     *
     * @param isTree {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation javadocTree(boolean isTree) {
        if (isTree) {
            options_.put("-j", "");
        } else {
            options_.remove("-j");
        }
        return this;
    }

    /**
     * Returns the command line options.
     *
     * @return the command line options
     */
    public Map<String, String> options() {
        return options_;
    }

    /**
     * Sets the output file.
     * <p>
     * Defaults to stdout.
     *
     * @param file the output file
     * @return the checkstyle operation
     */
    public CheckstyleOperation outputPath(String file) {
        if (isNotBlank(file)) {
            options_.put("-o", file);
        }
        return this;
    }

    /**
     * Sets the output file.
     * <p>
     * Defaults to stdout.
     *
     * @param file the output file
     * @return the checkstyle operation
     */
    public CheckstyleOperation outputPath(File file) {
        return outputPath(file.getAbsolutePath());
    }

    /**
     * Sets the output file.
     * <p>
     * Defaults to stdout.
     *
     * @param file the output file
     * @return the checkstyle operation
     */
    public CheckstyleOperation outputPath(Path file) {
        return outputPath(file.toFile().getAbsolutePath());
    }

    /**
     * Sets the property files to load.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation propertiesFile(String file) {
        if (isNotBlank(file)) {
            options_.put("-p", file);
        }
        return this;
    }

    /**
     * Sets the property files to load.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation propertiesFile(File file) {
        return propertiesFile(file.getAbsolutePath());
    }

    /**
     * Sets the property files to load.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation propertiesFile(Path file) {
        return propertiesFile(file.toFile().getAbsolutePath());
    }

    /**
     * Specifies the file(s) or folder(s) containing the source files to check.
     *
     * @param dir one or more directories
     * @return the checkstyle operation
     * @see #sourceDirStrings(Collection)
     */
    public CheckstyleOperation sourceDir(String... dir) {
        return sourceDirStrings(List.of(dir));
    }

    /**
     * Specifies the file(s) or folder(s) containing the source files to check.
     *
     * @param dir one or more directories
     * @return the checkstyle operation
     * @see #sourceDir(Collection)
     */
    public CheckstyleOperation sourceDir(File... dir) {
        return sourceDir(List.of(dir));
    }

    /**
     * Specifies the file(s) or folder(s) containing the source files to check.
     *
     * @param dir one or more directories
     * @return the checkstyle operation
     * @see #sourceDirPaths(Collection)
     */
    public CheckstyleOperation sourceDir(Path... dir) {
        return sourceDirPaths(List.of(dir));
    }

    /**
     * Specifies the file(s) or folder(s) containing the source files to check.
     *
     * @param dirs the directories
     * @return the checkstyle operation
     * @see #sourceDir(File...)
     */
    public CheckstyleOperation sourceDir(Collection<File> dirs) {
        sourceDir_.addAll(dirs);
        return this;
    }

    /**
     * Returns the file(s) or folders(s) containing the sources files to check
     *
     * @return the files or directories
     */
    public Set<File> sourceDir() {
        return sourceDir_;
    }

    /**
     * Specifies the file(s) or folder(s) containing the source files to check.
     *
     * @param dirs the directories
     * @return the checkstyle operation
     * @see #sourceDir(Path...)
     */
    public CheckstyleOperation sourceDirPaths(Collection<Path> dirs) {
        return sourceDir(dirs.stream().map(Path::toFile).toList());
    }

    /**
     * Specifies the file(s) or folder(s) containing the source files to check.
     *
     * @param dirs the directories
     * @return the checkstyle operation
     * @see #sourceDir(String...)
     */
    public CheckstyleOperation sourceDirStrings(Collection<String> dirs) {
        return sourceDir(dirs.stream().map(File::new).toList());
    }

    /**
     * Prints xpath suppressions at the file's line and column position. Argument is the line and column number
     * (separated by a {@code :} ) in the file that the suppression should be generated for. The option cannot be
     * used with other options and requires exactly one file to run on to be specified.
     * <p>
     * Note that the generated result will have few queries, joined by pipe({@code |}). Together they will match all
     * AST nodes on specified line and column. You need to choose only one and recheck that it works. Usage of all of
     * them is also ok, but might result in undesirable matching and suppress other issues.
     *
     * @param lineColumnNumber the line column number
     * @return the checkstyle operation
     */
    public CheckstyleOperation suppressionLineColumnNumber(String lineColumnNumber) {
        if (isNotBlank(lineColumnNumber)) {
            options_.put("-s", lineColumnNumber);
        }
        return this;
    }

    /**
     * Sets the length of the tab character. Used only with the
     * {@link #suppressionLineColumnNumber(String) suppressionLineColumnNumber} option.
     * <p>
     * Default value is {@code 8}.
     *
     * @param length the length
     * @return the checkstyle operation
     */
    public CheckstyleOperation tabWith(int length) {
        options_.put("-w", String.valueOf(length));
        return this;
    }

    /**
     * This option is used to display the Abstract Syntax Tree (AST) without any comments of the specified file. It can
     * only be used on a single file and cannot be combined with other options.
     *
     * @param isTree {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation tree(boolean isTree) {
        if (isTree) {
            options_.put("-t", "");
        } else {
            options_.remove("-t");
        }
        return this;
    }

    /**
     * This option is used to display the Abstract Syntax Tree (AST) with comment nodes excluding Javadoc of the
     * specified file. It can only be used on a single file and cannot be combined with other options.
     *
     * @param isTree {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation treeWithComments(boolean isTree) {
        if (isTree) {
            options_.put("-T", "");
        } else {
            options_.remove("-T");
        }
        return this;
    }

    /**
     * This option is used to display the Abstract Syntax Tree (AST) with Javadoc nodes of the specified file. It can
     * only be used on a single file and cannot be combined with other options.
     *
     * @param isTree {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation treeWithJavadoc(boolean isTree) {
        if (isTree) {
            options_.put("-J", "");
        } else {
            options_.remove("-J");
        }
        return this;
    }
}
