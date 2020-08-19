package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seasungames.db.pojo.AppItem;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jianghaitao on 2020/4/22.
 */
@Getter
@Setter
@RegisterForReflection
public class AppResponse {
    private String app;

    private String description;

    private String alias;

    private long ctime;

    @JsonProperty("ios_title")
    private String iosTitle;

    @JsonProperty("ios_bundle_id")
    private String iosBundleId;

    public static AppResponse from(AppItem appItem) {
        AppResponse response = new AppResponse();
        response.setCtime(appItem.ctime());
        response.setAlias(appItem.alias());
        response.setApp(appItem.app());
        response.setDescription(appItem.description());
        response.setIosBundleId(appItem.iosBundleId());
        response.setIosTitle(appItem.iosTitle());
        return response;
    }
}
