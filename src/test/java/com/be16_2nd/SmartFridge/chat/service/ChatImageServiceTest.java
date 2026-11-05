package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatImageServiceTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0};

    S3Client s3Client;
    ChatImageService service;
    String base;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        when(s3Client.utilities()).thenReturn(S3Utilities.builder().region(Region.AP_NORTHEAST_2).build());
        service = new ChatImageService(s3Client);
        ReflectionTestUtils.setField(service, "bucket", "test-bucket");
        base = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/";
    }

    @Test
    void 파일_종류는_내용으로_판별하고_방_경로에_저장한다() {
        // 클라이언트가 보낸 이름·Content-Type 은 무시된다
        List<String> urls = service.uploadImages(ChatRoomType.PURCHASE, 5L, List.of(
                file("a.html", "text/html", PNG), file("b", null, JPEG), file("c.png", "image/png", WEBP)));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(3)).putObject(captor.capture(), any(RequestBody.class));
        List<PutObjectRequest> requests = captor.getAllValues();
        assertThat(requests).extracting(PutObjectRequest::contentType)
                .containsExactly("image/png", "image/jpeg", "image/webp");
        assertThat(requests).extracting(PutObjectRequest::key)
                .allMatch(k -> k.startsWith("purchase/chat/5/"))
                .anyMatch(k -> k.endsWith(".png")).anyMatch(k -> k.endsWith(".jpg")).anyMatch(k -> k.endsWith(".webp"));
        assertThat(urls).allMatch(u -> u.startsWith(base + "purchase/chat/5/"));
    }

    @Test
    void 이미지가_아닌_파일은_확장자를_속여도_거부되고_하나도_올라가지_않는다() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes();
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script/></svg>".getBytes();

        assertThatThrownBy(() -> service.uploadImages(ChatRoomType.MANAGER, 1L,
                List.of(file("ok.png", "image/png", PNG), file("evil.jpg", "image/jpeg", html))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.uploadImages(ChatRoomType.MANAGER, 1L, List.of(file("x.svg", "image/svg+xml", svg))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 용량과_장수를_넘으면_거부된다() {
        byte[] big = new byte[(int) ChatImageService.MAX_IMAGE_BYTES + 1];
        System.arraycopy(PNG, 0, big, 0, PNG.length);
        assertThatThrownBy(() -> service.uploadImages(ChatRoomType.MANAGER, 1L, List.of(file("big.png", "image/png", big))))
                .isInstanceOf(IllegalArgumentException.class);

        List<MultipartFile> eleven = Collections.nCopies(ChatImageService.MAX_IMAGES_PER_MESSAGE + 1, file("a.png", "image/png", PNG));
        assertThatThrownBy(() -> service.uploadImages(ChatRoomType.MANAGER, 1L, eleven))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 메시지_이미지_URL은_이_방에_업로드된_것만_허용한다() {
        assertThatCode(() -> service.validateImageUrls(ChatRoomType.PURCHASE, 5L,
                List.of(base + "purchase/chat/5/abc.png"))).doesNotThrowAnyException();

        for (String bad : List.of(
                base + "purchase/chat/6/abc.png",           // 다른 방
                base + "manager/chat/5/abc.png",            // 다른 종류
                "https://evil.example.com/purchase/chat/5/abc.png",
                base + "purchase/chat/5/../6/abc.png",
                base + "purchase/chat/5/sub/abc.png")) {
            assertThatThrownBy(() -> service.validateImageUrls(ChatRoomType.PURCHASE, 5L, List.of(bad)))
                    .as(bad).isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static MockMultipartFile file(String name, String contentType, byte[] content) {
        return new MockMultipartFile("images", name, contentType, content);
    }
}
