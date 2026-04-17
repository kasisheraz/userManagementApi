package com.fincore.usermgmt.service;

import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for handling file storage operations with Google Cloud Storage.
 */
@Service
public class GcsFileStorageService {
    private static final Logger log = LoggerFactory.getLogger(GcsFileStorageService.class);

    @Value("${gcs.bucket-name}")
    private String bucketName;

    @Value("${gcs.project-id:}")
    private String projectId;

    @Value("${gcs.enabled:true}")
    private boolean gcsEnabled;

    @Value("${gcs.base-url:https://storage.googleapis.com}")
    private String baseUrl;

    private Storage storage;

    /**
     * Initialize GCS storage client.
     * Uses Application Default Credentials (ADC) which works automatically on GCP.
     * For local development, set GOOGLE_APPLICATION_CREDENTIALS environment variable.
     */
    private Storage getStorage() {
        if (storage == null && gcsEnabled) {
            try {
                if (projectId != null && !projectId.isEmpty()) {
                    storage = StorageOptions.newBuilder()
                            .setProjectId(projectId)
                            .build()
                            .getService();
                } else {
                    // Use default project from ADC
                    storage = StorageOptions.getDefaultInstance().getService();
                }
                log.info("GCS Storage initialized successfully with bucket: {}", bucketName);
            } catch (Exception e) {
                log.error("Failed to initialize GCS Storage. File uploads will fail.", e);
                throw new RuntimeException("GCS Storage initialization failed", e);
            }
        }
        return storage;
    }

    /**
     * Upload a file to Google Cloud Storage.
     *
     * @param file MultipartFile to upload
     * @param folder Folder path within the bucket (e.g., "kyc-documents")
     * @return Public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (!gcsEnabled) {
            log.warn("GCS is disabled. Returning mock file URL.");
            return "mock://" + folder + "/" + file.getOriginalFilename();
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        try {
            // Generate unique filename to avoid collisions
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String filename = String.format("%s/%s-%s%s", folder, timestamp, uniqueId, extension);

            // Upload to GCS
            BlobId blobId = BlobId.of(bucketName, filename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            Storage gcs = getStorage();
            Blob blob = gcs.create(blobInfo, file.getBytes());

            // Make the file publicly accessible (optional - remove if you want private files)
            // blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            String publicUrl = String.format("%s/%s/%s", baseUrl, bucketName, filename);
            log.info("File uploaded successfully to GCS: {}", publicUrl);

            return publicUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to GCS: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to upload file to GCS", e);
        }
    }

    /**
     * Delete a file from Google Cloud Storage.
     *
     * @param fileUrl Full URL of the file to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteFile(String fileUrl) {
        if (!gcsEnabled) {
            log.warn("GCS is disabled. Mock deletion returning true.");
            return true;
        }

        try {
            // Extract blob name from URL
            // Expected format: https://storage.googleapis.com/bucket-name/folder/filename.ext
            String blobName = fileUrl.replace(baseUrl + "/" + bucketName + "/", "");
            
            BlobId blobId = BlobId.of(bucketName, blobName);
            Storage gcs = getStorage();
            boolean deleted = gcs.delete(blobId);
            
            if (deleted) {
                log.info("File deleted successfully from GCS: {}", fileUrl);
            } else {
                log.warn("File not found in GCS: {}", fileUrl);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete file from GCS: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * Download a file from Google Cloud Storage.
     *
     * @param fileUrl Full URL of the file to download
     * @return Byte array of file content
     */
    public byte[] downloadFile(String fileUrl) throws IOException {
        if (!gcsEnabled) {
            log.warn("GCS is disabled. Returning empty mock file.");
            return new byte[0];
        }

        try {
            // Extract blob name from URL
            String blobName = fileUrl.replace(baseUrl + "/" + bucketName + "/", "");
            
            BlobId blobId = BlobId.of(bucketName, blobName);
            Storage gcs = getStorage();
            Blob blob = gcs.get(blobId);
            
            if (blob == null) {
                throw new IOException("File not found in GCS: " + fileUrl);
            }
            
            log.info("File downloaded successfully from GCS: {}", fileUrl);
            return blob.getContent();
        } catch (Exception e) {
            log.error("Failed to download file from GCS: {}", fileUrl, e);
            throw new IOException("Failed to download file from GCS", e);
        }
    }

    /**
     * Check if a file exists in Google Cloud Storage.
     *
     * @param fileUrl Full URL of the file
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String fileUrl) {
        if (!gcsEnabled) {
            return true;
        }

        try {
            String blobName = fileUrl.replace(baseUrl + "/" + bucketName + "/", "");
            BlobId blobId = BlobId.of(bucketName, blobName);
            Storage gcs = getStorage();
            Blob blob = gcs.get(blobId);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Failed to check file existence in GCS: {}", fileUrl, e);
            return false;
        }
    }
}
