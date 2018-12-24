package com.lan.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoZkSerializer implements ZkSerializer{

	@Override
	public byte[] serialize(Object data) throws ZkMarshallingError {
		if(data==null) {
			return null;
		}
		ByteArrayOutputStream baos = null;
		Output output = null;
		try {
			Kryo kryo = new Kryo();
			baos = new ByteArrayOutputStream();
			output = new Output(baos);
			kryo.writeClassAndObject(output, data);
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

	@Override
	public Object deserialize(byte[] bytes) throws ZkMarshallingError {
		if(bytes==null || bytes.length==0) {
			return null;
		}
		Kryo kryo = new Kryo();
		Input input = new Input(bytes);
		Object obj = kryo.readClassAndObject(input);
		input.close();
		return obj;
	}

}
