package com.atguigu.gulimall.commons.bean;

import lombok.Getter;

//只提供getter防止修改静态常量 引发的不安全问题 以及信息错乱
@Getter
public enum ResponseCode {
    SUCCESS(true, 0,"SUCCESS"),

    ERROR(false, 1, "ERROR"),
    UNKNOWN_REASON(false, 20001, "未知错误"),

    BAD_SQL_GRAMMAR(false, 21001, "sql语法错误"),
    JSON_PARSE_ERROR(false, 21002, "json解析异常"),

    PARAM_ERROR(false, 21003, "参数不正确"),
    FILE_UPLOAD_ERROR(false, 21004, "文件上传错误"),
    EXCEL_DATA_IMPORT_ERROR(false, 21005, "Excel数据导入错误"),
    ILLEAGAL_ARGUMENT(false,21006,"ILLEAGAL_ARGUMENT"),


    VIDEO_UPLOAD_ALIYUN_ERROR(false, 22001, "视频上传至阿里云失败"),
    VIDEO_UPLOAD_TOMCAT_ERROR(false, 22002, "视频上传至业务服务器失败"),
    VIDEO_DELETE_ALIYUN_ERROR(false, 22003, "阿里云视频文件删除失败"),
    FETCH_VIDEO_UPLOAD_PLAYAUTH_ERROR(false, 22004, "获取上传地址和凭证失败"),
    REFRESH_VIDEO_UPLOAD_PLAYAUTH_ERROR(false, 22005, "刷新上传地址和凭证失败"),
    REFRESH_VIDEO_PLAYAUTH_ERROR(false, 22006, "获取播放凭证失败"),

    URL_ENCODE_ERROR(false, 23001, "URL编码失败"),
    ILLEGAL_CALLBACK_REQUEST_ERROR(false, 23002, "非法回调请求"),
    FETCH_ACCESSTOKEN_FAILD(false, 23003, "获取accessToken失败"),
    FETCH_USERINFO_ERROR(false, 23004, "获取用户信息失败"),

    FETCH_UPLOAD_FILE_SIGN_ERROR(false, 24001, "获取文件上传签名失败");

    private Boolean success;
    private int code;
    private String desc;

    ResponseCode(Boolean success, int code, String desc){
        this.success = success;
        this.code = code;
        this.desc = desc;
    }

}
