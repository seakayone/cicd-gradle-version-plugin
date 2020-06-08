package org.kleinb.gradle.plugin.version

import org.ajoberstar.grgit.Grgit
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class VersionPluginSpock extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File propertiesFile
    File buildFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        propertiesFile = testProjectDir.newFile('gradle.properties')
        buildFile = testProjectDir.newFile('build.gradle')
        settingsFile << "rootProject.name='hello-world';"
        propertiesFile << "version=foo"
        buildFile << """
            plugins {
                id 'org.kleinb.gradle.version'  
            }
            task printVersion {
                doLast {
                    println "Hello world! \$project.version"
                }
            }
        """.stripIndent()
    }

    def "build fails if no git directory is present"() {
        when:
        def result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments('printVersion')
                .buildAndFail()

        then:
        result.output.contains("repository not found")
    }


    def "build succeeds if git directory is present"() {
        given:
        def git = Grgit.init(dir: testProjectDir.root.path)
        git.commit(message: "Init")

        when:
        def result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments('printVersion')
                .build()

        then:
        result.output.contains(git.head().abbreviatedId)
        result.task(":printVersion").outcome == TaskOutcome.SUCCESS
    }
}

