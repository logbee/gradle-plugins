package io.logbee.gradle.protobuf.python;

import io.logbee.gradle.protobuf.ProtobufExtension;
import io.logbee.gradle.protobuf.ProtobufPlugin;
import io.logbee.gradle.protobuf.ProtobufProviderPlugin;
import io.logbee.gradle.protobuf.tasks.GenerateProtobufTask;
import io.logbee.gradle.protobuf.tasks.PrepareProtobufTask;
import io.logbee.gradle.python.PythonBasePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;

import static io.logbee.gradle.protobuf.Backend.Python;
import static io.logbee.gradle.protobuf.ProtobufPlugin.PROTOC_CONFIGURATION_NAME;
import static io.logbee.gradle.protobuf.ProtobufProviderPlugin.PROTOBUF_SOURCESET_NAME;
import static io.logbee.gradle.protobuf.tasks.TaskNames.*;

public class ProtobufPythonPlugin implements Plugin<Project> {

    private final ObjectFactory objectFactory;

    @Inject
    public ProtobufPythonPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(final Project project) {
        project.getPlugins().apply(ProtobufPlugin.class);
        project.getPlugins().apply(PythonBasePlugin.class);

        final ProtobufExtension protobufExtension = project.getExtensions().getByType(ProtobufExtension.class);
        final SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        sourceSets.all(sourceSet -> {

            final SourceDirectorySet protoSourceDirectorySet = (SourceDirectorySet) sourceSet.getExtensions().getByName(PROTOBUF_SOURCESET_NAME);
            final Configuration generateConfiguration = project.getConfigurations().getByName(ProtobufProviderPlugin.PROTOBUF_GENERATE_CONFIGURATION_NAME);
            final Configuration includeConfiguration = project.getConfigurations().getByName(ProtobufProviderPlugin.PROTOBUF_INCLUDE_CONFIGURATION_NAME);

            final SourceDirectorySet pythonSourceDirectorySet = (SourceDirectorySet) sourceSet.getExtensions().getByName(Python.getSourceDirectoryName());

            final TaskProvider<PrepareProtobufTask> prepareSourcesTask = project.getTasks().register(getPrepareSourcesTaskName(sourceSet, Python), PrepareProtobufTask.class, task -> {

                task.setGroup("Protobuf");
                task.setDescription("Prepares '" + sourceSet.getName() +"' protobuf sources for code generation.");
                task.setDestinationDirectory(new File(project.getBuildDir(), "protobuf/" + Python.getSourceDirectoryName() + "/src/" + sourceSet.getName() + "/proto"));
                task.setRewritePackage(protobufExtension.getPython().getRewritePackage());
                task.prepare(protoSourceDirectorySet.getSourceDirectories());
                task.prepare(generateConfiguration);
            });

            final TaskProvider<PrepareProtobufTask> prepareIncludesTask = project.getTasks().register(getPrepareIncludesTaskName(sourceSet, Python), PrepareProtobufTask.class, task -> {

                task.setGroup("Protobuf");
                task.setDescription("Prepares '" + sourceSet.getName() +"' protobuf includes for code generation.");
                task.setDestinationDirectory(new File(project.getBuildDir(), "protobuf/" + Python.getSourceDirectoryName() + "/include/" + sourceSet.getName() + "/proto"));
                task.setRewritePackage(protobufExtension.getPython().getRewritePackage());
                task.prepare(includeConfiguration);
            });

            final TaskProvider<GenerateProtobufTask> generateTask = project.getTasks().register(getGenerateProtobufTaskName(sourceSet, Python), GenerateProtobufTask.class, task -> {

                task.setGroup("Protobuf");
                task.setDescription("Generates '" + sourceSet.getName() + "' python code.");
                task.setProtocExecutable(project.getConfigurations().getByName(PROTOC_CONFIGURATION_NAME).getSingleFile());
                task.setOutputBaseDir(new File(project.getBuildDir(), "protobuf/" + Python.getSourceDirectoryName() + "/generated/" + sourceSet.getName() + "/" + Python.getSourceDirectoryName()));
                task.setBackend(Python);
                task.sourceFiles(prepareSourcesTask);
                task.includeDirs(prepareSourcesTask);
                task.includeDirs(prepareIncludesTask);
            });

            pythonSourceDirectorySet.srcDir(generateTask);
        });

        final Configuration generateConfiguration = project.getConfigurations().getByName(ProtobufProviderPlugin.PROTOBUF_GENERATE_CONFIGURATION_NAME);
        final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        generateConfiguration.getIncoming().afterResolve(dependencies -> {
            for (Dependency dependency : dependencies.getDependencies()) {
                if (dependency instanceof ProjectDependency) {
                    final Project dependencyProject = ((ProjectDependency) dependency).getDependencyProject();
                    final Configuration includeConfiguration = dependencyProject.getConfigurations().getByName(ProtobufProviderPlugin.PROTOBUF_INCLUDE_CONFIGURATION_NAME);
                    final PrepareProtobufTask prepareIncludesTask = (PrepareProtobufTask) project.getTasks().getByName(getPrepareIncludesTaskName(mainSourceSet, Python));

                    prepareIncludesTask.prepare(includeConfiguration.getFiles());
                }
                else if (dependency instanceof ExternalModuleDependency) {
                    final ExternalModuleDependency externalDependency = (ExternalModuleDependency) dependency;
                    // TODO: Handle external dependencies.
                }
            }
        });

        // TODO: Is there a way to avoid this 'afterEvaluate' hook?
        project.afterEvaluate(__ -> {
            generateConfiguration.getFiles();
        });
    }
}
