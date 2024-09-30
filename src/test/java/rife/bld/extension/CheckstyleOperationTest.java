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

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.bld.extension.checkstyle.OutputFormat;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


class CheckstyleOperationTest {
    public static final String SRC_MAIN_JAVA = "src/main/java";
    public static final String SRC_TEST_JAVA = "src/test/java";
    private static final String ADD = "add";
    private static final String BAR = "bar";
    private static final String FOO = "foo";
    private static final String REMOVE = "remove";

    @BeforeAll
    static void beforeAll() {
        var level = Level.ALL;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
    }


    @Test
    void branchMatchingXpath() {
        var op = new CheckstyleOperation().fromProject(new Project()).branchMatchingXpath(FOO);
        assertThat(op.options().get("-b")).isEqualTo(FOO);
    }

    @Test
    void checkAllParameters() throws IOException {
        var args = Files.readAllLines(Paths.get("src", "test", "resources", "checkstyle-args.txt"));

        assertThat(args).isNotEmpty();

        var params = new CheckstyleOperation()
                .fromProject(new Project())
                .branchMatchingXpath("xpath")
                .debug(true)
                .configurationFile(new File("config"))
                .exclude(SRC_MAIN_JAVA)
                .excludeRegex("regex")
                .executeIgnoredModules(true)
                .format(OutputFormat.XML)
                .generateXpathSuppression(true)
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
                softly.assertThat(found).as(p + " not found.").isTrue();
            }
        }
    }

    @Test
    void configurationFile() {
        var op = new CheckstyleOperation().fromProject(new Project()).configurationFile(FOO);
        assertThat(op.options().get("-c")).isEqualTo(FOO);
    }

    @Test
    void debug() {
        var op = new CheckstyleOperation().fromProject(new Project()).debug(true);
        assertThat(op.options().containsKey("-d")).as(ADD).isTrue();
        op = op.debug(false);
        assertThat(op.options().containsKey("-d")).as(REMOVE).isFalse();
    }

    @Test
    void exclude() {
        var foo = new File(SRC_MAIN_JAVA);
        var bar = new File(SRC_TEST_JAVA);
        var e = "-e ";

        var op = new CheckstyleOperation().fromProject(new Project()).exclude(SRC_MAIN_JAVA, SRC_TEST_JAVA);
        assertThat(op.executeConstructProcessCommandList()).as("String...")
                .contains(e + foo.getAbsolutePath()).contains(e + bar.getAbsolutePath());

        op = new CheckstyleOperation().fromProject(new Project()).excludeStrings(List.of(SRC_MAIN_JAVA, SRC_TEST_JAVA));
        assertThat(op.executeConstructProcessCommandList()).as("List(String...)")
                .contains(e + foo.getAbsolutePath()).contains(e + bar.getAbsolutePath());

        op = new CheckstyleOperation().fromProject(new Project()).exclude(foo, bar);
        assertThat(op.executeConstructProcessCommandList()).as("File...")
                .contains(e + foo.getAbsolutePath()).contains(e + bar.getAbsolutePath());

        op = new CheckstyleOperation().fromProject(new Project()).exclude(List.of(foo, bar));
        assertThat(op.executeConstructProcessCommandList()).as("List(File...)")
                .contains(e + foo.getAbsolutePath()).contains(e + bar.getAbsolutePath());

        op = new CheckstyleOperation().fromProject(new Project()).exclude(foo.toPath(), bar.toPath());
        assertThat(op.executeConstructProcessCommandList()).as("Path...")
                .contains(e + foo.getAbsolutePath()).contains(e + bar.getAbsolutePath());

        op = new CheckstyleOperation().fromProject(new Project()).excludePaths(List.of(foo.toPath(), bar.toPath()));
        assertThat(op.executeConstructProcessCommandList()).as("List(Path...)")
                .contains(e + foo.getAbsolutePath()).contains(e + bar.getAbsolutePath());
    }

    @Test
    void excludeRegex() {
        var op = new CheckstyleOperation().fromProject(new Project()).excludeRegex(FOO, BAR);
        var x = "-x ";
        assertThat(op.executeConstructProcessCommandList()).contains(x + FOO, x + BAR);

        op = new CheckstyleOperation().fromProject(new Project()).excludeRegex(List.of(FOO, BAR));
        assertThat(op.executeConstructProcessCommandList()).as("as list").contains(x + FOO, x + BAR);
    }

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
        assertThat(tmpFile).exists();
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
        var op = new CheckstyleOperation().fromProject(new Project()).executeIgnoredModules(true);
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
        var op = new CheckstyleOperation()
                .fromProject(new WebProject())
                .sourceDir(SRC_MAIN_JAVA, SRC_TEST_JAVA)
                .configurationFile("src/test/resources/sun_checks.xml")
                .outputPath(tmpFile.getAbsolutePath());
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(tmpFile).exists();
    }

    @Test
    void format() {
        var op = new CheckstyleOperation().fromProject(new Project()).format(OutputFormat.XML);
        assertThat(op.options().get("-f")).isEqualTo("xml");
    }

    @Test
    void generateXpathSuppression() {
        var op = new CheckstyleOperation().fromProject(new Project()).generateXpathSuppression(true);
        assertThat(op.options().containsKey("-g")).as(ADD).isTrue();
        op = op.generateXpathSuppression(false);
        assertThat(op.options().containsKey("-g")).as(REMOVE).isFalse();
    }

    @Test
    void javadocTree() {
        var op = new CheckstyleOperation().fromProject(new Project()).javadocTree(true);
        assertThat(op.options().containsKey("-j")).as(ADD).isTrue();
        op = op.javadocTree(false);
        assertThat(op.options().containsKey("-j")).as(REMOVE).isFalse();
    }

    @Test
    void outputPath() {
        var op = new CheckstyleOperation().fromProject(new Project()).outputPath(FOO);
        assertThat(op.options().get("-o")).isEqualTo(FOO);
    }

    @Test
    void propertiesFile() {
        var op = new CheckstyleOperation().fromProject(new Project()).propertiesFile(FOO);
        assertThat(op.options().get("-p")).isEqualTo(FOO);

        var fooPath = Path.of(FOO);
        op = op.propertiesFile(fooPath);
        assertThat(op.options().get("-p")).isEqualTo(fooPath.toFile().getAbsolutePath());
    }

    @Test
    void sourceDir() {
        var foo = new File(FOO);
        var bar = new File(BAR);

        var op = new CheckstyleOperation().fromProject(new Project()).sourceDir(FOO, BAR);
        assertThat(op.sourceDir()).as("String...").hasSize(2).contains(foo, bar);
        op.sourceDir().clear();

        op = op.sourceDirStrings(List.of(FOO, BAR));
        assertThat(op.sourceDir()).as("List(String...)").hasSize(2).contains(foo, bar);
        op.sourceDir().clear();

        op = op.sourceDir(foo, bar);
        assertThat(op.sourceDir()).as("File...").hasSize(2).contains(foo, bar);
        op.sourceDir().clear();

        op = op.sourceDir(List.of(foo, bar));
        assertThat(op.sourceDir()).as("List(File...)").hasSize(2).contains(foo, bar);
        op.sourceDir().clear();

        op = op.sourceDir(foo.toPath(), bar.toPath());
        assertThat(op.sourceDir()).as("Path...").hasSize(2).contains(foo, bar);
        op.sourceDir().clear();

        op = op.sourceDirPaths(List.of(foo.toPath(), bar.toPath()));
        assertThat(op.sourceDir()).as("List(Path...)").hasSize(2).contains(foo, bar);
        op.sourceDir().clear();
    }


    @Test
    void suppressionLineColumnNumber() {
        var op = new CheckstyleOperation().fromProject(new Project()).suppressionLineColumnNumber(FOO + ':' + BAR);
        assertThat(op.options().get("-s")).isEqualTo(FOO + ':' + BAR);
    }

    @Test
    void tabWith() {
        var op = new CheckstyleOperation().fromProject(new Project()).tabWith(9);
        assertThat(op.options().get("-w")).isEqualTo("9");
    }

    @Test
    void tree() {
        var op = new CheckstyleOperation().fromProject(new Project()).tree(true);
        assertThat(op.options().containsKey("-t")).as(ADD).isTrue();
        op = op.tree(false);
        assertThat(op.options().containsKey("-t")).as(REMOVE).isFalse();
    }

    @Test
    void treeWithComments() {
        var op = new CheckstyleOperation().fromProject(new Project()).treeWithComments(true);
        assertThat(op.options().containsKey("-T")).as(ADD).isTrue();
        op = op.treeWithComments(false);
        assertThat(op.options().containsKey("-T")).as(REMOVE).isFalse();
    }

    @Test
    void treeWithJavadoc() {
        var op = new CheckstyleOperation().fromProject(new Project()).treeWithJavadoc(true);
        assertThat(op.options().containsKey("-J")).as(ADD).isTrue();
        op = op.treeWithJavadoc(false);
        assertThat(op.options().containsKey("-J")).as(REMOVE).isFalse();
    }
}
