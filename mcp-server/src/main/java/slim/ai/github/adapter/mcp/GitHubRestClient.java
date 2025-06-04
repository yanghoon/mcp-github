package slim.ai.github.adapter.mcp;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("github")
public interface GitHubRestClient {

    @GetMapping("user")
    Object getUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token);
    
    @GetMapping("/users/{username}/repos")
    List<Object> listRepos(
        @PathVariable("username") String username,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    );

}
