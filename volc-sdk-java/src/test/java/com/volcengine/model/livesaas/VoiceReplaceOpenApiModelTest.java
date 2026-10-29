package com.volcengine.model.livesaas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.volcengine.helper.Const;
import com.volcengine.helper.Utils;
import com.volcengine.model.ApiInfo;
import com.volcengine.model.livesaas.request.CreateTaskRequest;
import com.volcengine.model.livesaas.request.ListAvailableVoiceProfilesRequest;
import com.volcengine.model.livesaas.request.ListTasksRequest;
import com.volcengine.model.livesaas.response.GetMergedReportResponse;
import com.volcengine.model.livesaas.response.GetReportResponse;
import com.volcengine.model.livesaas.response.GetTaskResponse;
import com.volcengine.model.livesaas.response.ListAvailableVoiceProfilesResponse;
import com.volcengine.model.livesaas.response.ListTasksResponse;
import com.volcengine.service.livesaas.LivesaasConfig;
import com.volcengine.service.livesaas.LivesaasService;
import com.volcengine.service.livesaas.impl.LivesaasServiceImpl;
import org.apache.http.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class VoiceReplaceOpenApiModelTest {

    @Test
    public void createTaskPreservesVoiceSelectionAndMute() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectName("project-1");
        request.setTaskType(2);
        request.setActivityId(1879012086574103L);
        request.setMuteEnabled(true);
        request.setVoiceReplaceEnabled(true);
        request.setVoiceProfileID(9007199254740993L);
        JSONObject encoded = JSON.parseObject(JSON.toJSONString(request));
        Assert.assertEquals(Boolean.TRUE, encoded.getBoolean("VoiceReplaceEnabled"));
        Assert.assertEquals(Long.valueOf(9007199254740993L), encoded.getLong("VoiceProfileID"));
        Assert.assertEquals(Boolean.TRUE, encoded.getBoolean("MuteEnabled"));
        Assert.assertEquals(request, JSON.parseObject(encoded.toJSONString(), CreateTaskRequest.class));
    }

    @Test
    public void optionalRequestFieldsDistinguishOmittedAndFalse() {
        CreateTaskRequest create = new CreateTaskRequest();
        JSONObject absent = JSON.parseObject(JSON.toJSONString(create));
        Assert.assertFalse(absent.containsKey("VoiceReplaceEnabled"));
        Assert.assertFalse(absent.containsKey("VoiceProfileID"));
        create.setVoiceReplaceEnabled(false);
        Assert.assertEquals(Boolean.FALSE, JSON.parseObject(JSON.toJSONString(create)).getBoolean("VoiceReplaceEnabled"));

        ListTasksRequest list = new ListTasksRequest();
        Assert.assertFalse(JSON.parseObject(JSON.toJSONString(list)).containsKey("VoiceReplaceEnabledOnly"));
        for (boolean enabled : new boolean[]{false, true}) {
            list.setVoiceReplaceEnabledOnly(enabled);
            list.setMuteEnabledOnly(true);
            JSONObject encoded = JSON.parseObject(JSON.toJSONString(list));
            Assert.assertEquals(Boolean.valueOf(enabled), encoded.getBoolean("VoiceReplaceEnabledOnly"));
            Assert.assertEquals(Boolean.TRUE, encoded.getBoolean("MuteEnabledOnly"));
        }
    }

    @Test
    public void taskResponsesPreserveVoiceSwitchAndLegacyAbsence() {
        for (String fields : new String[]{"", "\"VoiceReplaceEnabled\":true", "\"VoiceReplaceEnabled\":false"}) {
            JSONObject expected = JSON.parseObject("{" + fields + "}");
            GetTaskResponse task = JSON.parseObject("{\"Result\":" + expected + "}", GetTaskResponse.class);
            Assert.assertEquals(expected, JSON.parseObject(JSON.toJSONString(task)).getJSONObject("Result"));
            ListTasksResponse tasks = JSON.parseObject("{\"Result\":{\"TaskDetails\":[" + expected + "]}}", ListTasksResponse.class);
            Assert.assertEquals(expected, JSON.parseObject(JSON.toJSONString(tasks)).getJSONObject("Result")
                    .getJSONArray("TaskDetails").getJSONObject(0));
        }
    }

    @Test
    public void availableVoicesPreserveVersionIdsAndEmptyPage() {
        String json = "{\"ResponseMetadata\":{\"RequestId\":\"req-list\"},\"Result\":{\"Items\":["
                + "{\"VoiceProfileID\":9007199254740993,\"VoiceProfileName\":\"清新女声\",\"VoiceType\":\"PRESET\"},"
                + "{\"VoiceProfileID\":18,\"VoiceProfileName\":\"主播小明\",\"VoiceType\":\"CUSTOM\"}],\"Total\":2}}";
        ListAvailableVoiceProfilesResponse response = JSON.parseObject(json, ListAvailableVoiceProfilesResponse.class);
        Assert.assertEquals(JSON.parseObject(json), JSON.parseObject(JSON.toJSONString(response)));
        CreateTaskRequest create = new CreateTaskRequest();
        create.setVoiceProfileID(response.getResult().getItems().get(0).getVoiceProfileID());
        Assert.assertEquals(Long.valueOf(9007199254740993L), create.getVoiceProfileID());

        ListAvailableVoiceProfilesResponse empty = JSON.parseObject(
                "{\"Result\":{\"Items\":[],\"Total\":2}}", ListAvailableVoiceProfilesResponse.class);
        Assert.assertTrue(empty.getResult().getItems().isEmpty());
        Assert.assertEquals(Long.valueOf(2), empty.getResult().getTotal());
    }

    @Test
    public void availableVoiceQueryUsesExpectedActionVersionAndParameters() throws Exception {
        ListAvailableVoiceProfilesRequest request = new ListAvailableVoiceProfilesRequest();
        request.setProjectName("project-1");
        List<NameValuePair> pairs = Utils.paramsToPair(request);
        Assert.assertEquals(1, pairs.size());
        Assert.assertEquals("project-1", queryValue(pairs, "ProjectName"));
        request.setPageNumber(2147483648L);
        request.setPageSize(100);
        pairs = Utils.paramsToPair(request);
        Assert.assertEquals(3, pairs.size());
        Assert.assertEquals("2147483648", queryValue(pairs, "PageNumber"));
        Assert.assertEquals("100", queryValue(pairs, "PageSize"));

        ApiInfo api = LivesaasConfig.apiInfoList.get(Const.ListAvailableVoiceProfiles);
        Assert.assertNotNull(api);
        Assert.assertEquals("GET", api.getMethod());
        Assert.assertEquals("/", api.getPath());
        Assert.assertEquals("ListAvailableVoiceProfiles", queryValue(api.getQuery(), "Action"));
        Assert.assertEquals("2024-08-01", queryValue(api.getQuery(), "Version"));
        Assert.assertEquals(ListAvailableVoiceProfilesResponse.class, LivesaasService.class
                .getMethod("listAvailableVoiceProfiles", ListAvailableVoiceProfilesRequest.class).getReturnType());
        Assert.assertEquals(ListAvailableVoiceProfilesResponse.class, LivesaasServiceImpl.class
                .getMethod("listAvailableVoiceProfiles", ListAvailableVoiceProfilesRequest.class).getReturnType());
    }

    @Test
    public void bothReportsPreserveAllVoiceResultsWithoutInternalDetails() {
        assertReportResult("PENDING", 0L);
        assertReportResult("SUCCEEDED", 1792376199000L);
        assertReportResult("FALLBACK_MUTE", 1792376199000L);
        assertReportResult("FAILED", 1792376199000L);
    }

    @Test
    public void legacyReportsDoNotAcquireVoiceFields() {
        String json = "{\"Result\":{\"RuleResultDetails\":[{\"MachineDetails\":{"
                + "\"CustomSensitiveResults\":[{\"AuditID\":1,\"MuteExecutionStatus\":\"FULL_HIT\","
                + "\"MuteExecutionDetail\":{\"BeginPtsMs\":1000,\"EndPtsMs\":2000,\"OccurredAt\":1792376199000,\"Reason\":\"\"}}],"
                + "\"SystemSensitiveResults\":[{\"AuditID\":2,\"MuteExecutionStatus\":\"PENDING\"}]}}]}}";
        Assert.assertEquals(JSON.parseObject(json), JSON.parseObject(JSON.toJSONString(JSON.parseObject(json, GetReportResponse.class))));
        Assert.assertEquals(JSON.parseObject(json), JSON.parseObject(JSON.toJSONString(JSON.parseObject(json, GetMergedReportResponse.class))));
    }

    private void assertReportResult(String status, long occurredAt) {
        JSONObject detail = new JSONObject();
        detail.put("BeginPtsMs", 1000L);
        detail.put("EndPtsMs", 2000L);
        detail.put("OccurredAt", occurredAt);
        detail.put("ReplacementText", "实惠");
        String json = "{\"Result\":{\"RuleResultDetails\":[{\"MachineDetails\":{\"CustomSensitiveResults\":[{"
                + "\"AuditID\":9007199254740993,\"VoiceReplaceExecutionStatus\":\"" + status + "\","
                + "\"VoiceReplaceExecutionDetail\":" + detail.toJSONString() + "}]}}]}}";
        String internalJson = json.replace("\"ReplacementText\":", "\"FailureCode\":\"TTS_TIMEOUT\",\"FailureReason\":\"内部排障信息\",\"FinalAction\":\"MUTE\",\"ReplacementText\":");
        GetReportResponse report = JSON.parseObject(internalJson, GetReportResponse.class);
        GetMergedReportResponse merged = JSON.parseObject(internalJson, GetMergedReportResponse.class);
        Assert.assertEquals(JSON.parseObject(json), JSON.parseObject(JSON.toJSONString(report)));
        Assert.assertEquals(JSON.parseObject(json), JSON.parseObject(JSON.toJSONString(merged)));
        Assert.assertEquals(status, report.getResult().getRuleResultDetails().get(0).getMachineDetails()
                .getCustomSensitiveResults().get(0).getVoiceReplaceExecutionStatus());
        Assert.assertEquals(Long.valueOf(occurredAt), merged.getResult().getRuleResultDetails().get(0).getMachineDetails()
                .getCustomSensitiveResults().get(0).getVoiceReplaceExecutionDetail().getOccurredAt());
    }

    private String queryValue(List<NameValuePair> pairs, String name) {
        for (NameValuePair pair : pairs) {
            if (name.equals(pair.getName())) {
                return pair.getValue();
            }
        }
        return null;
    }
}
