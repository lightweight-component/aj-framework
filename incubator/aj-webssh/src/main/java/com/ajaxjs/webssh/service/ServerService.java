package com.ajaxjs.webssh.service;

import com.ajaxjs.webssh.entity.ServerConfig;
import com.ajaxjs.webssh.entity.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServerService {
    @Autowired
    private ServerRepository serverRepository;
    
    public Long saveServer(ServerConfig server) {
        // 密码加密存储（生产环境建议）
        // server.setPassword(encryptPassword(server.getPassword()));
        return serverRepository.saveServer(server);
    }
    
    public List<ServerConfig> getAllServers() {
        List<ServerConfig> servers = serverRepository.findAllServers();
        servers.forEach(server -> server.setPassword(null));  // 不返回密码信息到前端

        return servers;
    }
    
    public Optional<ServerConfig> getServerById(Long id) {
        return serverRepository.findServerById(id);
    }
    
    public void deleteServer(Long id) {
        serverRepository.deleteServer(id);
    }
}