package io.logbee.gradle.protobuf.tasks;

import io.logbee.gradle.protobuf.Backend;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GenerateProtobufTask extends DefaultTask {

    private final Logger log = getLogger();
    private final ExecActionFactory execActionFactory;

    private File protocExecutable;
    private final ConfigurableFileCollection sourceFiles;
    private final ConfigurableFileCollection includeDirectories;
    private File outputBaseDirectory;
    private Backend backend;

    @Inject
    public GenerateProtobufTask(ExecActionFactory execActionFactory) {
        this.execActionFactory = execActionFactory;
        this.sourceFiles = getProject().files();
        this.includeDirectories = getProject().files();
    }

    public File getProtocExecutable() {
        return protocExecutable;
    }

    public void setProtocExecutable(File protocExecutable) {
        this.protocExecutable = protocExecutable;
    }

    @SkipWhenEmpty
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getSourceFiles() {
        return sourceFiles;
    }

    public void sourceFiles(Object... paths) {
        sourceFiles.from(paths);
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getIncludeDirs() {
        return includeDirectories;
    }

    public void includeDirs(Object... paths) {
        includeDirectories.from(paths);
    }

    @OutputDirectory
    public File getOutputBaseDir() {
        return outputBaseDirectory;
    }

    public void setOutputBaseDir(File outputBaseDirectory) {
        this.outputBaseDirectory = outputBaseDirectory;
    }

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    @TaskAction
    private void generate() {

        final ExecAction action = execActionFactory.newExecAction();
        final ExecResult result;

        if (outputBaseDirectory.exists()) {
            getProject().delete(outputBaseDirectory);
            outputBaseDirectory.mkdirs();
        }

        action.setStandardOutput(System.out);
        action.setErrorOutput(System.err);

        action.setExecutable(getProtocExecutable());

        action.args(getBackend().asArg(getOutputBaseDir().getPath()));

        for (File file : includeDirectories) {
            action.args("-I" + file.getPath());
        }

        for (File file : expand(sourceFiles)) {

            if (file.isDirectory()) {

            } else {

            }

            log.debug("Generating {} from '{}'.", getBackend(), file);
            action.args(file.getPath());
        }

        result = action.execute();
    }

    private List<File> expand(ConfigurableFileCollection files) {
        final List<File> result = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getProject().fileTree(file).getFiles());
            } else {
                result.add(file);
            }
        }

        return result;
    }
}
