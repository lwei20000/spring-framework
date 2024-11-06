package com.test01_java.dataStructure;


public class BinaryTreeDemo {
	public static void main(String[] args) {

		// 先需要创建一颗二叉树
		BinaryTree binaryTree = new BinaryTree();
		// 创建需要的结点
		HeroNode root = new HeroNode(1,"宋江");
		HeroNode node2 = new HeroNode(2,"吴用");
		HeroNode node3 = new HeroNode(3,"卢俊义");
		HeroNode node4 = new HeroNode(4,"林冲");
		HeroNode node5 = new HeroNode(5,"关胜");


		// 说明：我们先手动创建该二叉树；后面学习通过递归方法创建二叉树
		root.setLeft(node2);
		root.setRight(node3);
		node3.setLeft(node5);
		node3.setRight(node4);
		binaryTree.setRoot(root);

		// 测试--遍历
		System.out.println("前序遍历");
		binaryTree.preOrder();  // 1，2，3，4
		System.out.println("中序遍历");
		binaryTree.infixOrder();// 2，1，3，4
		System.out.println("后序遍历");
		binaryTree.postOrder(); // 2，4，3，1

		// 测试--查找
		System.out.println("前序查找");
		HeroNode resNode = binaryTree.preOrderSearch(5);
		if(resNode != null) {
			System.out.println("找到了：" + resNode.toString());
		} else {
			System.out.println("没有找到no=" + resNode.getNo() + "的节点");
		}

		// 测试--删除
		System.out.println("删除前，前序遍历");
		binaryTree.preOrder();
		binaryTree.delNode(5);
		System.out.println("删除后，前序遍历");
		binaryTree.preOrder();

	}
}

/**树**/
class BinaryTree {
	private HeroNode root;
	public void setRoot(HeroNode root) {
		this.root = root;
	}

	/******* 遍历 *********/
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

	/******* 查找 *********/
	public HeroNode preOrderSearch(int no) {
		if(root != null) {
			return root.preOrderSearch(no);
		} else {
			return null;
		}
	}

	public HeroNode infixOrderSearch(int no) {
		if(root != null) {
			return root.infixOrderSearch(no);
		} else {
			return null;
		}
	}

	public HeroNode postOrderSearch(int no) {
		if(root != null) {
			return root.preOrderSearch(no);
		} else {
			return null;
		}
	}

	/******* 删除 *********/
	public void delNode(int no) {
		if(root != null) {
			// 如果只有一个root节点，这里就需要立即判读是否root就是要删除的节点
			if(root.getNo() == no) {
				root = null;
			} else {
				// 递归删除
				root.delNode(no);
			}

		} else {
			System.out.println("空树，无法删除");
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

	// 前序查找
	public HeroNode preOrderSearch(int no) {
		// 比较当前节点是不是
		if(this.no == no) {
			return this;
		}

		// 1 则判断当前节点的左节点是否为空，如果不为空，则递归前序查找
		// 2 如果左递归前序查找，找到节点，则返回
		HeroNode resNode = null;  // 记录找到的节点
		if(this.left != null) {
			resNode = this.left.preOrderSearch(no);
		}
		if(resNode != null) {
			return resNode;
		}

		// 1 左递归前序查找，找到节点，则返回，否则继续判断。
		// 2 当前的节点的右子节点是否为空，如果不为空，则继续向右递归前序查找。
		if(this.right != null) {
			resNode = this.right.preOrderSearch(no);
		}
		return resNode;
	}

	// 中序查找
	public HeroNode infixOrderSearch(int no) {
		// 1 则判断当前节点的左节点是否为空，如果不为空，则递归前序查找
		// 2 如果左递归前序查找，找到节点，则返回
		HeroNode resNode = null;  // 记录找到的节点
		if(this.left != null) {
			resNode = this.left.infixOrderSearch(no);
		}
		if(resNode != null) {
			return resNode;
		}

		// 比较当前节点是不是
		if(this.no == no) {
			return this;
		}

		// 1 左递归前序查找，找到节点，则返回，否则继续判断。
		// 2 当前的节点的右子节点是否为空，如果不为空，则继续向右递归前序查找。
		if(this.right != null) {
			resNode = this.right.infixOrderSearch(no);
		}
		return resNode;
	}

	// 中序查找
	public HeroNode postOrderSearch(int no) {
		// 1 则判断当前节点的左节点是否为空，如果不为空，则递归前序查找
		// 2 如果左递归前序查找，找到节点，则返回
		HeroNode resNode = null;  // 记录找到的节点
		if(this.left != null) {
			resNode = this.left.postOrderSearch(no);
		}
		if(resNode != null) {
			return resNode;
		}

		// 1 左递归前序查找，找到节点，则返回，否则继续判断。
		// 2 当前的节点的右子节点是否为空，如果不为空，则继续向右递归前序查找。
		if(this.right != null) {
			resNode = this.right.postOrderSearch(no);
		}
		if(resNode != null) {
			return resNode;
		}

		// 比较当前节点是不是
		if(this.no == no) {
			return this;
		}

		return resNode;
	}

	// 递归删除节点：
	// 1 如果删除的是叶子节点，直接删除。
	// 2 如果删除的不是叶子节点，删除其子树
	public void delNode(int no) {
		// 2 如果当前节点的左子树不为空，并且左子节点就是要删除的节点，就将this.left = null;并且返回
		if(this.left != null && this.left.no == no) {
			this.left = null;
			return;
		}
		// 3 如果当前节点的右子树不为空，并且右子节点就是要删除的节点，就将this.right = null;并且返回
		if(this.right != null && this.right.no == no) {
			this.right = null;
			return;
		}

		// 4 我们就需要向左子树进行递归删除
		if(this.left != null) {
			this.left.delNode(no);
		}

		// 5 我们就需要向右子树进行递归删除
		if(this.right != null) {
			this.right.delNode(no);
		}
	}




























}
