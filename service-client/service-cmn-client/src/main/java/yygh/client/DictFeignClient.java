package yygh.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//Feign接口类实现调用
@FeignClient(value = "service-cmn",path = "/admin/cmn/dict")
@Service
public interface DictFeignClient {

    @GetMapping("/getName/{dictCode}/{value}")
    String getName(@PathVariable("dictCode") String dictCode,
                   @PathVariable("value") String value);

    @GetMapping("/getName/{value}")
    String getName(@PathVariable("value") String value);
}
