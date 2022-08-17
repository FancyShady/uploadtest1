package server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import server.service.PublishService;

/**
 * @author wangjunrui
 * @date 2022/8/4
 **/
@RestController
@RequestMapping("publish")
public class ReceiveController {
    private final PublishService publishService;

    public ReceiveController(PublishService publishService) {
        this.publishService = publishService;
    }

    /**
     * 上传脚本文件
     * @param filePart 请求文件
     * @param token 请求秘钥
     * @return
     */
    @PostMapping("script_upload")
    public Mono<Void> scriptUpload(@RequestHeader(value = "token") String token,
                                   @RequestPart("file") FilePart filePart) {
        // todo 检验 token 是否有效
        if (!publishService.checkToken(token)) {
            throw new IllegalStateException("");
        }
        // todo 文件处理
        // publishService.fileHandle(filePart);
        // todo 如果处理过程有问题，需要排除异常

        return publishService.fileHandle(filePart);
    }

}
