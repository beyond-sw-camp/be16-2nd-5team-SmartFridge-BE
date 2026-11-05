package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * 채팅 이미지 업로드
 * - 파일 종류는 클라이언트가 보낸 확장자·Content-Type 이 아니라 실제 파일 앞부분(시그니처)으로 판별한다
 *   (html·svg 등을 이미지로 속여 올려 버킷 주소에서 스크립트가 실행되는 것을 막는다)
 * - 저장 경로 : {manager|purchase}/chat/{roomId}/{uuid}.{확장자}
 * - 메시지에 담기는 이미지 URL 은 이 방 경로로 업로드된 것만 허용한다
 */
@Service
@RequiredArgsConstructor
public class ChatImageService {

    static final int MAX_IMAGES_PER_MESSAGE = 10;
    static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<String> uploadImages(ChatRoomType type, Long roomId, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }
        if (imageFiles.size() > MAX_IMAGES_PER_MESSAGE) {
            throw new IllegalArgumentException("이미지는 한 번에 " + MAX_IMAGES_PER_MESSAGE + "장까지 올릴 수 있습니다.");
        }

        // 모두 검증한 뒤 업로드 (중간에 실패해서 일부만 올라가는 것을 줄인다)
        List<ImageType> types = new ArrayList<>();
        List<byte[]> contents = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            if (imageFile.isEmpty() || imageFile.getSize() > MAX_IMAGE_BYTES) {
                throw new IllegalArgumentException("이미지는 1장당 " + (MAX_IMAGE_BYTES / 1024 / 1024) + "MB 이하만 올릴 수 있습니다.");
            }
            byte[] bytes = readBytes(imageFile);
            types.add(ImageType.detect(bytes));
            contents.add(bytes);
        }

        String prefix = keyPrefix(type, roomId);
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < contents.size(); i++) {
            String key = prefix + UUID.randomUUID() + "." + types.get(i).extension;
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(types.get(i).contentType)
                            .build(),
                    RequestBody.fromBytes(contents.get(i)));
            imageUrls.add(urlOf(key));
        }
        return imageUrls;
    }

    // 메시지에 담긴 이미지 URL 이 이 채팅방 경로로 업로드된 이미지인지 확인
    public void validateImageUrls(ChatRoomType type, Long roomId, List<String> imageUrls) {
        if (imageUrls.size() > MAX_IMAGES_PER_MESSAGE) {
            throw new IllegalArgumentException("이미지는 한 메시지에 " + MAX_IMAGES_PER_MESSAGE + "장까지 보낼 수 있습니다.");
        }
        String allowedPrefix = urlOf(keyPrefix(type, roomId));
        for (String url : imageUrls) {
            if (url == null || !url.startsWith(allowedPrefix) || url.contains("..")
                    || url.substring(allowedPrefix.length()).contains("/")) {
                throw new IllegalArgumentException("이 채팅방에 업로드된 이미지만 보낼 수 있습니다.");
            }
        }
    }

    static String keyPrefix(ChatRoomType type, Long roomId) {
        return type.name().toLowerCase() + "/chat/" + roomId + "/";
    }

    private String urlOf(String key) {
        return s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toExternalForm();
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지를 읽을 수 없습니다.");
        }
    }

    // 허용하는 이미지 종류 (파일 시그니처로 판별)
    enum ImageType {
        JPEG("jpg", "image/jpeg"),
        PNG("png", "image/png"),
        GIF("gif", "image/gif"),
        WEBP("webp", "image/webp");

        final String extension;
        final String contentType;

        ImageType(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        static ImageType detect(byte[] b) {
            if (startsWith(b, 0, 0xFF, 0xD8, 0xFF)) return JPEG;
            if (startsWith(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) return PNG;
            if (startsWith(b, 0, 'G', 'I', 'F', '8')) return GIF;
            if (startsWith(b, 0, 'R', 'I', 'F', 'F') && startsWith(b, 8, 'W', 'E', 'B', 'P')) return WEBP;
            throw new IllegalArgumentException("jpg, png, gif, webp 이미지만 올릴 수 있습니다.");
        }

        private static boolean startsWith(byte[] b, int offset, int... signature) {
            if (b.length < offset + signature.length) {
                return false;
            }
            for (int i = 0; i < signature.length; i++) {
                if ((b[offset + i] & 0xFF) != signature[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
