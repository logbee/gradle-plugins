package io.logbee.gradle.protobuf;

import java.io.File;

public class Util {

    public static boolean isZipArchive(final File file) {
        return file.getPath().endsWith(".jar")
            || file.getPath().endsWith(".zip");
    }

    public static boolean isTarArchive(final File file) {
        return file.getPath().endsWith(".tar")
            || file.getPath().endsWith(".tar.gz")
            || file.getPath().endsWith(".tar.bz2")
            || file.getPath().endsWith(".tgz");
    }
}
