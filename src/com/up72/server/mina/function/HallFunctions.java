package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.SystemMessage;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/8.
 * 大厅方法类
 */
public class HallFunctions extends TCPGameFunctions{


    /**
     * 大厅查询战绩
     * @param session
     * @param readData
     */
    public static void interface_100002(IoSession session, Map<String,Object> readData){
    	logger.I("大厅查询战绩,interfaceId -> 100002");
        Integer interfaceId = Integer.parseInt(String.valueOf(readData.get("interfaceId")));
        String userId = String.valueOf( readData.get("userId"));
        Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
        String userKey = Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat(userId);
       
        
        Long pageSize = RedisUtil.llen(userKey); 
        int start = (page-1)*Cnst.PAGE_SIZE;
        int end = start + Cnst.PAGE_SIZE -1;
        List<String> keys = RedisUtil.lrange(userKey, start, end);
        JSONObject info = new JSONObject();
        List<Map<String,String>> maps = new ArrayList<Map<String,String>>();
        for(String roomKey:keys){
        	Map<String,String> roomInfos = RedisUtil.hgetAll(Cnst.REDIS_PLAY_RECORD_PREFIX.concat(roomKey));
        	maps.add(roomInfos);
        }
        info.put("infos", maps);
        info.put("pages",pageSize==null?0:pageSize%Cnst.PAGE_SIZE==0?pageSize/Cnst.PAGE_SIZE:(pageSize/Cnst.PAGE_SIZE+1));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 大厅查询系统消息
     * @param session
     * @param readData
     */
    public static void interface_100003(IoSession session, Map<String,Object> readData){
    	logger.I("大厅查询系统消息,interfaceId -> 100003");
    	Integer interfaceId = Integer.parseInt(String.valueOf(readData.get("interfaceId")));
        Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
        List<SystemMessage> info = userService.getSystemMessage(null,(page-1)*Cnst.PAGE_SIZE,Cnst.PAGE_SIZE);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 大厅请求联系我们
     * @param session
     * @param readData
     */
    public static void interface_100004(IoSession session, Map<String,Object> readData){
    	 logger.I("大厅请求联系我们,interfaceId -> 100004");
         Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
         Map<String,String> info = new HashMap<>();
         info.put("connectionInfo",userService.getConectUs());
         JSONObject result = getJSONObj(interfaceId,1,info);
         ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
         session.write(pd);
    }


    /**
     * 大厅请求帮助信息
     * @param session
     * @param readData
     */
    public static void interface_100005(IoSession session, Map<String,Object> readData){
        logger.I("大厅请求帮助信息,interfaceId -> 100005");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Map<String,String> info = new HashMap<>();
        info.put("help","帮助帮助帮助帮助帮助帮助帮助帮助帮助");
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }

    /**
     * 反馈信息
     * @param session
     * @param readData
     */
    public static void interface_100006(IoSession session, Map<String,Object> readData){
        logger.I("反馈信息,interfaceId -> 100006");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Long userId = StringUtils.parseLong(readData.get("userId"));
        String content = String.valueOf(readData.get("content"));
        String tel = String.valueOf(readData.get("tel"));
        //插入反馈信息
        Feedback back = new Feedback();
        back.setContent(content);
        back.setCreateTime(new Date().getTime());
        back.setTel(tel);
        back.setUserId(userId);
        userService.userFeedback(back);
        //返回反馈信息
        Map<String,String> info = new HashMap<>();
        info.put("content","感谢您的反馈！");
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }


    /**
     * 创建房间-经典玩法
     * @param session
     * @param readData
     */
    public synchronized static void interface_100007(IoSession session, Map<String,Object> readData){
        logger.I("创建房间,interfaceId -> 100007");
        
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Long userId = StringUtils.parseLong(readData.get("userId"));
        Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));
        Integer roomType= StringUtils.parseInt(readData.get("roomType"));
        Integer type = StringUtils.parseInt(readData.get("type"));
        Integer xuanNum = null;
        Integer chuaiCircle = null;
        Integer chengChu = StringUtils.parseInt(readData.get("chengChu"));
        Integer shuangShun = StringUtils.parseInt(readData.get("shuangShun"));
        Integer A23 = StringUtils.parseInt(readData.get("A23"));
        Integer gongDan = StringUtils.parseInt(readData.get("gongDan"));
        Integer tiShi = StringUtils.parseInt(readData.get("tiShi"));
        if(type != Cnst.ROOM_PALYTYPE_AN){
        	chuaiCircle = StringUtils.parseInt(readData.get("chuaiCircle"));
        	xuanNum = StringUtils.parseInt(readData.get("xuanNum"));
        }
        
        Player p = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
       
        
        if(p.getMoney()<Cnst.moneyMap.get(circleNum)){//玩家房卡不足
            playerMoneyNotEnough(interfaceId,session,roomType);
            return ;
        }
        if (p.getRoomId()!=null) {//已存在其他房间
			playerExistOtherRoom(interfaceId,session);
			return ;
		}
        
        if (roomType!=null&&roomType.equals(Cnst.ROOM_TYPE_2)) {//自由模式开房，玩家房卡必须大于等于100
			if (p.getMoney()<100) {
				playerMoneyNotEnough(interfaceId,session,roomType);
	            return ;
			}
		}
        
        if (roomType!=null&&roomType.equals(Cnst.ROOM_TYPE_2)) {
            Set<String> roomMapKeys = RedisUtil.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
            int num = 0;
            if (roomMapKeys!=null&&roomMapKeys.size()>0) {
            	for(String roomId:roomMapKeys){
            		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
            		if (room.getCreateId().equals(userId)&&room.getRoomType()==roomType&&room.getState()!=Cnst.ROOM_STATE_YJS) {
						num++;
					}
            	}
            	if (num>=10) {
					roomEnough(interfaceId, session);
					return;
				}
            }
        }
        
        RoomResp room = new RoomResp();
        
        String createTime = String.valueOf(new Date().getTime());
        room.setCreateId(userId);
        room.setState(Cnst.ROOM_STATE_CREATED);
        room.setCircleNum(circleNum);
        room.setTotalNum(0);
        room.setLastNum(circleNum);
        room.setRoomType(roomType);
        room.setCreateTime(createTime);
        room.setOpenName(p.getUserName());
        room.setType(type);
        room.setA23(A23);
        room.setXuanNum(xuanNum);
        room.setChuaiCircle(chuaiCircle);
        room.setChengChu(chengChu);
        room.setShuangShun(shuangShun);
        room.setGongDan(gongDan);
        room.setTiShi(tiShi);
        room.setDiFen(1);
        room.setHasEndPlayerIds(new ArrayList<Long>());
        room.setChuType(0);//默认为0
        //初始化大接口的id
    	room.setWsw_sole_action_id(1);
    	room.setWsw_sole_main_id(1);
		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(6));// 设置随机房间密码
			if (RedisUtil.getRoomRespByRoomId(String.valueOf(room.getRoomId())) == null) {
				break;
			}
		}
      
        
        Long[] userIds = new Long[4];
        
        Map<String,Object> info = new HashMap<>();
        Map<String,Object> userInfos = new HashMap<String, Object>();
        //处理开房模式
        if (roomType==null) {
        	illegalRequest(interfaceId, session);
		}else if(roomType.equals(Cnst.ROOM_TYPE_1)){//房主模式
	        //设置用户信息
	        p.setPosition(getWind(null));//设置庄家位置为东
	        p.setPlayStatus(Cnst.PLAYER_STATE_IN);//进入房间状态
	        p.setRoomId(room.getRoomId());
	        p.setJoinIndex(1);
	        //初始化 用户
	        
	        p.initPlayer(p.getRoomId(),Cnst.PLAYER_STATE_IN,0,0,0,0,0,0,0,0);
	        
	        userIds[p.getPosition()-1] = p.getUserId();
	        info.put("reqState",Cnst.REQ_STATE_1);
	        info.put("playerNum",1);
	        p.setMoney(p.getMoney()-Cnst.moneyMap.get(room.getCircleNum()));
	        info.put("reqState",Cnst.REQ_STATE_1);
	        userInfos.put("playerNum",1);
	        userInfos.put("money", p.getMoney());
	        userInfos.put("playStatus",String.valueOf(Cnst.PLAYER_STATE_IN));
	        userInfos.put("position", p.getPosition());
		}else if(roomType.equals(Cnst.ROOM_TYPE_2)){//自由模式
			//突然发现什么都不需要处理……
	        p.setMoney(p.getMoney()-Cnst.moneyMap.get(room.getCircleNum()));
	        info.put("reqState",Cnst.REQ_STATE_10);
	        userInfos.put("money",p.getMoney());
	        userInfos.put("playerNum",0);
	        userInfos.put("playStatus",String.valueOf(Cnst.PLAYER_STATE_DATING));
		}else if(roomType.equals(Cnst.ROOM_TYPE_3)){//AA
	        comingSoon(interfaceId, session);
	        return;
		}else{
        	illegalRequest(interfaceId, session);
        	return ;
		}
        room.setPlayerIds(userIds);
        room.setIp(Cnst.SERVER_IP);
             
        info.put("userInfos", userInfos);
        //直接把传来的readData处理 返回
        readData.put("roomId", room.getRoomId());
        readData.put("state", room.getState());
        readData.put("userId", userId);
        readData.put("lastNum", room.getLastNum());
        readData.put("totalNum", room.getCircleNum());
        readData.remove("interfaceId");
        info.put("roomInfos", readData);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        
        //更新redis数据 player roomMap
        RedisUtil.updateRedisData(null, p);
        RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(room.getRoomId())), room, Cnst.ROOM_LIFE_TIME_CREAT);
                
      //解散房间超时任务开启
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_1);
        
    }

    /**
     * 加入房间
     * @param session
     * @param readData
     */
    public synchronized static void interface_100008(IoSession session, Map<String,Object> readData) {
   		logger.I("加入房间,interfaceId -> 100008");
   		
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Long userId = StringUtils.parseLong(readData.get("userId"));
        Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

        Player p = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
        
        
       
        //已经在其他房间里
        if (p.getRoomId()!=null){//玩家已经在非当前请求进入的其他房间里
            playerExistOtherRoom(interfaceId,session);
            return;
        }
        //房间不存在
        RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
        if (room==null||room.getState() == Cnst.ROOM_STATE_YJS) {
        	roomDoesNotExist(interfaceId,session);
            return;
		}
        
        //房间人满
        Long[] userIds = room.getPlayerIds();
        boolean hasNull = false;
        int jionIndex = 0;
        for(Long uId:userIds){
        	if (uId==null) {
				hasNull = true;
			}else{
				jionIndex++;
			}
        }
        if (!hasNull) {
        	roomFully(interfaceId,session);
            return;
		}
        
        //验证ip是否一致
        if (!Cnst.SERVER_IP.equals(room.getIp())) {
            Map<String,Object> info = new HashMap<>();
            info.put("reqState",Cnst.REQ_STATE_14);
            info.put("roomSn",roomId);
            info.put("roomIp",room.getIp().concat(":").concat(Cnst.MINA_PORT));
            JSONObject result = getJSONObj(interfaceId,1,info);
            ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
            session.write(pd);
            return;
		}
        
        
        //设置用户信息
        p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);//准备状态
        p.setRoomId(roomId);
        p.setPosition(getWind(userIds));
        userIds[p.getPosition()-1] = p.getUserId();
        //初始化用户
        p.initPlayer(p.getRoomId(),Cnst.PLAYER_STATE_IN,0,0,0,0,0,0,0,0);
        
        p.setJoinIndex(jionIndex+1);
  
     

        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
        info.put("playerNum",jionIndex+1);
        info.put("roomSn",roomId);
        info.put("ip",room.getIp().concat(":").concat(Cnst.MINA_PORT));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
       
        //更新redis数据
        RedisUtil.updateRedisData(room, p);
        

        //通知另外几个人        
        for(Long ids:userIds){
        	if(ids == null){
        		continue;
        	}
        	if(ids.equals(userId)){
        		continue;
        	}
        	Map<String,Object> userInfos = new HashMap<String, Object>();
        	userInfos.put("userId", p.getUserId());
        	userInfos.put("position", p.getPosition());
        	userInfos.put("score", p.getScore());
        	userInfos.put("money", p.getMoney());
        	userInfos.put("playStatus", p.getPlayStatus());
        	userInfos.put("userName", p.getUserName());
        	userInfos.put("userImg", p.getUserImg());
        	userInfos.put("ip", p.getIp());
        	userInfos.put("joinIndex", p.getJoinIndex());
        	userInfos.put("gender", p.getGender());
        	Player pp = RedisUtil.getPlayerByUserId(String.valueOf(ids));
        	IoSession se = MinaServerManager.tcpServer.getSessions()
    				.get(pp.getSessionId());
    		if (se!=null&&se.isConnected()) {
    			JSONObject result1 = getJSONObj(interfaceId,1,userInfos);
        	    ProtocolData pd1 = new ProtocolData(interfaceId, result1.toJSONString());
				se.write(pd1);
			}
        }
        
        //如果加入的代开房间 通知房主
        if(room.getRoomType() == Cnst.ROOM_TYPE_2 && !userId.equals(room.getCreateId())){
        	MessageFunctions.interface_100112(p,room);
        }
    }
    
    /**
     * 用户点击同意协议
     * @param session
     * @param readData
     */
    public static void interface_100009(IoSession session, Map<String,Object> readData) throws Exception{
        logger.I("用户点击同意协议,interfaceId -> 100009");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Player p = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
        if (p==null){
            illegalRequest(interfaceId,session);
            return;
        }
        p.setUserAgree(1);
        Map<String,Object> info = new JSONObject();
        info.put("reqState",Cnst.REQ_STATE_1);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);

        //更新redis数据
        RedisUtil.updateRedisData(null, p);

        /*刷新数据库，用户同意协议*/
        userService.updateUserAgree(p.getUserId());
    }
    
    
    /**
     * 查看代开房间列表
     * @param session
     * @param readData
     */
    public static void interface_100010(IoSession session, Map<String,Object> readData) throws Exception{
        logger.I("查看代开房间列表,interfaceId -> 100010");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Long userId = StringUtils.parseLong(readData.get("userId"));
        List<Map<String,Object>> info = new ArrayList<Map<String,Object>>();
        
        Set<String> roomMapKeys = RedisUtil.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
        if (roomMapKeys!=null&&roomMapKeys.size()>0) {
        	for(String roomId:roomMapKeys){
        		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
            	if (room.getCreateId().equals(userId)&&room.getState() != Cnst.ROOM_STATE_YJS && room.getRoomType() == Cnst.ROOM_TYPE_2) {
            		Map<String,Object> map = new HashMap<String, Object>();
            		map.put("roomId", room.getRoomId());
            		map.put("createTime", room.getCreateTime());
            		map.put("circleNum", room.getCircleNum());
            		map.put("lastNum", room.getLastNum());
            	
            		map.put("state", room.getState());

            		map.put("type", room.getType());
            		map.put("xuanNum", room.getXuanNum());
            		map.put("chuaiCircle", room.getChuaiCircle());
            		map.put("chengChu", room.getChengChu());
            		map.put("shuangShun", room.getShuangShun());
            		map.put("A23", room.getA23());
            		map.put("gongDan", room.getGongDan());
            		map.put("tiShi", room.getTiShi());

                    List<Map<String,Object>> playerInfo = new ArrayList<Map<String,Object>>();
            		
            		List<Player> list = RedisUtil.getPlayerList(room);
            		if (list!=null&&list.size()>0) {
						for(Player p:list){
							Map<String,Object> pinfo = new HashMap<String, Object>();
							pinfo.put("userId", p.getUserId());
							pinfo.put("position", p.getPosition());
							pinfo.put("userName", p.getUserName());
							pinfo.put("userImg", p.getUserImg());
							pinfo.put("state", p.getState());
							playerInfo.add(pinfo);
						}
					}
            		map.put("playerInfo", playerInfo);
            		info.add(map);
				}
            }
		}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    
    /**
     * 查看历史代开房间列表
     * @param session
     * @param readData
     */
    public static void interface_100011(IoSession session, Map<String,Object> readData) throws Exception{
        logger.I("查看历史代开房间列表,interfaceId -> 100011");
        Integer interfaceId = StringUtils.parseInt( readData.get("interfaceId"));
        String userId = String.valueOf( readData.get("userId"));
        Integer page = StringUtils.parseInt(readData.get("page"));
        String key = Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat(userId);
        
        Long pageSize = RedisUtil.llen(key); 
        int start = (page-1)*Cnst.PAGE_SIZE;
        int end = start + Cnst.PAGE_SIZE -1;
        List<String> keys = RedisUtil.lrange(key, start, end);
        
        Map<String,Object> info = new HashMap<>();
        List<Map<String,String>> maps = new ArrayList<Map<String,String>>();
        for(String roomKey:keys){
        	Map<String,String> roomInfos = RedisUtil.hgetAll(Cnst.REDIS_PLAY_RECORD_PREFIX.concat(roomKey));
        	maps.add(roomInfos);
        }
        info.put("roomInfo", maps);
        info.put("pages",pageSize==null?0:pageSize%Cnst.PAGE_SIZE==0?pageSize/Cnst.PAGE_SIZE:(pageSize/Cnst.PAGE_SIZE+1));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    
    
    
    
    
    /**
     * 代开模式中踢出玩家
     * @param session
     * @param readData
     */
    public static void interface_100012(IoSession session, Map<String,Object> readData){
    	logger.I("准备,interfaceId -> 100012");
    	Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
    	Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
    	Long userId = StringUtils.parseLong(readData.get("userId"));
        
    	 //房间不存在
        RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
        if (room==null){
            roomDoesNotExist(interfaceId,session);
            return;
        }
        
        try {
            //验证解散人是否是真正的房主
            Long createId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
            if (createId==null||!createId.equals(room.getCreateId())) {
    			illegalRequest(interfaceId, session);
    			return;
    		}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}
        //房间已经开局
        if (room.getState() != Cnst.ROOM_STATE_CREATED) {
        	roomIsGaming(interfaceId,session);
        	return;
		}
        
        List<Player> list = RedisUtil.getPlayerList(room);
        
        boolean hasPlayer = false;//列表中有当前玩家
        for(Player p:list){
        	if (p.getUserId().equals(userId)) {
        		//初始化玩家
        		 p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
				
		        //刷新房间用户列表
				Long[] pids = room.getPlayerIds();
		        if (pids!=null) {
					for(int i=0;i<pids.length;i++){
						if (userId.equals(pids[i])) {
							pids[i] = null;
							break;
						}
					}
				}
				
		        //更新redis数据
		        RedisUtil. updateRedisData(room, p);
		        hasPlayer = true;
		        IoSession se = MinaServerManager.tcpServer.getSessions().get(
						p.getSessionId());
				MessageFunctions.interface_100107(se, Cnst.EXIST_TYPE_EXIST,list);
				break;
			}
        }
        
        Map<String,String> info = new HashMap<String, String>();
        info.put("reqState", String.valueOf(hasPlayer?Cnst.REQ_STATE_1:Cnst.REQ_STATE_8));
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 代开模式中房主解散房间
     * @param session
     * @param readData
     */
    public static void interface_100013(IoSession session, Map<String,Object> readData){
    	logger.I("代开模式中踢出玩家,interfaceId -> 100013");
    	Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
    	Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
    	
        
    	 RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
         //房间不存在
           if (room==null){
               roomDoesNotExist(interfaceId,session);
               return;
           }
           
           try {
               //验证解散人是否是真正的房主
               Long createId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
               if (createId==null||!createId.equals(room.getCreateId())) {
       			illegalRequest(interfaceId, session);
       			return;
       		}
   		} catch (Exception e) {
   			illegalRequest(interfaceId, session);
   			return;
   		}
           
           //房间已经开局
           if (room.getState()!=Cnst.ROOM_STATE_CREATED) {
           	roomIsGaming(interfaceId,session);
           	return;
   		}
           List<Player> players = RedisUtil.getPlayerList(room);
           if (players!=null&&players.size()>0) {
   			for(Player p:players){
   				//初始化玩家
   			 p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
   			}
   			RedisUtil.setPlayersList(players);
   		}
           
           
           MessageFunctions.interface_100107(session, Cnst.EXIST_TYPE_DISSOLVE,players);
           //归还玩家房卡
           Player cp = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)));
           cp.setMoney(cp.getMoney()+Cnst.moneyMap.get(room.getCircleNum()));

           //更新房主的redis数据
           RedisUtil.updateRedisData(null, cp);

           
           RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
           
           Map<String,String> info = new HashMap<String, String>();
           info.put("reqState", String.valueOf(Cnst.REQ_STATE_1));
           info.put("money", String.valueOf(cp.getMoney()));
           JSONObject result = getJSONObj(interfaceId,1,info);
           ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
           session.write(pd);
    }
    
    /**
     * 回放的时候，获取房间的局数
     * @param session
     * @param readData
     */
    public static void interface_100014(IoSession session, Map<String,Object> readData){
    	Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
    	String roomId = StringUtils.toString((readData.get("roomSn")));
    	String createTime = StringUtils.toString(readData.get("createTime"));
        Map<String,Object> info = new HashMap<String, Object>();
        int juNum = BackFileUtil.getFileNumByRoomId(Integer.parseInt(roomId));
        info.put("num", juNum);
        info.put("url", Cnst.HTTP_URL.concat(Cnst.BACK_FILE_PATH));
        info.put("roomSn", String.valueOf(roomId));
        info.put("createTime", createTime);       
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    /**
     * 强制解散房间
     * @param session
     * @param readData
     * @throws Exception
     */
    public static void interface_100015(IoSession session,Map<String,Object> readData) throws Exception{
    	Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
    	Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
    	System.out.println("*******强制解散房间"+roomId);
    	Long userId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
    	if (userId==null) {
			illegalRequest(interfaceId, session);
			return;
		}
		if (roomId!=null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
			if (room!=null&&room.getCreateId().equals(userId)) {
				if(room.getState() == Cnst.ROOM_STATE_GAMIING){
					//中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for(int i=0;i<4;i++){
						xiaoJSInfo.add(0);
					}
				room.addXiaoJSInfo(xiaoJSInfo);
				room.setState(Cnst.ROOM_STATE_YJS);
				List<Player> players = RedisUtil.getPlayerList(room);
				
			
				MessageFunctions.updateDatabasePlayRecord(room);
							
				RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));//删除房间
				if (players!=null&&players.size()>0) {
					for(Player p:players){
						//初始化玩家
						 p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
				        RedisUtil.updateRedisData(null, p);
					}
					for(Player p:players){
				        IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
                        if(se!=null&&se.isConnected()){
                        	Map<String,Object> data = new HashMap<String, Object>();
                        	data.put("interfaceId", 100100);
                        	data.put("openId", p.getOpenId());
                        	data.put("cId", Cnst.cid);
                            MessageFunctions.interface_100100(se, data);
                        }
			        }
				}

//		        BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
			}else{
				System.out.println("*******强制解散房间"+roomId+"，房间不存在");
			}
		}

        Map<String,Object> info = new HashMap<>();
        info.put("reqState",Cnst.REQ_STATE_1);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        }	
    }


    /**
     * 产生随机的风
     * @param players
     * @return
     */
    protected static Integer getWind(Long[] userIds){
        List<Integer> ps = new ArrayList<>();
        ps.add(Cnst.WIND_EAST);
        ps.add(Cnst.WIND_SOUTH);
        ps.add(Cnst.WIND_WEST);
        ps.add(Cnst.WIND_NORTH);
        if (userIds!=null){
            for(int i=userIds.length-1;i>=0;i--){
                if (userIds[i]!=null){
                    ps.remove(i);
                }
            }
        }
        return ps.get(CommonUtil.getRamdonInNum(ps.size()));
    }
    
    /**
     * 或得到的是一个正数，要拿当前玩家的剩余房卡，减去这个值
     * @param userId
     * @return
     */
    private static Integer getFrozenMoney(Long userId){
    	int frozenMoney = 0;
        Set<String> roomMapKeys = RedisUtil.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
    	if (roomMapKeys!=null&&roomMapKeys.size()>0) {
        	for(String roomId:roomMapKeys){
        		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
            	if (room.getCreateId().equals( userId)&&room.getState() == Cnst.ROOM_STATE_CREATED) {
            		frozenMoney += Cnst.moneyMap.get(room.getCircleNum());
				}
            }
		}
    	return frozenMoney;
    }

    /**
     * 返回用户
     * @param openId
     * @param ip
     * @return
     * @throws Exception
     */
    static Integer i = 100;
    public static Player getPlayerInfos(String openId,String ip,String cid,IoSession session){
    	if (cid==null||!cid.equals(Cnst.cid)) {
			return null;
		}
        Player p = null;
        try {
        	String notice = RedisUtil.getStringByKey(Cnst.NOTICE_KEY);
        	if (notice==null) {
        		notice = userService.getNotice();
        		RedisUtil.setObject(Cnst.NOTICE_KEY, notice, null);
//        		setStringByKey(Cnst.NOTICE_KEY, "接口都是经济");
			}
            if (RedisUtil.exists(Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId))){//用户是断线重连
            	Long userId = RedisUtil.getUserIdByOpenId(openId);
            	p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
            	IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
                p.setNotice(notice);
                p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
                if (se!=null){
                	Long tempuserId = (Long) se.getAttribute(Cnst.USER_SESSION_USER_ID);
                	if (se.getId()!=session.getId()&&userId.equals(tempuserId)) {
                		MessageFunctions.interface_100106(se);
					}
                }
                if (p.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {//去数据库重新请求用户，//需要减去玩家开的房卡
                	p = userService.getByOpenId(openId,cid);
                	if (p==null) {
                		p = userService_login.getUserInfoByOpenId(openId);
                        if (p==null){
                            return null;
                        }else{
                        	synchronized (i) {
                        		 while(true){
                                     p.setUserId(Long.valueOf(CommonUtil.getGivenRamdonNum(6)));//唯一的userId，需要去数据库检测是否存在此id
                                     Integer temp = userService.isExistUserId(p.getUserId()+"");
                                     if (temp==null){
                                         break;
                                     }
                                 }
                                 p.setUserAgree(0);
                                 p.setGender(p.getGender());
                                 p.setTotalGameNum("0");
                                 p.setMoney(Cnst.MONEY_INIT);
                                 p.setLoginStatus(1);
                                 p.setCId(cid);
                                 String time = String.valueOf(new Date().getTime());
                                 p.setLastLoginTime(time);
                                 p.setSignUpTime(time);
                                 userService.save(p);
							}                         
                        }
					}
                    p.setScore(0);
                    p.setIp(ip);
                    p.setNotice(notice);
                    p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
                    p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
                    p.setMoney(p.getMoney()-getFrozenMoney(p.getUserId()));
				}
                //更新用户ip 最后登陆时间
                userService.updateIpAndLastTime(openId,ip);
                return p;
            }
            p = userService.getByOpenId(openId,cid);
            if (p!=null){//当前游戏的数据库中存在该用户
                p.setNotice(notice);
            }else{//如果没有，需要去微信的用户里查询
                p = userService_login.getUserInfoByOpenId(openId);
                if (p==null){
                    return null;
                }else{
                    while(true){
                        p.setUserId(Long.valueOf(CommonUtil.getGivenRamdonNum(6)));//唯一的userId，需要去数据库检测是否存在此id
                        Integer temp = userService.isExistUserId(p.getUserId()+"");
                        if (temp==null){
                            break;
                        }
                    }
                    p.setUserAgree(0);
                    p.setGender(p.getGender());
                    p.setTotalGameNum("0");
                    p.setMoney(Cnst.MONEY_INIT);
                    p.setLoginStatus(1);
                    p.setCId(cid);
                    String time = String.valueOf(new Date().getTime());
                    p.setLastLoginTime(time);
                    p.setSignUpTime(time);
                    userService.save(p);
                }
            }
            p.setScore(0);
            p.setIp(ip);
            p.setNotice(notice);
            p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
            p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
            p.setMoney(p.getMoney()-getFrozenMoney(p.getUserId()));
        }catch (Exception e){
            e.printStackTrace();
        }
        //更新用户ip 最后登陆时间
        userService.updateIpAndLastTime(openId, ip);
        return p;
    }


}
