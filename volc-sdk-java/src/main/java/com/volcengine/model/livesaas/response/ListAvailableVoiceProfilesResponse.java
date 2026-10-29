package com.volcengine.model.livesaas.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.volcengine.model.response.ResponseMetadata;
import lombok.Data;

import java.util.List;

@Data
public class ListAvailableVoiceProfilesResponse {
    @JSONField(name = "ResponseMetadata")
    ResponseMetadata responseMetadata;
    @JSONField(name = "Result")
    ListAvailableVoiceProfilesResponseBody result;

    @Data
    public static class ListAvailableVoiceProfilesResponseBody {
        @JSONField(name = "Items")
        List<AvailableVoiceProfile> Items;
        @JSONField(name = "Total")
        Long Total;
    }

    @Data
    public static class AvailableVoiceProfile {
        // 音色版本 ID，可直接用于同项目 CreateTask 的 VoiceProfileID。
        @JSONField(name = "VoiceProfileID")
        Long VoiceProfileID;
        @JSONField(name = "VoiceProfileName")
        String VoiceProfileName;
        // PRESET：预置音色；CUSTOM：自定义音色。
        @JSONField(name = "VoiceType")
        String VoiceType;
    }
}
