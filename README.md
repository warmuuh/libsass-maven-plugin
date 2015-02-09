Libsass Maven Plugin
==========

Libsass Maven Plugin uses [libsass](http://github.com/hcatlin/libsass) to compile sass files.
Uses Jna to interface with C-library.

Changelog:
* 0.1.0 - changed artefact group to `com.github.warmuuh`

Installation
-----
either add on-demand-repository (using https://jitpack.io/)
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

or install locally

```
git clone https://github.com/warmuuh/libsass-maven-plugin.git
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
         <version>0.1.0</version>
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

For windows, linux64 and osx, there are binaries included.

For rest: you probably have to compile libsass and add it by using -Djna.library.path=(path to the binary)


License
-------

MIT License.
