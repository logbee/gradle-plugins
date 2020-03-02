package io.logbee.gradle.protobuf.tasks;

import io.logbee.gradle.protobuf.Backend;
import org.gradle.api.tasks.SourceSet;

public class TaskNames {

    public static final String getPrepareSourcesTaskName(final SourceSet sourceSet, final Backend backend) {
        return sourceSet.getTaskName("prepareProtobuf", backend.name() + "Sources");
    }

    public static final String getPrepareIncludesTaskName(final SourceSet sourceSet, final Backend backend) {
        return sourceSet.getTaskName("prepareProtobuf", backend.name() + "Includes");
    }

    public static final String getGenerateProtobufTaskName(final SourceSet sourceSet, final Backend backend) {
        return sourceSet.getTaskName("generateProtobuf", backend.name());
    }
}
