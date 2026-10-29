package com.volcengine.model.livesaas.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class ListAvailableVoiceProfilesRequest {
    @JSONField(name = "ProjectName")
    String ProjectName;
    // 缺省或 0 使用第 1 页，不支持负数。
    @JSONField(name = "PageNumber")
    Long PageNumber;
    // 取值 1~100，缺省或 0 使用 20。
    @JSONField(name = "PageSize")
    Integer PageSize;
}
