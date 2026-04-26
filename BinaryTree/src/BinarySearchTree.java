import java.util.LinkedList;
import java.util.Queue;

public class BinarySearchTree <T extends Comparable<T>> {
    private BinaryNode<T> root;

    public BinarySearchTree() {
        root = null;
    } // end of BinarySearchTree

    private class BinaryNode<T> {
        T key;
        BinaryNode<T> leftChild;
        BinaryNode<T> rightChild;
        BinaryNode<T> parent;
        int balanceFactor;
        int height;

        public BinaryNode(T key) {
            this.key = key;
            this.leftChild = null;
            this.rightChild = null;
            this.parent = null;
            this.balanceFactor = 0;
            this.height = 0;
        } // end of BinaryNode

        // Key
        public T getKey() { return key; }
        public void setKey(T key) { this.key = key; }

        // Child
        public BinaryNode<T> getLeftChild() { return leftChild; }
        public void setLeftChild(BinaryNode<T> leftChild) { this.leftChild = leftChild; }
        public BinaryNode<T> getRightChild() { return rightChild; }
        public void setRightChild(BinaryNode<T> rightChild) { this.rightChild = rightChild; }

        // Parent
        public BinaryNode<T> getParent() { return parent; }
        public void setParent(BinaryNode<T> parent) { this.parent = parent; }

        // Balance
        public int getBalanceFactor() { return balanceFactor; }
        public void setBalanceFactor(int balanceFactor) { this.balanceFactor = balanceFactor; }

    } // end of Binary Node


    // Height
    private int height(BinaryNode<T> root){
        if(root == null) return -1;

        int leftHeight = height(root.leftChild);
        int rightHeight = height(root.rightChild);

        return Math.max(leftHeight, rightHeight) + 1;
    } // end of height

    public int height() { return height(root); } // end

    private int getHeight(BinaryNode<T> root){ return height(root); } // end


    // Depth
    public int depth(BinaryNode<T> query) {
        if (query == null) return -1;
        int depth = 0;
        BinaryNode<T> current = query;

        while (current.parent != null) {
            current = current.parent;
            depth++;

        } // end of while

        return (current == root) ? depth : -1;
    } // end of depth


    // inOrder
    public String inOrder() {
        StringBuilder sb = new StringBuilder();
        inOrder(root, sb);
        return sb.toString().replaceAll(", ", "");

    } // end of inOrder

    private void inOrder(BinaryNode<T> node, StringBuilder sb) {
        if (node == null)
            return;

        inOrder(node.leftChild, sb);
        sb.append(node.key).append(", ");
        inOrder(node.rightChild, sb);
    } // end of inOrder helper


    // preOrder
    public String preorder() {
        StringBuilder sb = new StringBuilder();
        preorder(root, sb);
        return sb.toString().replaceAll(", $", "");
    } // end of preorder

    private void preorder(BinaryNode<T> node, StringBuilder sb) {
        if(node == null)
            return;

        sb.append(node.key).append(", ");
        preorder(node.leftChild, sb);
        preorder(node.rightChild, sb);
    } // end of preorder helper

    // postorder
    public String postorder() {
        StringBuilder sb = new StringBuilder();
        postorder(root, sb);
        return sb.toString().replaceAll(", ", "");

    } // end of postorder

    private void postorder(BinaryNode<T> node, StringBuilder sb) {
        if(node == null)
            return;

        postorder(node.leftChild, sb);
        postorder(node.rightChild, sb);
        sb.append(node.key).append(", ");
    } // end of postorder helper


    // Level Order
    public String levelOrder() {
        if (root == null) return "";

        StringBuilder sb = new StringBuilder();
        Queue<BinaryNode<T>> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            BinaryNode<T> node = queue.poll();
            sb.append(node.key).append(", ");

            if (node.leftChild != null) queue.add(node.leftChild);
            if (node.rightChild != null) queue.add(node.rightChild);
        } // end of while

        return sb.toString().replaceAll(", $", "");
    } // end of levelOrder



    // remove
    public BinaryNode<T> remove(T data) {
        BinaryNode<T> target = find(data);

        if(target == null)
            return null;

        root = remove(root, data);

        return target;
    } // end of remove

    private BinaryNode<T> remove(BinaryNode<T> current, T data) {
        if(current == null)
            return null;

        int cmp = data.compareTo(current.key);

        if(cmp < 0)
            current.leftChild = remove(current.leftChild, data);

        else if (cmp > 0)
            current.rightChild = remove(current.rightChild, data);

        else {
            if(current.leftChild == null && current.rightChild == null)
                return null;

            if(current.leftChild == null)
                return current.rightChild;

            if (current.rightChild == null)
                return current.leftChild;

            BinaryNode<T> successor = findMin(current.rightChild);
            current.key = successor.key;
            current.rightChild = remove(current.rightChild, successor.key);
        } // end of else

        current.height = Math.max(height(current.leftChild), height(current.rightChild)) + 1;

        return rebalance(current);
    } // end of remove helper


    // Finders
    private BinaryNode<T> findMin(BinaryNode<T> node) {
        while(node.leftChild != null)
            node = node.leftChild;

        return node;
    } // end of findMin

    private BinaryNode<T> findMax(BinaryNode<T> node) {
        while(node.rightChild != null)
            node = node.rightChild;

        return node;
    } // end of findMax

    public BinaryNode<T> find(T data) {
        return find(root, data);
    } // end of find

    private BinaryNode<T> find(BinaryNode<T> current, T data) {
        if(current == null)
            return null;

        int cmp = data.compareTo(current.key);

        if(cmp == 0)
            return current;

        if(cmp < 0)
            return find(current.leftChild, data);

        return find(current.rightChild, data);
    } // end of find helper


    // Insert
    public boolean insert(T data) {
        if(root == null){
            root = new BinaryNode<>(data);
            return true;
        } // end of if

        boolean inserted = insert(root, data);
        if (inserted) root = rebalance(root);
        return inserted;

    } // end of insert

    private boolean insert(BinaryNode<T> current, T data) {
        int cmp = data.compareTo(current.key);

        if(cmp == 0)
            return false;

        boolean inserted;

        if(cmp < 0){
            if(current.leftChild == null){
                current.leftChild = new BinaryNode<>(data);
                current.leftChild.parent = current;
                inserted = true;
            } else {
                inserted = insert(current.leftChild, data);
            } // end of if

        } else {

            if(current.rightChild == null){
                current.rightChild = new BinaryNode<>(data);
                current.rightChild.parent = current;
                inserted = true;
            } else {
                inserted = insert(current.rightChild, data);
            } // end of if

        } // end of else

        if (inserted) {
            current.height = Math.max(height(current.leftChild), height(current.rightChild)) + 1;
            BinaryNode<T> rebalanced = rebalance(current);
            // Wire rebalanced subtree back into parent
            if (current.parent != null) {
                if (current.parent.leftChild == current)
                    current.parent.leftChild = rebalanced;
                else
                    current.parent.rightChild = rebalanced;
                rebalanced.parent = current.parent;
            } else {
                root = rebalanced;
            } // end of if else
        }  // end of if

        return inserted;
    } // end of insert helper


    // AVL rebalance helper
    private BinaryNode<T> rebalance(BinaryNode<T> node) {
        int bf = height(node.leftChild) - height(node.rightChild);
        node.balanceFactor = bf;

        if (bf > 1) {
            // Left heavy
            if (height(node.leftChild.leftChild) >= height(node.leftChild.rightChild)) {
                // LL case
                node = rightRotation(node);
            } else {
                // LR case
                node = leftRightRotation(node);
            } // end of if else
        } else if (bf < -1) {
            // Right heavy
            if (height(node.rightChild.rightChild) >= height(node.rightChild.leftChild)) {
                // RR case
                node = leftRotation(node);
            } else {
                // RL case
                node = rightLeftRotation(node);
            } // end of if else
        } // end of else if

        return node;
    } // end of rebalance


    // Left Rotate
    private BinaryNode<T> leftRotation(BinaryNode<T> node) {
        BinaryNode<T> y = node.rightChild;
        BinaryNode<T> T2 = y.leftChild;

        y.leftChild = node;
        node.rightChild = T2;

        // Update parents
        y.parent = node.parent;
        node.parent = y;
        if (T2 != null) T2.parent = node;

        node.height = Math.max(height(node.leftChild),height(node.rightChild)) + 1;
        y.height = Math.max(height(y.leftChild),height(y.rightChild)) + 1;

        return y;

    } // end of leftRotation


    // Right Rotate
    private BinaryNode<T> rightRotation(BinaryNode<T> node) {
        BinaryNode<T> y = node.leftChild;
        BinaryNode<T> T2 = y.rightChild;

        y.rightChild = node;
        node.leftChild = T2;

        // Update parents
        y.parent = node.parent;
        node.parent = y;
        if (T2 != null) T2.parent = node;

        node.height = Math.max(height(node.leftChild),height(node.rightChild)) + 1;
        y.height = Math.max(height(y.leftChild),height(y.rightChild)) + 1;

        return y;

    } // end of rightRotation


    // Left Right Rotate
    private BinaryNode<T> leftRightRotation(BinaryNode<T> node) {
        node.leftChild = leftRotation(node.leftChild);
        return rightRotation(node);

    } // end of left right rotate


    // Right Left Rotate
    private BinaryNode<T> rightLeftRotation(BinaryNode<T> node) {
        node.rightChild = rightRotation(node.rightChild);
        return leftRotation(node);

    } // end of right left rotation

} // end of BinarySearchTree