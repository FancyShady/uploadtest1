package server.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author denghaowen
 * @date 2022/8/11 16:18
 */
@Getter
@AllArgsConstructor
public class ScriptPublishDto {
    private final String fileServiceId;
    private final String fileServiceName;
    private final String fileServiceHost;
    private final Integer fileServiceExpireSecond;
    private final String token;
}
