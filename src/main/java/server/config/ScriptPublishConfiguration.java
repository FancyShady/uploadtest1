package server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangjunrui
 * @date 2022/8/4
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(ScriptPublishProperties.class)
public class ScriptPublishConfiguration {

}
