package com.ajaxjs.dataservice.fastcrud.dbconfig;

import com.ajaxjs.dataservice.fastcrud.Namespaces;
import com.ajaxjs.spring.annotation.BizAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供 FastCRUD 运行时配置管理接口的控制器。
 */
@RestController
@RequestMapping("/common_api/admin")
public class FastCrudConfigController {

    /**
     * 当前的 FastCRUD 命名空间注册表。
     */
    @Autowired(required = false)
    Namespaces namespaces;

    /**
     * 实时刷新配置
     *
     * @return 是否成功
     */
    @GetMapping("/reload")
    @BizAction("刷新配置")
    public boolean reload() {
        namespaces.reload();
        return true;
    }
}
