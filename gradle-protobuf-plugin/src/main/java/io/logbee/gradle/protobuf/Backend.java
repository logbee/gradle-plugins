package io.logbee.gradle.protobuf;

public enum Backend {

    CPP("cpp", "--cpp_out"),
    CSharp("csharp", "--csharp_out"),
    Java("java", "--java_out"),
    JavaScript("js", "--js_out"),
    ObjectiveC("objective_c", "--objc_out"),
    PHP("php", "--php_out"),
    Python("python", "--python_out"),
    Ruby("ruby", "--ruby_out");

    private final String sourceDirectoryName;
    private final String arg;

    Backend(String sourceDirectoryName, String arg) {
        this.sourceDirectoryName = sourceDirectoryName;
        this.arg = arg;
    }

    public String getSourceDirectoryName() {
        return sourceDirectoryName;
    }

    public String asArg(String path) {
        return arg + "=" + path;
    }
}
