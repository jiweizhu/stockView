package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class LocalTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException {
        String json = "{\"password\":{\"ttl_second\":777600,\"qa_url\":\"http://eric-idm-authn:8080/authn/v1/availability,\",\"cwou\":true,\"success_counter_name\":\"passwordSuccess\",\"to_check\":true},\"fpt\":{\"ttl_second\":1800,\"factor\":[\"have\",\"are\"],\"qa_url\":\"http://eric-idm-authn:8080/authn/v1/availability,\",\"cwou\":true,\"success_counter_name\":\"bioSuccess\",\"to_check\":true},\"bio_fingerprint\":{\"ttl_second\":1800,\"factor\":[\"have\",\"are\"],\"qa_url\":\"http://eric-idm-authn:8080/authn/v1/availability,\",\"cwou\":true,\"success_counter_name\":\"bioSuccess\",\"to_check\":true},\"security_question\":{\"ttl_second\":1800,\"qa_url\":\"http://eric-idm-authn:8080/authn/v1/availability,\",\"cwou\":true,\"success_counter_name\":\"securityQuestionSuccess\",\"to_check\":true},\"federation\":{\"ttl_second\":1800,\"qa_url\":\"http://eric-idm-idf:8080/idf/v1/availability\",\"cwou\":true,\"to_check\":true},\"temp_pin\":{\"ttl_second\":1800},\"pin\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-authorize:8080/oauth2/v1/availability\",\"cwou\":false,\"success_counter_name\":\"pinSuccess\",\"to_check\":true},\"sms_temp_pin\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-authorize:8080/oauth2/v1/availability\",\"cwou\":false,\"success_counter_name\":\"sms_temp_pinSuccess\",\"to_check\":true},\"email_temp_pin\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-authorize:8080/oauth2/v1/availability\",\"cwou\":false,\"success_counter_name\":\"email_temp_pinSuccess\",\"to_check\":true},\"tax_id\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-authorize:8080/oauth2/v1/availability\",\"cwou\":false,\"success_counter_name\":\"tax_idSuccess\",\"to_check\":true},\"ssn_4\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-authorize:8080/oauth2/v1/availability\",\"cwou\":false,\"success_counter_name\":\"ssn_4Success\",\"to_check\":true},\"google_otp\":{\"ttl_second\":1800,\"qa_url\":\"http://eric-idm-otp:8080/otp/v1/availability\",\"cwou\":false,\"success_counter_name\":\"googleOTPSuccess\",\"to_check\":true},\"rec_code\":{\"ttl_second\":1800,\"qa_url\":\"http://eric-idm-otp:8080/otp/v1/availability\",\"cwou\":false,\"success_counter_name\":\"recCodeSuccess\",\"to_check\":true},\"symantec_otp\":{\"ttl_second\":1800,\"qa_url\":\"http://eric-idm-otp:8080/otp/v1/availability\",\"cwou\":false,\"to_check\":true},\"hwk\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-das:8080/das/v1/availability\",\"cwou\":false,\"to_check\":true},\"push_temp_pin\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-das:8080/das/v1/availability\",\"cwou\":false,\"success_counter_name\":\"push_temp_pinSuccess\",\"to_check\":true},\"push_bio\":{\"ttl_second\":1800},\"push_bio_1fa\":{\"ttl_second\":1800},\"push_bio_1fa_2fa\":{\"ttl_second\":1800,\"factor\":[\"have\",\"are\"]},\"signup\":{\"ttl_second\":1800},\"rf\":{\"ttl_second\":1800},\"dat\":{\"ttl_second\":1800},\"bypass1fa\":{\"ttl_second\":1800},\"bypass2fa\":{\"ttl_second\":1800},\"ban_pin\":{\"ttl_second\":1800},\"tenant_password\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-sta:8080/sta/v1/availability\",\"cwou\":true,\"success_counter_name\":\"tenant_passwordSuccess\",\"to_check\":true},\"sms_random_otp\":{\"ttl_second\":1800,\"qa_url\":\"http://eric-idm-otp:8080/otp/v1/availability\",\"success_counter_name\":\"smsOTPSuccess\",\"to_check\":true},\"face\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-idv:8080/identityVerification/v1/availability\",\"cwou\":false},\"refresh\":{\"ttl_second\":1800},\"refresh_2fa\":{\"ttl_second\":1800},\"swk\":{\"ttl_second\":1800},\"doc\":{\"ttl_second\":1800,\"qa_url\":\"http://tmo-iam-ca-idv:8080/identityVerification/v1/availability\",\"cwou\":false}}";
        int index = 101;
        HashMap hashMap = objectMapper.readValue(json, HashMap.class);
        hashMap.keySet().forEach(key -> {
            Boolean cwou = (Boolean) ((HashMap<String, Object>) hashMap.get(key)).get("cwou");
            Boolean to_check = (Boolean) ((HashMap<String, Object>) hashMap.get(key)).get("to_check");
            String qa_url = (String) ((HashMap<String, Object>) hashMap.get(key)).get("qa_url");
            String success_counter_name = (String) ((HashMap<String, Object>) hashMap.get(key)).get("success_counter_name");
            ArrayList<String> factors = (ArrayList<String>) ((HashMap<String, Object>) hashMap.get(key)).get("factor");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("insert into amr (amrname, cwou, qaurl, tocheck, ttlsecond, successcountername, factors)values (")
                    .append("'"+key + "', ")
                    .append(cwou + ", ")
                    .append("'"+qa_url + "', ")
                    .append(to_check + ", ")
                    .append(86400000 + ", ")
                    .append("'"+success_counter_name + "', ")
                    .append(factors).append(");");
            System.out.println(stringBuilder);

        });
    }
}
