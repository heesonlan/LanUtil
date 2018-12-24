package com.lan.utils;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;

public class ZookeeperUtil {

	/**
	 * 1遍历展示某个节点下的所有节点（递归所有节点）
	 * @author LAN
	 * @date 2018年12月3日
	 * @param zkClient
	 * @param root
	 */
	public static void showZkPath(ZkClient zkClient, String root){
		List<String> children = zkClient.getChildren(root);//获取节点下的所有直接子节点
		if (children.isEmpty()) {
			return;
		}
		for (String s : children) {
			String childPath = root.endsWith("/") ? (root + s) : (root + "/" + s);
			System.err.println(childPath);
			showZkPath(zkClient, childPath);//递归获取所有子节点
		}
	}
	
	/**
	 * 1遍历展示某个节点下的所有节点及节点上的数据（递归所有节点）
	 * 2注意存储时的序列化方法和读取时的反序列化方法要对应
	 * @author LAN
	 * @date 2018年12月3日
	 * @param zkClient
	 * @param root
	 * @param serializer
	 */
	public static void showZkPathData(ZkClient zkClient, String root, ZkSerializer serializer){
		zkClient.setZkSerializer(serializer);
		List<String> children = zkClient.getChildren(root);
		if(children.isEmpty()){
			return;
		}
		for(String s:children){
			String childPath = root.endsWith("/")?(root+s):(root+"/"+s);
			Object data = zkClient.readData(childPath, true);
			if(data!=null) System.err.println(data.getClass());
			System.err.println(childPath+"("+data+")");
			showZkPathData(zkClient, childPath, serializer);
		}
	}
}
