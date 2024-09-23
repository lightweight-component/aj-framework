package com.ajaxjs.framework.entity.tree;

import java.io.Serializable;
import java.util.*;


/**
 * 列表转树
 *
 * @param <T> id 类型
 */
public class BeanList2Tree<T extends Serializable> extends BaseTreeHandle<TreeNode<T>> {
    public BeanList2Tree(List<TreeNode<T>> allNodes) {
        super(allNodes);
    }

    public void init() {
        findRoots();
        findChildren();
        setLeafFlag();
    }

    public List<TreeNode<T>> findRoots() {
        Set<T> allIds = new HashSet<>();
        List<TreeNode<T>> result = new ArrayList<>();

        for (TreeNode<T> node : getAllNodes())
            allIds.add(node.getId());

        Integer p = getTopNodeValue();

        for (TreeNode<T> node : getAllNodes()) {
            T parentId = node.getParentId();

            // 没有上级节点或列表中找不到父节点的节点都作为根基点处理
            if (parentId == null || parentId == p || !allIds.contains(parentId))
                result.add(node);
        }

        setRoots(result);
        return result;
    }

    public void findChildren() {
        for (TreeNode<T> root : getRoots())
            root.setChildren(findChildren(root.getId()));
    }

    /**
     * 查找节点的子节点列表
     *
     * @param nodeId 节点 id
     * @return 节点的子节点列表
     */
    private List<TreeNode<T>> findChildren(T nodeId) {
        List<TreeNode<T>> result = new ArrayList<>();

        for (TreeNode<T> node : getAllNodes()) {
            if (Objects.equals(node.getParentId(), nodeId)) {
                node.setChildren(findChildren(node.getId()));
                result.add(node);
            }
        }

        return result;
    }

    /**
     * 设置树中叶子节点的标志。
     * 通过遍历树节点列表，判断每个节点是否为叶子节点。叶子节点定义为没有子节点的节点。
     * 该方法通过记录所有节点的父节点ID，然后检查每个节点是否在父节点ID集合中，来确定节点是否为叶子节点。
     * 如果一个节点的ID在父节点ID集合中，说明它有父节点，因此不是叶子节点；反之，则是叶子节点。
     */
    @Override
    public void setLeafFlag() {
        // 使用HashSet存储所有父节点的ID，以便高效地进行包含检查
        Set<T> parentIds = new HashSet<>();

        // 遍历树节点列表，收集所有父节点的ID
        for (TreeNode<T> node : getAllNodes())
            parentIds.add(node.getParentId());

        // 再次遍历树节点列表，判断每个节点是否为叶子节点
        for (TreeNode<T> node : getAllNodes()) {
            boolean contains = parentIds.contains(node.getId());// 检查当前节点的ID是否在父节点ID集合中
            node.setIsLeaf(!contains); // 设置节点的叶子状态。如果当前节点的ID在父节点ID集合中，说明它不是叶子节点；反之，则是叶子节点
        }
    }

    @Override
    public void printTree() {
//        printTree(roots);
        for (TreeNode<?> root : getRoots())
            printTree(root, "----");
    }

    public static void printTree(List<TreeNode<?>> roots) {
        for (TreeNode<?> root : roots)
            printTree(root, "----");
    }

    public static void printTree(TreeNode<?> node, String prefix) {
        System.out.println(prefix + node.getName() + " isLeaf: " + node.getIsLeaf());
        List<? extends TreeNode<?>> children = node.getChildren();

        if (children != null && !children.isEmpty()) {
            for (TreeNode<?> child : children)
                printTree(child, prefix + "----");
        }
    }
}
