package com.quyquang.genzshoes3.service;

import com.quyquang.genzshoes3.entity.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface ImageService {
    void saveImage(Image image);
    void deleteImage(String uploadDir,String filename);
    List<String> getListImageOfUser(long userId);
    Map deleteImageCloudinary(String id);
    Map uploadImageCloudinaru(MultipartFile multipartFile);
}
