package com.atguigu.gulimall.pms.component;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.commons.utils.JsonUtil;
import com.atguigu.gulimall.pms.annotation.GuliCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 切面的步骤
 * 0)、导入aop的starter
 * 1）、这是一个切面，同时放在容器中
 * 2）、申明通知方法和切入点表达
 */
@Component
@Aspect
@Slf4j
public class GuliCacheAspect {
    //    不是乱加的，要缓存就切

    // 1. 利用AOP完成对标注了特定自定义注解的方法完成切面  方法上实现注解切面
    // 1.1 自定义切面
    // 未来自己写框架，也要抽取出很多的功能，以注解的方式


    //todo 建议使用jedis完成
    //服务器为什么会炸
    //每个请求都会进来开线程处理，开着开着没东西了，就炸了
    //资源耗尽，服务器就炸了
    //我觉得是读取数据库每次读大数据放到堆内存，当然不够用
    //
    //压炸了，服务不响应了  ，jvm堆内存已经不能再存放大数据new对象了，当然返回不了数据，本来是从redis读取json转为java内存中的对象再序列化返回的，现在堆内存爆了，转不了数据了，返回数据只能是null
    //
    //网络操作redis要建立里连接，也需要资源
    //spring redistemplate在高并发就炸，转不了数据，jedis可以
    @Autowired
    private StringRedisTemplate redisTemplate;

    ReentrantLock lock = new ReentrantLock();
    /**
     *  环绕通知  环绕通知在方法之前运行
     *  只切标了注解的方法
     *
     *  在切面中解决
     *      缓存穿透：null值缓存，设置短暂的过期时间
     *          * 2、缓存雪崩：过期时间+随机值
     *          * 3、缓存击穿：分布式锁
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.atguigu.gulimall.pms.annotation.GuliCache)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //方法执行之前，方法执行以后，方法异常，方法正常返回结果都能获取到
        //    point.getTarget()获取代理目标对象
        Object result = null;

        String prefix = "";
        try {
            log.info("切面介入工作....前置通知");
            Object[] args = point.getArgs();//获取目标方法的所有参数的值
            //拿到注解的值
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            GuliCache guliCacheAnno = method.getAnnotation(GuliCache.class);
//            System.out.println(guliCache.prefix());
//            String name = point.getSignature().getName();
//            for (Method method : point.getThis().getClass().getMethods()) {
//                if(method.getName().equals(name)){
//                    GuliCache guliCache = AnnotatedElementUtils.findMergedAnnotation(method, GuliCache.class);
//                    System.out.println("注解的值..."+guliCache.prefix());
//                }
//            }

            //累赘代码，进来肯定有注解啊
            if (guliCacheAnno == null) {
                return point.proceed(args);
            }
            //根据你的需求获取标注在注解的值 做处理
            prefix = guliCacheAnno.prefix();
            if (args != null&&args.length>0) {
                //根据你的需求获取被切方法的args参数做处理
                for (Object arg : args) {
                    //如果是基本参数，直接arg值在这拼接
                    //如果是对象，那么可以使用arg.hashcode()拼接
                    //基本参数整形的hashcode还是本身，所以都可以使用hashcode
                    //todo  按照道理只需要第一个即可
                    prefix += ":" + arg.toString();
                }
            }


            /*
            String categoryWithChildrensVoListStr = redisTemplate.opsForValue().get(prefix);
            if (StringUtils.isBlank(categoryWithChildrensVoListStr)){
                log.info("缓存没命中....");
                // result = point.proceed(args);
                result = point.proceed();
                redisTemplate.opsForValue().set(prefix, JsonUtil.obj2Json(result));
            } else{
                Class returnType = signature.getReturnType();
                result = JsonUtil.Json2Obj(categoryWithChildrensVoListStr, returnType);
                log.info("缓存命中....");
                return result;
            }*/
            //目标方法真正执行...
            result = getFromCache(prefix, signature);
            if (result!=null){
                return result;
            } else {
                lock.lock();
                log.info("切面介入工作....返回通知");
                //双检查
                result = getFromCache(prefix,signature);
                if (result!=null){
                    return result;
                }else {
                    log.info("缓存命中....");
                    result = point.proceed();
                    redisTemplate.opsForValue().set(prefix, JsonUtil.obj2Json(result));
                    return result;
                }

            }


        }catch (Exception e) {
            clearCurrentCache(prefix);
            log.info("切面介入工作....异常通知");
        }finally {
            log.info("切面介入工作....后置通知");
            //如果缓存中有了直接从缓存获取数据返回，没有加锁
            //线程把锁hold住就是 锁住了
            if(lock.isLocked()){
                lock.unlock();
            }
            //做一些善后工作
        }
        return result;
    }

    private Object getFromCache(String prefix, Signature signature) {
        String categoryWithChildrensVoListStr = redisTemplate.opsForValue().get(prefix);
        if (!StringUtils.isBlank(categoryWithChildrensVoListStr)) {
            //如果拿到的数据不是null。缓存有数据。目标方法不用执行
            Class returnType = ((MethodSignature) signature).getReturnType();//获取返回值类型
            //但是转不出来就是null，无所谓的
            return JsonUtil.Json2Obj(categoryWithChildrensVoListStr, returnType); // 可能会转化异常，redis存的数据和想要拿到的数据类型不一致，进入异常通知

        }
        //如果redis数据为null
        return null;
    }

    private void clearCurrentCache(String prefix){
        redisTemplate.delete(prefix);
    }
}
