package server.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

/**
 * @author wangjunrui
 * @date 2022/8/4
 **/
public interface PublishService {

    /**
     * 文件处理，将文件写入具体的位置
     * @param filePart
     * @return
     */
    Mono<Void> fileHandle(FilePart filePart);

    /**
     * 上报注册信息，定时任务，每隔一定时间会执行向 redis 去上报当前服务的地址和服务名
     * @return
     */
    Mono<Void> report();

    /**
     * 生成一个 token
     * @return
     */
    String generateToken();

    /**
     * 校验 token
     * @return
     */
    boolean checkToken(String token);
}
