package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LocalTest {


    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException {
        String json = "{\n" +
                "  \"sub\": \"100000000000000042000002\",\n" +
                "  \"name\": \"Perf0042000002\",\n" +
                "  \"admin\": true,\n" +
                "  \"jti\": \"bfa750c0-99d5-487e-87fa-581db3bdddcd\",\n" +
                "  \"iat\": 1496427213,\n" +
                "  \"amr\": [\"mfa\"],\n" +
                "  \"exp\": 1496430813\n" +
                "}";
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiAiMTAwMDAwMDAwMDAwMDAwMDQyMDAwMDAxIiwibmFtZSI6ICJQZXJmMDA0MjAwMDAwMSIsImFkbWluIjogdHJ1ZSwianRpIjogImJmYTc1MGMwLTk5ZDUtNDg3ZS04N2ZhLTU4MWRiM2JkZGRjZCIsICJhbXIiOlsibWZhIl0sImlhdCI6IDE0OTY0MjcyMTMsImV4cCI6IDE0OTY0MzA4MTN9.upEbvJFnPtlNhIBFRpuuFLhPzvJ79Nf8HZnkWCi-UBs";

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return;
        }
        String payload = parts[1];
        String decodedPayload = new String(Base64.decodeBase64(payload.getBytes(UTF_8)), UTF_8);
//        JacksonMapConvertor.fromJson(decodedPayload.getBytes());

        Map map = objectMapper.readValue(decodedPayload, Map.class);
        System.out.println("map = " + map);
    }
}
