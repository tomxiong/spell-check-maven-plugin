[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://raw.githubusercontent.com/tomxiong/spell-check-maven-plugin/main/LICENSE)
[![CI of Github Action](https://github.com/tomxiong/spell-check-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/tomxiong/spell-check-maven-plugin/actions/workflows/maven.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=tomxiong_spell-check-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=tomxiong_spell-check-maven-plugin)
[![Coverage Status](https://coveralls.io/repos/github/tomxiong/spell-check-maven-plugin/badge.svg?branch=main)](https://coveralls.io/github/tomxiong/spell-check-maven-plugin?branch=main)

# maven-spell-checker-plugin
The maven plugin for spell checker with specific files contains text which show in UI or message
 from properties and xml file.

# How to use it in maven project
Add below plugin to pom file
```
<plugin>
    <groupId>com.github.tomxiong</groupId>
    <artifactId>spell-check-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>validate</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <excludes>
            <exclude>**/*.java</exclude>
            <exclude>**/esapi/**</exclude>
        </excludes>
        <map>
            <simple-rule>name,comments,help</simple-rule>
            <severity-family>name,comments,help</severity-family>
            <message>CDATA</message>
            <string-rv>CDATA</string-rv>
        </map>
        <allowWord>foglight,fglam,pi,fgl</allowWord>
        <customDictionaryFile>customWord.txt</customDictionaryFile>
    </configuration>
</plugin>
```
# How to execute the spell check in project?
You can execute *mvn install* to build the project and the check action will be executed in validate step or you can execute below:
```
mvn spell-check-maven-plugin:check
```
It will execute spell check for all text of UI element or properties of you custom
Note: This plugin support properties file, java, groovy by default.For xml, you should define which part contains text show in ui, so the plugin will figure out the text base on the definition.  
```
mvn spell-check-maven-plugin:list
```
It will list all text instead of check. We can use it to build your custom dictionary base on your domain common language with this list file.
Note: It will help the user to tuning the definition of xml ui or custom dictionary.

# How to find the result of spell check?
You can find the _spelling_check_result.txt_ as the spell check result in the project folder when you execute the spell check.

# How to add your custom dictionary for your domain common language
You can add *<allowword>* for a few words or add a txt file to *<customeDictionaryFile>* for a lot of words.
Note: the customDictionaryFile format like below:
```
NAS
Cratio
```

# How to get help when you use this plugin
You can file a issue to this project in github if you have any question or find a bug or ask an enhancement. 

# How to extend the usage
Because its target to check the UI for specific UI project which using XML and properties to build the message of UI.
You can fork it and improve it by yourselves.

# Next plan
- [ ] Add groovy script support custom logical to find out the text element of XML ui
- [ ] Add ML support to build custom dictionary base on the specific domain of projects. 
