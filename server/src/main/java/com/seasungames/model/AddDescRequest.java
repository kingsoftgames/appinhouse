package com.seasungames.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seasungames.constant.Constants;
import com.seasungames.constant.Environment;
import com.seasungames.constant.Platform;
import com.seasungames.validation.Enum;
import com.seasungames.validation.NotNullIfAnotherFieldHasValue;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Created by jianghaitao on 2020/4/27.
 */

@NotNullIfAnotherFieldHasValue.List({
        @NotNullIfAnotherFieldHasValue(
                fieldName = "platform",
                fieldValue = Constants.IOS_PLATFORM,
                dependFieldName = "id",
                equal = true,
                message = "id is not null"),
        @NotNullIfAnotherFieldHasValue(
                fieldName = "platform",
                fieldValue = Constants.IOS_PLATFORM,
                dependFieldName = "title",
                equal = true,
                message = "title is not null"),
        @NotNullIfAnotherFieldHasValue(
                fieldName = "softwareUrlExtendName",
                dependFieldName = "softwareUrlExtendKey",
                message = "softwareUrlExtendKey is not null"),
        @NotNullIfAnotherFieldHasValue(
                fieldName = "softwareUrlExtendKey",
                dependFieldName = "softwareUrlExtendName",
                message = "softwareUrlExtendName is not null")
})

@Getter
@Setter
@RegisterForReflection
public class AddDescRequest extends Request {

    @NotBlank(message = "app is not null")
    private String app;

    @Enum(enumClass = Platform.class, method = "getName", message = "platform is invalid")
    private String platform;

    @NotBlank(message = "version is not null")
    private String version;

    @Enum(enumClass = Environment.class, method = "getName", message = "environment is invalid")
    private String environment;

    @NotBlank(message = "time is not null")
    @Pattern(regexp = Constants.ISO8601_REGEXP, message = "time is not a iso8601 time")
    private String time;

    @NotBlank(message = "channel is not null")
    private String channel;

    @NotBlank(message = "url is not null")
    @Pattern(regexp = Constants.HTTP_REGEXP, message = "url is not a url")
    private String url;

    @JsonProperty("software_url")
    @NotBlank(message = "software_url is not null")
    @Pattern(regexp = Constants.HTTP_REGEXP, message = "software_url is not a url")
    private String softwareUrl;

    private String id;

    private String description;

    private String title;

    @JsonProperty("full_url")
    @Pattern(regexp = Constants.HTTP_REGEXP, message = "full_url is not a url")
    private String fullUrl;

    @JsonProperty("display_url")
    @Pattern(regexp = Constants.HTTP_REGEXP, message = "display_url is not a url")
    private String displayUrl;

    @JsonProperty("software_url_extend_name")
    private String softwareUrlExtendName;

    @JsonProperty("software_url_extend_key")
    private String softwareUrlExtendKey;

}
