Libsass Maven Plugin
==========

Libsass Maven Plugin uses [libsass](http://github.com/hcatlin/libsass) to compile sass files.
Uses Jna to interface with C-library.

Changelog:
* 0.1.1 - scss files can now be placed in inputpath/ directly
* 0.1.0 - changed artefact group to `com.github.warmuuh`

Installation
-----
either add on-demand-repository (using https://jitpack.io/)
```
<pluginRepositories>
    <pluginRepository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </pluginRepository>
</pluginRepositories>
```

or install locally

```
git clone --recursive https://github.com/warmuuh/libsass-maven-plugin.git
cd libsass-maven-plugin
mvn install
```


Usage
-----
Configure plugin in your pom.xml:

```
<build>
   <plugins>
      <plugin>
         <groupId>com.github.warmuuh</groupId>
         <artifactId>libsass-maven-plugin</artifactId>
         <version>0.1.1</version>
         <executions>
            <execution>
               <phase>generate-resources</phase>
               <goals>
                  <goal>compile</goal>
               </goals>
            </execution>
         </executions>
         <configuration>
            <imgPath>../img</imgPath>
            <inputPath>${basedir}/src/main/sass/</inputPath>
            <outputPath>${basedir}/target/</outputPath>
            <includePath>${basedir}/src/main/sass/plugins/</includePath>
         </configuration>
      </plugin>
   </plugins>
</build>
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
      <td>`${project.build.directory}`</td>
      <td>The directory in which the compiled CSS files will be placed.</td>
    </tr>
    <tr>
      <td>inputPath</td>
      <td>`src/main/sass`</td>
      <td>
        The directory from which the source `.scss` files will be read. This directory will be
        traversed recursively, and all `.scss` files found in this directory or subdirectories
        will be compiled.
      </td>
    </tr>
    <tr>
      <td>imagePath</td>
      <td>`null`</td>
      <td>Location of images to for use by the image-url Sass function.</td>
    </tr>
    <tr>
      <td>includePath</td>
      <td>`null`</td>
      <td>Additional include path, ';'-separated</td>
    </tr>
    <tr>
      <td>outputStyle</td>
      <td>expanded</td>
      <td>
         Output style for the generlookated css code. One of `nested`, `expanded`,
         `compact`, `compressed`. Note that as of libsass 3.1, `expanded`
         and `compact` result in the same output as `nested`.
      </td>
    </tr>
    <tr>
      <td>generateSourceComments</td>
      <td>`false`</td>
      <td>
         Emit comments in the compiled CSS indicating the corresponding source line. The default
         value is `false`.
      </td>
    </tr>
    <tr>
      <td>generateSourceMap</td>
      <td>`true`</td>
      <td>
        Generate source map files. The generated source map files will be placed in the directory
        specified by `sourceMapOutputPath`.
      </td>
    </tr>
    <tr>
      <td>sourceMapOutputPath</td>
      <td>`${project.build.directory}`</td>
      <td>
        The directory in which the source map files that correspond to the compiled CSS will be placed
      </td>
    </tr>
    <tr>
      <td>omitSourceMapingURL</td>
      <td>`false`</td>
      <td>
        Prevents the generation of the `sourceMappingURL` special comment as the last
        line of the compiled CSS.
      </td>
    </tr>
    <tr>
      <td>embedSourceMapInCSS</td>
      <td>`false`</td>
      <td>
        Embeds the whole source map data directly into the compiled CSS file by transforming
        `sourceMappingURL` into a data URI.
      </td>
    </tr>
    <tr>
      <td>embedSourceContentsInSourceMap</td>
      <td>`false`</td>
      <td>
       Embeds the contents of the source `.scss` files in the source map file instead of the
       paths to those files
      </td>
    </tr>
    <tr>
      <td>inputSyntax</td>
      <td>`scss`</td>
      <td>
       Switches the input syntax used by the files to either `sass` or `scss`.
      </td>
    </tr>
    <tr>
      <td>precision</td>
      <td>`5`</td>
      <td>
       Precision for fractional numbers
      </td>
    </tr>
  </tbody>
</table>

For windows, linux64 and osx, there are binaries included.

For rest: you probably have to compile libsass and add it by using -Djna.library.path=(path to the binary)


License
-------

MIT License.
