package dev.brutils.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.net.URI
import javax.inject.Inject

/**
 * Copies the language-neutral conformance cases (youphenrique/brutils-spec) into [outputDir].
 *
 * Uses [localDir] when set; otherwise downloads the repository archive at [ref], which must be a full commit SHA
 * so the task stays reproducible and up to date between runs.
 */
abstract class SyncSpec : DefaultTask() {
    @get:Input
    abstract val ref: Property<String>

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val localDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val fs: FileSystemOperations

    @get:Inject
    abstract val archives: ArchiveOperations

    @TaskAction
    fun sync() {
        val local = localDir.orNull
        if (local != null) {
            fs.sync {
                from(local)
                into(outputDir)
            }
            return
        }

        val archive = temporaryDir.resolve("brutils-spec.tar.gz")
        URI("https://codeload.github.com/youphenrique/brutils-spec/tar.gz/${ref.get()}").toURL().openStream().use { input ->
            archive.outputStream().use { input.copyTo(it) }
        }
        fs.sync {
            // Archive entries look like brutils-spec-<sha>/<module>/cases.json; drop the top-level directory.
            from(archives.tarTree(archives.gzip(archive))) {
                eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
                includeEmptyDirs = false
            }
            into(outputDir)
        }
        if (outputDir.asFileTree.isEmpty) {
            throw GradleException("youphenrique/brutils-spec has no files at ${ref.get()}.")
        }
    }
}
