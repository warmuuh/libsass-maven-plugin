Libsass Maven Plugin
==========

Libsass Maven Plugin uses [libsass](http://github.com/hcatlin/libsass) to compile sass files.
Uses Jna to interface with C-library.

Installation
-----
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
         <groupId>wrm</groupId>
         <artifactId>libsass-maven-plugin</artifactId>
         <version>0.1.0-SNAPSHOT</version>
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
            <outputPath>${project.basedir}/target/</outputPath>
            <includePath>${basedir}/src/main/sass/plugins/</includePath>
         </configuration>
      </plugin>
   </plugins>
</build>
```



License
-------

MIT License.