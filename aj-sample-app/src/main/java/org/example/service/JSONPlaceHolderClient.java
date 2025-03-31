package org.example.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

@FeignClient(value = "jplaceholder", url = "https://jsonplaceholder.typicode.com/", configuration = ClientConfiguration.class)
public interface JSONPlaceHolderClient {

    @RequestMapping(method = RequestMethod.GET, value = "/posts")
    List<Post> getPosts();

    @RequestMapping(method = RequestMethod.GET, value = "/posts/{postId}", produces = HttpConstants.CONTENT_TYPE_JSON
)
    Post getPostById(@PathVariable("postId") Long postId);

    @RequestMapping(method = RequestMethod.GET, value = "/posts/{postId}", produces = HttpConstants.CONTENT_TYPE_JSON
)
    Map<String, Object> getPostMapById(@PathVariable("postId") Long postId);
}