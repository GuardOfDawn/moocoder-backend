package com.moekr.moocoder.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.moocoder.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CodeVO {
    private Integer examId;
    @JsonProperty("end_at")
    @JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
    private LocalDateTime endAt;
    private Integer questionId;
    private String language;
    private String projectName;
    private Map<String, String> editable;
    private Map<String, String> uneditable;

    public CodeVO(Integer examId, LocalDateTime endAt, Integer questionId, String language, String projectName, Map<String, String> editable, Map<String, String> uneditable){
        this.examId = examId;
        this.endAt = endAt;
        this.questionId = questionId;
        this.language = language;
        this.projectName = projectName;
        this.editable = editable;
        this.uneditable = uneditable;
    }
}
