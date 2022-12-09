# Unmaintained
this project is no longer maintained but a fork is: https://gitlab.com/haynes/libsass-maven-plugin


Libsass Maven Plugin [![Build Status](https://travis-ci.org/warmuuh/libsass-maven-plugin.svg?branch=master)](https://travis-ci.org/warmuuh/libsass-maven-plugin) [![Maven Central](https://img.shields.io/maven-central/v/com.github.warmuuh/libsass-maven-plugin.svg)](https://mvnrepository.com/artifact/com.github.warmuuh/libsass-maven-plugin)
==========

Libsass Maven Plugin uses [libsass](http://github.com/hcatlin/libsass) to compile sass files.
Uses [jsass](https://github.com/bit3/jsass) to interface with C-library.

Changelog:
* 0.2.10 - upgraded libsass to 3.5.3
* 0.2.9 - upgraded libsass to 3.4.7
  * refreshed output files for eclipse
  * enhanced error output with failing files - thanks to @VsevolodGolovanov
* 0.2.8 - upgraded libsass to 3.4.4
  * used compilation classpath for including webjars
  * fixed issue with os-dependent path-separator (using ';' for every OS)
  * plugin now aware of incremental builds
* 0.2.7 - upgraded libsass to 3.4.3
  * added webjar support - thanks to @flipp5b 
* 0.2.6 - upgraded libsass to 3.4.0
  * added libsass:watch goal to watch and recompile include directory - *thansk to @lorenzodee*
* 0.2.5 - added copySourceToOutput, changed default outputstyle to 'nested', upgraded libsass to 3.3.6
* 0.2.4 - fixed bug with empty spaces in path
* 0.2.3 - upgrade to libsass 3.3.4
* 0.2.2 - minor bugfixes, readded m2e lifecycle mapping 
* 0.2.1 - updated libsass to 3.3.3
* 0.2.0 - switched native bindings to bit3 bindings (using libsass 3.3.2), **java8-only**
* 0.1.7 - UTF8 encoding issue, used wrong file extension for sass style
* 0.1.6 - added m2e eclipse intergation, thanks @dashorst
* 0.1.5 - readded macOs binaries, thanks @tommix1987
* 0.1.4 - added contained libsass-version to artifact-version (e.g. `0.1.4-libsass_3.2.4-SNAPSHOT`). 
  * switched to new libsass API (sass_context.h)
  * removed image_path option (because of [#420](https://github.com/sass/libsass/issues/420))
  * added failOnError flag to skip errors and continue the build, if wanted
* 0.1.3 - fixed #10 - multi-module projects
* 0.1.2 - added PR #4, updated to libsass version 3.1 for windows, linux, macos - *thanks to @npiguet, @ogolberg*
* 0.1.1 - scss files can now be placed in inputpath/ directly
* 0.1.0 - changed artefact group to `com.github.warmuuh`

Installation
-----
libsass-maven-plugin is available on central-repository since version 0.1.2

Usage
-----
Configure plugin in your pom.xml:

```
<build>
   <plugins>
      <plugin>
         <groupId>com.github.warmuuh</groupId>
         <artifactId>libsass-maven-plugin</artifactId>
         <version><VERSION>-libsass_3.4.4</version>
         <executions>
            <execution>
               <phase>generate-resources</phase>
               <goals>
                  <goal>compile</goal>
               </goals>
            </execution>
         </executions>
         <configuration>
            <inputPath>${basedir}/src/main/sass/</inputPath>
            <outputPath>${basedir}/target/</outputPath>
            <includePath>${basedir}/src/main/sass/plugins/</includePath>
         </configuration>
      </plugin>
   </plugins>
</build>
```

Alternatively, you can use the `watch` goal to have the plugin watch your files and recompile on change:
```
mvn com.github.warmuuh:libsass-maven-plugin:<version>-libsass_3.4.4:watch
```

Configuration Elements
----------------------

<table>
  <thead>
    <tr>
       <td>Element</td>
       <td>Default value</td>
       <td>Documentation</td>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>outputPath</td>
      <td><code>${project.build.directory}</code></td>
      <td>The directory in which the compiled CSS files will be placed.</td>
    </tr>
    <tr>
      <td>inputPath</td>
      <td><code>src/main/sass</code></td>
      <td>
        The directory from which the source <code>.scss</code> files will be read. This directory will be
        traversed recursively, and all <code>.scss</code> files found in this directory or subdirectories
        will be compiled.
      </td>
    </tr>
    <tr>
      <td>includePath</td>
      <td><code>null</code></td>
      <td>Additional include path, ';'-separated</td>
    </tr>
    <tr>
      <td>outputStyle</td>
      <td><code>nested</code></td>
      <td>
         Output style for the generated css code. One of <code>nested</code>, <code>expanded</code>,
         <code>compact</code>, <code>compressed</code>. Note that as of libsass 3.1, <code>expanded</code>
         and <code>compact</code> result in the same output as <code>nested</code>.
      </td>
    </tr>
    <tr>
      <td>generateSourceComments</td>
      <td><code>false</code></td>
      <td>
         Emit comments in the compiled CSS indicating the corresponding source line. The default
         value is <code>false</code>.
      </td>
    </tr>
    <tr>
      <td>generateSourceMap</td>
      <td><code>true</code></td>
      <td>
        Generate source map files. The generated source map files will be placed in the directory
        specified by <code>sourceMapOutputPath</code>.
      </td>
    </tr>
    <tr>
      <td>sourceMapOutputPath</td>
      <td><code>${project.build.directory}</code></td>
      <td>
        The directory in which the source map files that correspond to the compiled CSS will be placed
      </td>
    </tr>
    <tr>
      <td>omitSourceMapingURL</td>
      <td><code>false</code></td>
      <td>
        Prevents the generation of the <code>sourceMappingURL</code> special comment as the last
        line of the compiled CSS.
      </td>
    </tr>
    <tr>
      <td>embedSourceMapInCSS</td>
      <td><code>false</code></td>
      <td>
        Embeds the whole source map data directly into the compiled CSS file by transforming
        <code>sourceMappingURL</code> into a data URI.
      </td>
    </tr>
    <tr>
      <td>embedSourceContentsInSourceMap</td>
      <td><code>false</code></td>
      <td>
       Embeds the contents of the source <code>.scss</code> files in the source map file instead of the
       paths to those files
      </td>
    </tr>
    <tr>
      <td>inputSyntax</td>
      <td><code>scss</code></td>
      <td>
       Switches the input syntax used by the files to either <code>sass</code> or <code>scss</code>.
      </td>
    </tr>
    <tr>
      <td>precision</td>
      <td><code>5</code></td>
      <td>
       Precision for fractional numbers
      </td>
    </tr>
    <tr>
      <td>enableClasspathAwareImporter</td>
      <td><code>false</code></td>
      <td>
       Enables classpath aware importer which make possible to <code>@import</code> files from classpath and WebJars.
       For classpath resources use <code>@import 'path/to/resource/in/classpath';</code>.
       For WebJar resources a shortcut can be used: <code>@import '{package}/{path}';</code> imports resource
       <code>META-INF/resources/webjars/{package}/{version}/{path}</code>.
      </td>
    </tr>
     <tr>
      <td>failOnError</td>
      <td><code>true</code></td>
      <td>
       should fail the build in case of compilation errors.
      </td>
    </tr>
    <tr>
      <td>copySourceToOutput</td>
      <td><code>false</code></td>
      <td>
       copies all files from source directory to output directory
      </td>
    </tr>
  </tbody>
</table>


License
-------

MIT License.
