package yygh.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("yygh.mapper")
public class UserConfig {
}
