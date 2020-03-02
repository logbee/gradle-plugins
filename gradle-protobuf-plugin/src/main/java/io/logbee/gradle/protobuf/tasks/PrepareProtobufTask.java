package io.logbee.gradle.protobuf.tasks;

import groovy.lang.Closure;
import io.logbee.gradle.protobuf.Backend;
import io.logbee.gradle.protobuf.python.ProtobufPythonExtension;
import io.logbee.gradle.protobuf.python.ProtobufPythonExtension.PackageRewrite;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.logbee.gradle.protobuf.Util.isTarArchive;
import static io.logbee.gradle.protobuf.Util.isZipArchive;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PrepareProtobufTask extends DefaultTask {

    private final Logger log = getLogger();
    private final ConfigurableFileCollection inputFiles = getProject().files();
    private File destinationDirectory;
    private PackageRewrite rewritePackage;

    @Inject
    public PrepareProtobufTask() {

    }

    @InputFiles
    @SkipWhenEmpty
    public ConfigurableFileCollection getInputFiles() {
        return inputFiles;
    }

    public void prepare(Object... paths) {
        inputFiles.from(paths);
    }

    public PackageRewrite getRewritePackage() {
        return rewritePackage;
    }

    public void setRewritePackage(PackageRewrite packageRewrite) {
        this.rewritePackage = packageRewrite;
    }

    @OutputDirectory
    protected File getDestinationDirectory() {
        return destinationDirectory;
    }

    public void setDestinationDirectory(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
        getOutputs().dir(destinationDirectory);
    }

    @TaskAction
    private void extract() throws IOException {

        destinationDirectory.mkdirs();

        inputFiles.forEach(file -> {
            log.debug("Preparing protos from '{}' to '{}'.", file, destinationDirectory);
            if (file.isDirectory()) {
                getProject().copy(copy -> {
                    copy.setIncludeEmptyDirs(false);
                    copy.from(file.getPath(), config -> config.include("**/*.proto"));
                    copy.into(destinationDirectory);
                });
            } else if (file.getPath().endsWith(".proto")) {
                getProject().copy(copy -> {
                    copy.setIncludeEmptyDirs(false);
                    copy.from(file.getPath());
                    copy.into(destinationDirectory);
                });
            } else if (isZipArchive(file)) {
                getProject().copy(copy -> {
                    copy.setIncludeEmptyDirs(false);
                    copy.from(getProject().zipTree(file.getPath()), config -> config.include("**/*.proto"));
                    copy.into(destinationDirectory);
                });
            } else if (isTarArchive(file)) {
                getProject().copy(copy -> {
                    copy.setIncludeEmptyDirs(false);
                    copy.from(getProject().tarTree(file.getPath()), config -> config.include("**/*.proto"));
                    copy.into(destinationDirectory);
                });
            } else {
                log.debug("Skipping unsupported file type ({}); handles only jar, tar, tar.gz, tar.bz2 & tgz", file.getPath());
            }
        });

        if (getRewritePackage() != null) {

            getProject().fileTree(getDestinationDirectory()).forEach(file -> {

                final Path path = file.toPath();
                final String filePath = file.getPath();
                final String newFilePath = filePath.replaceAll(rewritePackage.getReplace(), rewritePackage.getWith());
                final File newFile = new File(newFilePath);

                if (!filePath.equals(newFilePath)) {

                    log.debug("Rewriting: '{}' -> '{}'", file, newFile);

                    try {
                        String content = new String(Files.readAllBytes(path), UTF_8);
                        content = content.replaceAll(rewritePackage.getReplace(), rewritePackage.getWith());
                        Files.write(path, content.getBytes(UTF_8));
                    } catch (IOException e) {
                        throw new TaskExecutionException(this, e);
                    }

                    getProject().copy(copy -> {
                        copy.from(file);
                        copy.into(newFile.getParent());
                    });

                    getProject().delete(file);
                }
            });

            deleteEmptyDirectories();
        }
    }

    private void deleteEmptyDirectories() {
        final List<File> dirs = new ArrayList<>();
        getProject().fileTree(getDestinationDirectory()).visit(visitor -> {
            if (visitor.isDirectory()) {
                dirs.add(visitor.getFile());
            }
        });

        Collections.reverse(dirs);

        for (File dir : dirs) {
            if (dir.list().length == 0) {
                getProject().delete(dir);
            }
        }
    }
}
