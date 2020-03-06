package io.logbee.gradle.conda

import org.gradle.api.Project
import java.io.File

interface CondaExtension {

    val project: Project

    val miniconda: MinicondaExtension

    var environmentDir: File
}