package server.service;


import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import server.utils.JsonUtil;

import java.time.Duration;

/**
 * @author wangjunrui
 * @date 2022/8/4
 **/
@Service
public class RedisManger {
    private final ReactiveStringRedisTemplate template;

    public RedisManger(ReactiveStringRedisTemplate template) {
        this.template = template;
    }

    /**
     *
     * @param key 服务id / 服务名
     * @param data 注册信息
     * @param duration 超时时间
     * @return
     */
    public Mono<Void> setKey(String key, Object data, Duration duration) {
        // todo 将 info 这个对象（根据业务去改成自己的对象） 写到 redis
        String res = JsonUtil.writeValueAsString(data);
        return template.opsForValue().set(key, res, duration.getSeconds()).then();
    }
}
