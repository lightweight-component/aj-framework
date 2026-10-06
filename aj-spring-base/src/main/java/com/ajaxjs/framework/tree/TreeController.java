package com.ajaxjs.framework.tree;

import com.ajaxjs.spring.annotation.BizAction;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.util.ObjectHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class TreeController<T extends TreeNode> {
    private static final long ROOT_PARENT_ID = -1L;

    protected final String tableName;

    String getListSql() {
        return
// @formatter:off
"""
WITH RECURSIVE child_node_ids(id) AS (
    SELECT id FROM %s WHERE parent_id = ?  -- 种子查询 (Seed Query): 获取第一层子节点
    UNION DISTINCT
   -- 递归查询 (Recursive Query): 获取更深一层的子节点
    SELECT c.id FROM %s c INNER JOIN child_node_ids p ON c.parent_id = p.id -- 将当前表 (c) 与递归结果 (p) 连接
)
SELECT * FROM %s WHERE id IN (SELECT id FROM child_node_ids) ORDER BY parent_id, sort_no;
""".formatted(tableName, tableName, tableName);
// @formatter:on
    }

    @GetMapping("/list/{parentId}")
    @BizAction("List tree node by parent id")
    public List<Map<String, Object>> list(@PathVariable("parentId") Long parentId, @RequestParam(required = false) Long selectedId) {
        List<Map<String, Object>> list = new Action(getListSql()).query(parentId).list();

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

    @PostMapping("/")
    @BizAction("To create a tree node")
    public boolean create(@RequestBody T treeNode) {
        checkTreeNode(treeNode, null);

        return new Action(treeNode, tableName).create().execute(true).isOk();
    }

    @PutMapping("/{id}")
    @BizAction("To update a tree node")
    public boolean update(@RequestBody T treeNode, @PathVariable("id") Long id) {
        if (!exists(id))
            throw new IllegalArgumentException("The tree node does not exist: " + id);

        if (treeNode.getId() != null && !id.equals(treeNode.getId()))
            throw new IllegalArgumentException("The path id does not match the tree node id.");

        checkTreeNode(treeNode, id);
        treeNode.setId(id);

        return new Action(treeNode, tableName).update().withId("id", id).isOk();
    }

    @DeleteMapping("/{id}")
    @BizAction("To delete a tree node")
    public boolean delete(@PathVariable("id") Long id, @RequestParam(required = false) boolean isDelSubNode) {
        if (isDelSubNode)
            return new Action(getDeleteSubtreeSql()).update(id).execute().getEffectedRows() > 0;

        return new Action(getDeleteLeafSql()).update(id, id).execute().getEffectedRows() > 0;
    }

    private void checkTreeNode(T treeNode, Long nodeId) {
        if (treeNode == null)
            throw new IllegalArgumentException("The tree node is required.");

        if (ObjectHelper.isEmptyText(treeNode.getName()))
            throw new IllegalArgumentException("The name of tree node is required.");

        Long parentId = treeNode.getParentId();
        if (parentId == null)
            throw new IllegalArgumentException("The parent id should be not null or -1.");

        if (parentId == ROOT_PARENT_ID)
            return;

        if (nodeId != null && nodeId.equals(parentId))
            throw new IllegalArgumentException("A tree node cannot be its own parent.");

        if (!exists(parentId))
            throw new IllegalArgumentException("The parent tree node does not exist: " + parentId);

        if (nodeId != null && isDescendant(nodeId, parentId))
            throw new IllegalArgumentException("A tree node cannot be moved to one of its descendants.");
    }

    private boolean exists(Long id) {
        return id != null && new Action("SELECT id FROM " + tableName + " WHERE id = ?").query(id).one(Long.class) != null;
    }

    private boolean isDescendant(Long nodeId, Long candidateParentId) {
        String sql = """
WITH RECURSIVE child_nodes(id) AS (
    SELECT id FROM %s WHERE id = ?
    UNION DISTINCT
    SELECT c.id FROM %s c INNER JOIN child_nodes p ON c.parent_id = p.id
)
SELECT 1 FROM child_nodes WHERE id = ? LIMIT 1;
""".formatted(tableName, tableName);

        return new Action(sql).query(nodeId, candidateParentId).one(Integer.class) != null;
    }

    private String getDeleteLeafSql() {
        return """
DELETE FROM %s
WHERE id = ?
  AND NOT EXISTS (SELECT 1 FROM %s child WHERE child.parent_id = ?)
""".formatted(tableName, tableName);
    }

    private String getDeleteSubtreeSql() {
        return """
WITH RECURSIVE child_nodes(id) AS (
    SELECT id FROM %s WHERE id = ?
    UNION DISTINCT
    SELECT c.id FROM %s c INNER JOIN child_nodes p ON c.parent_id = p.id
)
DELETE FROM %s WHERE id IN (SELECT id FROM child_nodes)
""".formatted(tableName, tableName, tableName);
    }
}
