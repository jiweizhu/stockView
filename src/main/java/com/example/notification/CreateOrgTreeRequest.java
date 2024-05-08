package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CreateOrgTreeRequest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException {
        DecimalFormat xFormat = new DecimalFormat("00");
        DecimalFormat yFormat = new DecimalFormat("00");
        int index = 1;
        ArrayList<String> arrayList = new ArrayList();
        for (int j = 1; j  < 2; j++) {
            for (int i = 5; i < 20; i++) {
                String x = xFormat.format(j);
                String y = yFormat.format(i);
//                arrayList.add("{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_1_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_1_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_tree"+y+"_1_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_1_Boeing%08d\",\"poc\":\"poc_1_%08d\",\"org_domain\":\"org_1_domain_%08d\",\"status\":\"enabled\",\"parent_org_id\":null,\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"},{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_2_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_2_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_2_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_2_Boeing%08d\",\"poc\":\"poc_2_%08d\",\"org_domain\":\"org_2_domain_%08d\",\"status\":\"enabled\",\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"},{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_3_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_3_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_3_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_3_Boeing%08d\",\"poc\":\"poc_3_%08d\",\"org_domain\":\"org_3_domain_%08d\",\"status\":\"enabled\",\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"},{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_4_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_4_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_4_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_4_Boeing%08d\",\"poc\":\"poc_4_%08d\",\"org_domain\":\"org_4_domain_%08d\",\"status\":\"enabled\",\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"},{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_5_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_5_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_5_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_5_Boeing%08d\",\"poc\":\"poc_5_%08d\",\"org_domain\":\"org_5_domain_%08d\",\"status\":\"enabled\",\"parent_org_id\":null,\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"},{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_6_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_6_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_6_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_6_Boeing%08d\",\"poc\":\"poc_6_%08d\",\"org_domain\":\"org_6_domain_%08d\",\"status\":\"enabled\",\"parent_org_id\":null,\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"},{\"iam_org_id\":\"IAM_ORG77_tree"+y+"_7_ERIC%08d\",\"org_id\":\"ORG77_tree"+y+"_7_ERIC%08d\",\"chub_id\":\"chub_id_tree"+y+"_7_%08d\",\"creation_timestamp\":\"${rawsql('sysdate-2')}\",\"lastupdatedtimestamp\":\"${rawsql('sysdate-2')}\",\"org_name\":\"ORG77_tree"+y+"_7_Boeing%08d\",\"poc\":\"poc_7_%08d\",\"org_domain\":\"org_7_domain_%08d\",\"status\":\"enabled\",\"parent_org_id\":null,\"member_types\":\"org\",\"is_parent_allowed\":\"T\",\"is_child_allowed\":\"T\"}");
//                arrayList.add("{\"iam_tree_id\":\"ORG77_tree"+y+"_1_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_1_ERIC%08d\",\"parent_iam_org_id\":\"\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486},{\"iam_tree_id\":\"ORG77_tree"+y+"_2_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_2_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG77_tree"+y+"_1_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486},{\"iam_tree_id\":\"ORG77_tree"+y+"_3_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_3_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG77_tree"+y+"_1_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486},{\"iam_tree_id\":\"ORG77_tree"+y+"_4_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_4_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG77_tree"+y+"_3_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486},{\"iam_tree_id\":\"ORG77_tree"+y+"_5_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_5_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG77_tree"+y+"_4_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486},{\"iam_tree_id\":\"ORG77_tree"+y+"_6_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_6_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG77_tree"+y+"_4_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486},{\"iam_tree_id\":\"ORG77_tree"+y+"_7_treeId_%08d\",\"iam_org_id\":\"IAM_ORG77_tree"+y+"_7_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG77_tree"+y+"_3_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":\"17001"+y+"783486\",\"LASTUPDATEDTIMESTAMP\":17001"+y+"783486}");
                arrayList.add("{\"iam_tree_id\":\"IAM_ORG81_Tree"+y+"_ORG01_ERIC%08d\",\"iam_org_id\":\"IAM_ORG81_Tree"+y+"_ORG01_ERIC%08d\",\"parent_iam_org_id\":\"\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":1695119004758,\"LASTUPDATEDTIMESTAMP\":1695119004758},{\"iam_tree_id\":\"IAM_ORG81_Tree"+y+"_ORG02_ERIC%08d\",\"iam_org_id\":\"IAM_ORG81_Tree"+y+"_ORG02_ERIC%08d\",\"parent_iam_org_id\":\"IAM_ORG81_Tree"+y+"_ORG01_ERIC%08d\",\"creator\":\"PTLTCLIENT019\",\"creation_timestamp\":1695119004758,\"LASTUPDATEDTIMESTAMP\":1695119004758}");
                index++;
            }
        }
//        String valueAsString = objectMapper.writeValueAsString(arrayList);
        System.out.println("\"child_orgs\": " + arrayList);
    }
}
