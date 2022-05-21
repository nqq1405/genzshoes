package com.quyquang.genzshoes3.service;

import com.quyquang.genzshoes3.entity.Post;
import com.quyquang.genzshoes3.entity.User;
import com.quyquang.genzshoes3.model.dto.PageableDTO;
import com.quyquang.genzshoes3.model.request.CreatePostRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {
    PageableDTO adminGetListPost(String title, String status, int page);

    Post createPost(CreatePostRequest createPostRequest, User user);

    void updatePost(CreatePostRequest createPostRequest, User user, Long id);

    void deletePost(long id);

    Post getPostById(long id);

    Page<Post> adminGetListPosts(String title, String status, Integer page);

    List<Post> getLatesPost();

    Page<Post> getListPost(int page);

    List<Post> getLatestPostsNotId(long id);

    long getCountPost();
}
