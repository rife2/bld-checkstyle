package com.example;

import rife.bld.BaseProject;
import rife.bld.BuildCommand;
import rife.bld.extension.CheckstyleOperation;

import java.util.List;

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

        scope(test).include(dependency("com.puppycrawl.tools", "checkstyle", version(10, 20, 0)));

        testOperation().mainClass("com.example.ExamplesTest");
    }

    public static void main(String[] args) {
        new ExamplesBuild().start(args);
    }

    @BuildCommand(summary = "Check code style")
    public void checkstyle() throws Exception {
        new CheckstyleOperation()
                .fromProject(this)
                .configurationFile("src/test/resources/sun_checks.xml")
                .execute();
    }

}
