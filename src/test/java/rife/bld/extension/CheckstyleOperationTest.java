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

import org.junit.jupiter.api.Test;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


class CheckstyleOperationTest {
    private static final String ADD = "add";
    private static final String BAR = "bar";
    private static final String FOO = "foo";
    private static final String REMOVE = "remove";

    @Test
    void branchMatchingXpath() {
        var op = new CheckstyleOperation().fromProject(new Project()).branchMatchingXpath(FOO);
        assertThat(op.options.get("-b")).isEqualTo(FOO);
    }

    @Test
    void configurationFile() {
        var op = new CheckstyleOperation().fromProject(new Project()).configurationFile(FOO);
        assertThat(op.options.get("-c")).isEqualTo(FOO);
    }

    @Test
    void debug() {
        var op = new CheckstyleOperation().fromProject(new Project()).debug(true);
        assertThat(op.options.containsKey("-d")).as(ADD).isTrue();
        op = op.debug(false);
        assertThat(op.options.containsKey("-d")).as(REMOVE).isFalse();
    }

    @Test
    void exclude() {
        var op = new CheckstyleOperation().fromProject(new Project()).exclude(FOO, BAR);
        assertThat(op.executeConstructProcessCommandList()).contains("-e " + FOO, "-e " + BAR);

        op = new CheckstyleOperation().fromProject(new Project()).exclude(List.of(FOO, BAR));
        assertThat(op.executeConstructProcessCommandList()).as("as list").contains("-e " + FOO, "-e " + BAR);
    }

    @Test
    void excludeRegex() {
        var op = new CheckstyleOperation().fromProject(new Project()).excludeRegex(FOO, BAR);
        assertThat(op.executeConstructProcessCommandList()).contains("-x " + FOO, "-x " + BAR);

        op = new CheckstyleOperation().fromProject(new Project()).excludeRegex(List.of(FOO, BAR));
        assertThat(op.executeConstructProcessCommandList()).as("as list").contains("-x " + FOO, "-x " + BAR);
    }


    @Test
    void execute() throws IOException, ExitStatusException, InterruptedException {
        var tmpFile = File.createTempFile("checkstyle-google", ".txt");
        tmpFile.deleteOnExit();
        var op = new CheckstyleOperation()
                .fromProject(new WebProject())
                .sourceDir("src/main/java", "src/test/java")
                .configurationFile("src/test/resources/google_checks.xml")
                .outputPath(tmpFile.getAbsolutePath());
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
                .sourceDir("src/main/java", "src/test/java");
        assertThat(String.join(" ", op.executeConstructProcessCommandList()))
                .startsWith("java -cp ")
                .endsWith(
                        "com.puppycrawl.tools.checkstyle.Main " +
                                "-p config/checkstyle.properties " +
                                "-b xpath " +
                                "-c config/checkstyle.xml " +
                                "-d -E " +
                                "src/main/java src/test/java");
    }

    @Test
    void executeIgnoredModules() {
        var op = new CheckstyleOperation().fromProject(new Project()).executeIgnoredModules(true);
        assertThat(op.options.containsKey("-E")).as(ADD).isTrue();
        op = op.executeIgnoredModules(false);
        assertThat(op.options.containsKey("-E")).as(REMOVE).isFalse();
    }

    @Test
    void executeSunChecks() throws IOException {
        var tmpFile = File.createTempFile("checkstyle-sun", ".txt");
        tmpFile.deleteOnExit();
        var op = new CheckstyleOperation()
                .fromProject(new WebProject())
                .sourceDir(List.of("src/main/java", "src/test/java"))
                .configurationFile("src/test/resources/sun_checks.xml")
                .outputPath(tmpFile.getAbsolutePath());
        assertThatCode(op::execute).isInstanceOf(ExitStatusException.class);
        assertThat(tmpFile).exists();
    }

    @Test
    void format() {
        var op = new CheckstyleOperation().fromProject(new Project()).format(CheckstyleFormatOption.XML);
        assertThat(op.options.get("-f")).isEqualTo("xml");
    }

    @Test
    void generateXpathSuppression() {
        var op = new CheckstyleOperation().fromProject(new Project()).generateXpathSuppression(true);
        assertThat(op.options.containsKey("-g")).as(ADD).isTrue();
        op = op.generateXpathSuppression(false);
        assertThat(op.options.containsKey("-g")).as(REMOVE).isFalse();
    }

    @Test
    void javadocTree() {
        var op = new CheckstyleOperation().fromProject(new Project()).javadocTree(true);
        assertThat(op.options.containsKey("-j")).as(ADD).isTrue();
        op = op.javadocTree(false);
        assertThat(op.options.containsKey("-j")).as(REMOVE).isFalse();
    }

    @Test
    void outputPath() {
        var op = new CheckstyleOperation().fromProject(new Project()).outputPath(FOO);
        assertThat(op.options.get("-o")).isEqualTo(FOO);
    }

    @Test
    void propertiesFile() {
        var op = new CheckstyleOperation().fromProject(new Project()).propertiesFile(FOO);
        assertThat(op.options.get("-p")).isEqualTo(FOO);
    }

    @Test
    void sourceDir() {
        var op = new CheckstyleOperation().fromProject(new Project()).sourceDir(FOO);
        assertThat(op.sourceDirs).contains(FOO);
        op = op.sourceDir(FOO, BAR);
        assertThat(op.sourceDirs).as("foo, bar").hasSize(2).contains(FOO).contains(BAR);
    }

    @Test
    void suppressionLineColumnNumber() {
        var op = new CheckstyleOperation().fromProject(new Project()).suppressionLineColumnNumber(FOO + ':' + BAR);
        assertThat(op.options.get("-s")).isEqualTo(FOO + ':' + BAR);
    }

    @Test
    void tabWith() {
        var op = new CheckstyleOperation().fromProject(new Project()).tabWith(9);
        assertThat(op.options.get("-w")).isEqualTo("9");
    }

    @Test
    void tree() {
        var op = new CheckstyleOperation().fromProject(new Project()).tree(true);
        assertThat(op.options.containsKey("-t")).as(ADD).isTrue();
        op = op.tree(false);
        assertThat(op.options.containsKey("-t")).as(REMOVE).isFalse();
    }

    @Test
    void treeWithComments() {
        var op = new CheckstyleOperation().fromProject(new Project()).treeWithComments(true);
        assertThat(op.options.containsKey("-T")).as(ADD).isTrue();
        op = op.treeWithComments(false);
        assertThat(op.options.containsKey("-T")).as(REMOVE).isFalse();
    }

    @Test
    void treeWithJavadoc() {
        var op = new CheckstyleOperation().fromProject(new Project()).treeWithJavadoc(true);
        assertThat(op.options.containsKey("-J")).as(ADD).isTrue();
        op = op.treeWithJavadoc(false);
        assertThat(op.options.containsKey("-J")).as(REMOVE).isFalse();
    }
}
