package com.up72.server.mina.utils.dcuse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.up72.game.constant.Cnst;

public class CreateInterfacedFile {
	public static void createJieKou(){
		FileWriter fw = null;
    	BufferedWriter w = null;
    	try {
    		String url = Cnst.FILE_ROOT_PATH;
//			File parent = new File(url);
//			if (!parent.exists()) {
//				parent.mkdirs();
//			}
			String fineName = new StringBuffer().append(url).append("interfaceIdFile.txt").toString();
			File file = new File(fineName);
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			String interFaced = JSON.toJSONString(Cnst.ROUTE_MAP);
			w.write(interFaced);
			w.flush();
			System.out.println("===============================>生成接口文件");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fw != null){
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(w != null){
				try {
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
