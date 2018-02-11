package com.android.providers.mst;


//add by liyang 2016-7-10
public class MstBinaryTree {

	String data;      //根节点数据
	MstBinaryTree left;    //左子树
	MstBinaryTree right;   //右子树
	
	

	public String getData() {
		return data;
	}
	
	

	public MstBinaryTree getLeft() {
		return left;
	}



	public MstBinaryTree getRight() {
		return right;
	}




	public MstBinaryTree(String data)    //实例化二叉树类
	{
		this.data = data;
		left = null;
		right = null;
	}

	public void insert(MstBinaryTree root,String data1,String data2){     //向二叉树中插入子节点


		if(root.right==null){
			root.right=new MstBinaryTree(data1);
		}else{
			this.insert(root.right,data1,data2);
		}
		if(root.left==null){
			root.left=new MstBinaryTree(data2);
		}else{
			this.insert(root.left,data1,data2);
		}
	}

}
