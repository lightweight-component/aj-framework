package com.ajaxjs.business.net.netcard;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.NetworkInterface;
import java.net.SocketException;

import static com.ajaxjs.business.net.netcard.GetCardInfo.*;

@Slf4j
public class TestGetNetCardInfo {
    @Test
    public void testGetPhysicalNICs() {
        log.info("ipv4AddressesOfPhysicalNICs :{}", ipv4AddressesOfPhysicalNICs());
        log.info("addressesOfPhysicalNICs(FILTER_IPV4) nic:{}", addressesOfPhysicalNICs(FILTER_IPV4));
        log.info("ipv4AddressesOfNoVirtualNICs :{}", ipv4AddressesOfNoVirtualNICs());
        log.info("addressesOfNoVirtualNICs(FILTER_IPV4):{}", addressesOfNoVirtualNICs(FILTER_IPV4));
    }

    @Test
    void test() throws SocketException {
        for (NetworkInterface nic : GetCardInfo.getPhysicalNICs()) {
            System.out.println(nic.getName());
            System.out.println(nic.getDisplayName());
            System.out.println(GetCardInfo.formatMac(nic.getHardwareAddress(), "-"));
            System.out.println(nic.getInetAddresses());
            System.out.println(nic.getSubInterfaces());
            System.out.println("------------------------------------------------");
        }
    }
}
