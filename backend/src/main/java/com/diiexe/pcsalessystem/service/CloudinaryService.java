package com.diiexe.pcsalessystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folderName) throws IOException {
        String publicId = UUID.randomUUID().toString();

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folderName,
                "public_id", publicId // Use UUID as random filename to prevent overwriting
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return uploadResult.get("secure_url").toString();
    }
}
