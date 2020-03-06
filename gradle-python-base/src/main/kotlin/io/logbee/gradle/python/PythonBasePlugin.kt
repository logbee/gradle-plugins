package io.logbee.gradle.python

import io.logbee.gradle.python.Configurations.API_CONFIGURATION_NAME
import io.logbee.gradle.python.Configurations.DEFAULT_CONFIGURATION_NAME
import io.logbee.gradle.python.Configurations.RUNTIME_CONFIGURATION_NAME
import io.logbee.gradle.python.Configurations.TEST_CONFIGURATION_NAME
import io.logbee.gradle.python.Configurations.TEST_RUNTIME_CONFIGURATION_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.tasks.DefaultSourceSetContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSetContainer
import javax.inject.Inject

class PythonBasePlugin @Inject constructor(val objectFactory: ObjectFactory) : Plugin<Project> {

    override fun apply(project: Project) {
        createSourceSets(project)
        createConfigurations(project)
    }

    private fun createSourceSets(project: Project) {

        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)?: project.extensions.create("sourceSets", DefaultSourceSetContainer::class.java)

        sourceSets.maybeCreate(MAIN_SOURCE_SET_NAME)
        sourceSets.maybeCreate(TEST_SOURCE_SET_NAME)

        sourceSets.all { sourceSet ->
            val sources = objectFactory.sourceDirectorySet("python", "${sourceSet.name} Python sources")
            sources.srcDir("src/" + sourceSet.name + "/python")
            sources.filter.include("**/*.py")

            val resources = objectFactory.sourceDirectorySet("resources", "${sourceSet.name} resources")
            resources.srcDir("src/" + sourceSet.name + "/resources")
            resources.filter.include("**/*.*")

            sourceSet.extensions.add("python", sources)
            if (sourceSet.extensions.findByName("resources") != null) {
                sourceSet.extensions.add("resources", resources)
            }
        }
    }

    private fun createConfigurations(project: Project) {

        project.configurations.create(DEFAULT_CONFIGURATION_NAME)

        val apiConfiguration = project.configurations.create(API_CONFIGURATION_NAME) { configuration ->
            configuration.description = "This is where you should declare dependencies."
            configuration.isCanBeResolved = false
            configuration.isCanBeConsumed = false
            configuration.isTransitive = true
            configuration.isVisible = true
        }

        val testConfiguration = project.configurations.create(TEST_CONFIGURATION_NAME) { configuration ->
            configuration.description = "This is where you should declare dependencies required for tests."
            configuration.isCanBeResolved = false
            configuration.isCanBeConsumed = false
            configuration.isTransitive = true
            configuration.isVisible = true
            configuration.extendsFrom(apiConfiguration)
        }

        project.configurations.create(RUNTIME_CONFIGURATION_NAME) { configuration ->
            configuration.description = ""
            configuration.isCanBeResolved = false
            configuration.isCanBeConsumed = true
            configuration.isTransitive = true
            configuration.isVisible = false
            configuration.extendsFrom(apiConfiguration)
        }

        project.configurations.create(TEST_RUNTIME_CONFIGURATION_NAME) { configuration ->
            configuration.description = ""
            configuration.isCanBeResolved = false
            configuration.isCanBeConsumed = true
            configuration.isTransitive = true
            configuration.isVisible = false
            configuration.extendsFrom(apiConfiguration, testConfiguration)
        }
    }
}