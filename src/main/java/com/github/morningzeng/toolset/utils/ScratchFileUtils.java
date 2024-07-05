package com.github.morningzeng.toolset.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.morningzeng.toolset.Constants;
import com.github.morningzeng.toolset.annotations.ScratchConfig;
import com.google.common.collect.Maps;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.RefreshQueue;
import com.intellij.openapi.vfs.newvfs.RefreshSession;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.morningzeng.toolset.utils.JacksonUtils.IGNORE_TRANSIENT_AND_NULL;

/**
 * @author Morning Zeng
 * @since 2024-06-27
 */
public final class ScratchFileUtils {

    static final String ROOT_DIRECTORY = "Toolset";

    @SneakyThrows
    public static <T> void write(final T t) {
//        write(name, IGNORE_TRANSIENT_AND_NULL.toPrettyJson(t));
        final Class<?> tClass = t.getClass();
        final ScratchConfig scratchConfig = tClass.getAnnotation(ScratchConfig.class);
        if (Objects.isNull(scratchConfig)) {
            return;
        }
        final String filename = String.join(Constants.DOT, scratchConfig.filename(), scratchConfig.outputType().suffix());
        final VirtualFile file = findOrCreate(scratchConfig.directory(), filename);
        final String content = read(file);

        Map<String, Object> yamlMap;
        if (StringUtil.isEmpty(content)) {
            yamlMap = Maps.newHashMap();
        } else {
            yamlMap = scratchConfig.outputType().deserialize(content, new TypeReference<>() {
            });
        }
        yamlMap.put(scratchConfig.value(), t);
        write(file, scratchConfig.outputType().serialize(yamlMap));
    }

    @SneakyThrows
    public static <T> void write(final String directory, String filename, final ScratchConfig.OutputType outputType, final T t) {
        filename = String.join(Constants.DOT, filename, outputType.suffix());
        final VirtualFile file = findOrCreate(directory, filename);
        write(file, outputType.serialize(t));
    }

    public static void write(final String name, final String content) {
        write(name, content, virtualFile -> {
        });
    }

    public static <T> void writeAndOpen(final Project project, @NotNull String name, final T t) {
        write(name, IGNORE_TRANSIENT_AND_NULL.toPrettyJson(t), scratchFile -> open(project, scratchFile));
    }

    public static void writeAndOpen(final Project project, @NotNull String name, final String content) {
        write(name, content, scratchFile -> open(project, scratchFile));
    }

    @SneakyThrows
    public static void write(final String filename, final String content, final Consumer<VirtualFile> consumer) {
        final VirtualFile scratchFile = findOrCreate(filename);
        write(scratchFile, content, consumer);
    }

    public static void write(final VirtualFile file, final String content) {
        write(file, content, virtualFile -> {
        });
    }

    public static void write(final VirtualFile file, final String content, final Consumer<VirtualFile> consumer) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        VfsUtil.saveText(file, content);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    RefreshSession rs = RefreshQueue.getInstance().createSession(true, true, null);
                    rs.addFile(file);
                    rs.launch();
                });
                consumer.accept(file);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public static String read(final VirtualFile file) {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            try {
                return VfsUtil.loadText(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SneakyThrows
    public static String read(final String directory, final String filename) {
        return read(findOrCreate(directory, filename));
    }

    public static <T> T read(final String directory, final String filename, final ScratchConfig.OutputType outputType, final Class<T> type) {
        final String json = read(directory, filename);
        return outputType.deserialize(json, type);
    }

    public static <T> T read(final String directory, final String filename, final ScratchConfig.OutputType outputType, final TypeReference<T> type) {
        final String json = read(directory, filename);
        return outputType.deserialize(json, type);
    }

    public static <T> T read(final VirtualFile file, final ScratchConfig.OutputType outputType, final Type type) {
        final String json = read(file);
        return outputType.deserialize(json, type);
    }

    public static <T> T read(final VirtualFile file, final ScratchConfig.OutputType outputType, final Class<T> type) {
        final String json = read(file);
        return outputType.deserialize(json, type);
    }

    public static <T> T read(final VirtualFile file, final ScratchConfig.OutputType outputType, final TypeReference<T> type) {
        final String json = read(file);
        return outputType.deserialize(json, type);
    }

    public static <T> T read(final String directory, final String filename, final ScratchConfig.OutputType outputType, final Type type) {
        final String json = read(directory, filename);
        return outputType.deserialize(json, type);
    }

    @SneakyThrows
    public static void openFile(final Project project, final String directory, final String filename) {
        final VirtualFile virtualFile = findOrCreate(directory, filename);
        open(project, virtualFile);
    }

    public static void childrenFile(final String dir, final Consumer<Stream<VirtualFile>> consumer) {
        final VirtualFile directory = directory(dir);
        ApplicationManager.getApplication().invokeLater(
                () -> consumer.accept(Arrays.stream(directory.getChildren()))
        );
    }

    static void open(final Project project, final VirtualFile scratchFile) {
        ApplicationManager.getApplication().invokeAndWait(() -> FileEditorManager.getInstance(project).openFile(scratchFile, true));
    }

    static VirtualFile findOrCreate(final String filename) {
        return findOrCreate(null, filename);
    }

    static VirtualFile findOrCreate(final String directory, final String filename) {
        return ApplicationManager.getApplication().runWriteAction((Computable<VirtualFile>) () -> {
            try {
                return directory(directory).findOrCreateChildData(null, filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static VirtualFile directory(final String directory) {
        final ScratchFileService service = ScratchFileService.getInstance();
        final ScratchRootType rootType = ScratchRootType.getInstance();
        final String scratch = service.getRootPath(rootType);
        final Path configPath = Paths.get(String.join(File.separator, scratch, ROOT_DIRECTORY, directory));

        final VirtualFile scratchDirectory = LocalFileSystem.getInstance().findFileByNioFile(configPath);
        return ApplicationManager.getApplication().runWriteAction(
                (Computable<VirtualFile>) () -> Optional.ofNullable(scratchDirectory)
                        .orElse(mkdirs(configPath))
        );
    }

    @SneakyThrows
    static VirtualFile mkdirs(final Path path) {
        final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        final VirtualFile file = localFileSystem.findFileByNioFile(path);
        if (Objects.nonNull(file)) {
            return file;
        }
        final Path parent = path.getParent();
        final VirtualFile parentDirectory = mkdirs(parent);
        return parentDirectory.createChildDirectory(null, path.getFileName().toString());
    }

}
