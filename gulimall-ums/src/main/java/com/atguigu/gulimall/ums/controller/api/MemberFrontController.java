package com.atguigu.gulimall.ums.controller.api;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.bean.ServerResponse;
import com.atguigu.gulimall.ums.service.MemberService;
import com.atguigu.gulimall.ums.vo.MemberLoginVo;
import com.atguigu.gulimall.ums.vo.MemberRegistVo;
import com.atguigu.gulimall.ums.vo.MemberRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员
 *
 * @author heyijie
 * @email hyj78586421@outlook.com
 * @date 2019-08-01 19:21:59
 */

@Slf4j
@Api(tags = "用户功能")
@RestController
@RequestMapping("/ums/member")
public class MemberFrontController {

    @Autowired
    private MemberService memberService;


    /**
     * @param memberRegisterVo
     * @return
     *
     * SpringCloud：Http+Json;
     *      @RequestBody:将请求体中的json数据转为指定的这对象
     *
     * 以后的post请去都代表接受json数据
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public ServerResponse register(MemberRegistVo memberRegisterVo)  {
//        throws MySQLIntegrityConstraintViolationException 处理的不好，api无法得到报错字段

        log.debug("{} 用户正在注册：", memberRegisterVo.getMobile());
       /* try {
            memberService.registerUser(vo);
        } catch (Exception pe){
        //抛出异常本该 由全局异常 监控管理  但多此一举，因为全局异常也是将异常处理为 serverresponse json返回
            fail.setMsg(pe.getMessage());
            return fail;
        }*/

        return memberService.register(memberRegisterVo);
    }


    @PostMapping("/login")
    public ServerResponse login(MemberLoginVo memberLoginVo){


        return memberService.login(memberLoginVo);
    }

    @ApiOperation("获取短信验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile",value = "手机号",required = true),
            @ApiImplicitParam(name = "type",value = "(采用不同模板)验证码类型 1000-注册,1001-登录",required = true)
    })

    @PostMapping("/sendsms")
    public ServerResponse<String> sendSms(@RequestParam("mobile") String mobile,String type) {
//        smsTemplate.sendCodeSms(mobile,code);
        //cookie/session；浏览器
        /**
         * 浏览器：
         *      同一个页面共享：pageContext
         *      同一次请求：request
         *      同一次会话：session：Map
         *      同一个应用：application；
         *
         * 多端了；
         *      同一个页面共享：各端使用自己的方式
         *      同一次请求共享数据：将数据以json写出去；
         *      同一次会话：把数据一个公共的地方【redis】，
         *                ：把数据一个公共的地方【redis】，
         *
         *                success("").msg("短信发送完成")
         */

        ServerResponse serverResponse = memberService.sendSms(mobile,type);
        return serverResponse;
    }
}
