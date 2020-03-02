package io.logbee.gradle.protobuf.python;

import groovy.lang.Closure;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;

public class ProtobufPythonExtension {

    private final Project project;
    private File generatedBaseDir;
    private PackageRewrite rewritePackage;

    @Inject
    public ProtobufPythonExtension(final Project project) {
        this.project = project;
        this.generatedBaseDir = new File(project.getBuildDir(), "generated/protobuf");
    }

    public File getGeneratedBaseDir() {
        return generatedBaseDir;
    }

    public void setGeneratedBaseDir(File generatedBaseDir) {
        this.generatedBaseDir = generatedBaseDir;
    }

    public PackageRewrite getRewritePackage() {
        return this.rewritePackage;
    }

    public void setRewritePackage(PackageRewrite packageRewrite) {
        this.rewritePackage = packageRewrite;
    }

    public void rewritePackage(Closure<PackageRewrite> closure) {
        this.rewritePackage = new PackageRewrite();
        closure.rehydrate(rewritePackage, rewritePackage, rewritePackage).call();
    }

    public static class PackageRewrite {

        private String replace;
        private String with;

        public String getReplace() {
            return replace;
        }

        public void setReplace(String replace) {
            this.replace = replace;
        }

        public void replace(String replace) {
            this.replace = replace;
        }

        public String getWith() {
            return with;
        }

        public void setWith(String with) {
            this.with = with;
        }

        public void with(String replacement) {
            this.with = replacement;
        }
    }
}
