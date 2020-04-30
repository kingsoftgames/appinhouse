package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by jianghaitao on 2020/4/22.
 */
@Getter
@Setter
@Builder
@RegisterForReflection
public class AppsResponse {
    List<AppResponse> items;

    @JsonProperty("has_more")
    boolean hasMore;
}
