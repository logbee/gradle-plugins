package io.logbee.gradle.conda.internal

import io.logbee.gradle.conda.CondaExtension
import io.logbee.gradle.conda.MinicondaExtension
import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

open class CondaExtensionImpl @Inject constructor(override val project: Project) : CondaExtension {

    override val miniconda: MinicondaExtension = MinicondaExtensionImpl(project)

    override var environmentDir: File = File(project.projectDir, ".gradle/conda")
}