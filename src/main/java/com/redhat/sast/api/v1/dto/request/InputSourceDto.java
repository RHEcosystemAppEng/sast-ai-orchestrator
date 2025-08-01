package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.InputSourceType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputSourceDto {

    @JsonProperty("type")
    private InputSourceType type;

    @JsonProperty("url")
    private String url;
}
