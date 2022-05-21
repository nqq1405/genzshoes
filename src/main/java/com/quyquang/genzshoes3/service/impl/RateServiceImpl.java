package com.quyquang.genzshoes3.service.impl;//package com.quyquang.genzshoes3.service.impl;
//
//import com.quyquang.genzshoes3.entity.Product;
//import com.quyquang.genzshoes3.entity.Rate;
//import com.quyquang.genzshoes3.entity.User;
//import com.quyquang.genzshoes3.exception.InternalServerException;
//import com.quyquang.genzshoes3.model.request.CreateRateProductRequest;
//import com.quyquang.genzshoes3.repository.ProductRepository;
//import com.quyquang.genzshoes3.repository.RateRepository;
//import com.quyquang.genzshoes3.service.RateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.sql.Timestamp;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//public class RateServiceImpl implements RateService {
//
//    @Autowired
//    private RateRepository rateRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Override
//    public Rate createRate(CreateRateProductRequest createRateProductRequest, long userId) {
//        Rate rate = new Rate();
//        rate.setRating(createRateProductRequest.getRate());
//        rate.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//        Product product = new Product();
//        product.setId(createRateProductRequest.getProductId());
//        rate.setProduct(product);
//        User user = new User();
//        user.setId(userId);
//        rate.setUser(user);
//        try {
//            rateRepository.save(rate);
//        }catch (Exception e){
//            throw new InternalServerException("Có lỗi khi đánh giá");
//        }
//        return rate;
//    }
//
//}
