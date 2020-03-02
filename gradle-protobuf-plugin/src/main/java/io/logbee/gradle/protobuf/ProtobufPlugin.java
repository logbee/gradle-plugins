package io.logbee.gradle.protobuf;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;

public class ProtobufPlugin implements Plugin<Project> {

    public static final String PROTOC_CONFIGURATION_NAME = "protoc";

    @Override
    public void apply(final Project project) {
        project.getPlugins().apply("com.google.osdetector");
        project.getPlugins().apply(ProtobufProviderPlugin.class);
        project.getExtensions().create("protobuf", ProtobufExtension.class, project);

        final Configuration protocConfiguration = project.getConfigurations().create(PROTOC_CONFIGURATION_NAME, configuration -> {
            configuration.setVisible(false);
            configuration.setTransitive(false);
            configuration.setExtendsFrom(emptyList());
        });

        protocConfiguration.getIncoming().beforeResolve(dependencies -> {
            if (dependencies.getDependencies().isEmpty()) {
                final Map<String, String> notation = new HashMap<>();
                notation.put("group", "com.google.protobuf");
                notation.put("name", "protoc");
                notation.put("version", project.getExtensions().getByType(ProtobufExtension.class).getVersion());
                notation.put("classifier", getOsClassifier(project));
                notation.put("ext", "exe");
                project.getDependencies().add(protocConfiguration.getName(), notation);
            }
        });

        protocConfiguration.getIncoming().afterResolve(dependencies -> {
            final File executable = protocConfiguration.getSingleFile();
            if (!executable.canExecute() && !executable.setExecutable(true)) {
                throw new GradleException("Cannot set '" + executable + "' as executable");
            }
        });
    }

    private String getOsClassifier(final Project project) {
        final Object extension = project.getExtensions().getByName("osdetector");
        try {
            final Method getClassifier = extension.getClass().getDeclaredMethod("getClassifier");
            return (String) getClassifier.invoke(extension);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
