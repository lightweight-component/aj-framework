package com.ajaxjs.framework.tree;

import com.ajaxjs.framework.mvc.unifiedreturn.BizAction;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.util.ObjectHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Data Dict Controller
 */
@RestController
@RequestMapping("/data_dict")
public class DataDictController {
    private final static String SQL = "WITH RECURSIVE child_nodes AS (\n" +
            "    SELECT *  FROM sys_datadict WHERE parent_id = ?  -- 种子查询 (Seed Query): 获取第一层子节点\n" +
            "    UNION ALL\n" +
            "    -- 递归查询 (Recursive Query): 获取更深一层的子节点\n" +
            "    SELECT c.* FROM sys_datadict c INNER JOIN child_nodes p ON c.parent_id = p.id -- 将当前表 (c) 与递归结果 (p) 连接\n" +
            ")\n" +
            "SELECT * FROM child_nodes\n" +
            "ORDER BY parent_id, sort_no; ";

    /**
     * Get data dict by the parent id.
     *
     * @param parentId The parent id.
     * @return
     */
    @GetMapping("/{parentId}")
    @BizAction("Get data dict by the parent id")
    public List<Map<String, Object>> getDataDict(@PathVariable Long parentId, @RequestParam(required = false) Long selectedId) {
        List<Map<String, Object>> list = new Action(SQL).query(parentId).list();

        if (ObjectHelper.isEmpty(list))
            return null;

        list.forEach(item -> { // add some fields for iView tree select
            item.put("title", item.get("name"));
            item.put("expand", true);
            item.put("value", item.get("id"));

            if (selectedId != null && FlatArrayToTree.getLongValue(selectedId) == FlatArrayToTree.getLongValue(item.get("id")))
                item.put("selected", true);
            else
                item.put("selected", false);
        });
        FlatArrayToTree flatArrayToTree = new FlatArrayToTree();
        flatArrayToTree.setTopNodeValue(parentId);

        return flatArrayToTree.mapAsTree(Long.class, list);
    }
}
