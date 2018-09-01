package com.up72.server.mina.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.redis.RedisUtil;

public class BackFileUtil {
	
	static int num = 1;
	
	public static void rr(){
		try {
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append("redis-set-fail-log-"+num++).toString();
			File file = new File(fineName);
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void tt(){}
	
	/**
	 * 目前room这个参数是必填的
	 * @param pd
	 * @param interfaceId
	 * @param room
	 * @param players
	 * @param infos
	 * @return
	 */
	public static boolean write(ProtocolData pd,Integer interfaceId,RoomResp room,List<Player> players,Object infos){
		boolean result = true;
		FileWriter fw = null;
		BufferedWriter w = null;
		try {
			
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String time_prefix = new SimpleDateFormat("yyyyMMddHHmmss").format(d);
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append(time_prefix)
					.append("-")
					.append(room.getRoomId())
					.append("-")
					.append(room.getXiaoJuNum()==null?1:room.getXiaoJuNum())
					.append(".txt").toString();
			File file = new File(fineName);
			boolean fileExists = true;
			if (!file.exists()) {//说明是新开的小局
				fileExists = false;
				file.createNewFile();
			}
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			if (!fileExists) {
				w.write("{\"state\":1,\"info\":[");
				w.newLine();
			}
			
			Map<String,Object> obj = new LinkedHashMap<String, Object>();
			switch (interfaceId) {
			case 100200://开局
				if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
					Map<String,Object> map = new HashMap<String, Object>();
					List<Map<String,Object>> userInfos = new ArrayList<Map<String,Object>>();
					for (Player p:players) {
						Map<String,Object> users = new HashMap<String, Object>();
						users.put("userId",p.getUserId());
						users.put("gender", p.getGender());
						users.put("userName", p.getUserName());
						users.put("userImg", p.getUserImg());
						users.put("position", p.getPosition());
						users.put("score", p.getScore());
						users.put("playStatus", Cnst.PLAYER_STATE_GAME);
						List<Card> cards = p.getStartCardList();
						List<Integer> pais = new ArrayList<Integer>();
						for(Card c:cards){
							pais.add(c.getOrigin());
						}
						users.put("paiInfos", pais);
						userInfos.add(users);						
					}
					map.put("userInfos", TCPGameFunctions.getNewList(userInfos));
					map.put("roomInfo",  TCPGameFunctions.getNewMap(MessageFunctions.getRoomInfo(room)));
					obj.put("interfaceId", 100200);
					obj.put("jsonStr", map);
				}
				break;
			case 100102://小结算
				obj.put("interfaceId", 100102);
				obj.put("jsonStr", infos);
				break;
			case 100103://大结算
				obj.put("interfaceId", 100103);
				obj.put("jsonStr", infos);
				break;
			case 100202://动作提示
				obj.put("interfaceId", 100104);
				obj.put("jsonStr", infos);
				break;
			case 100201://出牌提示
				obj.put("interfaceId", 100105);
				obj.put("jsonStr", infos);
				break;
			case 100207://请求发牌
				obj.put("interfaceId", 100207);
				obj.put("jsonStr", infos);
				break;
			}
			if (obj.size()>0) {
				if (interfaceId.equals(100102)) {
					w.write(",");
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
					w.write("]}");
				}else if(interfaceId.equals(100103)){
					if (room.getDissolveRoom()!=null) {
						BufferedReader re = new BufferedReader(new FileReader(file));
						String lastLine = null;
						String temp = null;
						while((temp = re.readLine())!=null){
							lastLine = temp;
						}
						if (lastLine!=null&&lastLine.equals("]}")) {
							
						}else{
							w.write(",");
							w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
							w.newLine();
							w.write("]}");
						}
						re.close();
					}
				}else if(interfaceId.equals(100200)){
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
				}else{
					w.write(",");
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
				}
			}
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}finally{
			try {
				if (w!=null) {
					w.close();
				}
				if (fw!=null) {
					fw.close();
				}
			} catch (Exception e2) {
				
			}
		}
		
		return result;
	}
		
	public static boolean writeXiaoJieSuanInfo(RoomResp room,Object infos){
		boolean result = true;

		FileWriter fw = null;
		BufferedWriter w = null;
		try {
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append(new SimpleDateFormat("yyyyMMddHHmmss").format(d))
					.append("-")
					.append(room.getRoomId())
					.append(".txt").toString();
			File file = new File(fineName);
			boolean fileExists = true;
			if (!file.exists()) {
				fileExists = false;
				file.createNewFile();
			}
			
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			if (!fileExists) {
				w.write("{\"state\":1,\"info\":[");
				w.newLine();
			}
			
			Map<String,Object> obj = new LinkedHashMap<String, Object>();
			obj.put("xjn", room.getXiaoJuNum());
			obj.put("jsInfo", infos);
			if (room.getXiaoJuNum()==1) {//第一句，不用写逗号
				if (room.getDissolveRoom()!=null) {//解散了
					if (room.getState() == Cnst.ROOM_STATE_XJS) {//小结算状态解散，不写入数据
						w.write("]}");
					}else{
						w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
						w.newLine();
						w.write("]}");
					}
				}else{//没解散
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
				}
			}else{//大于第一局
				if (room.getDissolveRoom()!=null) {//解散了
					if (room.getState() == Cnst.ROOM_STATE_XJS) {//小结算状态解散，不写入数据
						w.write("]}");
					}else{
						w.write(",");
						w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
						w.newLine();
						w.write("]}");
					}
				}else{//没解散
					w.write(",");
					w.write(JSON.toJSONString(obj,SerializerFeature.DisableCircularReferenceDetect));
					w.newLine();
					if (room.getState() == Cnst.ROOM_STATE_YJS) {
						w.write("]}");
					}
				}
			}
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if (w!=null) {
					w.close();
				}
				if (fw!=null) {
					fw.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		
		
		return result;
	}
	
	//文件格式为yyyyMMddHHmmss-roomId-xiaoJuNum.txt
	public static void deletePlayRecord(){
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			long currentDate = new Date().getTime();
			if (files!=null&&files.length>0) {
				for(int i=0;i<files.length;i++){
					File f = files[i];
					String name = f.getName();
					String dateStr = name.split("_")[0];
					Date createDate = sdf.parse(dateStr);
					if ((currentDate-createDate.getTime())>=Cnst.BACKFILE_STORE_TIME) {
						f.delete();
					}else{
						break;
					}
				}
			}
			System.out.println("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
	
	
	/**
	 * 每次启动服务，清零所有回放文件
	 */
	public static void deleteAllRecord(){
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();

			if (files!=null&&files.length>0) {
				for(int i=0;i<files.length;i++){
					File f = files[i];
					f.delete();
				}
			}
			path.delete();
			System.out.println("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    
	}
	
	
	
	public static Integer getFileNumByRoomId(Integer roomId){
		Integer num = 0;
		File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
		File[] files = path.listFiles();
		if (files!=null&&files.length>0) {
			for(int i=0;i<files.length;i++){
				File f = files[i];
				if (f.getName().contains("-".concat(String.valueOf(roomId)).concat("-"))) {
					num++;
				}
			}
		}
		return num;
	}
	
}
