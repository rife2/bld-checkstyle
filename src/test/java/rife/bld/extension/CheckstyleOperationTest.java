/*
 * Copyright 2023 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;


class CheckstyleOperationTest {
    private static final String BAR = "bar";
    private static final String FOO = "foo";

    @Test
    void branchMatchingXpath() {
        var op = new CheckstyleOperation().fromProject(new Project()).branchMatchingXpath(FOO);
        assertThat(op.optionsWithArg.get("-b")).isEqualTo(FOO);
    }

    @Test
    void configurationFile() {
        var op = new CheckstyleOperation().fromProject(new Project()).configurationFile(FOO);
        assertThat(op.optionsWithArg.get("-c")).isEqualTo(FOO);
    }

    @Test
    void debug() {
        var op = new CheckstyleOperation().fromProject(new Project()).debug(true);
        assertThat(op.options.contains("-d")).as("add").isTrue();
        op = op.debug(false);
        assertThat(op.options.contains("-d")).as("remove").isFalse();
    }

    @Test
    void exclude() {
        var op = new CheckstyleOperation().fromProject(new Project()).exclude(FOO);
        assertThat(op.optionsWithArg.get("-e")).isEqualTo(FOO);
    }

    @Test
    void excludedPathPattern() {
        var op = new CheckstyleOperation().fromProject(new Project()).excludedPathPattern(FOO);
        assertThat(op.optionsWithArg.get("-x")).isEqualTo(FOO);
    }

    @Test
    void execute() throws IOException, ExitStatusException, InterruptedException {
        var tmpFile = File.createTempFile("checkstyle", ".txt");
        tmpFile.deleteOnExit();
        var op = new CheckstyleOperation()
                .fromProject(new WebProject())
                .sourceDir("src/main/java", "src/test/java")
                .configurationFile("src/test/resources/google_checks.xml")
                .output(tmpFile.getAbsolutePath());
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
                        "-d -E " +
                        "-p config/checkstyle.properties " +
                        "-b xpath " +
                        "-c config/checkstyle.xml " +
                        "src/main/java src/test/java");
    }

    @Test
    void executeIgnoredModules() {
        var op = new CheckstyleOperation().fromProject(new Project()).executeIgnoredModules(true);
        assertThat(op.options.contains("-E")).as("add").isTrue();
        op = op.executeIgnoredModules(false);
        assertThat(op.options.contains("-E")).as("remove").isFalse();
    }

    @Test
    void format() {
        var op = new CheckstyleOperation().fromProject(new Project()).format(FOO);
        assertThat(op.optionsWithArg.get("-f")).isEqualTo(FOO);
    }

    @Test
    void generateXpathSuppression() {
        var op = new CheckstyleOperation().fromProject(new Project()).generateXpathSuppression(true);
        assertThat(op.options.contains("-g")).as("add").isTrue();
        op = op.generateXpathSuppression(false);
        assertThat(op.options.contains("-g")).as("remove").isFalse();
    }

    @Test
    void javadocTree() {
        var op = new CheckstyleOperation().fromProject(new Project()).javadocTree(true);
        assertThat(op.options.contains("-j")).as("add").isTrue();
        op = op.javadocTree(false);
        assertThat(op.options.contains("-j")).as("remove").isFalse();
    }

    @Test
    void lineColumn() {
        var op = new CheckstyleOperation().fromProject(new Project()).lineColumn(FOO);
        assertThat(op.optionsWithArg.get("-s")).isEqualTo(FOO);
    }

    @Test
    void output() {
        var op = new CheckstyleOperation().fromProject(new Project()).output(FOO);
        assertThat(op.optionsWithArg.get("-o")).isEqualTo(FOO);
    }

    @Test
    void propertiesFile() {
        var op = new CheckstyleOperation().fromProject(new Project()).propertiesFile(FOO);
        assertThat(op.optionsWithArg.get("-p")).isEqualTo(FOO);
    }

    @Test
    void sourceDir() {
        var op = new CheckstyleOperation().fromProject(new Project()).sourceDir(FOO);
        assertThat(op.sourceDirs).contains(FOO);
        op = op.sourceDir(FOO, BAR);
        assertThat(op.sourceDirs).as("foo, bar").hasSize(2).contains(FOO).contains(BAR);
    }

    @Test
    void tabWith() {
        var op = new CheckstyleOperation().fromProject(new Project()).tabWith(9);
        assertThat(op.optionsWithArg.get("-w")).isEqualTo("9");
    }

    @Test
    void tree() {
        var op = new CheckstyleOperation().fromProject(new Project()).tree(true);
        assertThat(op.options.contains("-t")).as("add").isTrue();
        op = op.tree(false);
        assertThat(op.options.contains("-t")).as("remove").isFalse();
    }

    @Test
    void treeWithComments() {
        var op = new CheckstyleOperation().fromProject(new Project()).treeWithComments(true);
        assertThat(op.options.contains("-T")).as("add").isTrue();
        op = op.treeWithComments(false);
        assertThat(op.options.contains("-T")).as("remove").isFalse();
    }

    @Test
    void treeWithJavadoc() {
        var op = new CheckstyleOperation().fromProject(new Project()).treeWithJavadoc(true);
        assertThat(op.options.contains("-J")).as("add").isTrue();
        op = op.treeWithJavadoc(false);
        assertThat(op.options.contains("-J")).as("remove").isFalse();
    }
}