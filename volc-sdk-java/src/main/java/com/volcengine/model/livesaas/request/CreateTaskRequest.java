package com.volcengine.model.livesaas.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.volcengine.model.beans.livesaas.SpecifiableRuleArg;
import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {
    @JSONField(name = "ProjectName")
    String ProjectName;
    @JSONField(name = "TaskName")
    String TaskName;
    @JSONField(name = "TaskType")
    Integer TaskType;
    @JSONField(name = "ScenarioId")
    Long ScenarioId;
    @JSONField(name = "LivePullURL")
    String LivePullURL;
    @JSONField(name = "CallbackURL")
    String CallbackURL;
    @JSONField(name = "Extra")
    String Extra;
    @JSONField(name = "ActivityId")
    Long ActivityId;
    @JSONField(name = "TaskStartTime")
    Long TaskStartTime;
    @JSONField(name = "TagIds")
    List<Long> TagIds;
    @JSONField(name = "SpecifiableRuleArgs")
    List<SpecifiableRuleArg> SpecifiableRuleArgs;
    @JSONField(name = "MuteEnabled")
    Boolean MuteEnabled;
    // 仅支持企业直播直播间质检，缺省为 false，可与消音同时开启。
    @JSONField(name = "VoiceReplaceEnabled")
    Boolean VoiceReplaceEnabled;
    // 开启替换时必填，使用同项目 ListAvailableVoiceProfiles 返回的音色版本 ID。
    @JSONField(name = "VoiceProfileID")
    Long VoiceProfileID;

}
