package com.ajaxjs.framework.FastCrudController;

import com.ajaxjs.framework.model.PageVO;
import com.ajaxjs.framework.mvc.unifiedreturn.BizAction;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FastCRUD 控制器
 */
@RestController
@RequestMapping("/common-api")
public interface FastCrudController {
    /**
     * 获取单笔详情
     *
     * @param namespace 实体的命名空间
     * @param id        实体 id
     * @return 实体 Map
     */
    @GetMapping("/{namespace}/{id}")
    @BizAction("获取单笔详情")
    Map<String, Object> info(@PathVariable String namespace, @PathVariable Long id);

    /**
     * 获取实体列表
     *
     * @param namespace 实体的命名空间
     * @return 实体列表
     */
    @GetMapping("/{namespace}/list")
    @BizAction("获取实体列表")
    List<Map<String, Object>> list(@PathVariable String namespace);

    /**
     * 分页获取实体列表
     *
     * @param namespace 实体的命名空间
     * @return 实体列表
     */
    @GetMapping("/{namespace}/page")
    @BizAction("分页获取实体列表")
    PageVO<Map<String, Object>> page(@PathVariable String namespace);

    /**
     * 创建实体
     * 这是 Raw Body POST for Form-Data 的版本
     *
     * @param namespace 实体的命名空间
     * @param params    实体
     * @return 实体 id
     */
    @PostMapping(value = "/{namespace}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @BizAction("创建实体")
    Long create(@PathVariable String namespace, @RequestParam Map<String, Object> params);

    /**
     * 创建实体
     * 这是 Raw Body POST for JSON 的版本
     *
     * @param namespace 实体的命名空间
     * @param params    实体
     * @return 实体 id
     */
    @PostMapping(value = "/{namespace}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @BizAction("创建实体")
    Long create(@RequestBody Map<String, Object> params, @PathVariable String namespace);

    /**
     * 修改实体
     * 这是 Raw Body POST for Form-Data 的版本
     *
     * @param namespace 实体的命名空间
     * @param params    实体
     * @return 是否成功
     */
    @PutMapping(value = "/{namespace}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @BizAction("修改实体")
    Boolean update(@PathVariable String namespace, @RequestParam Map<String, Object> params);

    /**
     * 修改实体
     * 这是 Raw Body POST for JSON 的版本
     *
     * @param namespace 实体的命名空间
     * @param params    实体
     * @return 是否成功
     */
    @PutMapping(value = "/{namespace}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @BizAction("修改实体")
    Boolean update(@RequestBody Map<String, Object> params, @PathVariable String namespace);

    /**
     * 删除实体
     *
     * @param namespace 实体的命名空间
     * @param id        实体 id
     * @return 是否成功
     */
    @DeleteMapping("/{namespace}/{id}")
    @BizAction("删除实体")
    Boolean delete(@PathVariable String namespace, @PathVariable Long id);
}