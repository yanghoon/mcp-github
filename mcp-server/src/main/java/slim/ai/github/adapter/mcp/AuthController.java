package slim.ai.github.adapter.mcp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    
    @GetMapping("home")
    public String home() {
        return "Welcome to the GitHub MCP Server!";
    }

    @GetMapping("token")
    public String token() {
        return "Welcome to the GitHub MCP Server!";
    }

}
