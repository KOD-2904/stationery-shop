package org.example.stationery_shop.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface CloudinaryService {
    String uploadImage(MultipartFile file, String folder);
    String uploadImage(Path filePath, String folder, String publicId);
}
