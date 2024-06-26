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
import rife.bld.operations.AbstractProcessOperation;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Static code analysis using <a href="https://checkstyle.sourceforge.io/">Checkstyle</a>.
 *
 * @author <a href="https://erik.thauvin.net">Erik C. Thauvin</a>
 * @since 1.0
 */
public class CheckstyleOperation extends AbstractProcessOperation<CheckstyleOperation> {
    private static final Logger LOGGER = Logger.getLogger(CheckstyleOperation.class.getName());
    protected final List<String> exclude = new ArrayList<>();
    protected final List<String> excludeRegex = new ArrayList<>();
    /**
     * The command line options.
     */
    protected final Map<String, String> options = new ConcurrentHashMap<>();
    /**
     * The source files(s) or folder(s).
     */
    protected final Set<String> sourceDirs = new TreeSet<>();
    private BaseProject project_;

    /**
     * Shows Abstract Syntax Tree(AST) branches that match given XPath query.
     *
     * @param xPathQuery the xPath query
     * @return the checkstyle operation
     */
    public CheckstyleOperation branchMatchingXpath(String xPathQuery) {
        if (isNotBlank(xPathQuery)) {
            options.put("-b", xPathQuery);
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
            options.put("-c", file);
        }
        return this;
    }

    /**
     * Prints all debug logging of CheckStyle utility.
     *
     * @param isDebug {@code true} or {@code false}
     * @return the checkstyle operation
     */
    public CheckstyleOperation debug(boolean isDebug) {
        if (isDebug) {
            options.put("-d", "");
        } else {
            options.remove("-d");
        }
        return this;
    }

    /**
     * Directory/file to exclude from CheckStyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param path one or more paths
     * @return the checkstyle operation
     * @see #sourceDir(Collection)
     */
    public CheckstyleOperation exclude(String... path) {
        for (var p : path) {
            if (isNotBlank(p)) {
                exclude.add(p);
            }
        }
        return this;
    }

    /**
     * Directory/file to exclude from CheckStyle. The path can be the full, absolute path, or relative to the current
     * path. Multiple excludes are allowed.
     *
     * @param paths the paths
     * @return the checkstyle operation
     * @see #exclude(String...)
     */
    public CheckstyleOperation exclude(Collection<String> paths) {
        for (var p : paths) {
            if (isNotBlank(p)) {
                exclude.add(p);
            }
        }
        return this;
    }

    /**
     * Directory/file pattern to exclude from CheckStyle. Multiple exclude are allowed.
     *
     * @param regex the pattern to exclude
     * @return the checkstyle operation
     * @see #excludeRegex(Collection)
     */
    public CheckstyleOperation excludeRegex(String... regex) {
        for (var r : regex) {
            if (isNotBlank(r)) {
                excludeRegex.add(r);
            }
        }

        return this;
    }

    /**
     * Directory/file pattern to exclude from CheckStyle. Multiple exclude are allowed.
     *
     * @param regex the patterns to exclude
     * @return the checkstyle operation
     * @see #excludeRegex(String...)
     */
    public CheckstyleOperation excludeRegex(Collection<String> regex) {
        for (var r : regex) {
            if (isNotBlank(r)) {
                excludeRegex.add(r);
            }
        }

        return this;
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        if (project_ == null) {
            LOGGER.severe("A project must be specified.");
        } else if (sourceDirs.isEmpty()) {
            sourceDirs.add(project_.srcMainJavaDirectory().getPath());
            sourceDirs.add(project_.srcTestJavaDirectory().getPath());
        }

        final List<String> args = new ArrayList<>();
        args.add(javaTool());

        args.add("-cp");
        args.add(String.format("%s:%s:%s:%s", Path.of(project_.libTestDirectory().getPath(), "*"),
                Path.of(project_.libCompileDirectory().getPath(), "*"), project_.buildMainDirectory(),
                project_.buildTestDirectory()));
        args.add("com.puppycrawl.tools.checkstyle.Main");

        options.forEach((k, v) -> {
            args.add(k);
            if (!v.isEmpty()) {
                args.add(v);
            }
        });

        if (!exclude.isEmpty()) {
            for (var e : exclude) {
                if (isNotBlank(e)) {
                    args.add("-e " + e);
                }
            }
        }

        if (!excludeRegex.isEmpty()) {
            for (var e : excludeRegex) {
                if (isNotBlank(e)) {
                    args.add("-x " + e);
                }
            }
        }

        args.addAll(sourceDirs);

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
            options.put("-E", "");
        } else {
            options.remove("-E");
        }
        return this;
    }

    /**
     * Specifies the output format. Valid values: {@link CheckstyleFormatOption#XML},
     * {@link CheckstyleFormatOption#SARIF}, {@link CheckstyleFormatOption#PLAIN} for the XML, sarif and default logger
     * respectively.
     * <p>
     * Defaults to {@link CheckstyleFormatOption#PLAIN}.
     *
     * @param format the output format
     * @return the checkstyle operation
     */
    public CheckstyleOperation format(CheckstyleFormatOption format) {
        options.put("-f", format.label.toLowerCase());
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
            options.put("-g", "");
        } else {
            options.remove("-g");
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
            options.put("-j", "");
        } else {
            options.remove("-j");
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
    public CheckstyleOperation outputPath(String file) {
        if (isNotBlank(file)) {
            options.put("-o", file);
        }
        return this;
    }

    /**
     * Sets the property files to load.
     *
     * @param file the file
     * @return the checkstyle operation
     */
    public CheckstyleOperation propertiesFile(String file) {
        if (isNotBlank(file)) {
            options.put("-p", file);
        }
        return this;
    }

    /**
     * Specified the file(s) or folder(s) containing the source files to check.
     *
     * @param dir one or more directories
     * @return the checkstyle operation
     * @see #sourceDir(Collection)
     */
    public CheckstyleOperation sourceDir(String... dir) {
        sourceDirs.addAll(Arrays.stream(dir).filter(this::isNotBlank).toList());
        return this;
    }

    /**
     * Specified the file(s) or folder(s) containing the source files to check.
     *
     * @param dirs the directories
     * @return the checkstyle operation
     * @see #sourceDir(String...)
     */
    public CheckstyleOperation sourceDir(Collection<String> dirs) {
        sourceDirs.addAll(dirs.stream().filter(this::isNotBlank).toList());
        return this;
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
            options.put("-s", lineColumnNumber);
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
        options.put("-w", String.valueOf(length));
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
            options.put("-t", "");
        } else {
            options.remove("-t");
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
            options.put("-T", "");
        } else {
            options.remove("-T");
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
            options.put("-J", "");
        } else {
            options.remove("-J");
        }
        return this;
    }
}
