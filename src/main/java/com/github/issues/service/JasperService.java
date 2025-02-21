package com.github.issues.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Service
@Slf4j
public class JasperService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public File generateFile(Path path, String fileName) throws IOException {
        String formattedDate = LocalDateTime.now().format(formatter);
        Path filePath = path.resolve(fileName);
        String fileNameWithPath = filePath.toString().formatted(formattedDate);
        File outputFile = new File(fileNameWithPath);
        if(isValid(outputFile)) {
            Files.write(outputFile.toPath(), new byte[0]);
            log.info("Finished creating file {}", fileName);
        } else {
            throw new IllegalStateException("Unable to create file");
        }

        return outputFile;
    }

    public boolean isValid(File file) {
        boolean isValid = false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.error("Couldn't create dir: {}", parent);
        } else {
            isValid = true;
        }
        return isValid;
    }
}