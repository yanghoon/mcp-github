# GitHub MCP Server

## Features

* Support OAuth2

## Test with MCP Inspector

```bash
# npx @modelcontextprotocol/inspector
git clone https://github.com/modelcontextprotocol/inspector && cd inspector
npm install
npm run dev
```

### Test Scenario

1. Run MCP Inspector
2. Connect to GitHub MCP Server
    * Type `Transport Type` as `SSE` (Server Sent Events)
    * Type `URL` as `http://localhost:8080/sse`
    * Click `Connect`
3. Test MCP Server Functions
4. Read logs
    * Click a item in the `History` section

#### Test Tools

1. List tools from MCP Server
    * Click `List Tools` in `Tools`
    * Check tools and description

2. Call a Tool
    * Click a tool in the list
    * Type arguments
    * Click `Run Tool`


## References

* https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html
* https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html
