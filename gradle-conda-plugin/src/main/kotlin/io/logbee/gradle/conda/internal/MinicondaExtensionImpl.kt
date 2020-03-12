package io.logbee.gradle.conda.internal

import io.logbee.gradle.conda.MinicondaExtension
import io.logbee.gradle.conda.MinicondaExtension.MinicondaNotation
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import java.io.File
import javax.inject.Inject

open class MinicondaExtensionImpl @Inject constructor(override val project: Project): MinicondaExtension {

    companion object {
        val OS = OperatingSystem.current()
    }

    override var baseDir: File = File(project.gradle.gradleUserHomeDir, "caches/io.logbee.gradle/")

    override var version: String = "4.7.10" // "4.7.12.1"

    override val installationDir: File
        get() = File(baseDir, getName() + "-" + version)

    override val condaExecutable: File
        get() = File(installationDir, "bin/conda")

    override val minicondaNotation: MinicondaNotation
        get() = MinicondaNotation(
                name = getName(),
                version = version,
                type = getExtension(),
                extension = getExtension(),
                classifier = getOsName() + "-" + getArch()
        )

    private fun getName(): String {
        return "Miniconda3"
    }

    private fun getOsName(): String {
        return when {
            OS.isWindows -> {
                "Windows"
            }
            OS.isMacOsX -> {
                "MacOSX"
            }
            OS.isLinux -> {
                "Linux"
            }
            else -> {
                throw IllegalArgumentException("Supported operating systems are: Windows, MacOSX, Linux")
            }
        }
    }

    private fun getExtension(): String {
        return if (OS.isWindows) "exe" else "sh"
    }

    private fun getArch(): String {
        return "x86_64"
    }
}