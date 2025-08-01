package com.redhat.sast.api.v1.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.sast.api.enums.InputSourceType;

public class InputSourceDto {

    @JsonProperty("type")
    private InputSourceType type;

    @JsonProperty("url")
    private String url;

    public InputSourceDto() {}

    public InputSourceDto(InputSourceType type, String url) {
        this.type = type;
        this.url = url;
    }

    public InputSourceType getType() {
        return type;
    }

    public void setType(InputSourceType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
