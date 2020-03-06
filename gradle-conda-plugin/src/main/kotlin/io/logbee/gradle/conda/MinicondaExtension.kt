package io.logbee.gradle.conda

import org.gradle.api.Project
import java.io.File

interface MinicondaExtension {

    class MinicondaNotation(val name: String, val version: String, val extension: String, val classifier: String) {
        fun asModuleNotation(): Map<String, String> {
            val notation: MutableMap<String, String> = HashMap()
            notation["group"] = "miniconda"
            notation["name"] = name
            notation["version"] = version
            return notation
        }
    }

    val project: Project

    var baseDir: File

    var version: String

    val installationDir: File

    val condaExecutable: File

    val minicondaNotation: MinicondaNotation
}