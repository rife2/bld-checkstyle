package com.example;

import rife.bld.BaseProject;
import rife.bld.BuildCommand;
import rife.bld.extension.CheckstyleOperation;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Scope.*;

public class ExamplesBuild extends BaseProject {
    public ExamplesBuild() {
        pkg = "com.example";
        name = "Examples";
        mainClass = "com.example.ExamplesMain";
        version = version(0, 1, 0);

        javaRelease = 17;

        autoDownloadPurge = true;
        downloadSources = true;

        repositories = List.of(MAVEN_CENTRAL);

        scope(test).include(dependency("com.puppycrawl.tools", "checkstyle", version(12, 3, 1)));

        testOperation().mainClass("com.example.ExamplesTest");
    }

    public static void main(String[] args) {
        // Enable detailed logging for the extensions
        var level = Level.ALL;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();

        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);

        new ExamplesBuild().start(args);
    }

    @BuildCommand(summary = "Check code style using Sun coding conventions")
    public void checkstyle() throws Exception {
        new CheckstyleOperation()
                .fromProject(this)
                .configurationFile("src/test/resources/sun_checks.xml")
                .execute();
    }

    @BuildCommand(value = "checkstyle-custom", summary = "Check code style using custom coding conventions")
    public void checkstyleCustom() throws Exception {
        new CheckstyleOperation()
                .fromProject(this)
                .configurationFile("src/test/resources/checkstyle.xml")
                .execute();
    }
}
