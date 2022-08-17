package server.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author denghaowen
 * @date 2022/8/11 16:15
 */
@Data
@AllArgsConstructor
public class ScriptPublishDo {
    @Id
    private String FileServiceId;
    private String FileServiceName;
    private String FileServiceHost;
    private Integer FileRegisterExpireSecond;
    private List<String> Registers;
}
