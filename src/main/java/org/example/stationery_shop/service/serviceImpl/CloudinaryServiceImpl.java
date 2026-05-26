package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.config.CloudinaryProperties;
import org.example.stationery_shop.exception.AppException;
import org.example.stationery_shop.exception.ErrorCode;
import org.example.stationery_shop.service.CloudinaryService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final CloudinaryProperties cloudinaryProperties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_IS_EMPTY);
        }
        if (cloudinaryProperties.getCloudName().isBlank()
                || cloudinaryProperties.getApiKey().isBlank()
                || cloudinaryProperties.getApiSecret().isBlank()) {
            throw new AppException(ErrorCode.CLOUDINARY_NOT_CONFIGURED);
        }

        try {
            long timestamp = Instant.now().getEpochSecond();
            String normalizedFolder = folder == null || folder.isBlank()
                    ? "Jewelry Shop"
                    : folder;
            String transformation = "c_fill,g_auto,h_"
                    + cloudinaryProperties.getImageHeight()
                    + ",q_auto,w_"
                    + cloudinaryProperties.getImageWidth();

            Map<String, String> signatureParams = new TreeMap<>();
            signatureParams.put("folder", normalizedFolder);
            signatureParams.put("timestamp", String.valueOf(timestamp));
            signatureParams.put("transformation", transformation);
            String signature = sign(signatureParams);

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("api_key", cloudinaryProperties.getApiKey());
            body.add("timestamp", String.valueOf(timestamp));
            body.add("folder", normalizedFolder);
            body.add("transformation", transformation);
            body.add("signature", signature);

            String uploadUrl = "https://api.cloudinary.com/v1_1/"
                    + cloudinaryProperties.getCloudName()
                    + "/image/upload";

            Map response = restClientBuilder.build()
                    .post()
                    .uri(uploadUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Object secureUrl = response == null ? null : response.get("secure_url");
            if (secureUrl == null) {
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
            }

            return secureUrl.toString();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private String sign(Map<String, String> params) throws Exception {
        String payload = params.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"))
                + cloudinaryProperties.getApiSecret();
        return sha1(payload);
    }

    private String sha1(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
