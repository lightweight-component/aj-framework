package org.example;

import com.ajaxjs.service.tools.IIdCard;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
public class IdCard implements IIdCard {
    @Override
    public boolean checkIdCard(String idCardNo) {
        log.info("check222");
        return false;
    }
}