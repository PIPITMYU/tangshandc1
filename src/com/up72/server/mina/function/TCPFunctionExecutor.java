package com.up72.server.mina.function;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

public class TCPFunctionExecutor {

	private static final MyLog log = MyLog.getLogger(TCPFunctionExecutor.class);

	public static void execute(IoSession session, ProtocolData readDatas)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, InstantiationException, Exception {

        int interfaceId = readDatas.getInterfaceId();
        JSONObject obj = JSONObject.parseObject(readDatas.getJsonString());
        
        //路由转换
        Map<String,Object> readData = new ConcurrentHashMap<String, Object>();
        Iterator<String> iterator = obj.keySet().iterator();
        while(iterator.hasNext()) {  
            String str = iterator.next();  
            readData.put(Cnst.ROUTE_MAP.get(str), obj.get(str));
        }
        //转换完成
        System.out.println("==========================>接收到的数据"+readData);
		switch (interfaceId) {
		// 大厅消息段
		case 100002:
			HallFunctions.interface_100002(session, readData);
			break;
		case 100003:
			HallFunctions.interface_100003(session, readData);
			break;
		case 100004:
			HallFunctions.interface_100004(session, readData);
			break;
		case 100005:
			HallFunctions.interface_100005(session, readData);
			break;
		case 100006:
			HallFunctions.interface_100006(session, readData);
			break;
		case 100007:
			HallFunctions.interface_100007(session, readData);
			break;// 经典玩法创建房间
		case 100008:
			HallFunctions.interface_100008(session, readData);
			break;
		case 100009:
			HallFunctions.interface_100009(session, readData);
			break;
		case 100010:
			HallFunctions.interface_100010(session, readData);
			break;
		case 100011:
			HallFunctions.interface_100011(session, readData);
			break;
		case 100012:
			HallFunctions.interface_100012(session, readData);
			break;
		case 100013:
			HallFunctions.interface_100013(session, readData);
			break;
		case 100014:
			HallFunctions.interface_100014(session, readData);
			break;
		case 100015:
			HallFunctions.interface_100015(session, readData);
			break;

		// 推送消息段
		case 100100:
			MessageFunctions.interface_100100(session, readData);
			break;// 大接口
		case 100102:
			MessageFunctions.interface_100102(session, readData);
			break;// 小结算
		case 100103:
			MessageFunctions.interface_100103(session, readData);
			break;// 大结算

		// 游戏中消息段
		case 100200:
			GameFunctions.interface_100200(session, readData);
			break;
		case 100201:
			GameFunctions.interface_100201(session, readData);
			break;
		case 100202:
			GameFunctions.interface_100202(session, readData);
			break;
		case 100203:
			GameFunctions.interface_100203(session, readData);
			break;
		case 100204:
			GameFunctions.interface_100204(session, readData);
			break;
		case 100205:
			GameFunctions.interface_100205(session, readData);
			break;
		case 100206:
			GameFunctions.interface_100206(session, readData);
			break;
		case 100207:
			GameFunctions.interface_100207(session, readData);
			break;
		case 100208:
			GameFunctions.interface_100208(session, readData);
			break;


			// 强制解散房间
		case 999800:
			disRoomForce(session, readData);			
			break;

		
		default:
			Map<String,String> user = RedisUtil.hgetAll
			(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(session.getAttribute(Cnst.USER_SESSION_USER_ID)+""));
			if (user == null) {

			} else {
				log.I("未知interfaceId" + interfaceId);
				MessageFunctions.illegalRequest(interfaceId, session);// 非法请求
			}
			break;
		}
		
		
		
		
		if (interfaceId!=100100) {
			if(readData.get("userId")!=null){
				Long userId = Long.valueOf(String.valueOf(readData.get("userId")));
				Player cp = RedisUtil.getPlayerByUserId(String.valueOf(userId));
				if (cp!=null) {
					if (cp.getRoomId()==null&&((Integer)Cnst.PLAYER_STATE_GAME).equals(cp.getPlayStatus())) {
						System.err.println("玩家状态不正确******************************************************************");
						System.out.println(readData);
						System.out.println();
					}
				}
			}			
		}
		
		
		
		
		
		

	}


	/**
	 * 心跳操作
	 * 所有用户的id对应心跳，存在redis的map集合里，集合的key为Cnst.REDIS_HEART_PREFIX，map的key为userId，value为最后long类型的心跳时间
	 * 当用户掉线之后，会把map中的这个userId删掉，（考虑：是否修改用户的state字段）
	 * @param session
	 * @param readData
	 */
	public synchronized static void heart(IoSession session, ProtocolData readData)
			throws Exception {
		String userIdStr = String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID));
		if (userIdStr==null) {
			session.close(true);
		}else{
			try {
				Player p = RedisUtil.getPlayerByUserId(userIdStr);
				if (p!=null) {
					long ct = System.currentTimeMillis();
					String lastHeartTime = RedisUtil.hget(Cnst.REDIS_HEART_PREFIX, userIdStr);
					if (lastHeartTime==null) {//说明用户重新上线
						//需要看用户是否在房间里，如果在，要通知其他玩家
						String roomId = String.valueOf(p.getRoomId()); 
						if (roomId!=null) {//用户在房间里
							RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
							if (room!=null&&room.getState()!=(Cnst.ROOM_STATE_YJS)) {//房间还没解散
								//toAdd  通知房间内其他人用户上线
								List<Player> players =  RedisUtil.getPlayerList(room); 
								List<Long> userIds = new ArrayList<Long>();
								userIds.add(p.getUserId());
								List<Integer> status = new ArrayList<Integer>();
								status.add(Cnst.PLAYER_LINE_STATE_INLINE);
								MessageFunctions.interface_100109(players, status,userIds,1);
							}
						}
					}else{//说明用户正常心跳
						//如果玩家在房间里，需要计算其他用户是否心跳超时
						String roomId = String.valueOf(p.getRoomId());
						if (roomId!=null) {//用户在房间里
							RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
							if (room!=null&&room.getState()!=(Cnst.ROOM_STATE_YJS)) {//房间还没解散
								//toAdd 计算房间里其他玩家的心跳时间
								
								Long[] uids = room.getPlayerIds();
								List<Long> outs = new ArrayList<Long>();
								List<Integer> status = new ArrayList<Integer>();
								for(Long uid:uids){
									if (uid!=null&&!(String.valueOf(uid)).equals(userIdStr)) {
										String uidHeartTime = RedisUtil.hget(Cnst.REDIS_HEART_PREFIX,String.valueOf(uid));
										if (uidHeartTime!=null) {
											long t = Long.valueOf(uidHeartTime);
											if ((ct-t)>Cnst.HEART_TIME) {
												RedisUtil.hdel(Cnst.REDIS_HEART_PREFIX, String.valueOf(uid));
												outs.add(uid);
												status.add(Cnst.PLAYER_LINE_STATE_OUT);
											}
										}
									}
								}
								
								if (outs.size()>0) {
									//toAdd  通知其他人，outs里面的玩家掉线
									List<Player> players =  RedisUtil.getPlayerList(room); 
									MessageFunctions.interface_100109(players, status,outs,2);
								}
							}
						}
					}
					//更新用户心跳时间
					RedisUtil.hset(Cnst.REDIS_HEART_PREFIX, userIdStr,String.valueOf(ct), null);
				}else{
					session.close(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				session.close(true);
			}
		}
	}

	/**
	 * 强制解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void disRoomForce(IoSession session, Map<String,Object> readData)
			throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		System.out.println("*******强制解散房间" + roomId);
		if (roomId != null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String
					.valueOf(roomId));
			if (room != null) {
				MessageFunctions.updateDatabasePlayRecord(room);				
				room.setState(Cnst.ROOM_STATE_YJS);
				List<Player> players = RedisUtil.getPlayerList(room);

				RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP
						.concat(String.valueOf(roomId)));// 删除房间
				if (players != null && players.size() > 0) {
					for (Player p : players) {
						p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0, 0, 0, 0, 0,
								0, 0, 0);
						RedisUtil.updateRedisData(null, p);
					}
					for (Player p : players) {
						IoSession se = MinaServerManager.tcpServer
								.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							Map<String,Object> readDatas = new HashMap<String, Object>();
							readDatas.put("interfaceId", 100100);
							readDatas.put("openId", p.getOpenId());
							readDatas.put("cId", "7");
							MessageFunctions.interface_100100(se,readDatas);
						}
					}
				}

			} else {
				System.out.println("*******强制解散房间" + roomId + "，房间不存在");
			}
		}

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

	}
//
//	/**
//	 * 在线房间列表
//	 * 
//	 * @param session
//	 * @param readData
//	 */
//	public static void onLineRooms(IoSession session, ProtocolData readData) {
//		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
//		Integer interfaceId = obj.getInteger("interfaceId");
//		Integer roomId = obj.getInteger("roomSn");
//		Integer page = obj.getInteger("page");
//		Map<String, Object> infos = new HashMap<String, Object>();
//		List<Map<String, Object>> rooms = new ArrayList<>();
//		int pages = 0;// 总页数
//		try {
//			if (roomId == null || roomId.equals("")) {
//				Set<String> keys = TCPGameFunctions
//						.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
//				if (keys != null && keys.size() > 0) {
//					pages = keys.size() % 10 == 0 ? keys.size() / 10 : keys
//							.size() / 10 + 1;
//					int startNum = 0;
//					int endNum = 0;
//					if (page == 1) {
//						startNum = 1;
//						endNum = 10;
//					} else {
//						startNum = 10 * (page - 1) + 1;
//						endNum = 10 * page;
//					}
//
//					int num = 0;
//					for (String key : keys) {
//						num++;
//						if (num > endNum) {
//							break;
//						}
//						if (num >= startNum && num <= endNum) {
//							Map<String, Object> oneRoom = new HashMap<String, Object>();
//							RoomResp room = TCPGameFunctions
//									.getRoomRespByRoomId(key.replace(
//											Cnst.REDIS_PREFIX_ROOMMAP, ""));
//							List<Player> players = new ArrayList<Player>();
//							if (room != null) {
//								Long[] pids = room.getPlayerIds();
//								if (pids != null && pids.length > 0) {
//									for (Long pid : pids) {
//										Player p = TCPGameFunctions
//												.getPlayerByUserId(String
//														.valueOf(pid));
//										if (p != null) {
//											players.add(p);
//										}
//									}
//								}
//							}
//							oneRoom.put("roomInfo", room);
//							oneRoom.put("playersInfo", players);
//							rooms.add(oneRoom);
//						}
//					}
//				}
//			} else {
//				pages = 1;
//				Map<String, Object> oneRoom = new HashMap<String, Object>();
//				RoomResp room = TCPGameFunctions.getRoomRespByRoomId(String
//						.valueOf(roomId));
//				List<Player> players = new ArrayList<Player>();
//				if (room != null) {
//					Long[] pids = room.getPlayerIds();
//					if (pids != null && pids.length > 0) {
//						for (Long pid : pids) {
//							Player p = TCPGameFunctions
//									.getPlayerByUserId(String.valueOf(pid));
//							if (p != null) {
//								players.add(p);
//							}
//						}
//					}
//				}
//				oneRoom.put("roomInfo", room);
//				oneRoom.put("playersInfo", players);
//				rooms.add(oneRoom);
//			}
//
//		} catch (Exception e) {
//
//		}
//		infos.put("pages", pages);
//		infos.put("rooms", rooms);
//		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, infos);
//		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
//		session.write(pd);
//	}
//
//	/**
//	 * 在线房间数以及在线人数
//	 * 
//	 * @param session
//	 * @param readData
//	 */
//	public static void onLineNum(IoSession session, ProtocolData readData) {
//		JSONObject obj = JSONObject.parseObject(readData.getJsonString());
//		Integer interfaceId = obj.getInteger("interfaceId");
//		ScheduledTask.cleanUserEveryHour();// 执行清理
//		Map<Long, IoSession> maps = MinaServerManager.tcpServer.getSessions();
//		Map<String, Object> info = new HashMap<>();
//		if (maps != null) {
//			info.put("userNum", maps.size());
//		}
//		Set<String> keys = TCPGameFunctions
//				.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
//		if (keys != null) {
//			info.put("roomNum", keys.size());
//		}
//
//		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
//		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
//		session.write(pd);
//	}

	public static void beneficiate(IoSession s, int protocol_num) {
		log.I("s.getCurrentWriteRequest() --> " + s.getFilterChain());
		log.I("s.getRemoteAddress() --> " + s.getRemoteAddress());
		log.I("s.getServiceAddress() --> " + s.getServiceAddress());
		log.I("请 求 进 来 :" + "\n\tinterfaceId -> [ " + protocol_num + " ]");
	}
}
