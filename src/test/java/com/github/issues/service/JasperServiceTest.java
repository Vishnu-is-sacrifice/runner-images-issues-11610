package com.github.issues.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@Slf4j
class JasperServiceTest {
    @TempDir
    private static Path tempDirectory;

    private final JasperService fileService = new JasperService();

    @Test
    void fileTest() {
        try {
            File file = fileService.generateFile(tempDirectory, "pdf-file-%s.pdf");
            assertNotNull(file);
            assertTrue(file.exists());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void fileTest2() {
        try {
            File file = fileService.generateFile(tempDirectory, "txt-file-%s.txt");
            assertNotNull(file);
            assertTrue(file.exists());
        } catch (IOException e) {
            fail(e);
        }
    }
}
