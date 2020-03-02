package io.logbee.gradle.protobuf;

import io.logbee.gradle.protobuf.python.ProtobufPythonExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public class ProtobufExtension {

    private final Project project;

    private String version;

    private final ProtobufPythonExtension python;

    @Inject
    public ProtobufExtension(Project project) {
        this.project = project;
        this.python = new ProtobufPythonExtension(project);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ProtobufPythonExtension getPython() {
        return this.python;
    }

    public void python(Action<? super ProtobufPythonExtension> action) {
        action.execute(python);
    }

}
