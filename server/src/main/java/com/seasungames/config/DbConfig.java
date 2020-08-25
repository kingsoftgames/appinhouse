package com.seasungames.config;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by jianghaitao on 2020/4/22.
 */
@ConfigProperties(prefix = "dynamodb")
public interface DbConfig {
    @ConfigProperty(name = "table.onDemandBilling", defaultValue = "true")
    boolean onDemandBilling();

    @ConfigProperty(name = "table.throughput.read", defaultValue = "1")
    long tableReadThroughput();

    @ConfigProperty(name = "table.throughput.write", defaultValue = "1")
    long tableWriteThroughput();

    @ConfigProperty(name = "table.name.apps", defaultValue = "appinhouse.apps")
    String appsTableName();

    @ConfigProperty(name = "table.name.appversions", defaultValue = "appinhouse.appversions")
    String appVersionsTableName();

    @ConfigProperty(name = "ttl.days", defaultValue = "30")
    int ttlDays();
}
