package com.ajaxjs.framework.entity.tree;

import com.ajaxjs.util.ObjectHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestTree {
    @Test
    public void testBean() {
        List<TreeNode<Integer>> nodes = new ArrayList<>();
        nodes.add(new TreeNode<>(1, "TreeNode 1", 0));
        nodes.add(new TreeNode<>(2, "TreeNode 2", 1));
        nodes.add(new TreeNode<>(3, "TreeNode 3", 1));
        nodes.add(new TreeNode<>(4, "TreeNode 4", 2));
        nodes.add(new TreeNode<>(5, "TreeNode 5", 3));
        nodes.add(new TreeNode<>(6, "TreeNode 6", 3));
        nodes.add(new TreeNode<>(7, "TreeNode 7", 0));
        nodes.add(new TreeNode<>(8, "TreeNode 8", 7));
        nodes.add(new TreeNode<>(9, "TreeNode 9", 7));
        nodes.add(new TreeNode<>(10, "TreeNode 10", 8));
        nodes.add(new TreeNode<>(11, "TreeNode 11", 9));
        nodes.add(new TreeNode<>(12, "TreeNode 12", 10));

        BeanList2Tree<Integer> list2Tree = new BeanList2Tree<>(nodes);
        list2Tree.init();
        list2Tree.printTree();
    }

    @Test
    public void testMap() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(ObjectHelper.hashMap("id", 1, "name", "TreeNode 1", "parentId", -1));
        nodes.add(ObjectHelper.hashMap("id", 2, "name", "TreeNode 2", "parentId", 1));
        nodes.add(ObjectHelper.hashMap("id", 3, "name", "TreeNode 3", "parentId", 1));
        nodes.add(ObjectHelper.hashMap("id", 4, "name", "TreeNode 4", "parentId", 2));

        MapList2Tree<Integer> mapList2Tree = new MapList2Tree<>(nodes);
        mapList2Tree.init();
        mapList2Tree.printTree();
    }
}
