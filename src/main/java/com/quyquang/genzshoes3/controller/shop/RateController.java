package com.quyquang.genzshoes3.controller.shop;//package com.quyquang.genzshoes3.controller.shop;
//
//import com.quyquang.genzshoes3.entity.Rate;
//import com.quyquang.genzshoes3.entity.User;
//import com.quyquang.genzshoes3.model.request.CreateRateProductRequest;
//import com.quyquang.genzshoes3.security.CustomUserDetails;
//import com.quyquang.genzshoes3.service.RateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@Controller
//public class RateController {
//
//    @Autowired
//    private RateService rateService;
//
//    @PostMapping("/api/rates/product")
//    public ResponseEntity<Object> createRate(@RequestBody CreateRateProductRequest createRateProductRequest){
//        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
//        Rate rate = rateService.createRate(createRateProductRequest,user.getId());
//        return ResponseEntity.ok(rate);
//    }
//
//}
