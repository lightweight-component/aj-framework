package com.ajaxjs.framework.dataservice.fastcrud.dbconfig;

import com.ajaxjs.framework.dataservice.fastcrud.Namespaces;
import com.ajaxjs.spring.annotation.BizAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/common_api/admin")
public class FastCrudConfigController {

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
