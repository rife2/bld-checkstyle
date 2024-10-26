# [Checkstyle](https://checkstyle.sourceforge.io/) Extension for [b<span style="color:orange">l</span>d](https://rife2.com/bld)

[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.1.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/releases/com/uwyn/rife2/bld-checkstyle/maven-metadata.xml?color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-checkstyle)
[![Snapshot](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/snapshots/com/uwyn/rife2/bld-checkstyle/maven-metadata.xml?label=snapshot)](https://repo.rife2.com/#/snapshots/com/uwyn/rife2/bld-checkstyle)
[![GitHub CI](https://github.com/rife2/bld-checkstyle/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-checkstyle/actions/workflows/bld.yml)

To install, please refer to the [extensions documentation](https://github.com/rife2/bld/wiki/Extensions).

To check your code with Chesktyle, include the following in your build file:

```java
@BuildCommand(summary = "Check code style")
public void checkstyle() throws Exception {
    new CheckstyleOperation()
            .fromProject(this)
            .configurationFile("config/sun_checks.xml")
            .execute();
}
```

```console
./bld checkstyle
```

- [View Examples](https://github.com/rife2/bld-checkstyle/blob/master/examples/src/bld/java/com/example/)

Please check the [CheckstyleOperation documentation](https://rife2.github.io/bld-checkstyle/rife/bld/extension/CheckstyleOperation.html#method-summary)
for all available configuration options.

## Checkstyle Dependency

Don't forget to add a Checkstyle `test` dependency to your build file, as it is
not provided by the extension. For example:

```java
repositories = List.of(MAVEN_CENTRAL);
scope(test).include(dependency("com.puppycrawl.tools", "checkstyle", version(10, 19, 0)));
```
