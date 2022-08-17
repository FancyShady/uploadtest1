package server.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import server.config.ScriptPublishProperties;
import server.data.ScriptPublishDto;
import server.utils.CompressUtil;
import server.utils.OverrideUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wangjunrui
 * @date 2022/8/4
 **/
@Slf4j
@Service
public class DefaultPublishService implements PublishService {
    private final ScriptPublishProperties properties;
    private final RedisManger redisManger;

    private final AtomicReference<String> currentToken = new AtomicReference<>();
    private ReactiveStringRedisTemplate redisTemplate;

    public DefaultPublishService(ScriptPublishProperties properties, RedisManger redisManger) {
        this.properties = properties;
        this.redisManger = redisManger;
    }

    @PostConstruct
    void init() {
        // todo 初始化一个线程，定时去执行 report 方法
        Flux.interval(Duration.ofMillis(30000))
                .flatMap(x -> report())
                .subscribeOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(5)))
                .subscribe();

        /**
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        Flux<String> stringFlux = sink.asFlux();
        new Thread(() -> {
            for (int i = 0; i<10; i++) {
                sink.tryEmitNext(i + "");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            sink.tryEmitComplete();
        }).start();
        TimeUnit.SECONDS.sleep(2);
        stringFlux.subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(20);
         */
    }

    /**
     * 上传文件到目标目录并解压
     * @param filePart
     * @return
     */
    @Override
    public Mono<Void> fileHandle(FilePart filePart) {
        // 根据 properties 内的配置去处理文件
        String filePath = this.properties.getLocalPath();
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalStateException("文件路径为空");
        }
        String sysTmpDir = System.getProperty("java.io.tmpdir");
        Path tempFilePath = Path.of(sysTmpDir, filePart.filename());
        File destDir = new File(this.properties.getLocalPath());
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return filePart.transferTo(tempFilePath)
                .thenReturn(true)
                .map(ignored -> {
                    if (Path.of(this.properties.getLocalPath()).toFile().exists()) {
                        // 判断是否覆盖
                        if(this.properties.isOverride()) {
                            // 删除目标目录下文件
                            FileSystemUtils.deleteRecursively(Path.of(this.properties.getLocalPath()).toFile());
                            // 解压文件到目标目录
                            CompressUtil.decompress(tempFilePath.toFile(), this.properties.getLocalPath());
                        } else {
                            // 直接解压
                            CompressUtil.decompress(tempFilePath.toFile(), this.properties.getLocalPath());
                        }
                    } /**else {
                        // 直接覆盖
                        CompressUtil.decompress(tempFilePath.toFile(), this.properties.getLocalPath());
                    }*/
                    // OverrideUtil.createTempFile(tempFilePath.getFileName().toString(), CompressUtil.CompressType.ZIP.name(), null);
                    return 1;
                })
                .then();
    }

    @Override
    public Mono<Void> report() {
        // 创建一个 token 并将 token 和注册信息一起写到redis
        String token = generateToken();
        currentToken.set(token);
        Object data = new ScriptPublishDto(
                properties.getRegister().getServiceId(),
                properties.getRegister().getServiceName(),
                properties.getRegister().getHost(),
                properties.getRegister().getExpireSecond(),
                token);
        // todo redisManger.setKey()
        return redisManger.setKey(properties.getRegister().getServiceId(), data, Duration.ofSeconds(30));

    }

    @Override
    public String generateToken() {
        // 随机生成一个 token ，后续用于接口检验
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.replace("-", "");
    }

    @Override
    public boolean checkToken(String token) {
        log.info("检查token是否有效：{}", token);
        return currentToken.get().equals(token);
    }
}
