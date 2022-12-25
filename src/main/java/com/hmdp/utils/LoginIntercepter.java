package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginIntercepter implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    public LoginIntercepter(StringRedisTemplate redisTemplate) {
        this.stringRedisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //1.从redis中获取token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            System.out.println("你被拦截了");
            response.setStatus(401);
            return false;
        }
        String key = RedisConstants.LOGIN_USER_KEY + token;
        //2.根据token从redis中获取用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //3.用户是否为空，不存在拦截

        if (userMap.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        //4.把hash结构转化为bean格式
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //5.存在放在threadlocal中，放行
        UserHolder.saveUser(userDTO);
        //6.设置时间
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        System.out.println("后端没问题");
        return true;
    }
}
