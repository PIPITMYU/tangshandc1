package com.up72.server.mina.function;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.service.IRoomService;
import com.up72.game.service.IUserService;
import com.up72.game.service.IUserService_login;
import com.up72.game.service.impl.RoomServiceImpl;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.game.service.impl.UserService_loginImpl;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.PostUtil;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.TaskUtil.DissolveRoomTask;
import com.up72.server.mina.utils.redis.RedisUtil;

public class TCPGameFunctions {

    public static final MyLog logger = MyLog.getLogger(TCPGameFunctions.class);
    public static IUserService userService = new UserServiceImpl();
    public static IUserService_login userService_login = new UserService_loginImpl();
    public static IRoomService roomService = new RoomServiceImpl();

    //由于需要线程notify，需要保存线程的锁，所以保留这两个静态变量
    //独立id，对应相对的任务，无论什么type的任务，id是唯一的
    public static ConcurrentHashMap<Integer, TaskUtil.DissolveRoomTask> disRoomIdMap = new ConcurrentHashMap<>(); //解散房间的任务
    //如果房间开局或者解散时没超过5分钟就有结果了，才会向这个集合里放数据，数据格式为id--1
    public static ConcurrentHashMap<Integer, Integer> disRoomIdResultInfo = new ConcurrentHashMap<>(); //房间解散状态集合

    public static ExecutorService taskExecuter = Executors.newFixedThreadPool(200);
    
    /**
     * 获取统一格式的返回obj
     * @param interfaceId
     * @param state
     * @param object
     * @return
     */
    public static JSONObject getJSONObj(Integer interfaceId,Integer state,Object object){
        JSONObject obj = new JSONObject();
        obj.put("interfaceId",interfaceId);
        obj.put("state",state);
        obj.put("message","");
        obj.put("info",object);
        obj.put("others","");
        obj = getNewObj(obj);
        return obj;
    }
    
    //路由转换
    public static JSONObject getNewObj(JSONObject temp){
    	Iterator<String> iterator = temp.keySet().iterator();
    	JSONObject result = new JSONObject();
    	while(iterator.hasNext()) {  
            String str = iterator.next();  
            Object o = temp.get(str);
			if (o instanceof List) {
				result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str), getNewList(o));
			}else if(o instanceof Map){
				result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str), getNewMap(o));
			}else{
				result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str), o);
			}
    	}
    	return result;
    }
    
    public static List getNewList(Object list){
    	List<Object> temp1 = (List<Object>) list;
    	List<Object> temp = new ArrayList<Object>(temp1);
    	if (temp!=null&&temp.size()>0) {
    		for(int i=0;i<temp.size();i++) {  
    			Object o = temp.get(i);
    			if (o instanceof List) {
    				temp.set(i, getNewList(o));
    			}else if(o instanceof Map){//基本上全是这个类型
    				temp.set(i, getNewMap(o));
    			}else{//默认为String
    				try {
    					JSONObject obj = JSONObject.parseObject(o.toString());
        				temp.set(i, getNewObj(obj));
					} catch (Exception e) {
//						e.printStackTrace();
					}
    			}
        	}
		}
    	return temp;
    }
    
    public static Map getNewMap(Object map){
    	Map<String,Object> temp1 = (Map<String, Object>) map;
    	Map<String,Object> temp = new HashMap<String, Object>(temp1);
    	Map<String,Object> result = new ConcurrentHashMap<String, Object>();
    	if (temp!=null&&temp.size()>0) {
    		Iterator<String> iterator = temp.keySet().iterator();
    		while(iterator.hasNext()) {  
    			String str = String.valueOf(iterator.next());  
	            Object o = temp.get(str);
				if (o instanceof List) {
					result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str),getNewList(o));
				}else if(o instanceof Map){
					result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str),getNewMap(o));
				}else{
					try {
						try {
	    					JSONObject obj = JSONObject.parseObject(o.toString());
	    					result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str), getNewObj(obj));
						} catch (Exception e) {
	    					result.put(Cnst.ROUTE_MAP.get(str)==null?str:Cnst.ROUTE_MAP.get(str), o);
						}
					} catch (Exception e) {
						
					}
				}
        	}
		}
    	return result;
    }
    //转换完成
    
    


    /**
     * 房间不存在
     * @param interfaceId
     * @param session
     */
    public static void roomDoesNotExist(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState", Cnst.REQ_STATE_4);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 玩家在其他房间
     * @param interfaceId
     * @param session
     */
    public static void playerExistOtherRoom(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_3);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 房间已满
     * @param interfaceId
     * @param session
     */
    public static void roomFully(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_5);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 玩家房卡不足
     * @param interfaceId
     * @param session
     */
    public static void playerMoneyNotEnough(Integer interfaceId,IoSession session,Integer roomType){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_2);//余额不足，请及时充值
        info.put("roomType",roomType);//余额不足，请及时充值
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 代开房间不能超过10个
     * @param interfaceId
     * @param session
     */
    public static void roomEnough(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_11);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 非法请求
     * @param session
     * @param interfaceId
     */
    public static void illegalRequest(Integer interfaceId,IoSession session){
    	 Map<String,Object> info = new HashMap<>();
    	 JSONObject result = getJSONObj(interfaceId,0,info);
        result.put("c","非法请求！");
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        session.close(true);
    }
    
    /**
     * 参数错误
     */
    public static void parameterError(Integer interfaceId,IoSession session){
    	 Map<String,Object> info = new HashMap<>();
    	 JSONObject result = getJSONObj(interfaceId,0,info);
        result.put("c","参数错误！");
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        session.close(true);
    }
    
    
    /**
     * 敬请期待
     * @param interfaceId
     * @param session
     */
    public static void comingSoon(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_FUYI);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);}

    /**
     * 游戏中，不能退出房间
     * @param interfaceId
     * @param session
     */
    public static void roomIsGaming(Integer interfaceId,IoSession session){
        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_6);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    //像数据库添加房间信息
  	 public static void addRoomToDB(RoomResp room){
  	    	
		Integer circle = room.getCircleNum();
		Long createId = room.getCreateId();
		Integer roomType = room.getRoomType();
		Long[] playerIds = room.getPlayerIds();
		
		taskExecuter.execute(new Runnable() {
			public void run() {
				// 扣除房主房卡
				userService.updateMoney(userService.getUserMoneyByUserId(createId)
						- Cnst.moneyMap.get(circle), createId+"");
			}
		});
		// 添加玩家消费记录
		PlayerMoneyRecord mr = new PlayerMoneyRecord();
		mr.setUserId(createId);
		mr.setMoney(Cnst.moneyMap.get(circle));
		mr.setType(100);
		mr.setCreateTime(new Date().getTime());
		

		/* 向数据库添加房间信息 */
		Map<String,String> roomSave = new HashMap<String, String>();
		roomSave.put("userId1", String.valueOf(playerIds[0]));
		roomSave.put("userId2", String.valueOf(playerIds[1]));
		roomSave.put("userId3", String.valueOf(playerIds[2]));
		roomSave.put("userId4", String.valueOf(playerIds[3]));
		roomSave.put("isPlaying", "1");
		roomSave.put("roomId", String.valueOf(room.getRoomId()));
		roomSave.put("createId", String.valueOf(room.getCreateId()));
		roomSave.put("createTime", String.valueOf(room.getCreateTime()));
		roomSave.put("roomType", String.valueOf(room.getRoomType()));
		roomSave.put("circleNum", String.valueOf(room.getCircleNum()));
		roomSave.put("type", String.valueOf(room.getType()));
		roomSave.put("chengChu", String.valueOf(room.getChengChu()));
		roomSave.put("shuangShun", String.valueOf(room.getShuangShun()));
		roomSave.put("A23", String.valueOf(room.getA23()));
		roomSave.put("gongDan",String.valueOf( room.getGongDan()));
		roomSave.put("ip", room.getIp());
		roomSave.put("XiaoJuNum",String.valueOf( room.getXiaoJuNum()));
		roomSave.put("tiShi", String.valueOf(room.getTiShi()));
		taskExecuter.execute(new Runnable() {
			public void run() {
				userService.insertPlayerMoneyRecord(mr);//消费记录
				roomService.save(roomSave);//房间信息
			}
		});

		// 统计消费
		taskExecuter.execute(new Runnable() {
			@Override
			public void run() {
				try {
					PostUtil.doCount(createId, Cnst.moneyMap.get(circle),
							roomType);
				} catch (Exception e) {
					System.out.println("调用统计借口出错");
					e.printStackTrace();
				}
			}
		});
  	    }
  	/**
  	 *  向数据库添加玩家分数信息
  	 */
  	public static void updateDatabasePlayRecord(RoomResp room) {
  		if (room == null)
  			return;
  		
  		// 刷新数据库
  		taskExecuter.execute(new Runnable() {		
			public void run() {
				roomService.updateRoomState(room.getRoomId(),room.getXiaoJuNum());
			}
		});
  		
  		//判断totalNum 在小结算时+1
  		Integer roomType = room.getRoomType();
  		Map<String,String> roomSave = new HashMap<String,String>();
  		if(room.getXiaoJuNum() != null && room.getXiaoJuNum() != 0){
  			Long[] playerIds = room.getPlayerIds();
  			
  			roomSave.put("endTime", String.valueOf(System.currentTimeMillis()));
  			List<Player> players = RedisUtil.getPlayerList(room);
  			
  			List<Map> redisRecord = new ArrayList<Map>();
  			for(Player p:players){
  				Map<String,Object> map = new HashMap<String, Object>();
  				map.put("userId", p.getUserId());
  				map.put("finalScore", p.getScore());
  				map.put("chuNum", p.getChuNum());
  				map.put("duChuNum", p.getDuChuNum());
  				map.put("quanJ", p.getQuanJ());
  				map.put("banJ", p.getBanJ());
  				map.put("heJ", p.getHeJ());
  				map.put("bBanJ", p.getbBanJ());
  				map.put("bQuanJ", p.getbQuanJ());
  				map.put("position", p.getPosition());
//  			map.put("userName", p.getUserName());
//  			map.put("userImg", p.getUserImg());
  				redisRecord.add(map);
  			}
  			//写入回放
  			BackFileUtil.write(null, 100103, room, null, getNewList(redisRecord));
  			//setOverInfo 信息 大结算时 调用
  			String key = room.getRoomId()+"-"+room.getCreateTime();
  			RedisUtil.setObject(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(key), redisRecord,Cnst.OVERINFO_LIFE_TIME_COMMON );
  			
  			String userId1 = String.valueOf(playerIds[0]);
  			String score = String.valueOf(players.get(0).getScore());
  			roomSave.put("eastUserId", userId1);
  			roomSave.put("eastUserName", players.get(0).getUserName());
  			roomSave.put("eastUserMoneyRecord", score);
  			roomSave.put("eastUserMoneyRemain", score);
  			String userId2 = String.valueOf(playerIds[1]);
  			score = String.valueOf(players.get(1).getScore());
  			roomSave.put("southUserId", userId2);
  			roomSave.put("southUserName", players.get(1).getUserName());
  			roomSave.put("southUserMoneyRecord", score);
  			roomSave.put("southUserMoneyRemain", score);
  			String userId3 = String.valueOf(playerIds[2]);
  			score = String.valueOf(players.get(2).getScore());
  			roomSave.put("westUserId", userId3);
  			roomSave.put("westUserName", players.get(2).getUserName());
  			roomSave.put("westUserMoneyRecord", score);
  			roomSave.put("westUserMoneyRemain", score);
  			String userId4 = String.valueOf(playerIds[3]);
  			score = String.valueOf(players.get(3).getScore());
  			roomSave.put("northUserId", userId4);
  			roomSave.put("northUserName", players.get(3).getUserName());
  			roomSave.put("northUserMoneyRecord", score);
  			roomSave.put("northUserMoneyRemain", score);
  			roomSave.put("roomId", String.valueOf(room.getRoomId()));
  			roomSave.put("createTime", room.getCreateTime());
  			roomSave.put("endTime", String.valueOf(System.currentTimeMillis()));
  			roomSave.put("circleNum", String.valueOf(room.getCircleNum()));
  			roomSave.put("lastNum",String.valueOf( room.getLastNum()));
  			roomSave.put("state", String.valueOf(room.getState()));
            roomSave.put("type", String.valueOf(room.getType()));
            roomSave.put("xuanNum", String.valueOf(room.getXuanNum()));
            roomSave.put("chuaiCircle", String.valueOf(room.getChuaiCircle()));
            roomSave.put("chengChu", String.valueOf(room.getChengChu()));
            roomSave.put("shuangShun", String.valueOf(room.getShuangShun()));
            roomSave.put("A23", String.valueOf(room.getA23()));
            roomSave.put("gongDan", String.valueOf(room.getGongDan()));
            roomSave.put("tiShi", String.valueOf(room.getTiShi()));
            roomSave.put("XiaoJuNum", String.valueOf(room.getXiaoJuNum()));
            //小局结算信息 回放用
            roomSave.put("xiaoJuInfo", JSONObject.toJSONString(room.getXiaoJSInfo()));
			String fineName = new StringBuffer().append("http://").append(room.getIp()).append(":8086/")
					.append(Cnst.BACK_FILE_PATH).toString();
            roomSave.put("backUrl", fineName);
  			//更新redis 缓存
  			RedisUtil.hmset(Cnst.REDIS_PLAY_RECORD_PREFIX.concat(key), roomSave, Cnst.PLAYOVER_LIFEE_TIME);
  			haveRedisRecord(userId1,key);
  			haveRedisRecord(userId2,key);
  			haveRedisRecord(userId3,key);
  			haveRedisRecord(userId4,key);
  			if(roomType!=null&&roomType == Cnst.ROOM_TYPE_2){
  				//代开模式
  				String key1 = Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat(String.valueOf(room.getCreateId()));	  	
  		  		RedisUtil.lpush(key1, null, key);  		  		
  			}
  			

  		}else{
  			return;
  		}
  		 		
		taskExecuter.execute(new Runnable() {
			public void run() {
				userService.insertPlayRecord(roomSave);
			}
		});
  	}
  	public static void haveRedisRecord(String userId,String value){
  		String key = Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat(userId);
  		RedisUtil.lpush(key, null, value); 		
  	}
    /**
     * 开启等待解散房间任务
     * @param roomId
     * @param type
     */
    public static void startDisRoomTask(int roomId,int type){
        RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
        Integer createDisId = null;
        while(true){
            createDisId = CommonUtil.getGivenRamdonNum(8);
            if (!disRoomIdMap.containsKey(createDisId)) {
				break;
			}
        }
        if (type==Cnst.DIS_ROOM_TYPE_1){
            room.setCreateDisId(createDisId);
        }else if(type==Cnst.DIS_ROOM_TYPE_2){
            room.setApplyDisId(createDisId);
        }
        TaskUtil.DissolveRoomTask task = new TaskUtil().new DissolveRoomTask(roomId,type,createDisId);
        disRoomIdMap.put(createDisId, task);
        RedisUtil.updateRedisData(room, null);
        new Thread(task).start();
    }

    /**
     * 关闭解散房间任务
     * @param roomId
     * @param type
     */
    public static void notifyDisRoomTask(RoomResp room,int type,boolean needAddRoomToDB){
        if (room==null) {
			return;
		}
        Integer taskId = 0;
        if (type==Cnst.DIS_ROOM_TYPE_1){
            taskId = room.getCreateDisId();
            room.setCreateDisId(null);
        }else if (type==Cnst.DIS_ROOM_TYPE_2){
            taskId = room.getApplyDisId();
            room.setApplyDisId(null);
            notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1,needAddRoomToDB);
        }
        if (taskId==null) {
			return;
		}
        TaskUtil.DissolveRoomTask task = disRoomIdMap.get(taskId);
        disRoomIdResultInfo.put(taskId, Cnst.DIS_ROOM_RESULT);
        if (task!=null) {
//        	if (type==Cnst.DIS_ROOM_TYPE_1&&needAddRoomToDB) {
//                //首先向数据库添加房间记录
//                addRoomToDB(room);
//			}
        	synchronized (task){
                task.notify();
            }
		}
    }
}