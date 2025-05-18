package com.infrastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * A generic tree data structure that allows arbitrary number of child nodes.
 *
 * @param <T> the type of data stored in the tree nodes
 */
public class Tree<T> {

    /**
     * Represents a node in the tree.
     */
    public static class TreeNode<T> {
        private T data;
        private TreeNode<T> parent;
        private List<TreeNode<T>> children;
        private int subtreeSize;

        /**
         * Constructs a new tree node.
         *
         * @param data   the data to store in this node
         * @param parent the parent node (null for root)
         */
        private TreeNode(T data, TreeNode<T> parent) {
            this.data = data;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.subtreeSize = 1;

            if (parent != null) {
                updateAncestorSubtreeSizes(1);
            }
        }

        /**
         * Updates subtree sizes of all ancestors by the specified delta.
         */
        private void updateAncestorSubtreeSizes(int delta) {
            TreeNode<T> current = this;
            while (current != null) {
                current.subtreeSize += delta;
                current = current.parent;
            }
        }

        public T getData() {
            return data;
        }

        public TreeNode<T> getParent() {
            return parent;
        }

        /**
         * Returns an unmodifiable view of the children list.
         */
        public List<TreeNode<T>> getChildren() {
            return Collections.unmodifiableList(children);
        }

        /**
         * Returns the number of nodes in the subtree rooted at this node.
         */
        public int getSubtreeSize() {
            return subtreeSize;
        }
    }

    private TreeNode<T> root;

    /**
     * Constructs a new tree with the specified root data.
     *
     * @param rootData the data for the root node
     */
    public Tree(T rootData) {
        this.root = new TreeNode<>(rootData, null);
    }

    /**
     * Returns the root node of the tree.
     */
    public TreeNode<T> getRoot() {
        return root;
    }

    /**
     * Adds a new child node to the specified parent node.
     *
     * @param parent the parent node to add the child to
     * @param data   the data for the new child node
     * @return the newly created child node
     * @throws IllegalArgumentException if parent is null
     */
    public TreeNode<T> addChild(TreeNode<T> parent, T data) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent node cannot be null");
        }
        TreeNode<T> newNode = new TreeNode<>(data, parent);
        parent.children.add(newNode);
        return newNode;
    }

    /**
     * Removes the specified node and its entire subtree from the tree.
     *
     * @param node the node to remove
     * @throws IllegalArgumentException if node is null
     */
    public void removeNode(TreeNode<T> node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }

        if (node == root) {
            root = null;
        } else {
            TreeNode<T> parent = node.parent;
            if (parent != null) {
                parent.children.remove(node);
                parent.updateAncestorSubtreeSizes(-node.subtreeSize);
                node.parent = null;
            }
        }
    }

    /**
     * Returns the total number of nodes in the tree.
     */
    public int size() {
        return root != null ? root.subtreeSize : 0;
    }

    /**
     * Returns the height of the tree (longest path from root to leaf).
     */
    public int height() {
        return root != null ? height(root) : 0;
    }

    private int height(TreeNode<T> node) {
        int maxHeight = 0;
        for (TreeNode<T> child : node.children) {
            int childHeight = height(child);
            maxHeight = Math.max(maxHeight, childHeight);
        }
        return maxHeight + 1;
    }

    /**
     * Performs a pre-order traversal of the tree.
     *
     * @param visitor the consumer to process each node's data
     */
    public void traversePreOrder(Consumer<T> visitor) {
        traversePreOrder(root, visitor);
    }

    private void traversePreOrder(TreeNode<T> node, Consumer<T> visitor) {
        if (node == null) return;
        visitor.accept(node.data);
        for (TreeNode<T> child : node.children) {
            traversePreOrder(child, visitor);
        }
    }

    /**
     * Performs a post-order traversal of the tree.
     *
     * @param visitor the consumer to process each node's data
     */
    public void traversePostOrder(Consumer<T> visitor) {
        traversePostOrder(root, visitor);
    }

    private void traversePostOrder(TreeNode<T> node, Consumer<T> visitor) {
        if (node == null) return;
        for (TreeNode<T> child : node.children) {
            traversePostOrder(child, visitor);
        }
        visitor.accept(node.data);
    }

    /**
     * Performs a level-order (breadth-first) traversal of the tree.
     *
     * @param visitor the consumer to process each node's data
     */
    public void traverseLevelOrder(Consumer<T> visitor) {
        if (root == null) return;
        Queue<TreeNode<T>> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            TreeNode<T> node = queue.poll();
            visitor.accept(node.data);
            queue.addAll(node.children);
        }
    }

    /**
     * Finds and returns the first node that satisfies the given predicate.
     *
     * @param predicate the condition to test nodes against
     * @return the first matching node, or null if none found
     */
    public TreeNode<T> findNode(Predicate<T> predicate) {
        return root != null ? findNode(root, predicate) : null;
    }

    private TreeNode<T> findNode(TreeNode<T> node, Predicate<T> predicate) {
        if (predicate.test(node.data)) {
            return node;
        }
        for (TreeNode<T> child : node.children) {
            TreeNode<T> found = findNode(child, predicate);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns a list of data values from all leaf nodes.
     */
    public List<T> getLeaves() {
        List<T> leaves = new ArrayList<>();
        if (root != null) {
            getLeaves(root, leaves);
        }
        return leaves;
    }

    private void getLeaves(TreeNode<T> node, List<T> leaves) {
        if (node.children.isEmpty()) {
            leaves.add(node.data);
        } else {
            for (TreeNode<T> child : node.children) {
                getLeaves(child, leaves);
            }
        }
    }
}
