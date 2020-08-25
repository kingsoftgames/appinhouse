package com.seasungames.model;

import com.seasungames.constant.Constants;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Created by jianghaitao on 2020/4/27.
 */

@Getter
@Setter
@RegisterForReflection
public class AddAppVersionRequest extends Request {

    @NotBlank(message = "app is not null")
    private String app;

    @NotBlank(message = "version is not null")
    private String version;

    @NotBlank(message = "time is not null")
    @Pattern(regexp = Constants.ISO8601_REGEXP, message = "time is not a iso8601 time")
    private String time;

    @NotBlank(message = "url is not null")
    @Pattern(regexp = Constants.HTTP_REGEXP, message = "url is not a url")
    @Pattern(regexp = Constants.INSTALL_PACK_REGEXP, message = "url is not a install pack")
    private String url;

    private String description;
}
