package com.test_java.dataStructure;


public class BinaryTreeDemo {
	public static void main(String[] args) {

		// 先需要创建一颗二叉树
		BinaryTree binaryTree = new BinaryTree();
		// 创建需要的结点
		HeroNode root = new HeroNode(1,"宋江");
		HeroNode node2 = new HeroNode(2,"吴用");
		HeroNode node3 = new HeroNode(3,"卢俊义");
		HeroNode node4 = new HeroNode(4,"林冲");

		// 说明：我们先手动创建该二叉树；后面学习通过递归方法创建二叉树
		root.setLeft(node2);
		root.setRight(node3);
		node3.setRight(node4);
		binaryTree.setRoot(root);

		// 测试
		System.out.println("前序遍历"); // 1，2，3，4
		binaryTree.preOrder();



	}
}

/**树**/
class BinaryTree {
	private HeroNode root;
	public void setRoot(HeroNode root) {
		this.root = root;
	}

	// 前序遍历
	public void preOrder() {
		if(this.root != null) {
			this.root.preOrder();
		} else {
			System.out.println("当前二叉树为空，无法遍历！");
		}
	}

	// 中序遍历
	public void infixOrder() {
		if(this.root != null) {
			this.root.infixOrder();
		} else {
			System.out.println("当前二叉树为空，无法遍历！");
		}
	}

	// 后序遍历
	public void postOrder() {
		if(this.root != null) {
			this.root.postOrder();
		} else {
			System.out.println("当前二叉树为空，无法遍历！");
		}
	}
}

/**节点**/
class HeroNode{
	private int no;
	private String name;
	private HeroNode left;
	private HeroNode right;
	public HeroNode(int no,String name) {
		this.no = no;
		this.name=name;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HeroNode getLeft() {
		return left;
	}

	public void setLeft(HeroNode left) {
		this.left = left;
	}

	public HeroNode getRight() {
		return right;
	}

	public void setRight(HeroNode right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "HeroNode{" +
				"no=" + no +
				", name='" + name + '\'' +
				", left=" + left +
				", right=" + right +
				'}';
	}

	// 前序遍历
	public void preOrder() {
		System.out.println(this); // 输出父节点
		// 递归向左子树前序遍历
		if(this.left != null) {
			this.left.preOrder();
		}
		// 递归向右子树前序遍历
		if(this.right != null) {
			this.right.preOrder();
		}
	}
	// 中序遍历
	public void infixOrder() {
		// 递归向左子树中序遍历
		if(this.left != null) {
			this.left.infixOrder();
		}
		System.out.println(this); // 输出父节点
		// 递归向右子树中序遍历
		if(this.right != null) {
			this.right.infixOrder();
		}
	}
	// 后序遍历
	public void postOrder() {
		// 递归向左子树后序遍历
		if(this.left != null) {
			this.left.postOrder();
		}
		// 递归向右子树后序遍历
		if(this.right != null) {
			this.right.postOrder();
		}
		System.out.println(this); // 输出父节点
	}

}
