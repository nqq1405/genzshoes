package com.quyquang.genzshoes3.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.quyquang.genzshoes3.entity.Image;
import com.quyquang.genzshoes3.exception.BadRequestException;
import com.quyquang.genzshoes3.exception.InternalServerException;
import com.quyquang.genzshoes3.repository.ImageRepository;
import com.quyquang.genzshoes3.security.CustomUserDetails;
import com.quyquang.genzshoes3.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Component
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    private final Cloudinary cloudinary;

    public ImageServiceImpl() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "nqq-cloudinary",
                "api_key", "558733855959418",
                "api_secret", "JPxDqEkHUDBBpEfQm0ZV1VbNTm8"
//                , "secure", true
        ));
    }

    @Override
    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    @Override
    @Transactional(rollbackFor = InternalServerException.class)
    public void deleteImage(String uploadDir, String filename) {

        //Lấy đường dẫn file
        String link = "/media/static/" + filename;
        //Kiểm tra link
        Image image = imageRepository.findByLink(link);
        if (image == null) {
            throw new BadRequestException("File không tồn tại");
        }

        //Kiểm tra ảnh đã được dùng
        Integer inUse = imageRepository.checkImageInUse(link);
        if (inUse != null) {
            throw new BadRequestException("Ảnh đã được sử dụng không thể xóa!");
        }

        //Xóa file trong databases
        imageRepository.delete(image);

        //Kiểm tra file có tồn tại trong thư mục
        File file = new File(uploadDir + "/" + filename);
        if (file.exists()) {
            //Xóa file ở thư mục
            if (!file.delete()) {
                throw new InternalServerException("Lỗi khi xóa ảnh!");
            }
        }
    }

    @Override
    public List<String> getListImageOfUser(long userId) {
        return imageRepository.getListImageOfUser(userId);
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public Map deleteImageCloudinary(String id) {
        //Kiểm tra link
        Optional<Image> image = imageRepository.findById(id);
        if (image.isEmpty()) {
            throw new BadRequestException("File không tồn tại");
        }
        //Kiểm tra ảnh đã được dùng
        Integer inUse = imageRepository.checkImageInUse(image.get().getLink());
        if (inUse != null) {
            throw new BadRequestException("Ảnh đã được sử dụng không thể xóa!");
        }
        //Xóa file trong databases
        imageRepository.delete(image.get());

        try {
            Map resultMap = cloudinary.uploader().destroy(id, ObjectUtils.emptyMap());
            return resultMap;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Có lỗi trong quá trình xóa file Cloudinary!");
        }
    }

    @Override
    public Map uploadImageCloudinaru(MultipartFile multipartFile) {
        try {
            File file = convert(multipartFile);
            Map resultMap = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

            String originalFilename = multipartFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (originalFilename.length() > 0) {
                //Kiểm tra xem file có đúng định dạng không
                if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif") && !extension.equals("svg") && !extension.equals("jpeg")) {
                    throw new BadRequestException("Không hỗ trợ định dạng file này!");
                }
                Image image = new Image();
                image.setId((String) resultMap.get("public_id"));
                image.setName(multipartFile.getName());
                image.setSize(multipartFile.getSize());
                image.setType(multipartFile.getOriginalFilename());

                image.setLink((String) resultMap.get("url"));
                image.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                image.setCreatedBy(((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser());

                saveImage(image);
                return resultMap;
            }
            throw new BadRequestException("File không hợp lệ!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Có lỗi trong quá trình upload file Cloudinary!");
        }
    }

    private File convert(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream fo = new FileOutputStream(file);
        fo.write(multipartFile.getBytes());
        fo.close();
        return file;
    }
}
