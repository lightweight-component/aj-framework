package com.ajaxjs.memviz.controller;

import com.ajaxjs.memviz.model.GraphModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memviz")
public class MemvizController {
    private final HprofParseService parseService;

    private static final String HOTSPOT_BEAN = "com.sun.management:type=HotSpotDiagnostic";
    private static final String DUMP_METHOD = "dumpHeap";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 生成 HPROF 快照文件
     *
     * @param live 是否仅包含存活对象（会触发一次 STW）
     * @param dir  目录（建议挂到独立磁盘/大空间）
     * @return hprof 文件路径
     */
    public File dump(boolean live, File dir) throws Exception {
        if (!dir.exists() && !dir.mkdirs())
            throw new IllegalStateException("Cannot create dump dir: " + dir);

        String name = "heap_" + LocalDateTime.now().format(FMT) + (live ? "_live" : "") + ".hprof";
        File out = new File(dir, name);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objName = new ObjectName(HOTSPOT_BEAN);
        // 防御：限制最大文件大小（环境变量控制）
        assertDiskHasSpace(dir.toPath(), 512L * 1024 * 1024); // 至少 512MB 空间

        server.invoke(objName, DUMP_METHOD, new Object[]{out.getAbsolutePath(), live},
                new String[]{"java.lang.String", "boolean"});
        return out;
    }

    /**
     * 触发一次快照，返回文件名（安全：默认 live=false）
     */
    @PostMapping("/snapshot")
    public Map<String, String> snapshot(@RequestParam(defaultValue = "false") boolean live, @RequestParam(defaultValue = "/tmp/memviz") String dir) throws Exception {
        File f = dump(live, new File(dir));
        Map<String, String> map = new HashMap<>();
        map.put("file", f.getAbsolutePath());

        return map;
    }

    /**
     * 解析指定快照 → 图模型（支持过滤&折叠）
     */
    @GetMapping(value = "/graph", produces = MediaType.APPLICATION_JSON_VALUE)
    public GraphModel graph(@RequestParam String file,
                            @RequestParam(required = false) String include, // 例如: com.myapp.,java.util.HashMap
                            @RequestParam(defaultValue = "true") boolean collapseCollections) throws Exception {
        Predicate<String> filter = null;

        if (StringUtils.hasText(include)) {
            String[] prefixes = include.split(",");
            filter = fqcn -> {
                // 总是包含重要的基础类，以便显示大对象
                if (fqcn.equals("java.lang.String") || fqcn.equals("byte[]") ||
                        fqcn.startsWith("java.lang.String[") || fqcn.startsWith("java.util.ArrayList"))
                    return true;

                // 检查用户指定的前缀
                for (String p : prefixes)
                    if (fqcn.startsWith(p.trim()))
                        return true;

                return false;
            };
        }

        return parseService.parseToGraph(new File(file), filter, collapseCollections);
    }

    public static void assertDiskHasSpace(Path dir, long minFreeBytes) throws IOException {
        FileStore store = Files.getFileStore(dir);
        if (store.getUsableSpace() < minFreeBytes) {
            throw new IllegalStateException("Low disk space for heap dump: need " + minFreeBytes + " bytes free");
        }
    }
}