package server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wangjunrui
 * @date 2022/8/4
 **/
@Data
@ConfigurationProperties("script.publish")
public class ScriptPublishProperties {

    /**
     * 脚本路径
     */
    private String localPath;

    /**
     * 强制覆盖
     */
    private boolean override;

    /**
     * 注册信息
     */
    private Register register;

    @Data
    public static class Register {

        /**
         * 服务id
         */
        private String serviceId;

        /**
         * 服务名称
         */
        private String serviceName;

        /**
         * 当前服务请求域名
         */
        private String host;

        /**
         * 注册信息过期时间
         */
        private Integer expireSecond;

        public String getServiceName() {
            return serviceName == null || serviceName.length() == 0 ? serviceId : serviceName;
        }
    }
}
