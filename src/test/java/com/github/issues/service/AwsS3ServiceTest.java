package com.github.issues.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.issues.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AwsS3ServiceTest {
    @TempDir
    private static Path sharedTempDir;

    @Mock
    private AmazonS3 mockS3Client;

    private AwsS3Service awsS3Service;

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeEach
    void setUp() {
        awsS3Service = new AwsS3Service();

        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(AwsS3Service.class)).addAppender(logWatcher);
    }

    @AfterEach
    void teardown() {
        ((Logger) LoggerFactory.getLogger(AwsS3Service.class)).detachAndStopAllAppenders();
    }

    @Test
    void writeToS3_SuccessfulUpload() {
        // Arrange
        String bucketName = "XXXXXXXXXXXX";

        try (MockedStatic<AmazonS3ClientBuilder> mockedStatic = mockStatic(AmazonS3ClientBuilder.class)) {
            File testFile = FileUtil.createTempFile("application/pdf", sharedTempDir);
            AmazonS3ClientBuilder builder = mock(AmazonS3ClientBuilder.class);
            mockedStatic.when(AmazonS3ClientBuilder::standard).thenReturn(builder);
            when(builder.build()).thenReturn(mockS3Client);

            // Act
            awsS3Service.writeToS3(testFile, bucketName);

            // Assert
            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(mockS3Client).putObject(requestCaptor.capture());

            PutObjectRequest capturedRequest = requestCaptor.getValue();
            assertEquals(bucketName, capturedRequest.getBucketName());
            assertEquals(testFile.getName(), capturedRequest.getKey());
            assertEquals(testFile, capturedRequest.getFile());

            ObjectMetadata metadata = capturedRequest.getMetadata();
            assertNotNull(metadata);
            assertEquals("application/pdf", metadata.getContentType());
            assertEquals("I am a test Metadata by PR", metadata.getUserMetadata().get("title"));
        }
    }

    @Test
    void writeToS3_WithNullFile() {
        // Arrange
        String bucketName = "XXXXXXXXXXX";

        try (MockedStatic<AmazonS3ClientBuilder> mockedStatic = mockStatic(AmazonS3ClientBuilder.class)) {
            AmazonS3ClientBuilder builder = mock(AmazonS3ClientBuilder.class);
            mockedStatic.when(AmazonS3ClientBuilder::standard).thenReturn(builder);
            when(builder.build()).thenReturn(mockS3Client);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> awsS3Service.writeToS3(null, bucketName));
            verify(mockS3Client, never()).putObject(any(PutObjectRequest.class));
        }
    }

    @Test
    void exceptionTest() {
        // Arrange
        String bucketName = "XXXXXXXXXXX";

        try (MockedStatic<AmazonS3ClientBuilder> mockedStatic = mockStatic(AmazonS3ClientBuilder.class)) {
            File testFile = FileUtil.createTempFile("application/pdf", sharedTempDir);

            AmazonS3ClientBuilder builder = mock(AmazonS3ClientBuilder.class);
            mockedStatic.when(AmazonS3ClientBuilder::standard).thenReturn(builder);
            when(builder.build()).thenReturn(mockS3Client);

            mockStatic(Files.class).when(() -> Files.probeContentType(testFile.toPath())).thenThrow(new IOException("Test IO Exception"));

            // Act
            awsS3Service.writeToS3(testFile, bucketName);

            // Assert
            int logSize = logWatcher.list.size();
            assertEquals(2, logSize);
            boolean exceptionMessage = (logWatcher.list.get(logSize - 1).getFormattedMessage()).contains("Test IO Exception");
            assertTrue(exceptionMessage);
        }
    }
}
