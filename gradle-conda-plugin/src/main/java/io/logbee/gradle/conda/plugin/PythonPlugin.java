package io.logbee.gradle.conda.plugin;

import io.logbee.gradle.conda.CondaPlugin;
import io.logbee.gradle.conda.conda.actions.InstallDependenciesAction;
import io.logbee.gradle.conda.python.PythonPluginExtension;
import io.logbee.gradle.conda.python.internal.DefaultPythonPluginExtension;
import io.logbee.gradle.conda.python.test.PyTestTask;
import io.logbee.gradle.python.PythonBasePlugin;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Delete;

import javax.inject.Inject;

import static io.logbee.gradle.python.Configurations.API_CONFIGURATION_NAME;
import static io.logbee.gradle.python.Configurations.TEST_CONFIGURATION_NAME;

public class PythonPlugin implements Plugin<ProjectInternal> {

    private final ObjectFactory objectFactory;

    @Inject
    public PythonPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(ProjectInternal project) {
        project.getPlugins().apply(CondaPlugin.class);
        project.getPlugins().apply(PythonBasePlugin.class);

        addExtensions(project);
        registerTasks(project);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                final InstallDependenciesAction installAction = objectFactory.newInstance(InstallDependenciesAction.class, project);
                installAction.execute(project.getConfigurations().getByName(API_CONFIGURATION_NAME).getIncoming());
                installAction.execute(project.getConfigurations().getByName(TEST_CONFIGURATION_NAME).getIncoming());
            }
        });
    }

    private void addExtensions(final ProjectInternal project) {
        final PythonPluginExtension extension = project.getExtensions().create(PythonPluginExtension.class, "python", DefaultPythonPluginExtension.class, project, objectFactory);
    }

    private void registerTasks(ProjectInternal project) {
        PyTestTask.register(project);
        registerCleanTask(project);
    }

    private void registerCleanTask(Project project) {
        if (project.getTasks().findByName("clean") == null) {
            project.getTasks().register("clean", Delete.class, task -> {
                task.setGroup("Build");
                task.setDescription("Deletes the build directory.");
                task.delete(project.getBuildDir());
            });
        }
    }
}
