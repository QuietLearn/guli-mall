package com.atguigu.gulimall.pms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource originDataSource(@Value("${spring.datasource.url}") String jdbcUrl){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(jdbcUrl);
        return hikariDataSource;
    }

    @Bean
    @Primary //这个返回的对象是默认的数据源
    public DataSource dataSource(DataSource dataSource){
//        进行数据源代理，不仅可以连接数据库进行操作，而且对rollback方法进行了增强
//        以便于完成分布式事务，保证数据的一致性

        return new DataSourceProxy(dataSource);
    }

}
