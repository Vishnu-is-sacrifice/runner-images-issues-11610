package com.github.issues.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@Slf4j
public class AwsS3Service {
    public void writeToS3(File file, String bucketName) {
        log.info("Starting S3 Operation ");
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

        try {
            final String objectName = file.getName();

            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);

            ObjectMetadata metadata = new ObjectMetadata();

            String mimeType = Files.probeContentType(file.toPath());
            metadata.setContentType(mimeType);
            metadata.addUserMetadata("title", "I am a test Metadata by PR");
            request.setMetadata(metadata);

            log.info("Put Object to S3 ...");
            s3Client.putObject(request);
            log.info("... done putting Object to S3");

            log.info("Read Object From S3...");
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
