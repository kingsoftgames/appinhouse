package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seasungames.db.pojo.AppVersionItem;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by jianghaitao on 2020/4/27.
 */

@Getter
@Setter
@RegisterForReflection
public class AppVersionResponse {

    private String app;
    private String version;
    private String platform;
    private String description;
    private long ctime;
    private String url;
    @JsonProperty("more_urls")
    private Map<String, String> moreUrls;

    public static AppVersionResponse from(AppVersionItem item) {
        AppVersionResponse response = new AppVersionResponse();
        response.setApp(item.app());
        response.setPlatform(item.platform());
        response.setDescription(item.desc());
        response.setVersion(item.version());
        response.setCtime(item.time());
        response.setUrl(item.url());
        response.setMoreUrls(item.moreUrls());
        return response;
    }
}
