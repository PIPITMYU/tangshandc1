package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(IoSession session, Map<String,Object> readData)
			throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = String.valueOf(readData.get("openId"));
			Player currentPlayer = null;
			String cid = null;
			if (openId == null) {			
				illegalRequest(interfaceId, session);
				return;	
			} else {
				String ip = (String) session.getAttribute(Cnst.USER_SESSION_IP);
				cid = String.valueOf(readData.get("cId"));
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid,
						session);
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, session);
				return;
			}

			// 更新心跳为最新上线时间
//			RedisUtil.hset(Cnst.REDIS_HEART_PREFIX,String.valueOf(currentPlayer.getUserId()) , String.valueOf(new Date().getTime()), null);
			
			if (cid != null) {
				currentPlayer.setCId(cid);
			}
			currentPlayer.setSessionId(session.getId());// 更新sesisonId
			session.setAttribute(Cnst.USER_SESSION_USER_ID,
					currentPlayer.getUserId());
			if (openId != null) {
				RedisUtil.setObject(Cnst.REDIS_PREFIX_OPENIDUSERMAP.concat(openId),currentPlayer.getUserId() ,null);
			}

			RoomResp room = null;
			List<Player> players = null;
			
			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = RedisUtil.getRoomRespByRoomId(String.valueOf(currentPlayer
						.getRoomId()));
				if (room != null
						&& room.getState() != Cnst.ROOM_STATE_YJS) {
					info.put("roomInfo", getRoomInfo(room));
					players = RedisUtil.getPlayerList(room);
					for (int m = 0; m < players.size(); m++) {
						Player p = players.get(m);
						if (p.getUserId().equals(currentPlayer.getUserId())) {
							players.remove(m);
							break;
						}
					}

					
					info.put("anotherUsers", getAnotherUserInfo(players, room));

				} else {
					currentPlayer.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
				}

			} else {
				currentPlayer.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
			}

			RedisUtil.updateRedisData(room, currentPlayer);
			info.put("currentUser", getCurrentUserInfo(currentPlayer,room));

			if (room != null) {
				// room.setWsw_sole_main_id(room.getWsw_sole_main_id()+1);

				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info
						.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info
						.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);
			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);
			}

		}  else {
			session.close(true);
		}

	}
	
	//封装currentUser
	public static Map<String,Object> getCurrentUserInfo(Player player,RoomResp room){
		Map<String,Object> currentUserInfo = new HashMap<String, Object>();
		currentUserInfo.put("version", String.valueOf(Cnst.version));
		currentUserInfo.put("userId",player.getUserId());
		currentUserInfo.put("position", player.getPosition());
		currentUserInfo.put("playStatus", player.getPlayStatus());
		currentUserInfo.put("userName", player.getUserName());
		currentUserInfo.put("userImg", player.getUserImg());
		currentUserInfo.put("gender", player.getGender());
		currentUserInfo.put("ip", player.getIp());
		currentUserInfo.put("joinIndex", player.getJoinIndex());
		currentUserInfo.put("userAgree", player.getUserAgree());
		currentUserInfo.put("money", player.getMoney());
		currentUserInfo.put("score", player.getScore());
		currentUserInfo.put("notice", player.getNotice());
		currentUserInfo.put("endNum", player.getEndNum());
		if(room!=null && room.getPlayStatus()!= null){
			Integer playStatus = room.getPlayStatus();
			List<Integer> paiInfos = new ArrayList<Integer>();
			List<Card> currentCardList = player.getCurrentCardList();
				if(playStatus == Cnst.ROOM_PALYSTATE_MENGXUAN){
									
					Integer xuanNum = room.getXuanNum();				
					for(int i=0;i<xuanNum;i++){
						paiInfos.add(currentCardList.get(i).getOrigin());
					}				
				}
				else if(playStatus == Cnst.ROOM_PALYSTATE_MENGCHUAI){			
					Integer xuanNum = room.getXuanNum();
					if(xuanNum == 3){
						for(int i=0;i<5;i++){
							paiInfos.add(currentCardList.get(i).getOrigin());
						}	
					}
					if(xuanNum == 5){
						for(int i=0;i<8;i++){
							paiInfos.add(currentCardList.get(i).getOrigin());
						}	
					}			
				}
				else{
					for(int i=0;i<currentCardList.size();i++){
						paiInfos.add(currentCardList.get(i).getOrigin());
					}	
				}
			
			currentUserInfo.put("paiInfos", paiInfos);
			currentUserInfo.put("endNum", player.getEndNum());
		}
		return currentUserInfo;
	}
	//封装anotherUsers
	public static List<Map<String,Object>> getAnotherUserInfo(List<Player> players ,RoomResp room){
		List<Map<String,Object>> anotherUserInfos = new ArrayList<Map<String,Object>>();
		for(Player player:players){
			Map<String,Object> currentUserInfo = new HashMap<String, Object>();
			currentUserInfo.put("userId", player.getUserId());
			currentUserInfo.put("position", player.getPosition());
			currentUserInfo.put("playStatus", player.getPlayStatus());
			currentUserInfo.put("userName", player.getUserName());
			currentUserInfo.put("userImg", player.getUserImg());
			currentUserInfo.put("gender", player.getGender());
			currentUserInfo.put("ip", player.getIp());
			currentUserInfo.put("joinIndex", player.getJoinIndex());
			currentUserInfo.put("userAgree", player.getUserAgree());
			currentUserInfo.put("money", player.getMoney());
			currentUserInfo.put("score", player.getScore());
			currentUserInfo.put("notice", player.getNotice());
			currentUserInfo.put("endNum", player.getEndNum());
			if(room!=null && room.getPlayStatus()!=null){
				Integer playStatus = room.getPlayStatus();
				Integer paiInfos = 0;
					if(playStatus == Cnst.ROOM_PALYSTATE_MENGXUAN){
						paiInfos = room.getXuanNum();
					}
					else if(playStatus == Cnst.ROOM_PALYSTATE_MENGCHUAI){			
						Integer xuanNum = room.getXuanNum();
						if(xuanNum == 3){
							paiInfos = 5;
						}
						if(xuanNum == 5){
							paiInfos = 8;
						}			
					}
					else{
						paiInfos = player.getCurrentCardList().size() ;
					}
				
				currentUserInfo.put("paiInfos", paiInfos);
				currentUserInfo.put("endNum", player.getEndNum());
			}
			anotherUserInfos.add(currentUserInfo);
		}
		return anotherUserInfos;
	}
	//封装房间信息
	public static Map<String,Object> getRoomInfo(RoomResp room){
		Map<String,Object> roomInfo = new HashMap<String, Object>();
		roomInfo.put("userId",room.getCreateId());
		roomInfo.put("openName", room.getOpenName());
		roomInfo.put("createTime", room.getCreateTime());
		roomInfo.put("roomId", room.getRoomId());
		roomInfo.put("state", room.getState());
		roomInfo.put("playStatus", room.getPlayStatus());
		roomInfo.put("lastNum", room.getLastNum());
		roomInfo.put("totalNum", room.getCircleNum());//总局数
 		roomInfo.put("roomType", room.getRoomType());
		
		if(room.getLastChuPai() != null && room.getLastChuPai().size() != 0){
			List<Integer> lastChuPai = new ArrayList<Integer>();
			for(Card c:room.getLastChuPai()){
				lastChuPai.add(c.getOrigin());
			}
			roomInfo.put("lastChuPai", lastChuPai);
		}else{
			roomInfo.put("lastChuPai", null);
		}
		roomInfo.put("lastUserId", room.getLastUserId());
		roomInfo.put("xjst", room.getXjst());
		roomInfo.put("type", room.getType());
		roomInfo.put("xuanNum", room.getXuanNum());
		roomInfo.put("chuaiCircle", room.getChuaiCircle());
		roomInfo.put("chengChu", room.getChengChu());
		roomInfo.put("shuangShun", room.getShuangShun());
		roomInfo.put("A23", room.getA23());
		roomInfo.put("gongDan", room.getGongDan());
//		roomInfo.put("chuColors", room.getChuColor());
//		roomInfo.put("chuPlayers",room.getChuPlayers() );
		roomInfo.put("chuType", room.getChuType());
		roomInfo.put("diFen", room.getDiFen());
		roomInfo.put("currAction", room.getCurrAction());
		roomInfo.put("currActionUser", room.getCurrActionUser());
		roomInfo.put("lastAction", room.getLastAction());
		roomInfo.put("lastActionUser", room.getLastActionUser());
		roomInfo.put("lastActionPai", room.getLastActionPai());
		roomInfo.put("tiShi", room.getTiShi());
		//pingChuaiCircle 各阶段发起者
		if(room.getPlayStatus()!=null){
			if(room.getType() == Cnst.ROOM_PALYTYPE_MING){
				if(room.getPlayStatus() < Cnst.ROOM_PALYSTATE_JIAOPAI){
					if(room.getMengXuan() != null){
					JSONArray chu = new JSONArray();
					chu.add(room.getMengXuan());
					roomInfo.put("chuPlayers", chu);
					roomInfo.put("chuColors", room.getChuColor());
					}
				}else{
					roomInfo.put("chuPlayers", room.getChuPlayers());
					roomInfo.put("chuColors", room.getChuColor());
				}
				if(room.getPlayStatus() == Cnst.ROOM_PALYSTATE_CHUCHUAI){
					roomInfo.put("pingChuaiCircle", room.getHasChuaiCirle());
				}
				if(room.getPlayStatus() == Cnst.ROOM_PALYSTATE_FEICHUCHUAI){
					roomInfo.put("pingChuaiCircle", room.getHasChuaiCirle()==null?1:room.getHasChuaiCirle()+1);
					if(room.getFeiChuPlayers().size() == 3){
						roomInfo.put("actionStartId", room.getPingChuaiFeiChu());
					}
				}
			}else{
				if(room.getIfJiu() == null || room.getIfJiu() == false){
					roomInfo.put("chuPlayers", room.getLiangChuIds());
				}else{
					roomInfo.put("chuPlayers", room.getChuPlayers());
				}
				roomInfo.put("chuColors", room.getChuColor());
			}
			//明暗储通用
			if(room.getPlayStatus() == Cnst.ROOM_PALYSTATE_JIAOPAI && room.getFeiChuPlayers().size() == 3){
				roomInfo.put("actionStartId", room.getFeiChuPlayers().get(0));
			}
			if(room.getPlayStatus() == Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI && room.getFeiChuPlayers().size() == 3){
				roomInfo.put("actionStartId", room.getFeiChuBaoChuai());
			}
		}
		if(room.getDissolveRoom()!=null){
			Map<String,Object> dissolveRoom = new HashMap<String, Object>();
			dissolveRoom.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
			dissolveRoom.put("userId", room.getDissolveRoom().getUserId());
			dissolveRoom.put("othersAgree", room.getDissolveRoom().getOthersAgree());
			roomInfo.put("dissolveRoom", dissolveRoom);
		}else{
			roomInfo.put("dissolveRoom", null);
		}
		return roomInfo;
	}
	
	/** 
	 * 小结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100102(IoSession session, Map<String,Object> readData) {
		 Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
	     Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
	     RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
	     List<Player> players = RedisUtil.getPlayerList(room);
		List<Map<String,Object>> userInfos = new ArrayList<Map<String,Object>>();
		for(Player p:players){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("userId", p.getUserId());
			map.put("winType", p.getWinType());
			map.put("Chu",p.getChu());
			List<Integer> pais = new ArrayList<Integer>();
			for(Card c:p.getStartCardList()){
				pais.add(c.getOrigin());
			}
			map.put("pais", pais);
			map.put("score", p.getThisScore());
			map.put("finalScore", p.getScore());
			map.put("fanInfo", p.getFanInfo());
			map.put("endNum", p.getEndNum());
			userInfos.add(map);
		}
		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		info.put("totalFan", room.getDiFen());
		info.put("userInfos", userInfos);
		JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
	}

	/**
	 * 大结算
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100103(IoSession session, Map<String,Object> readData) {
		 Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
	     Long userId = StringUtils.parseLong(readData.get("userId"));
	     Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
	     RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
	     String key = roomId+"-"+room.getCreateTime();
		List<Map> userInfos = RedisUtil.getPlayRecord(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(key));
		JSONObject info = new JSONObject();
		info.put("XiaoJuNum", room.getXiaoJuNum());
		if(!RedisUtil.exists(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(key))){
			List<Map<String,Object>> zeroUserInfos = new ArrayList<Map<String,Object>>();
			List<Player> players = RedisUtil.getPlayerList(room);
			for(Player p:players){
  				Map<String,Object> map = new HashMap<String, Object>();
  				map.put("userId", p.getUserId());
  				map.put("finalScore", 0);
  				map.put("chuNum", 0);
  				map.put("duChuNum",0);
  				map.put("quanJ", 0);
  				map.put("banJ",0);
  				map.put("heJ",0);
  				map.put("bBanJ",0);
  				map.put("bQuanJ",0);
  				map.put("position", p.getPosition());
  				map.put("userName", p.getUserName());
  				map.put("userImg", p.getUserImg());
  				zeroUserInfos.add(map);
  			}
			info.put("userInfos",zeroUserInfos);
		}else{
			info.put("userInfos", userInfos);
		}
		
		JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        
        //更新 player
        Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
        p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);

        Integer outNum = room.getOutNum()==null?1:room.getOutNum()+1;
        if(outNum == 4){
        	RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
        }else{
        	 //更新outNum
        	room.setOutNum(outNum);
            RedisUtil.updateRedisData(room, p);
        }      
	}
	/**
	 * 动作回应
	 */
	public static void interface_100104(JSONObject info,RoomResp room,Integer interfaceId){
		JSONObject result = getJSONObj(interfaceId,1,info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = RedisUtil.getPlayerList(room);
		for(int i=0;i<players.size();i++){
			IoSession se = MinaServerManager.tcpServer.getSessions().get(players.get(i).getSessionId());
			if (se!=null&&se.isConnected()) {
				se.write(pd);
			}
		}
	}
	/**
	 * 发牌推送
	 */
	public static void interface_100105(JSONObject info,RoomResp room,Integer interfaceId){
		JSONObject result = getJSONObj(interfaceId,1,info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = RedisUtil.getPlayerList(room);
		for(int i=0;i<players.size();i++){
			IoSession se = MinaServerManager.tcpServer.getSessions().get(players.get(i).getSessionId());
			if (se!=null&&se.isConnected()) {
				se.write(pd); 
			}
		}
	}
	

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(IoSession session) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1, "out");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(IoSession session, Integer type,
			List<Player> players) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();

		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", session.getAttribute(Cnst.USER_SESSION_USER_ID));
		info.put("type", type);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(IoSession session) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}
	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, List<Integer> states,List<Long> userIds,int type) {
		Integer interfaceId = 100109;
		if (type==1) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("userId", userIds);
			info.put("state", states);
			info.put("type", 2);//给其他人发2
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			List<Long> others = new ArrayList<Long>();
			if (players != null && players.size() > 0) {
				boolean flag = false;
				for (Player p : players) {
					if (p != null && !userIds.contains(p.getUserId())) {
						flag = true;
						others.add(p.getUserId());
						IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							se.write(pd);
						}
					}
				}
				if (flag) {
					info.put("type", 1);//给自己发1
					info.put("userId", others);
					List<Integer> ostates = new ArrayList<Integer>();
					if (others.size()>0) {
						for(Long uid:others){
							String lastHeartTime = RedisUtil.hget(Cnst.REDIS_HEART_PREFIX, String.valueOf(uid));
							if (lastHeartTime==null) {
								ostates.add(Cnst.PLAYER_LINE_STATE_OUT);
							}else{
								ostates.add(Cnst.PLAYER_LINE_STATE_INLINE);
							}
						}
					}
					info.put("state", ostates);
					result = getJSONObj(interfaceId, 1, info);
					pd = new ProtocolData(interfaceId, result.toJSONString());
					IoSession se = MinaServerManager.tcpServer.getSessions().get(userIds.get(0));
					if (se != null && se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}else if(type==2){
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("userId", userIds);
			info.put("state", states);
			info.put("type", 2);//给其他人发2
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			List<Long> others = new ArrayList<Long>();
			if (players != null && players.size() > 0) {
				for (Player p : players) {
					if (p != null && !userIds.contains(p.getUserId())) {
						others.add(p.getUserId());
						IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							se.write(pd);
						}
					}
				}
			}
		}
	}
	
	

    
    /**
     * 后端主动解散房间推送
     * @param reqState
     * @param players
     */
	public static void interface_100111(int reqState,List<Player> players,Integer roomId){
    	Integer interfaceId = 100111;
        Map<String,Object> info = new HashMap<String, Object>();
        info.put("reqState",reqState);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        if (players!=null&&players.size()>0) {
			for(Player p:players){
				if (p.getRoomId()!=null&&p.getRoomId().equals(roomId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
					if (se!=null&&se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}
    	
    }
	
	 /**
     * 后端主动加入代开房间推送
     * @param reqState
     * @param players
     */
	public static void interface_100112(Player player,RoomResp room){
    	Integer interfaceId = 100112;
    	//先判断房主是否在线
    	Player roomCreater = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()));
    	IoSession se = MinaServerManager.tcpServer.getSessions().get(roomCreater.getSessionId());
		if (se!=null&&se.isConnected()) {
			 Map<String,Object> info = new HashMap<String, Object>();
		     info.put("roomSn",room.getRoomId());
		     info.put("userId",player.getUserId());
		     info.put("userName",player.getUserName());
		     info.put("userImg", player.getUserImg());
		     info.put("position", player.getPosition());
		     JSONObject result = getJSONObj(interfaceId,1,info);
		     ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());				
			se.write(pd);
		}else{
			return;
		}
       
	   	
    }

}
