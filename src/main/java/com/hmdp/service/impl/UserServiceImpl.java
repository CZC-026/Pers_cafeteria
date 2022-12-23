package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.验证拿到的手机号码的合法性
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不符合，返回错误信息
            return Result.fail("您输入的手机号码格式不正确");
        }
        //3.符合生成验证码
        String code = RandomUtil.randomString(6);
        //4.保存到session
        session.setAttribute("code", code);
        //5.发送验证码
        log.debug("短信已发送给用户,验证码为{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.验证手机号的有效性
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式无效");
        }
        //2.验证验证码的有效性
        Object code = session.getAttribute("code");
        if (code == null || !code.equals(loginForm.getCode())) {
            //3.验证码校验，无效
            return Result.fail("验证码错误");
        }
        //4.有效，查询手机号用户 select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();
        //5.用户为空就创建
        if (user == null) {
            user = createWithPhone(phone);
        }
        //6.用户存在session中
        session.setAttribute("user", user);
        return Result.ok();
    }

    private User createWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(8));
        save(user);
        return user;
    }
}
