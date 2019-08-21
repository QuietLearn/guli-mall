package com.atguigu.gulimall.pms.common;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

//@ConfigurationProperties(prefix="defineTest")
@PropertySource(value = "classpath:redispool.properties")
@Component
public class RedisPool implements InitializingBean {
    private static JedisPool pool;//jedis连接池
    @Value("${redis.max.total}")
    private  Integer maxTotal; //最大连接数
    @Value("${redis.max.idle}")
    private  Integer maxIdle;//在jedispool中最大的idle状态(空闲的)的jedis实例的个数
    @Value("${redis.min.idle}")
    private  Integer minIdle ;//在jedispool中最小的idle状态(空闲的)的jedis实例的个数

    //当我们从jedis pool中包有一个jedis实例，拿一个jedis实例，即java与redis服务端的通信客户端，是否需要测试
    @Value("${redis.test.borrow}")
    private  Boolean testOnBorrow ;//在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true。则得到的jedis实例肯定是可以用的。
    @Value("${redis.test.return}")
    private  Boolean testOnReturn ;//在return一个jedis实例的时候，是否要进行验证操作，如果赋值true。则放回jedispool的jedis实例肯定是可以用的。

    @Value("${redis1.ip}")
    private  String redisIp ;
    @Value("${redis1.port}")
    private  Integer redisPort ;

//    static
//    static
//    static
//    static
//    static

    @Override
    public void afterPropertiesSet() throws Exception {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true);//连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。

        pool = new JedisPool(config,redisIp,redisPort,1000*2);
    }

    private static void initPool(){

    }

    /**
     * 为了这个类在加载到jvm的时候,就初始化连接池
     */
    static{
//        initPool();
    }

    public static Jedis getJedisResource(){
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedisResource();
        jedis.set("hyj","fighting");
        RedisPool.returnResource(jedis);

        pool.destroy();//临时调用，销毁连接池中的所有连接
        System.out.println("program is end");
    }


}
