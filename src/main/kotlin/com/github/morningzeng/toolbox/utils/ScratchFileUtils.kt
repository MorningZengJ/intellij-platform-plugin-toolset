package com.github.morningzeng.toolbox.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.github.morningzeng.toolbox.annotations.ScratchConfig
import com.github.morningzeng.toolbox.enums.JacksonType
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.util.containers.stream
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
@Suppress("unused")
object ScratchFileUtils {
    private const val ROOT_DIRECTORY: String = "Toolset"

    fun <T> writeAndOpen(project: Project, filename: String, type: JacksonType, t: T) {
        write(filename, type.prettySerialize(t) ?: "") { open(project, it) }
    }

    fun <T> writeAndOpen(project: Project, filename: String, content: String) {
        write(filename, content) { open(project, it) }
    }

    fun <T> write(t: T, typeReference: TypeReference<T>) {
        t?.let {
            getConfig(typeReference).also {
                val filename = it.outputType.fullName(it.value)
                val file = findOrCreate(it.directory, filename)
                write(file, it.outputType, t)
            }
        }
    }

    fun <T> write(directory: String?, filename: String, type: JacksonType, data: T) {
        write(findOrCreate(directory, type.fullName(filename)), type, data)
    }

    fun <T> write(file: VirtualFile, type: JacksonType, data: T) = write(
        file, type.serialize(data) ?: ""
    )

    fun write(filename: String, content: String) = write(
        findOrCreate(filename), content
    )

    fun write(filename: String, content: String, consumer: Consumer<VirtualFile>) = write(
        findOrCreate(filename), content, consumer
    )

    fun write(file: VirtualFile, content: String) = write(file, content) { }

    fun write(file: VirtualFile, content: String, consumer: Consumer<VirtualFile>) {
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                VfsUtil.saveText(file, content)
            }
            RefreshQueue.getInstance().createSession(true, true, null)
                .also { it.addFile(file) }
                .also { it.launch() }
            consumer.accept(file)
        }
    }

    fun <T> read(clazz: Class<T>): T? = getConfig(clazz).let {
        read(it.directory, it.value, it.outputType, clazz)
    }

    fun <T> read(typeReference: TypeReference<T>): T? = getConfig(typeReference).let {
        read(it.directory, it.value, it.outputType, typeReference)
    }

    fun <T> read(typeParameter: Type): T? = getConfig(typeParameter).let {
        read(it.directory, it.value, it.outputType, typeParameter)
    }

    fun <T> read(directory: String?, filename: String, type: JacksonType, clazz: Class<T>): T? {
        return read(directory, type.fullName(filename)).let {
            type.deserialize(it, clazz)
        }
    }

    fun <T> read(directory: String?, filename: String, type: JacksonType, typeReference: TypeReference<T>): T? {
        return read(directory, type.fullName(filename)).let {
            type.deserialize(it, typeReference)
        }
    }

    fun <T> read(directory: String?, filename: String, type: JacksonType, typeParameter: Type): T? {
        return read(directory, type.fullName(filename)).let {
            type.deserialize(it, typeParameter)
        }
    }

    fun <T> read(file: VirtualFile, type: JacksonType, clazz: Class<T>): T? {
        return read(file).let {
            type.deserialize(it, clazz)
        }
    }

    fun <T> read(file: VirtualFile, type: JacksonType, typeReference: TypeReference<T>): T? {
        return read(file).let {
            type.deserialize(it, typeReference)
        }
    }

    fun <T> read(file: VirtualFile, type: JacksonType, typeParameter: Type): T? {
        return read(file).let {
            type.deserialize(it, typeParameter)
        }
    }

    fun read(directory: String?, filename: String): String {
        return read(findOrCreate(directory, filename))
    }

    fun read(file: VirtualFile): String {
        return ApplicationManager.getApplication().runReadAction<String> {
            VfsUtil.loadText(file)
        }
    }

    fun open(project: Project, directory: String?, filename: String) {
        findOrCreate(directory, filename).also {
            open(project, it)
        }
    }

    fun childrenFile(directory: String?, consumer: Consumer<Stream<VirtualFile>>) {
        ApplicationManager.getApplication().invokeLater {
            consumer.accept(directory(directory).children.stream())
        }
    }

    fun findOrCreate(filename: String): VirtualFile = findOrCreate(null, filename)

    fun findOrCreate(directory: String?, filename: String): VirtualFile {
        return ApplicationManager.getApplication().runWriteAction<VirtualFile> {
            directory(directory).findOrCreateChildData(null, filename)
        }
    }

    fun open(project: Project, file: VirtualFile) {
        ApplicationManager.getApplication().invokeAndWait {
            FileEditorManager.getInstance(project).openFile(file, true)
        }
    }

    fun directory(directory: String?): VirtualFile {
        val service = ScratchFileService.getInstance()
        val rootType = ScratchRootType.getInstance()
        val configPath = service.getRootPath(rootType).let {
            if (directory == null) {
                Paths.get("$it${File.separator}$ROOT_DIRECTORY")
            } else {
                Paths.get("$it${File.separator}$ROOT_DIRECTORY${File.separator}$directory")
            }
        }
        return ApplicationManager.getApplication().runWriteAction<VirtualFile> {
            mkdirs(configPath)
        }
    }

    fun mkdirs(path: Path): VirtualFile {
        val instance = LocalFileSystem.getInstance()
        val virtualFile = instance.findFileByNioFile(path)
        if (virtualFile != null) {
            return virtualFile
        }
        val parent = mkdirs(path.parent)
        return parent.createChildDirectory(null, path.fileName.toString())
    }

    private fun <T> getConfig(typeReference: TypeReference<T>): ScratchConfig {
        val type = typeReference.type
        return getConfig(type)
    }

    private fun getConfig(type: Type): ScratchConfig {
        if (type is Class<*>) {
            return type.getAnnotation(ScratchConfig::class.java)
        }
        if (type is ParameterizedType) {
            return (type.actualTypeArguments[0] as Class<*>).getAnnotation(ScratchConfig::class.java)
        }
        throw IllegalArgumentException("ScratchConfig annotation is missing")
    }
}