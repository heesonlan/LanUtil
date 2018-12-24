package com.lan.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * 使用Kryo
 * 把java对象序列化和反序列化
 * 虽然所序列化和反序列化的类无需实现java.io.Serializable接口，
 * 但还是建议实现java.io.Serializable接口，避免类修改后无法反序列化
 * @author LAN
 * @date 2018年11月13日
 */
public class KryoSerializeUtil {
	
	/**
	 * 把java对象序列化成byte数组
	 * @author LAN
	 * @date 2018年11月13日
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) {
		if(object==null) {
			return null;
		}
		ByteArrayOutputStream baos = null;
		Output output = null;
		try {
			Kryo kryo = new Kryo();
			baos = new ByteArrayOutputStream();
			output = new Output(baos);
			kryo.writeObject(output, object);
			output.flush();
			return baos.toByteArray();
		}  finally {
			try {
				if(baos!=null) baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			output.close();
		}
	}
	
	/**
	 * 把byte数组反序列化得到java对象
	 * @author LAN
	 * @date 2018年11月13日
	 * @param bytes
	 * @param clazz
	 * @return
	 */
	public static <T> T unserialize(byte[] bytes, Class<T> clazz) {
		if(bytes==null || bytes.length==0) {
			return null;
		}
		Kryo kryo = new Kryo();
		Input input = new Input(bytes);
		T obj = kryo.readObject(input, clazz);
		input.close();
		return obj;
	}
}
