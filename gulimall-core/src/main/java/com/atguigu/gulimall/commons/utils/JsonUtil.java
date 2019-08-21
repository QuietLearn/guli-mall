package com.atguigu.gulimall.commons.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;

@Slf4j
public class JsonUtil {
    public static String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        //取消默认转换timestamps形式，不然默认是从1970到现在的时间的ms
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        //忽略空Bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));

        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //这样空字符串变为空。如果您希望它转换为实际的空List
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
    }

    public static <T> String obj2Json(T obj){
        if (obj == null)
            return null;
        try {
            return obj instanceof String? (String )obj:objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to Json error",e);
            return null;
        }
    }
    public static <T> String obj2PrettyJson(T obj){
        if (obj == null)
            return null;
        try {
            return obj instanceof String? (String )obj:objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to Json error",e);
            return null;
        }
    }


    //<T> 代表声明方法持有一个类型T,或者可以理解成将此方法声明为泛型方法
    //T 返回值的类型
    public static <T> T Json2Obj(String json,Class<T> clazz){
        StringUtils.contains("ss","s");
        if (StringUtils.isBlank(json)||clazz==null)
            return null;
        try {
            return clazz.equals(String.class)?(T)json:objectMapper.readValue(json,clazz);
        } catch (Exception e) {
            log.warn("Parse Json to Object error",e);
            return null;
        }
    }

    public static <T> T Json2Obj(String json, TypeReference<T> typeReference){
        if (StringUtils.isBlank(json)||typeReference==null)
            return null;
        try {
            //getType拿到这个方法
            return typeReference.getType().equals(String.class)?(T)json:objectMapper.readValue(json,typeReference);
        } catch (Exception e) {
            log.warn("Parse Json to Object error",e);
            return null;
        }
    }


    public static <T> T Json2Obj(String json, Class<?> collectionClass, Class<?>... elementClasses){
        if (StringUtils.isBlank(json)||collectionClass== null||elementClasses==null)
            return null;
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

        try {
            //getType拿到这个方法
            return objectMapper.readValue(json,javaType);
        } catch (Exception e) {
            log.warn("Parse Json to Object error",e);
            return null;
        }
    }

    public static void main(String[] args) {
        /*User u1 = new User();
        u1.setId(1);
        u1.setEmail("784510436@qq.com");

        String json = JsonUtil.obj2Json(u1);
        System.out.println(json);
        User member = JsonUtil.Json2Obj(json, User.class);
        System.out.println(member);

        User u2 = new User();
        u2.setId(2);
        u2.setEmail("13065708090@163.com");

        List list = Lists.newArrayList();
        list.add(u1);
        list.add(u2);

        String prettyJson = JsonUtil.obj2PrettyJson(list);
        System.out.println(prettyJson);

        List<User> list1 = JsonUtil.Json2Obj(prettyJson, List.class);


        List<User> list2 = JsonUtil.Json2Obj(prettyJson, new TypeReference<List<User>>() {});

        List<User> list3 = JsonUtil.Json2Obj(prettyJson, List.class,User.class);
        System.out.println(list1);
*/

      /*  TestPojo testPojo = new TestPojo();

        String json1 = JsonUtil.obj2Json(testPojo);
        System.out.println(json1);*/
    }



}
