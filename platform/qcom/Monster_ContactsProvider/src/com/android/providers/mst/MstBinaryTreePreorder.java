package com.android.providers.mst;

import android.util.Log;

//add by liyang 2016-7-10
public class MstBinaryTreePreorder {

	private static final String TAG = "MstBinaryTreePreorder";

	public static void preOrder(MstBinaryTree root){  //先根遍历
		if(root!=null){
			Log.d(TAG,root.data+"-");
			preOrder(root.left);
			preOrder(root.right);
		}
	}

	public static void inOrder(MstBinaryTree root){     //中根遍历

		if(root!=null){
			inOrder(root.left);
			System.out.print(root.data+"--");
			inOrder(root.right);
		}
	}

	public static void postOrder(MstBinaryTree root){    //后根遍历

		if(root!=null){
			postOrder(root.left);
			postOrder(root.right);
			System.out.print(root.data+"---");
		}
	}

	//匹配拼音+汉字，首字母+汉字
	//打印所有当前树的所有路径
	//参数为：根节点，存储结点的数组，控制路径长度的整形变量
	//提示 ：整形变量size在这里十分重要
	public static void printWay(MstBinaryTree root,String[] path,int size){
		//如果根节点为空直接返回
		if(root == null){
			return ;
		}
		//不为空把根节点存储到数组当中
		Log.d(TAG,"size:"+size);
		path[size++] = root.getData();
		//进行逻辑判断，看当前结点是不是叶子结点 如果是叶子结点 按照长度为 size进行打印当前数组
		if(root.getLeft()==null&&root.getRight()==null){
			Log.d(TAG,"路径为");
			for(int i=0;i<size;i++){
				Log.d(TAG," i:"+i+" path:"+path[i]);
			}
			//如果不是叶子结点递归进去继续往数组中插值
		}else{
			Log.d(TAG,"else,size:"+size);
			printWay(root.getLeft(),path,size);
			printWay(root.getRight(),path,size);
		}
	}

}
