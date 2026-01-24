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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.bld.extension.checkstyle.OutputFormat;
import rife.bld.extension.testing.LoggingExtension;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(LoggingExtension.class)
class CheckstyleOperationTests {

    private static final String ADD = "add";
    private static final String BAR = "bar";
    private static final String FOO = "foo";

    @RegisterExtension
    @SuppressWarnings({"unused"})
    private static final LoggingExtension LOGGING_EXTENSION =
            new LoggingExtension(CheckstyleOperation.class.getName());

    private static final String REMOVE = "remove";
    private static final String SRC_MAIN_JAVA = "src/main/java";
    private static final String SRC_TEST_JAVA = "src/test/java";

    private CheckstyleOperation newCheckstyleOperation() {
        return new CheckstyleOperation().fromProject(new Project());
    }

    @Nested
    @DisplayName("Exclude Tests")
    class ExcludeTests {

        private static final String e = "-e ";
        private static final String x = "-x ";
        private final File srcMainJavaDir = new File(SRC_MAIN_JAVA);
        private final File srcTestJavaDir = new File(SRC_TEST_JAVA);

        @Test
        void exclude() {
            var op = newCheckstyleOperation().exclude(FOO, BAR);
            assertThat(op.exclude()).hasSize(2).contains(new File(FOO), new File(BAR));
        }

        @Test
        void excludeFromFileArray() {
            var op = newCheckstyleOperation().exclude(srcTestJavaDir, srcMainJavaDir);
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .contains(e + srcTestJavaDir.getAbsolutePath()).contains(e + srcMainJavaDir.getAbsolutePath());
        }

        @Test
        void excludeFromFileList() {
            var op = newCheckstyleOperation()
                    .exclude(List.of(srcTestJavaDir, srcMainJavaDir));
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .contains(e + srcTestJavaDir.getAbsolutePath()).contains(e + srcMainJavaDir.getAbsolutePath());
        }

        @Test
        void excludeFromPathArray() {
            var op = newCheckstyleOperation()
                    .exclude(srcTestJavaDir.toPath(), srcMainJavaDir.toPath());
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .contains(e + srcTestJavaDir.getAbsolutePath()).contains(e + srcMainJavaDir.getAbsolutePath());
        }

        @Test
        void excludeFromPathList() {
            var op = newCheckstyleOperation()
                    .excludePaths(List.of(srcTestJavaDir.toPath(), srcMainJavaDir.toPath()));
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .contains(e + srcTestJavaDir.getAbsolutePath()).contains(e + srcMainJavaDir.getAbsolutePath());
        }

        @Test
        void excludeFromStringArray() {
            var op = newCheckstyleOperation().exclude(SRC_MAIN_JAVA, SRC_TEST_JAVA);
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .contains(e + srcTestJavaDir.getAbsolutePath()).contains(e + srcMainJavaDir.getAbsolutePath());
        }

        @Test
        void excludeFromStringList() {
            var op = newCheckstyleOperation()
                    .excludeStrings(List.of(SRC_MAIN_JAVA, SRC_TEST_JAVA));
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .contains(e + srcMainJavaDir.getAbsolutePath()).contains(e + srcTestJavaDir.getAbsolutePath());
        }

        @Test
        void excludeNonExistingPath() {
            var op = newCheckstyleOperation().exclude(FOO);
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .doesNotContain(e + FOO);
        }

        @Test
        void excludeNonExistingPaths() {
            var op = newCheckstyleOperation().exclude(FOO, BAR);
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .doesNotContain(e + FOO).doesNotContain(e + BAR);
        }

        @Test
        void excludeRegex() {
            var op = newCheckstyleOperation().excludeRegex(FOO, BAR);
            assertThat(op.excludeRegex()).hasSize(2).contains(FOO, BAR);
        }

        @Test
        void excludeRegexArray() {
            var op = newCheckstyleOperation().excludeRegex(FOO, BAR);
            assertThat(op.excludeRegex()).hasSize(2).contains(FOO, BAR);
            assertThat(String.join(" ", op.executeConstructProcessCommandList())).contains(x + FOO, x + BAR);
        }

        @Test
        void excludeRegexFromList() {
            var op = newCheckstyleOperation().excludeRegex(List.of(FOO, BAR));
            assertThat(String.join(" ", op.executeConstructProcessCommandList())).contains(x + FOO, x + BAR);
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    class ExecuteTests {

        @Test
        void execute() throws IOException, ExitStatusException, InterruptedException {
            var tmpFile = File.createTempFile("checkstyle-google", ".txt");
            tmpFile.deleteOnExit();
            var op = new CheckstyleOperation()
                    .fromProject(new WebProject())
                    .sourceDir(SRC_MAIN_JAVA, SRC_TEST_JAVA)
                    .configurationFile(Path.of("src/test/resources/google_checks.xml"))
                    .outputPath(tmpFile.toPath());
            op.execute();
            assertThat(tmpFile).as("tmp file should exist").exists();
        }

        @Test
        void executeConstructProcessCommandList() {
            var op = new CheckstyleOperation().fromProject(new BaseProject())
                    .configurationFile("config/checkstyle.xml")
                    .branchMatchingXpath("xpath")
                    .propertiesFile("config/checkstyle.properties")
                    .debug(true)
                    .executeIgnoredModules(true)
                    .sourceDir(SRC_MAIN_JAVA, SRC_TEST_JAVA);
            assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                    .startsWith("java -cp ")
                    .endsWith(
                            "com.puppycrawl.tools.checkstyle.Main " +
                                    "-p config/checkstyle.properties " +
                                    "-b xpath " +
                                    "-c config/checkstyle.xml " +
                                    "-d -E " +
                                    new File(SRC_MAIN_JAVA).getAbsolutePath() + " " +
                                    new File(SRC_TEST_JAVA).getAbsolutePath());
        }

        @Test
        void executeIgnoredModules() {
            var op = newCheckstyleOperation().executeIgnoredModules(true);
            assertThat(op.options().containsKey("-E")).as(ADD).isTrue();
            op = op.executeIgnoredModules(false);
            assertThat(op.options().containsKey("-E")).as(REMOVE).isFalse();
        }

        @Test
        void executeNoProject() {
            var op = new CheckstyleOperation();
            assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        }

        @Test
        void executeSunChecks() throws IOException {
            var tmpFile = File.createTempFile("checkstyle-sun", ".txt");
            tmpFile.deleteOnExit();
            var op = newCheckstyleOperation()
                    .sourceDir(SRC_MAIN_JAVA, SRC_TEST_JAVA)
                    .configurationFile("src/test/resources/sun_checks.xml")
                    .outputPath(tmpFile.getAbsolutePath());
            assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
            assertThat(tmpFile).as("tmp file should exist").exists();
        }
    }

    @Nested
    @DisplayName("Options Tests")
    class OptionsTests {

        @Test
        void branchMatchingXpath() {
            var op = newCheckstyleOperation().branchMatchingXpath(FOO);
            assertThat(op.options().get("-b")).isEqualTo(FOO);
        }

        @Test
        @EnabledOnOs(OS.LINUX)
        void checkAllParameters() throws IOException {
            var args = Files.readAllLines(Paths.get("src", "test", "resources", "checkstyle-args.txt"));

            assertThat(args).isNotEmpty();

            var params = newCheckstyleOperation()
                    .branchMatchingXpath("xpath")
                    .debug(true)
                    .configurationFile(new File("config"))
                    .exclude(SRC_MAIN_JAVA)
                    .excludeRegex("regex")
                    .executeIgnoredModules(true)
                    .format(OutputFormat.XML)
                    .generateXpathSuppression(true)
                    .generateChecksAndFileSuppression(true)
                    .javadocTree(true)
                    .outputPath(new File("optionPath"))
                    .propertiesFile(new File("properties"))
                    .suppressionLineColumnNumber("12")
                    .tabWith(1)
                    .tree(true)
                    .treeWithComments(true)
                    .treeWithJavadoc(true)
                    .executeConstructProcessCommandList();

            try (var softly = new AutoCloseableSoftAssertions()) {
                for (var p : args) {
                    var found = false;
                    for (var a : params) {
                        if (a.startsWith(p)) {
                            found = true;
                            break;
                        }
                    }
                    softly.assertThat(found).as("%s not found.", p).isTrue();
                }
            }
        }

        @Test
        void configurationFile() {
            var op = newCheckstyleOperation().configurationFile(FOO);
            assertThat(op.options().get("-c")).isEqualTo(FOO);
        }

        @Test
        void debug() {
            var op = newCheckstyleOperation().debug(true);
            assertThat(op.options().containsKey("-d")).as(ADD).isTrue();
            op = op.debug(false);
            assertThat(op.options().containsKey("-d")).as(REMOVE).isFalse();
        }

        @Test
        void format() {
            var op = newCheckstyleOperation().format(OutputFormat.XML);
            assertThat(op.options().get("-f")).isEqualTo("xml");
        }

        @Test
        void generateChecksAndFileSuppression() {
            var op = newCheckstyleOperation().generateChecksAndFileSuppression(true);
            assertThat(op.options().containsKey("-G")).as(ADD).isTrue();
            op = op.generateChecksAndFileSuppression(false);
            assertThat(op.options().containsKey("-G")).as(REMOVE).isFalse();
        }

        @Test
        void generateXpathSuppression() {
            var op = newCheckstyleOperation().generateXpathSuppression(true);
            assertThat(op.options().containsKey("-g")).as(ADD).isTrue();
            op = op.generateXpathSuppression(false);
            assertThat(op.options().containsKey("-g")).as(REMOVE).isFalse();
        }

        @Test
        void javadocTree() {
            var op = newCheckstyleOperation().javadocTree(true);
            assertThat(op.options().containsKey("-j")).as(ADD).isTrue();
            op = op.javadocTree(false);
            assertThat(op.options().containsKey("-j")).as(REMOVE).isFalse();
        }

        @Test
        void outputPath() {
            var op = newCheckstyleOperation().outputPath(FOO);
            assertThat(op.options().get("-o")).isEqualTo(FOO);
        }

        @Test
        void propertiesFile() {
            var op = newCheckstyleOperation().propertiesFile(FOO);
            assertThat(op.options().get("-p")).isEqualTo(FOO);

            var fooPath = Path.of(FOO);
            op = op.propertiesFile(fooPath);
            assertThat(op.options().get("-p")).isEqualTo(fooPath.toFile().getAbsolutePath());
        }

        @Test
        void suppressionLineColumnNumber() {
            var op = newCheckstyleOperation().suppressionLineColumnNumber(FOO + ':' + BAR);
            assertThat(op.options().get("-s")).isEqualTo(FOO + ':' + BAR);
        }

        @Test
        void tabWith() {
            var op = newCheckstyleOperation().tabWith(9);
            assertThat(op.options().get("-w")).isEqualTo("9");
        }

        @Test
        void tree() {
            var op = newCheckstyleOperation().tree(true);
            assertThat(op.options().containsKey("-t")).as(ADD).isTrue();
            op = op.tree(false);
            assertThat(op.options().containsKey("-t")).as(REMOVE).isFalse();
        }

        @Test
        void treeWithComments() {
            var op = newCheckstyleOperation().treeWithComments(true);
            assertThat(op.options().containsKey("-T")).as(ADD).isTrue();
            op = op.treeWithComments(false);
            assertThat(op.options().containsKey("-T")).as(REMOVE).isFalse();
        }

        @Test
        void treeWithJavadoc() {
            var op = newCheckstyleOperation().treeWithJavadoc(true);
            assertThat(op.options().containsKey("-J")).as(ADD).isTrue();
            op = op.treeWithJavadoc(false);
            assertThat(op.options().containsKey("-J")).as(REMOVE).isFalse();
        }

        @Nested
        @DisplayName("Source Dir Tests")
        class SourceDirTests {

            private final File bar = new File(BAR);
            private final File foo = new File(FOO);
            private final CheckstyleOperation op = newCheckstyleOperation().sourceDir(FOO, BAR);

            @Test
            void sourceDirFromFileArray() {
                op.sourceDir(foo, bar);
                assertThat(op.sourceDir()).as("File...").hasSize(2).contains(foo, bar);
                op.sourceDir().clear();
            }

            @Test
            void sourceDirFromFileList() {
                op.sourceDir(List.of(foo, bar));
                assertThat(op.sourceDir()).as("List(File...)").hasSize(2).contains(foo, bar);
                op.sourceDir().clear();
            }

            @Test
            void sourceDirFromPathArray() {
                op.sourceDir(foo.toPath(), bar.toPath());
                assertThat(op.sourceDir()).as("Path...").hasSize(2).contains(foo, bar);
                op.sourceDir().clear();
            }

            @Test
            void sourceDirFromPathList() {
                op.sourceDirPaths(List.of(foo.toPath(), bar.toPath()));
                assertThat(op.sourceDir()).as("List(Path...)").hasSize(2).contains(foo, bar);
                op.sourceDir().clear();
            }

            @Test
            void sourceDirFromStringArray() {
                op.sourceDir("foo", "bar");
                assertThat(op.sourceDir()).as("String...").hasSize(2).contains(foo, bar);
                op.sourceDir().clear();
            }

            @Test
            void sourceDirFromStringList() {
                op.sourceDirStrings(List.of(FOO, BAR));
                assertThat(op.sourceDir()).as("List(String...)").hasSize(2).contains(foo, bar);
                op.sourceDir().clear();
            }
        }
    }
}
