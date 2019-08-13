package com.atguigu.gulimall.commons.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/*import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;*/

/**
 * 复用度非常高的服务端响应对象
 * @param <T>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
@ApiModel(value = "全局统一返回结果类")
@Data
public class ServerResponse<T> implements Serializable {
    @ApiModelProperty(value = "响应是否成功")
    @JsonProperty("success")
    private Boolean success;

    @ApiModelProperty("请求是否成功的状态码。0：成功   1-其他：失败")
    private int code;
    @ApiModelProperty("成功或者失败的提示")
    private String msg;
    @ApiModelProperty("真正的数据")
    private T data;

    public ServerResponse() {
    }
    private ServerResponse(int code){
        this.code = code;
    }

    private ServerResponse(Boolean success, int code , String msg){
        this.success = success;
        this.code= code;
        this.msg = msg;
    }
    private ServerResponse(Boolean success, int code , T data){
        this.success = success;
        this.code= code;
        this.data = data;
    }

    private ServerResponse(Boolean success, int code , String msg, T data){
        this.success = success;
        this.code= code;
        this.msg = msg;
        this.data = data;
    }


    private ServerResponse(int code , String msg){
        this.code= code;
        this.msg = msg;
    }

    private ServerResponse(int code, String msg, T data){
        this.code= code;
        this.msg = msg;
        this.data = data;
    }

    private ServerResponse(int code, T data){
        this.code = code;
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return this.code == ResponseCode.SUCCESS.getCode();
    }


    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse(ResponseCode.SUCCESS.getCode());
    }


    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse(ResponseCode.SUCCESS.getSuccess(),ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getSuccess(),ResponseCode.SUCCESS.getCode(),data);
    }

    /**
     * 因为泛型方法里的T算是单独声明的，与泛型类的泛型不属于同一个，声明方法的泛型并不能确定类的泛型,
     * 确定方法--->确定类-->确定类里面T的变量或其他用到T的地方
     *
     * 这样指定泛型是因为，首先需要T data 来指定ServerResponse<T>中的泛型类型 ，所以都是T
     * 只声明一个泛型变量
     * new com.hyj.crowdfunding.common.ServerResponse<T>是因为jdk7.0以前两边都要写，7.0以后只要声明引用部分的泛型，那后面对象的泛型的具体类型也确定了
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ServerResponse<T> createBySuccess(String msg, T data){
        return new ServerResponse(ResponseCode.SUCCESS.getSuccess(),ResponseCode.SUCCESS.getCode(),msg,data);
    }

    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMsg){
        return new ServerResponse(ResponseCode.ERROR.getCode(),errorMsg);
    }
    public static <T> ServerResponse<T> createByErrorDataCodeMessage(ResponseCode responseCode,T data){
        return new ServerResponse(responseCode.getSuccess(),responseCode.getCode(),responseCode.getDesc(),data);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMsg){
        return new ServerResponse<T>(errorCode,errorMsg);
    }
    public static <T> ServerResponse<T> createByErrorCodeMessage(ResponseCode responseCode){
        return new ServerResponse<T>(responseCode.getSuccess(),responseCode.getCode(),responseCode.getDesc());
    }

    /**
     * 如何设计链式调用。总是返回这个对象，
     * @return
     */
    /*public ServerResponse<T> message(String message){
        this.setMessage(message);
        return this;
    }

    public ServerResponse code(Integer code){
        this.setCode(code);
        return this;
    }

    public ServerResponse success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public ServerResponse data(Map<String, Object> map){
        this.setData(map);
        return this;
    }

    public ServerResponse data(String key, Object value){
        this.data.put(key, value);
        return this;
    }*/

    public static void main(String[] args) {
        ServerResponse<Object> bySuccess = ServerResponse.createBySuccess("hh", "这是 hh");
        Object data = bySuccess.getData();
        System.out.println(data);
    }
}
