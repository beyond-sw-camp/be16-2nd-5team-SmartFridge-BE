package com.be16_2nd.SmartFridge.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatImageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<String> uploadImages(List<MultipartFile> imageFiles, String pattern) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {

            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")); // ".jpg"
            }
            String uuid = UUID.randomUUID().toString();
            String fileName = pattern + uuid + fileExtension;
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(imageFile.getContentType())
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageFile.getBytes()));
                String imageUrl = s3Client.utilities().getUrl(b -> b.bucket(bucket).key(fileName)).toExternalForm();
                imageUrls.add(imageUrl);

            } catch (Exception e) {
                throw new IllegalArgumentException("이미지업로드 실패");
            }
        }
        return imageUrls;
    }
}
