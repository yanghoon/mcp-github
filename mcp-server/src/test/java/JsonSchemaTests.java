import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.ai.util.json.schema.SpringAiSchemaModule;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public class JsonSchemaTests {
    
    @Test
    void test_json_schema_gen() {
        var methods = ReflectionUtils.getDeclaredMethods(MockTools.class);
        Stream.of(methods)
            .map(m -> JsonSchemaGenerator.generateForMethodInput(m))
            .forEach(System.out::println);
    }

    @Test
    void test_v2() {
        var configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON).with(new Swagger2Module());
        var generator = new SchemaGenerator(configBuilder.build());

        // PersonRequest 클래스의 JSON Schema 생성
        JsonNode schema = generator.generateSchema(Req.class);
        JsonNode schema2 = generator.generateSchema(ReqRoot.class);

        System.out.println(schema.toPrettyString());
        System.out.println(schema2.toPrettyString());
    }

    @Test
    void test_v3() {
        var configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
            // .with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
            .with(new Swagger2Module())
            .with(new SpringAiSchemaModule());
        var generator = new SchemaGenerator(configBuilder.build());

        // PersonRequest 클래스의 JSON Schema 생성
        JsonNode schema = generator.generateSchema(Req.class);
        // JsonNode schema2 = generator.generateSchema(ReqRoot.class);

        System.out.println(schema.toPrettyString());
        // System.out.println(schema2.toPrettyString());
    }

    public static class MockTools {
        // public String tool1(String input) { return ""; }

        // public String tool2(
        //     @ToolParam(required = false, description = "for tool2")
        //     String input
        // ) { return ""; }

        // public String tool3(
        //     @Schema(requiredMode = RequiredMode.REQUIRED, description = "for tool3", defaultValue = "default")
        //     String input
        // ) { return ""; }

        public String tool4(@ToolParam Req req) { return ""; }
    }

    public static class ReqRoot {
        Req req;
    }

    public static class Req {
        @Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "for tool4", defaultValue = "default")
        String input;
    }
}
