package com.atguigu.gulimall.ums.service.impl;

import com.atguigu.gulimall.commons.bean.*;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.atguigu.gulimall.commons.utils.JsonUtil;
import com.atguigu.gulimall.commons.utils.RandomDataUtil;
import com.atguigu.gulimall.ums.common.state.MessageTemplateEnum;
import com.atguigu.gulimall.ums.component.AliSmsTemplate;
import com.atguigu.gulimall.ums.dao.TMemberPhoneCodeMapper;
import com.atguigu.gulimall.ums.entity.MemberPhoneCode;
import com.atguigu.gulimall.ums.vo.MemberLoginRespVo;
import com.atguigu.gulimall.ums.vo.MemberLoginVo;
import com.atguigu.gulimall.ums.vo.MemberRegistVo;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.ums.dao.MemberDao;
import com.atguigu.gulimall.ums.entity.MemberEntity;
import com.atguigu.gulimall.ums.service.MemberService;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private AliSmsTemplate aliSmsTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TMemberPhoneCodeMapper memberPhoneCodeMapper;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public ServerResponse<MemberEntity>  checkMemberExist(String username) {


        MemberEntity member = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", username));

        if (member==null){
            return ServerResponse.createByErrorMessage("此用户不存在");
        }

        return ServerResponse.createBySuccess("检验成功，该用户存在",member);
    }



    @Override
    public ServerResponse register(MemberRegistVo memberRegisterVo) {
        String username = memberRegisterVo.getUsername();
        String mobile = memberRegisterVo.getMobile();
        String email = memberRegisterVo.getEmail();
        String password = memberRegisterVo.getPassword();
        String code = memberRegisterVo.getCode();

        if (memberRegisterVo == null) {
            ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
//        ||StringUtils.isBlank(code)
        if (StringUtils.isBlank(mobile)||StringUtils.isBlank(email)||StringUtils.isBlank(password)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }

        if (!checkUsername(username)){
            log.info(username+"已经存在");
            return ServerResponse.createByErrorMessage(username+"已经注册");
        }

        if (!checkMobile(mobile)){
            log.info(mobile+"已经存在");
            return ServerResponse.createByErrorMessage(mobile+"已经注册");
        }

        if (!checkEmail(email)){
            log.info(email+"已经存在");
            return ServerResponse.createByErrorMessage(email+"已经注册");
        }

//        手机验证码
        String phoneCode = stringRedisTemplate.opsForValue().get(Constant.memberInfo.REDIS_REGISTER_CODE_PREFIX + mobile);
        if (StringUtils.isBlank(phoneCode)){
            return ServerResponse.createByErrorMessage("验证码已失效,请重新获取");
        }

        if (!StringUtils.equals(code,phoneCode)){
            return ServerResponse.createByErrorMessage("验证码不正确，请重新输入");
        }

        MemberEntity registerMember = assemRegisterMember(memberRegisterVo);
        int insertCount = this.baseMapper.insert(registerMember);
        if (insertCount<=0){
            return ServerResponse.createByErrorMessage("注册失败");
        }


        return ServerResponse.createBySuccessMessage("注册 成功");
    }


    @Override
    public ServerResponse loginByCode(String loginAccout, String verificationCode) {

        ServerResponse<MemberEntity> memberServerResponse = checkMemberExist(loginAccout);
        if (!memberServerResponse.isSuccess())
            return memberServerResponse;

        String code = stringRedisTemplate.opsForValue().get(Constant.memberInfo.REDIS_LOGIN_MEMBER_PREFIX + loginAccout);

        if (StringUtils.isBlank(verificationCode)){
            return ServerResponse.createByErrorMessage("验证码已失效,请重新获取");
        }

        if (!StringUtils.equals(code,verificationCode)){
            return ServerResponse.createByErrorMessage("验证码不正确，请重新输入");
        }

        MemberEntity member = memberServerResponse.getData();

        String loginToken = UUID.randomUUID().toString().replace("-", "");

        MemberLoginRespVo memberLoginVo = assemMemberLoginVo(member,loginToken);

        //登录成功把用户信息存储到redis中
        //ValueOperations
        stringRedisTemplate.opsForValue().set(Constant.memberInfo.REDIS_LOGIN_MEMBER_PREFIX+loginToken,
                JsonUtil.obj2Json(member),Constant.memberInfo.MEMBER_REDIS_SESSION_EXTIME, TimeUnit.MINUTES);


        return ServerResponse.createBySuccess("登录成功",memberLoginVo);
    }

    private MemberLoginRespVo assemMemberLoginVo(MemberEntity memberDto, String loginMemberToken){
        MemberLoginRespVo memberLoginVo = new MemberLoginRespVo();
        BeanUtils.copyProperties(memberDto,memberLoginVo);

        memberLoginVo.setAccessToken(loginMemberToken);
        return memberLoginVo;
    }

    @Override
    public ServerResponse sendSms(String mobile, String type) {


        String redisCodePrefix = MessageTemplateEnum.getRedisCodePrefix(type);


        Long expire = stringRedisTemplate.getExpire(redisCodePrefix+ mobile, TimeUnit.MINUTES);
        if (expire>=Constant.memberInfo.CODE_SESSION_EXTIME-1&&expire<=Constant.memberInfo.CODE_SESSION_EXTIME){
            log.info("{}手机发送短信1分钟不能超过1次",mobile);
            return ServerResponse.createByErrorMessage(mobile+"手机1分钟内发送短信不能超过1次");
        }

        String phoneCode = RandomDataUtil.generateSixRandomNum();

        boolean isSendSuccess = aliSmsTemplate.sendCodeSms(mobile, type,phoneCode);
        if (!isSendSuccess)
            return ServerResponse.createByErrorMessage("给手机"+mobile+"发送短信"+phoneCode+"失败");


        stringRedisTemplate.opsForValue().set(redisCodePrefix+mobile,phoneCode,Constant.memberInfo.CODE_SESSION_EXTIME,TimeUnit.MINUTES);


        MemberPhoneCode memberPhoneCode = assemMemberPhoneCode(phoneCode,mobile,type);

        int insertCount = memberPhoneCodeMapper.insert(memberPhoneCode);

        if (insertCount<=0){
            log.error("插入{}手机验证码{}相关信息失败",mobile,memberPhoneCode);
        }

        return ServerResponse.createBySuccess("发送短信成功",phoneCode);
    }

    @Override
    public ServerResponse login(MemberLoginVo memberLoginVo) {
        String loginacct = memberLoginVo.getLoginacct();
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<MemberEntity>().or().eq("username", loginacct)
                .or().eq("mobile", loginacct)
                .or().eq("email", loginacct);
        MemberEntity member = this.baseMapper.selectOne(queryWrapper);

        if(member == null){
            //登录失败
            return ServerResponse.createByErrorMessage("账号不存在");
//            throw new UsernameAndPasswordInvaildException();
        }

        String password = memberLoginVo.getPassword();
        if (StringUtils.isBlank(password))
            return ServerResponse.createByErrorMessage("密码错误");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        //前 明文密码，  后加密密码
        // 会根据数据库的加密密码 判断出加盐的盐值，让进来的明文加盐加密 比对
        boolean matches = encoder.matches(password, member.getPassword());
        if (!matches)
            return ServerResponse.createByErrorMessage("密码错误");

        //登录成功
        //不用缓存，个人的信息一般只有 自己请求会查， 并发量不大，不用分布式锁 锁住
        String token = UUID.randomUUID().toString().replace("-", "");

        //1、将用户的详细信息保存在redis中；
        stringRedisTemplate.opsForValue().set(Constant.memberInfo.REDIS_LOGIN_MEMBER_PREFIX+token,JsonUtil.obj2Json(member),
                Constant.memberInfo.MEMBER_REDIS_WEEK_EX,TimeUnit.DAYS);

        HashMap<String, Object> map = Maps.newHashMap();
        map.put("memberId",member.getId());
        map.put("nickName",member.getNickname());
        map.put("header",member.getHeader());
        map.put("token",token);

        //将我们在redis中的token做成jwt返回过去
        String jwt = GuliJwtUtils.buildJwt(map, null);
        /*
          MemberRespVo respVo = new MemberRespVo();
        BeanUtils.copyProperties(member,respVo);
        respVo.setToken(jwt);

        else {
            throw new UsernameAndPasswordInvaildException();
        }*/

        return ServerResponse.createBySuccess("登录成功",jwt);
    }

    private MemberPhoneCode assemMemberPhoneCode(String phoneCode,String phone,String type){
        MemberPhoneCode memberPhoneCode= new MemberPhoneCode();
        memberPhoneCode.setPhone(phone);
        memberPhoneCode.setType(type);
        memberPhoneCode.setCode(phoneCode);
        return memberPhoneCode;
    }

    private MemberEntity assemRegisterMember(MemberRegistVo memberRegisterVo){
        MemberEntity registerMember = new MemberEntity();

        BeanUtils.copyProperties(memberRegisterVo,registerMember);


        //该加密算法盐值随机生成
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodePwd = encoder.encode(memberRegisterVo.getPassword());
        registerMember.setPassword(encodePwd);

        registerMember.setStatus(1);

        return registerMember;
    }


    private boolean checkUsername(String username){
        Integer mobileCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        /*if (mobileCount>0)
            throw new UsernameExistException();*/
        return mobileCount==0?true:false;
    }

    private boolean checkMobile(String mobile){
        Integer mobileCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", mobile));
        /*if (mobileCount>0)
            throw new UsernameExistException();*/
        return mobileCount==0?true:false;
    }

    private boolean checkEmail(String email){
        Integer emailCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("email", email));
      /*  if (emailCount>0)
            throw new EmailExistException();*/
        return emailCount==0?true:false;
    }
}