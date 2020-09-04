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
public class AddAppVersionUrlRequest extends Request {

    @NotBlank(message = "app is not null")
    private String app;

    @NotBlank(message = "version is not null")
    private String version;

    @NotBlank(message = "name is not null")
    private String name;

    @NotBlank(message = "url is not null")
    @Pattern(regexp = Constants.HTTP_REGEXP, message = "url is not a url")
    @Pattern(regexp = Constants.INSTALL_PACK_REGEXP, message = "url is not a install pack")
    private String url;
}
