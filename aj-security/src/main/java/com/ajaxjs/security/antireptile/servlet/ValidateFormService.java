package com.ajaxjs.security.antireptile.servlet;


import com.ajaxjs.security.antireptile.model.VerifyImageDTO;
import com.ajaxjs.security.antireptile.model.VerifyImageVO;
import com.ajaxjs.security.antireptile.rule.RuleActuator;
import com.ajaxjs.security.antireptile.util.VerifyImageUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * 验证码表单业务服务，负责验证答案及刷新验证码。
 * <p>校验成功时通知全部规则重置与原始请求相关的状态；刷新时删除旧答案并缓存新答案。
 * 返回值为供 Servlet 直接写入响应的 JSON 字符串。</p>
 */
public class ValidateFormService {
    /**
     * 验证码通过后重置规则状态的执行器。
     */
    @Autowired
    private RuleActuator actuator;

    /**
     * 用于生成验证码及操作本地缓存答案缓存的工具。
     */
    @Autowired
    private VerifyImageUtil verifyImageUtil;

    /**
     * 通过 Servlet 标准参数接口读取表单，并比对本地缓存中的验证码答案。
     * <p>读取 {@code verifyId}、{@code result} 与 {@code realRequestUri} 字段，
     * 将提交答案转为小写后比较；通过后重置全部规则。读取答案不会删除验证码。
     * 支持 multipart 和普通表单；multipart 解析依赖验证 Servlet 注册时的 multipart 配置。
     * 缺少必要字段时直接返回验证失败，容器解析异常仍向调用方传播。</p>
     *
     * @param request 含验证码标识、答案和原始请求 URI 的表单请求
     * @return 校验通过返回 {@code {"result":true}}，答案不存在或不匹配返回 {@code {"result":false}}
     * @throws UnsupportedEncodingException 无法将请求字符编码设置为 UTF-8 时抛出
     */
    public String validate(HttpServletRequest request) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        String verifyId = request.getParameter("verifyId");
        String result = request.getParameter("result");
        String realRequestUri = request.getParameter("realRequestUri");

        if (verifyId == null || result == null || realRequestUri == null)
            return "{\"result\":false}";

        String actualResult = verifyImageUtil.getVerifyCode(verifyId);

        if (actualResult != null && actualResult.equals(result.toLowerCase(Locale.ROOT))) {
            actuator.reset(request, realRequestUri);

            return "{\"result\":true}";
        }

        return "{\"result\":false}";
    }

    /**
     * 使旧验证码失效，生成并保存有效期为 60 秒的新验证码。
     *
     * @param request 通过 {@code verifyId} 参数携带旧验证码标识的请求
     * @return 包含 {@code verifyId} 和 {@code verifyImgStr} 的 JSON 字符串，不含正确答案
     */
    public String refresh(HttpServletRequest request) {
        String verifyId = request.getParameter("verifyId");
        verifyImageUtil.deleteVerifyCode(verifyId);
        VerifyImageDTO verifyImage = verifyImageUtil.generateVerifyImg();
        verifyImageUtil.saveVerifyCode(verifyImage);
        VerifyImageVO verifyImageVO = new VerifyImageVO();
        BeanUtils.copyProperties(verifyImage, verifyImageVO);

        return "{\"verifyId\": \"" + verifyImageVO.getVerifyId() + "\",\"verifyImgStr\": \"" + verifyImageVO.getVerifyImgStr() + "\"}";
    }
}
