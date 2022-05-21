package com.quyquang.genzshoes3.service;

import com.quyquang.genzshoes3.entity.Comment;
import com.quyquang.genzshoes3.model.request.CreateCommentPostRequest;
import com.quyquang.genzshoes3.model.request.CreateCommentProductRequest;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    Comment createCommentPost(CreateCommentPostRequest createCommentPostRequest,long userId);
    Comment createCommentProduct(CreateCommentProductRequest createCommentProductRequest, long userId);
}
