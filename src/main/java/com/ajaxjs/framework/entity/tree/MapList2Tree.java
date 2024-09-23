package com.ajaxjs.framework.entity.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.*;

/**
 * @param <T>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MapList2Tree<T extends Serializable> extends BaseTreeHandle<Map<String, Object>> {
    private String idField = "id";

    private String parentIdField = "parentId";

    private String childrenField = "children";

    /**
     * @param allNodes 节点列表，包含所有需要转换的数据
     */
    public MapList2Tree(List<Map<String, Object>> allNodes) {
        super(allNodes);
    }

    @Override
    public void init() {
        setLeafFlag();
        toTree();
    }

    /**
     * 将列表 Map 数据转换为树状结构的列表。
     * 此方法适用于具有父子关系的数据结构，通过指定ID类型和节点列表，将扁平化的数据转换为树形结构。
     *
     * <p>
     * 方法内部通过构建一个映射来加速节点查找，然后遍历节点列表，根据每个节点的父ID将其链接到相应的父节点上。
     * 如果节点的父ID指向根节点（即最顶级的节点），则将其直接添加到结果列表中。
     *
     * @return 转换后的树状结构列表。
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> toTree() {
        List<Map<String, Object>> nodes = getAllNodes();
        // 使用映射存储所有节点，以ID为键，节点对象为值，加快后续节点查找速度
        // 扁平化的列表转换为 tree 结构
        Map<T, Map<String, Object>> parents = new HashMap<>();

        // 遍历节点列表，将每个节点按ID存储到 Map 中
        for (Map<String, Object> node : nodes)
            parents.put((T) node.get(getIdField()), node);

        // 初始化结果列表，用于存储转换后的树状结构
        List<Map<String, Object>> tree = new ArrayList<>();

        // 再次遍历节点列表，根据每个节点的父ID将其链接到树中
        for (Map<String, Object> node : nodes) {
            T parentId = (T) node.get(getParentIdField());

            // 判断节点的父ID是否为根节点，如果是，则直接将其添加到结果列表
            if (!parentId.equals(getTopNodeValue())) {
                Map<String, Object> parent = parents.get(parentId);
                Object _children = parent.get(getChildrenField());
                List<Map<String, Object>> children;

                // 如果父节点的“子节点”字段为空，则初始化为新的列表，并添加到父节点
                if (_children == null) {
                    children = new ArrayList<>();
                    parent.put(getChildrenField(), children);
                } else
                    // 如果父节点的“子节点”字段已初始化，则直接将其转换为列表
                    children = (List<Map<String, Object>>) _children;

                children.add(node);// 将当前节点添加到父节点的“子节点”列表中
            } else
                tree.add(node);// 如果节点的父ID为根节点，则直接将其添加到结果列表的根节点位置
        }

        setRoots(tree);
        // 返回转换后的树状结构列表
        return tree;
    }

    /**
     * Map 里面每个元素是树节点，都有 id、pid 字段，pid 是指向父亲节点，
     * 如何求得这个节点下面没有 child，即 isLeaf = true，并将这 isLeaf 设置到 map
     */
    @Override
    public void setLeafFlag() {
        setLeafFlag("isLeaf");
    }

    /**
     * Map 里面每个元素是树节点，都有 id、pid 字段，pid 是指向父亲节点，
     * 如何求得这个节点下面没有 child，即 isLeaf = true，并将这 isLeaf 设置到 map
     *
     * @param leafField 是否叶子的字段名
     */
    @SuppressWarnings("unchecked")
    public void setLeafFlag(String leafField) {
        // 如果一个节点的 id 在 parentIds 集合中找不到对应的元素，则可以认为该节点是叶子节点，因为没有其他节点引用它
        Set<T> parentIds = new HashSet<>();

        for (Map<String, Object> node : getAllNodes())
            parentIds.add((T) node.get(parentIdField));

        for (Map<String, Object> node : getAllNodes()) {
            boolean contains = parentIds.contains((T) node.get("id"));

            node.put(leafField, !contains);
        }
    }

    @Override
    public void printTree() {
//        printTree(roots);
        for (Map<String, Object> root : getRoots())
            printTree(root, "----");
    }

    @SuppressWarnings("unchecked")
    public void printTree(Map<String, Object> node, String prefix) {
        System.out.println(prefix + node.get("name") + " isLeaf: " + node.get("isLeaf"));
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get(childrenField);

        if (children != null && !children.isEmpty()) {
            for (Map<String, Object> child : children)
                printTree(child, prefix + "----");
        }
    }
}

