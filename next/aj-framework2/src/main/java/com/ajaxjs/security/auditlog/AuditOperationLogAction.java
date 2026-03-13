package com.ajaxjs.security.auditlog;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Data
@Slf4j
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.HttpReferer.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.http-referer")
public class AuditOperationLogAction extends InterceptorAction<AuditOperationLog> {
    /**
     * 需要被 SpEl 解析的模板前缀和后缀 {{ expression  }}
     */
    public static final TemplateParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext("{{", "}}");

    /**
     * SpEL解析器
     */
    public static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();


    @Override
    public boolean action(AuditOperationLog annotation, HttpServletRequest req) {
        String expression = annotation.expression();

        if (ObjectHelper.hasText(expression)) {
            try {
                EvaluationContext evaluationContext = new StandardEvaluationContext();

                // SpEL解析的上下文，把 HandlerMethod 的形参都添加到上下文中，并且使用参数名称作为KEY
//                for (int i = 0; i < args.length; i ++)
//                    evaluationContext.setVariable(parameterNames[i], args[i]);

                String logContent = EXPRESSION_PARSER.parseExpression(expression, TEMPLATE_PARSER_CONTEXT).getValue(evaluationContext, String.class);

                // TODO 异步存储日志

                log.info("operationLog={}", logContent);
            } catch (Exception e) {
                log.error("操作日志SpEL表达式解析异常: {}", e.getMessage());
            }
        }

        return true;
    }
}
