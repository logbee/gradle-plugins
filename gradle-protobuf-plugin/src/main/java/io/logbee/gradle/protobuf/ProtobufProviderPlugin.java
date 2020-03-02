package io.logbee.gradle.protobuf;

import io.logbee.gradle.protobuf.configuration.ProtobufArchiveRule;
import io.logbee.gradle.protobuf.configuration.ProtobufElements;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSetContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;

import static io.logbee.gradle.protobuf.configuration.ProtobufElements.PROTOBUF_ATTRIBUTE;
import static io.logbee.gradle.protobuf.configuration.ProtobufElements.PROTOBUF_SOURCES;

public class ProtobufProviderPlugin implements Plugin<Project> {

    public static final String PROTOBUF_SOURCESET_NAME = "proto";
    public static final String PROTOBUF_GENERATE_CONFIGURATION_NAME = "protobufGenerate";
    public static final String PROTOBUF_INCLUDE_CONFIGURATION_NAME = "protobufInclude";
    public static final String PROTOBUF_PROVIDED_CONFIGURATION_NAME = "protobufProvided";

    private final ObjectFactory objectFactory;

    @Inject
    public ProtobufProviderPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(final Project project) {

        SourceSetContainer sourceSets = getSourceSets(project);

        if (sourceSets == null) {
            sourceSets = project.getExtensions().create("sourceSets", DefaultSourceSetContainer.class);
        }

        if (sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME) == null) {
            sourceSets.create(SourceSet.MAIN_SOURCE_SET_NAME);
        }

        if (sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME) == null) {
            sourceSets.create(SourceSet.TEST_SOURCE_SET_NAME);
        }

        sourceSets.all(sourceSet -> {
            if (sourceSet.getExtensions().findByName(PROTOBUF_SOURCESET_NAME) == null) {
                final SourceDirectorySet sourceDirectory = objectFactory.sourceDirectorySet(sourceSet.getName(), String.format("%s Protobuf source", sourceSet.getName()));
                sourceDirectory.srcDir("src/" + sourceSet.getName() + "/proto");
                sourceDirectory.getFilter().include("**/*.proto");
                sourceSet.getExtensions().add(PROTOBUF_SOURCESET_NAME, sourceDirectory);
                if (!sourceDirectory.getSrcDirs().isEmpty()) {
                    project.getPlugins().withType(IdeaPlugin.class, idea -> {
                        final IdeaModel model = project.getExtensions().findByType(IdeaModel.class);
                        if (model != null) {
                            if (sourceSet.getName().contains("main")) {
                                model.getModule().getSourceDirs().addAll(sourceDirectory.getSrcDirs());
                            } else if (sourceSet.getName().contains("test")) {
                                model.getModule().getTestSourceDirs().addAll(sourceDirectory.getSrcDirs());
                            }
                        }
                    });
                }
            }
        });

        final Configuration generateConfiguration = project.getConfigurations().create(PROTOBUF_GENERATE_CONFIGURATION_NAME, configuration -> {
            configuration.setVisible(false);
            configuration.setTransitive(true);
            configuration.setExtendsFrom(Collections.emptyList());
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(false);
            configuration.getAttributes().attribute(PROTOBUF_ATTRIBUTE, objectFactory.named(ProtobufElements.class, PROTOBUF_SOURCES));
        });

        final Configuration providedConfiguration = project.getConfigurations().create(PROTOBUF_PROVIDED_CONFIGURATION_NAME, configuration -> {
            configuration.setVisible(true);
            configuration.setTransitive(true);
            configuration.setExtendsFrom(Collections.emptyList());
            configuration.setCanBeResolved(false);
            configuration.setCanBeConsumed(true);
            configuration.getAttributes().attribute(PROTOBUF_ATTRIBUTE, objectFactory.named(ProtobufElements.class, PROTOBUF_SOURCES));
        });

        final Configuration includeConfiguration = project.getConfigurations().create(PROTOBUF_INCLUDE_CONFIGURATION_NAME, configuration -> {
            configuration.setVisible(false);
            configuration.setTransitive(true);
            configuration.setExtendsFrom(Collections.emptyList());
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(true);
        });

        final TaskProvider<Tar> archiveProtobuf = project.getTasks().register("archiveProtobuf", Tar.class, task -> {
            final SourceSet mainSourceSet = getSourceSets(project).findByName(SourceSet.MAIN_SOURCE_SET_NAME);
            task.setGroup("Protobuf");
            task.setDescription("Creates an archive (.tar) containing all protobuf files.");
            task.from(mainSourceSet.getExtensions().getByName(PROTOBUF_SOURCESET_NAME));
            task.include("**/*.proto");
            task.getDestinationDirectory().set(new File(project.getBuildDir(), "/protobuf/lib"));
            task.getArchiveBaseName().set(project.getName());
            task.getArchiveClassifier().set("protobuf");
            task.setCompression(Compression.NONE);
        });

        project.getDependencies().getAttributesSchema().attribute(PROTOBUF_ATTRIBUTE, attribute -> {
            attribute.getCompatibilityRules().add(ProtobufArchiveRule.class);
        });

        providedConfiguration.getOutgoing().artifact(archiveProtobuf);
    }

    private SourceSetContainer getSourceSets(final Project project) {
        return (SourceSetContainer) project.getExtensions().findByName("sourceSets");
    }
}
