package com.github.issues.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@Slf4j
public class FileUtil {
    public static File createTempFile(String mimeType, Path directory) {
        try {
            // Verify temp directory exists and is writable
            if (directory == null || !Files.isWritable(directory)) {
                log.error("Temp directory is null or not writable");
                return null;
            }

            String fileName = String.format("%s.pdf",
                    RandomStringGenerator.builder()
                            .withinRange('0', '9')
                            .withinRange('a', 'z')
                            .filteredBy(LETTERS, DIGITS)
                            .get()
                            .generate(16));

            FileAttribute<Set<PosixFilePermission>> ownerWritable = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx"));
            Path path = Files.createTempFile(directory, null, fileName, ownerWritable);
            if (path == null) {
                log.error("Failed to create temp file");
                return null;
            }

            Files.write(path, mimeType.getBytes());
            Files.write(path, "Test".getBytes(StandardCharsets.UTF_8));
            File file = path.toFile();
            file.deleteOnExit();
            return file;

        } catch (IOException e) {
            log.error("Failed to create temporary file", e);
            return null;
        }
    }
}
