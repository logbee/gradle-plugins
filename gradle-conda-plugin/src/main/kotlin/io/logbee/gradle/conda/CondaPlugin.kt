package io.logbee.gradle.conda

import groovy.lang.Closure
import io.logbee.gradle.conda.Configurations.MINICONDA_INSTALLER_CONFIGURATION_NAME
import io.logbee.gradle.conda.conda.actions.BootstrapMinicondaAction
import io.logbee.gradle.conda.conda.actions.CreateCondaEnvironmentAction
import io.logbee.gradle.conda.internal.CondaExtensionImpl
import io.logbee.gradle.conda.internal.MinicondaExtensionImpl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject


class CondaPlugin @Inject constructor(private val objectFactory: ObjectFactory) : Plugin<Project> {

    companion object {
        const val IVY_REPO_URL = "https://repo.continuum.io"
    }

    override fun apply(project: Project) {
        createExtensions(project)
        createRepository(project)
        createConfigurations(project)
        createMinicondaInstallerDependency(project)
        createAfterEvaluationHooks(project)
    }

    private fun createExtensions(project: Project) {
        project.extensions.create(CondaExtension::class.java, "conda", CondaExtensionImpl::class.java, project)
        project.extensions.create(MinicondaExtension::class.java, "miniconda", MinicondaExtensionImpl::class.java, project)
    }

    private fun createRepository(project: Project) {
        project.repositories.ivy { ivy ->
            ivy.setUrl(IVY_REPO_URL)
            ivy.patternLayout { layout ->
                layout.artifact("[organisation]/[module]-[revision]-[classifier].[ext]")
            }
        }
    }

    private fun createConfigurations(project: Project) {
        project.configurations.create(MINICONDA_INSTALLER_CONFIGURATION_NAME) { configuration ->
            configuration.isCanBeResolved = true
            configuration.isCanBeConsumed = false
            configuration.isTransitive = false
            configuration.isVisible = false
        }

        // Intercept the declaration of dependencies to hook-in functionality later.
        val dependencyHandler = project.dependencies
        dependencyHandler.extensions.add("conda", object : Closure<Dependency?>(dependencyHandler) {
            override fun call(vararg args: Any): Dependency? {
                return dependencyHandler.create(args[0])
            }
        })
    }

    private fun createMinicondaInstallerDependency(project: Project) {
        val extension = project.extensions.getByType(MinicondaExtension::class.java)
        val miniconda = extension.minicondaNotation
        val minicondaConfiguration = project.configurations.getByName(MINICONDA_INSTALLER_CONFIGURATION_NAME)
        minicondaConfiguration.incoming.beforeResolve { dependencies ->
            if (dependencies.dependencies.isEmpty()) {
                val handler = project.dependencies
                val minicondaInstaller = handler.add(MINICONDA_INSTALLER_CONFIGURATION_NAME, miniconda.asModuleNotation()) as ModuleDependency
                minicondaInstaller.artifact { artifact ->
                    artifact.name = miniconda.name
                    artifact.type = miniconda.extension
                    artifact.classifier = miniconda.classifier
                    artifact.extension = miniconda.extension
                }
            }
        }
    }

    private fun createAfterEvaluationHooks(project: Project) {
        project.afterEvaluate(objectFactory.newInstance(BootstrapMinicondaAction::class.java));
        project.afterEvaluate(objectFactory.newInstance(CreateCondaEnvironmentAction::class.java));
    }
}