package io.logbee.gradle.conda.conda.actions;

import io.logbee.gradle.conda.CondaExtension;
import io.logbee.gradle.conda.MinicondaExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

import javax.inject.Inject;
import java.io.File;

public class CreateCondaEnvironmentAction implements Action<Project> {

    private final ExecActionFactory execActionFactory;

    @Inject
    public CreateCondaEnvironmentAction(ExecActionFactory execActionFactory) {
        this.execActionFactory = execActionFactory;
    }

    @Override
    public void execute(Project project) {
        final MinicondaExtension minicondaExtension = project.getExtensions().getByType(MinicondaExtension.class);
        final CondaExtension conda = project.getExtensions().getByType(CondaExtension.class);
        final ExecAction action = execActionFactory.newExecAction();
        final File dir = conda.getEnvironmentDir();

        action.executable(minicondaExtension.getCondaExecutable());
        action.args("create", "--prefix", dir, "python=3.7");

        if (!dir.exists()) {
            try {
                action.execute();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create conda-environment in: " + dir, e);
            }
        }
    }
}
