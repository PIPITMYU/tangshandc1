package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;
import com.up72.server.mina.utils.dcuse.GameUtil;
import com.up72.server.mina.utils.dcuse.JieSuan;

/**
 * Created by Administrator on 2017/7/13. 游戏中
 */

public class GameFunctions extends TCPGameFunctions {
	final static Object object = new Object();

	/**
	 * 用户点击准备，用在小结算那里，
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100200(IoSession session,
			Map<String, Object> readData) {
		logger.I("准备,interfaceId -> 100200");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		Player currentPlayer = null;
		List<Player> players = RedisUtil.getPlayerList(room);
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}

		if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
			return;
		}
		if (currentPlayer == null
				|| currentPlayer.getPlayStatus() == Cnst.PLAYER_STATE_PREPARED) {
			return;
		}

		currentPlayer.initPlayer(currentPlayer.getRoomId(),
				Cnst.PLAYER_STATE_PREPARED, currentPlayer.getScore(),
				currentPlayer.getQuanJ(), currentPlayer.getBanJ(),
				currentPlayer.getHeJ(), currentPlayer.getbBanJ(),
				currentPlayer.getbQuanJ(), currentPlayer.getChuNum(),
				currentPlayer.getDuChuNum());

		boolean allPrepared = true;

		for (Player p : players) {
			if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
				allPrepared = false;
			}
		}

		if (allPrepared && players != null && players.size() == 4) {
			startGame(room, players);
			//关闭解散房间计时任务
	        
			 BackFileUtil.write(null, interfaceId, room,players,null);//写入文件内容
		}
		Map<String, Object> info = new HashMap<String, Object>();
		List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
		// old
		for (Player p : players) {
			Map<String, Object> i = new HashMap<String, Object>();
			i.put("userId", p.getUserId());
			i.put("playStatus", p.getPlayStatus());
			userInfo.add(i);
		}
		info.put("userInfos", userInfo);
		Map<String, Object> roominfo = new HashMap<String, Object>();
		roominfo.put("state", room.getState());
		roominfo.put("playStatus", room.getPlayStatus());
		info.put("roomInfos", roominfo);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		RedisUtil.setPlayersList(players);
	}

	/**
	 * 开局发牌
	 * 
	 * @param roomId
	 */
	public static void startGame(RoomResp room, List<Player> players) {
		room.setDiFen(1);
		room.setXiaoJuNum(room.getXiaoJuNum() == null ? 1
				: room.getXiaoJuNum() + 1);
		room.setXjst(System.currentTimeMillis());

		// 初始化 realPlayerIds
		room.setRealPlayerIds(GameUtil.changeList(room.getPlayerIds()));

		Integer xuanNum = room.getXuanNum();
		// 初始化房间手牌
		List<Card> cards = new ArrayList<Card>();
		for (int i = 0; i < Cnst.CARD_ARRAY.length; i++) {
			cards.add(new Card(Cnst.CARD_ARRAY[i]));
		}
		// 明储
		if (room.getType() == Cnst.ROOM_PALYTYPE_MING) {
			List<Long> canMengXuan = new ArrayList<Long>();
			List<Long> canMengChuai = new ArrayList<Long>();
			for (int i = 0; i < players.size(); i++) {
				// 开局每人13张牌
				List<Card> thisCard = new ArrayList<Card>();
//				for (int j = 1; j <= 13; j++) {
//					Card card = cards
//							.get((int) (Math.random() * (cards.size())));
//					if (card.getType() == 2 && card.getSymble() == 3) {
//						room.setHongXinThree(players.get(i).getUserId());
//					}
//					thisCard.add(card);
//					cards.remove(card);
//				}
				//=====测试用牌
				GameUtil.mingTestPai(i, thisCard);
				if(i == 2){
					room.setHongXinThree(players.get(i).getUserId());
				}
				//=====测试用牌
				players.get(i).setCurrentCardList(thisCard);
				players.get(i).setStartCardList(thisCard);
				// 3张
				if (xuanNum == 3) {
					// 判断梦宣
					in: for (int m = 0; m < 3; m++) {
						if (thisCard.get(m).getSymble() == 1) {
							canMengXuan.add(players.get(i).getUserId());
							break in;
						}
					}
					// 判断梦踹
					if (GameUtil.checkMengC(thisCard, 5)) {
						canMengChuai.add(players.get(i).getUserId());// 第一轮判断
																		// 在踹完之后除掉轰或炸
																		// 在进行判断
					}
				}

				// 5张
				if (xuanNum == 5) {
					// 判断梦宣
					in: for (int m = 0; m < 5; m++) {
						if (thisCard.get(m).getSymble() == 1) {
							canMengXuan.add(players.get(i).getUserId());
							break in;
						}
					}
					// 判断梦踹
					if (GameUtil.checkMengC(thisCard, 8)) {
						canMengChuai.add(players.get(i).getUserId());// 第一轮判断
																		// 在踹完之后除掉轰或炸
																		// 在进行判断
					}
				}
				// 最后一个人 最后一张牌
				if (i == 3) {
					room.setLastFaCard(thisCard.get(12));
				}
			}
			// 更新能梦宣 梦踹的集合
			room.setCanMengXuan(canMengXuan);
			room.setCanMengChuai(canMengChuai);
			// 如果没有人梦宣 系统设置储方 有人梦宣那里 重新初始化
			if (canMengXuan.size() == 0) {
				List<Long> chuPlayers = new ArrayList<Long>();
				List<Long> feiChuPlayers = new ArrayList<Long>();
				Integer chuColor = room.getLastFaCard().getType();// 最后一张的花色
				for (int i = 0; i < players.size(); i++) {
					List<Card> currentCardList = players.get(i)
							.getCurrentCardList();
					Long thisUserId = players.get(i).getUserId();
					in: for (Card c : currentCardList) {
						if (chuColor == 1 || chuColor == 3) {
							if (c.getSymble() == 1
									&& (c.getType() == 1 || c.getType() == 3)) {
								// 设置成储方
								chuPlayers.add(thisUserId);
								break in;
							}
						}
						if (chuColor == 2 || chuColor == 4) {
							if (c.getSymble() == 1
									&& (c.getType() == 2 || c.getType() == 4)) {
								// 设置成储方
								chuPlayers.add(thisUserId);
								break in;
							}
						}
					}
					if (!chuPlayers.contains(thisUserId)) {
						// 设置成非储
						feiChuPlayers.add(thisUserId);
					}
					// 没人梦宣 是否有人可以撑储 有人梦宣 则在执行梦宣动作 时 在判断一次
					if (!chuPlayers.contains(thisUserId) && 
							GameUtil.checkDuChu(currentCardList, chuColor)
							) {
						room.setCanDuChuId(thisUserId);
					}

				}
				room.setChuColor(chuColor);
				room.setChuPlayers(chuPlayers);
				room.setFeiChuPlayers(feiChuPlayers);
			}
			room.setState(Cnst.ROOM_STATE_GAMIING);

			if (room.getCanMengXuan() != null
					&& room.getCanMengXuan().size() > 0) {
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_MENGXUAN);
				room.setCurrActionUser(room.getCanMengXuan().get(0));
				List<Integer> actions = new ArrayList<Integer>();
				actions.add(Cnst.ACTION_GUO);
				actions.add(Cnst.ACTION_MENGXUAN);

				room.setCurrAction(actions);
			}
			if (room.getCanMengXuan().size() == 0
					&& room.getCanMengChuai().size() != 0) {
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_MENGCHUAI);
				room.setCurrActionUser(room.getCanMengChuai().get(0));
				List<Integer> actions = new ArrayList<Integer>();
				actions.add(Cnst.ACTION_GUO);
				actions.add(Cnst.ACTION_MENGCHUAI);

				room.setCurrAction(actions);
			}

			if (room.getCanMengChuai().size() == 0
					&& room.getCanMengXuan().size() == 0) {
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_JIAOPAI);
				room.setCurrActionUser(room.getChuPlayers().get(0));
				List<Integer> actions = new ArrayList<Integer>();
				actions.add(Cnst.ACTION_GUO);
				actions.add(Cnst.ACTION_JIAO);

				room.setCurrAction(actions);

				// 初始化 动作统计
				room.setChuActionInfos(getSameList(room.getChuPlayers()));
				room.setFeiChuActionInfos(getSameList(room.getFeiChuPlayers()));
			}
		} else {

			// 暗储
			List<Long> chuPlayers = new ArrayList<Long>();
			List<Long> feiChuPlayers = new ArrayList<Long>();
			List<Long> jiuChuPlayers = new ArrayList<Long>();
			for (int i = 0; i < players.size(); i++) {
				// 开局每人13张牌
				List<Card> thisCard = new ArrayList<Card>();
				for (int j = 1; j <= 13; j++) {
					Card card = cards
							.get((int) (Math.random() * (cards.size())));
					if (card.getType() == 2 && card.getSymble() == 3) {
						room.setHongXinThree(players.get(i).getUserId());
					}
					thisCard.add(card);
					cards.remove(card);
				}
				//=====测试用牌
//				GameUtil.mingTestPai(i, thisCard);
//				if(i == 0){
//					room.setHongXinThree(players.get(i).getUserId());
//				}
				//=====测试用牌
				players.get(i).setCurrentCardList(thisCard);
				players.get(i).setStartCardList(thisCard);
				Long thisUserId = players.get(i).getUserId();
				in: for (Card c : thisCard) {
					if (c.getSymble() == 1
							&& (c.getType() == 1 || c.getType() == 3)) {
						// 黑A 为储
						chuPlayers.add(thisUserId);
						break in;
					}
				}
				if (!chuPlayers.contains(thisUserId)) {
					feiChuPlayers.add(thisUserId);
					// 判断是否可以揪储
					if (GameUtil.checkHong(thisCard, 13)) {
						jiuChuPlayers.add(thisUserId);
					}
					// 判断是否有人可以撑储 暗储 不能有黑A
					if (!chuPlayers.contains(thisUserId) && 
							GameUtil.checkDuChu(thisCard, 1)) {
						room.setCanDuChuId(thisUserId);
					}
				}
			}
			room.setChuPlayers(chuPlayers);
			room.setFeiChuPlayers(feiChuPlayers);
			room.setJiuChu(jiuChuPlayers);
			room.setState(Cnst.ROOM_STATE_GAMIING);
			room.setChuColor(1);
			if (room.getChengChu() == 1 && room.getCanDuChuId() != null && chuPlayers.size()>1) {
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_CHENGCHU);
				room.setCurrActionUser(room.getCanDuChuId());
				List<Integer> actions = new ArrayList<Integer>();
				actions.add(Cnst.ACTION_GUO);
				actions.add(Cnst.ACTION_CHENGCHU);
				room.setCurrAction(actions);
			} else if (jiuChuPlayers.size() != 0) {
				// 有人可以揪储
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_JIUCHU);
				room.setCurrActionUser(room.getJiuChu().get(0));
				List<Integer> actions = new ArrayList<Integer>();
				actions.add(Cnst.ACTION_GUO);
				actions.add(Cnst.ACTION_JIUCHU);
				room.setCurrAction(actions);
				// 初始化 动作统计
				room.setFeiChuActionInfos(getSameList(room.getJiuChu()));
				room.getFeiChuActionInfos().remove(0);
			} else {
				// 开局
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_CHUPAI);
				room.setCurrActionUser(room.getHongXinThree());
			}

		}
		// 更新 room players
		RedisUtil.setObject(
				Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(room.getRoomId())), room,null);
		for(Player p:players){
			p.setPlayStatus(Cnst.PLAYER_STATE_GAME);
		}
		if(room.getXiaoJuNum() == 1){
			notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,true);
			addRoomToDB(room);
		}
	}

	/**
	 * 请求发牌
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100207(IoSession session,
			Map<String, Object> readData) {
		logger.I("准备,interfaceId -> 100207");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		Integer playStatus = room.getPlayStatus();
		Integer xuanNum = room.getXuanNum();

		JSONObject roomInfos = new JSONObject();// 封装房间信息
		JSONObject userInfos = new JSONObject();// 封装玩家信息
		JSONArray paiInfos = new JSONArray();// 封装当前手牌
		if(room.getType() == Cnst.ROOM_PALYTYPE_MING){
			if (playStatus == Cnst.ROOM_PALYSTATE_MENGXUAN) {

				if (xuanNum == 3) {
					for (int i = 0; i < 3; i++) {
						paiInfos.add(player.getCurrentCardList().get(i).getOrigin());
					}
				}
				if (xuanNum == 5) {
					for (int i = 0; i < 5; i++) {
						paiInfos.add(player.getCurrentCardList().get(i).getOrigin());
					}
				}
				userInfos.put("paiInfos", paiInfos);
			}
			if (playStatus == Cnst.ROOM_PALYSTATE_MENGCHUAI) {
				List<Player> players = RedisUtil.getPlayerList(room);
				boolean hasMengXuan = false;
				in: for (Player p : players) {
					List<Card> pCards = p.getCurrentCardList();
					for (int i = 0; i < xuanNum; i++) {
						if (pCards.get(i).getSymble() == 1) {
							hasMengXuan = true;
							break in;
						}

					}
				}
				if (xuanNum == 3) {
					if (hasMengXuan == true) {
						for (int i = 3; i < 5; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					} else {
						for (int i = 0; i < 5; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}

				}
				if (xuanNum == 5) {

					if (hasMengXuan == true) {
						for (int i = 5; i < 8; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					} else {
						for (int i = 0; i < 8; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}
				}
				userInfos.put("paiInfos", paiInfos);
			}
			if (playStatus == Cnst.ROOM_PALYSTATE_JIAOPAI) {
				List<Player> players = RedisUtil.getPlayerList(room);
				// 这里需要检测下 之前是否有过梦宣 梦踹状态

				boolean hasMengChuai = false;
				boolean hasMengXuan = false;
				out: for (Player p : players) {
					List<Card> pCards = p.getCurrentCardList();
					for (int i = 0; i < xuanNum; i++) {
						if (pCards.get(i).getSymble() == 1) {
							hasMengXuan = true;
							break out;
						}

					}
				}
				if (xuanNum == 3) {
					in: for (Player p : players) {
						if(room.getMengXuan()!=null && p.getUserId().equals(room.getMengXuan())){
							continue;
						}
						List<Card> pCards = p.getCurrentCardList();
						List<Card> qian5Cards = new ArrayList<Card>();
						for (int i = 0; i < 5; i++) {
							qian5Cards.add(pCards.get(i));
						}
						if (GameUtil.checkMengC(qian5Cards, 5)) {
							hasMengChuai = true;
							break in;
						}
					}
					if (hasMengChuai == true) {
						for (int i = 5; i < 13; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}
					if (hasMengChuai == false && hasMengXuan == false) {
						for (int i = 0; i < 13; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}
					if (hasMengChuai == false && hasMengXuan == true) {
						for (int i = 3; i < 13; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}

				}
				if (xuanNum == 5) {
					in: for (Player p : players) {
						if(room.getMengXuan()!=null && p.getUserId().equals(room.getMengXuan())){
							continue;
						}
						List<Card> pCards = p.getCurrentCardList();
						List<Card> qian8Cards = new ArrayList<Card>();
						for (int i = 0; i < 8; i++) {
							qian8Cards.add(pCards.get(i));
						}
						if (GameUtil.checkMengC(qian8Cards, 8)) {
							hasMengChuai = true;
							break in;
						}
					}
					if (hasMengChuai == true) {
						for (int i = 8; i < 13; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}
					if (hasMengChuai == false && hasMengXuan == false) {
						for (int i = 0; i < 13; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}
					if (hasMengChuai == false && hasMengXuan == true) {
						for (int i = 5; i < 13; i++) {
							paiInfos.add(player.getCurrentCardList().get(i)
									.getOrigin());
						}
					}
				}
				userInfos.put("paiInfos", paiInfos);
			}
		}else{
			if(player.isAnHasFaPai() == true){
				userInfos.put("paiInfos", paiInfos);
			}else{
				//防止 用户第一次掉线没有请求发牌刷新大接口 的尴尬情况
				if(playStatus == Cnst.ROOM_PALYSTATE_JIAOPAI || 
						(room.getLastAction()!=null && playStatus == Cnst.ROOM_PALYSTATE_CHUPAI) || 
						playStatus == Cnst.ROOM_PALYSTATE_FANCHUAI){
					
				}else{
					for (int i = 0; i < 13; i++) {
						paiInfos.add(player.getCurrentCardList().get(i)
								.getOrigin());
					}
				}				
				userInfos.put("paiInfos", paiInfos);
				player.setAnHasFaPai(true);
				RedisUtil.updateRedisData(null,player);
			}			
		}
		

		JSONObject info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		userInfos.put("playStatus", player.getPlayStatus());
		userInfos.put("state", player.getState());
		userInfos.put("userId", userId);
		info.put("userInfos", userInfos);

		roomInfos.put("action", room.getCurrAction());
		roomInfos.put("actionUser", room.getCurrActionUser());
		roomInfos.put("playStatus", playStatus);
		
		if(room.getType() == Cnst.ROOM_PALYTYPE_MING){
			if(playStatus < Cnst.ROOM_PALYSTATE_JIAOPAI){
				if(room.getMengXuan() != null){
				JSONArray chu = new JSONArray();
				chu.add(room.getMengXuan());
				roomInfos.put("chuPlayers", chu);
				roomInfos.put("chuColors", room.getChuColor());
				}
			}else{
				roomInfos.put("chuPlayers", room.getChuPlayers());
				roomInfos.put("chuColors", room.getChuColor());
			}
		}else{			
			if(room.getIfJiu() == null || room.getIfJiu() == false){
				
			}else{
				roomInfos.put("chuPlayers", room.getChuPlayers());
			}
			roomInfos.put("chuColors", room.getChuColor());
		}	
		roomInfos.put("diFen", room.getDiFen());
		roomInfos.put("state", room.getState());
		info.put("roomInfos", roomInfos);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		//小结算之后不写回放 防止文件不完整
//		if(room.getState() != Cnst.ROOM_STATE_XJS){
//			BackFileUtil.write(null, interfaceId,room,null,getNewObj(info));//写入文件内容
//		}		
	}

	/**
	 * 出牌
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100201(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("出牌,interfaceId -> 100201");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String roomId = StringUtils.toString((readData.get("roomSn")));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		List<Integer> pairs = null;
		Integer isYao = StringUtils.parseInt(readData.get("isYao"));
		if(isYao == 1){
			pairs = JSONObject.parseArray(
					StringUtils.toString(readData.get("pais")), Integer.class);
		}
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if(room == null){
//			回放出来请求出牌????????
			return;
		}
		if (room.getAddIsChu() == null || room.getAddIsChu() == 0) {
			List<Long> chuPlayers = room.getChuPlayers();
			for (int i = 0; i < chuPlayers.size(); i++) {
				Player p = RedisUtil.getPlayerByUserId(String
						.valueOf(chuPlayers.get(i)));
				List<Card> chuCards = p.getCurrentCardList();

				if (room.getChuColor() == 1 || room.getChuColor() == 3) {
					// 黑储
					for (Card c : chuCards) {
						if (c.getOrigin() == 101 || c.getOrigin() == 301) {
							c.setChu(true);
						}
					}
				}
				if (room.getChuColor() == 2 || room.getChuColor() == 4) {
					// 红储
					for (Card c : chuCards) {
						if (c.getOrigin() == 201 || c.getOrigin() == 401) {
							c.setChu(true);
						}
					}
				}
				RedisUtil.updateRedisData(null, p);
			}
			room.setAddIsChu(1);
		}
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		// user
		List<Card> currentCards = player.getCurrentCardList();// 用 户当前手牌
		Integer endNum = player.getEndNum();// 几跑
		// room
		List<Long> realPlayerIds = room.getRealPlayerIds();
		List<Long> hasEndPlayerIds = room.getHasEndPlayerIds();
		JSONObject info = new JSONObject();
		//红桃三 缴牌人 出牌
		if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO && pairs != null && pairs.size() != 0 && pairs.get(0) == 203){
			room.setCurrActionUser(realPlayerIds.get(0));
			List<Card> lastCard = new ArrayList<Card>();
			lastCard.add(new Card(203));
			room.setLastChuPai(lastCard);
			room.setLastUserId(userId);
			room.setLastActionUser(userId);
			info.put("nextChuPlayerId", realPlayerIds.get(0));
			info.put("playStatus", player.getPlayStatus());
			info.put("paiNum", currentCards.size());
			info.put("lastUserId", room.getLastUserId());
			info.put("pais", pairs);
			info.put("isYao", isYao);
			info.put("userId", userId);
			info.put("endNum", endNum);
			info.put("reqState", Cnst.REQ_STATE_1);
			info.put("state", room.getState());
			room.setJieFeng(true);
			room.setJieFengNum(0);
			RedisUtil.updateRedisData(room, player);

			// 发送 更新redis
			MessageFunctions.interface_100105(info, room, 100105);
			return;
		}
		// 校验状态
		if (player.getPlayStatus() != Cnst.PLAYER_STATE_GAME || room.getState() == Cnst.ROOM_STATE_XJS 
				|| room.getPlayStatus() != Cnst.ROOM_PALYSTATE_CHUPAI || !room.getCurrActionUser().equals(userId)) {
			return;
		}
		
		
		Integer index = realPlayerIds.indexOf(userId);
		Integer nextIndex = index + 1;
		// 最后一个
		if (index + 1 == realPlayerIds.size()) {
			nextIndex = 0;
		}
		info.put("nextChuPlayerId", realPlayerIds.get(nextIndex));
		//设置当前出牌人 
		room.setCurrActionUser(realPlayerIds.get(nextIndex));
		room.setLastActionUser(userId);
		// 要
		List<Card> lastCards = new ArrayList<Card>();
		if (isYao == 1) {
			// 判断当前手牌是否含有这些牌
			for (Integer pai : pairs) {
				Card card = new Card(pai);
				if (!currentCards.contains(card)) {
					return;
				}
				// 有则 移除掉
				lastCards.add(currentCards.get(currentCards.indexOf(card)));
				currentCards.remove(card);
//				lastCards.add(card);
			}
			//判断是否能要的起 --
			
			if(GameUtil.checkYao(lastCards, room) == false){
				 illegalRequest(interfaceId, session);
				 return;
			}
			room.setLastChuPai(lastCards);
			room.setLastUserId(userId);
			room.setLastAction(Cnst.ACTION_YAO);
			// 设置最后出牌人id
			//如果是接风中 重置
			if(room.isJieFeng() == true){
				room.setJieFeng(false);
				room.setJieFengNum(null);
			}
			// 出完牌
			if (currentCards.size() == 0) {
				player.setPlayStatus(Cnst.PLAYER_STATE_over);
				realPlayerIds.remove(userId);
				// 判断几跑
				Integer n = hasEndPlayerIds.size();
				endNum = n + 1;
				player.setEndNum(endNum);
				hasEndPlayerIds.add(userId);
				room.setHasEndPlayerIds(hasEndPlayerIds);
				room.setJieFeng(true);
				room.setJieFengNum(0);
			}
		} else {
			// 不要 似乎什么也不用做
//			pairs = new ArrayList<Integer>();
//			for(int i=0;i<room.getLastChuPai().size();i++){
//				pairs.add(room.getLastChuPai().get(i).getOrigin());
//			}
			room.setLastAction(Cnst.ACTION_BUYAO);
			//算接风
			if(room.isJieFeng() == true){
				room.setJieFengNum(room.getJieFengNum()+1);
				if(room.getJieFengNum() == realPlayerIds.size()){
					//新一轮出牌
					room.setJieFeng(false);
					room.setJieFengNum(null);
					room.setLastUserId(room.getCurrActionUser());//前端要的
					room.setLastChuPai(null);
//					pairs = new ArrayList<Integer>();
				}
			}
		}
		//如果是暗储 黑A 亮明身份
		if(room.getType() == Cnst.ROOM_PALYTYPE_AN && pairs!=null && pairs.size()!=0 && 
				(pairs.contains(101) || pairs.contains(301))){			
			if(room.getLiangChuIds() == null){
				room.setLiangChuIds(new ArrayList<Long>());
			}
			room.getLiangChuIds().add(userId);			
		}
		// 判断是否开始结算
		if (realPlayerIds.size() == 1) {	
			room.setState(Cnst.ROOM_STATE_XJS);
		}
		if (realPlayerIds.size() == 2) {
			// 判断两人是否是一家
			if(room.getChuPlayers().contains(realPlayerIds.get(0)) && room.getChuPlayers().contains(realPlayerIds.get(1)) || 
					room.getFeiChuPlayers().contains(realPlayerIds.get(0)) && room.getFeiChuPlayers().contains(realPlayerIds.get(1))	){
				room.setState(Cnst.ROOM_STATE_XJS);
			}
		}
		if(room.getChuPlayers().size() == 1 && room.getHasEndPlayerIds().contains(room.getChuPlayers().get(0))){
			//独储先跑
			room.setState(Cnst.ROOM_STATE_XJS);
		}
		info.put("playStatus", player.getPlayStatus());
		info.put("paiNum", currentCards.size());
		info.put("lastUserId", room.getLastUserId());
		info.put("isYao", isYao);
		if(isYao == 0){
			
		}else{
			info.put("pais", pairs);
		}
		
		info.put("userId", userId);
		info.put("endNum", endNum);
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("state", room.getState());
		
		if(room.getLastUserId() != null && room.getCurrActionUser().equals(room.getLastUserId())){
			room.setLastChuPai(null);
		}
		RedisUtil.updateRedisData(room, player);
		//Message里写入 可能小结算后还有出牌
		BackFileUtil.write(null, interfaceId,room,null,getNewObj(info));//写入文件内容
		// 发送 更新redis
		if (room.getState() == Cnst.ROOM_STATE_XJS) {
			JieSuan.xiaoJieSuan(roomId);
		}
		MessageFunctions.interface_100105(info, room, 100105);
		

 	}

	/**
	 * 玩家动作
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100202(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100202");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer action = StringUtils.parseInt(readData.get("action"));// 动作
		List<Integer> actionPais = JSONObject.parseArray(String.valueOf(readData.get("actionPai")), Integer.class);//数字转成数组
		Integer actionPai = 0;
		if(actionPais != null && actionPais.size() != 0){
			actionPai = actionPais.get(0);
		}

		Card card = new Card(actionPai);
		Long currActionUser = null;// 当前动作人 *
		List<Integer> currAction = new ArrayList<Integer>();// 当前动作 *

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		//小结算或解散房间
		if(room == null || room.getState() == Cnst.ROOM_STATE_XJS || room.getState() == Cnst.ROOM_STATE_YJS 
				|| !room.getCurrActionUser().equals(userId)){
			return;
		}
		
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		// ======room
		Integer playStatus = room.getPlayStatus();// 房间状态
		Integer type = room.getType();// 明储 暗储 选项
		List<Long> canMengXuan = room.getCanMengXuan();
		List<Long> canMengChuai = room.getCanMengChuai();
		List<Long> chuPlayers = room.getChuPlayers();
		List<Long> feiChuPlayers = room.getFeiChuPlayers();
		List<Long> chuActionInfos = room.getChuActionInfos();
		List<Long> feiChuActionInfos = room.getFeiChuActionInfos();

		Map<String, Object> fanInfo = player.getFanInfo();

		// 要更新的lastAction 因为下面包踹 踹会用到旧的 这里不初始化
		Integer newLastAction = action; // *
		Long newLastActionUserId = userId;// *

		JSONObject info = new JSONObject();
		info.put("playStatus", playStatus);// 给老的状态
		List<Integer> nextActions = new ArrayList<Integer>();
		Long nextActionUserId = null;
		Integer ifContinue = 1;//
		switch (action) {
		// =============================================================>梦宣
		case Cnst.ACTION_MENGXUAN:
			// 每次动作时 都要判断房间状态

			if (card.getSymble() != 1) {
				System.out.println("====================>" + "此牌不能梦宣");
				return;
			}
			List<Player> players = RedisUtil.getPlayerList(room);
			room.setMengXuan(userId);//设置梦踹Id
			// 把梦宣的人 移除梦踹队列

			if (canMengChuai != null && canMengChuai.size() != 0) {
				canMengChuai.remove(userId);
			}
			chuPlayers = new ArrayList<Long>();
			feiChuPlayers = new ArrayList<Long>();
			Integer chuColor = card.getType();
			for (int i = 0; i < players.size(); i++) {
				List<Card> currentCardList = players.get(i)
						.getCurrentCardList();
				Long thisUserId = players.get(i).getUserId();
				in: for (Card c : currentCardList) {
					if (chuColor == 1 || chuColor == 3) {
						if (c.getSymble() == 1
								&& (c.getType() == 1 || c.getType() == 3)) {
							// 设置成储方
							chuPlayers.add(thisUserId);
							break in;
						}
					}
					if (chuColor == 2 || chuColor == 4) {
						if (c.getSymble() == 1
								&& (c.getType() == 2 || c.getType() == 4)) {
							// 设置成储方
							chuPlayers.add(thisUserId);
							break in;
						}
					}
				}
				if (!chuPlayers.contains(thisUserId)) {
					// 设置成非储
					feiChuPlayers.add(thisUserId);
				}
				// 没人梦宣 是否有人可以撑储 有人梦宣 则在执行梦宣动作 时 在判断一次
				if (!chuPlayers.contains(thisUserId) && 
						GameUtil.checkDuChu(currentCardList, card.getType()) ) {
					room.setCanDuChuId(thisUserId);
				}
			}
			room.setChuPlayers(chuPlayers);
			room.setFeiChuPlayers(feiChuPlayers);
			room.setChuColor(card.getType());
			// 增加底分
			room.setDiFen(room.getDiFen() * 4);
			fanInfo.put(String.valueOf(action), 4);

			canMengXuan.clear();
			// 重置梦宣集合 状态请求做判断

			// 更新players 数据
			if (canMengChuai == null || canMengChuai.size() == 0) {
				// 这里要扣除 梦踹到自己身上的分数

				// 进入缴牌状态
				playStatus = Cnst.ROOM_PALYSTATE_JIAOPAI;
				nextActionUserId = chuPlayers.get(0);
				nextActions.add(Cnst.ACTION_GUO);
				nextActions.add(Cnst.ACTION_JIAO);

				// 初始化 动作统计
				chuActionInfos = getSameList(chuPlayers);
				feiChuActionInfos = getSameList(feiChuPlayers);
	
			} else {
				// 梦踹状态
				playStatus = Cnst.ROOM_PALYSTATE_MENGCHUAI;
				nextActionUserId = canMengChuai.get(0);
				nextActions.add(Cnst.ACTION_GUO);
				nextActions.add(Cnst.ACTION_MENGCHUAI);
			
			}
			break;
			// =============================================================>梦踹
		case Cnst.ACTION_MENGCHUAI:
			//3张A 不能梦踹
			if(actionPai%100 == 1){
				illegalRequest(interfaceId, session);
				return;
			}
			List<Integer> mengChuaiPai = room.getMengChuaiPai();
			if(mengChuaiPai == null){
				mengChuaiPai = new ArrayList<Integer>();
			}
			if(actionPai == 0 || mengChuaiPai.contains(actionPai%100)){
				illegalRequest(interfaceId, session);
				return;
			}					
			if(actionPai != 0){
				mengChuaiPai.add(actionPai%100);
				room.setMengChuaiPai(mengChuaiPai);
			}
			
			// 增加底分
			room.setDiFen(room.getDiFen() * 2);
			String fanOverValue = String.valueOf(Cnst.ACTION_MENGCHUAI);
			if(fanInfo.get(fanOverValue)==null){
				fanInfo.put(fanOverValue, 2);
			}else{
				fanInfo.put(fanOverValue, StringUtils.parseInt(fanInfo.get(fanOverValue))*2);
			}

			List<Card> currentCardList = player.getCurrentCardList();
			List<Long> mengChuai = room.getMengChuai();
			if(mengChuai == null){
				mengChuai = new ArrayList<Long>();
			}
			mengChuai.add(userId);
			room.setMengChuai(mengChuai);
			canMengChuai.remove(userId);
			// 判断此玩家是否可以继续梦踹 没有则移出队列 8张牌 时进行检测
			if (room.getXuanNum() == 5) {
				List<Card> newCards = new ArrayList<Card>();
				for (int i = 0; i < 8; i++) {
					if (mengChuaiPai.contains(currentCardList.get(i).getSymble())) {
						continue;
					}
					newCards.add(currentCardList.get(i));
				}
				// 重新检测可以继续梦踹
				if (GameUtil.checkMengC(newCards, newCards.size())) {
					canMengChuai.add(userId);// 添加到 下一轮梦踹集合中
				}
			}
			// 是否有人继续可以梦踹
			if (canMengChuai.size() != 0) {
				ifContinue = 0;
				nextActions.add(Cnst.ACTION_GUO);
				nextActions.add(Cnst.ACTION_MENGCHUAI);

				nextActionUserId = canMengChuai.get(0);
			}
			if (canMengChuai.size() == 0) {
				// 进入缴牌状态
				playStatus = Cnst.ROOM_PALYSTATE_JIAOPAI;
				nextActionUserId = chuPlayers.get(0);
				nextActions.add(Cnst.ACTION_GUO);
				nextActions.add(Cnst.ACTION_JIAO);

				// 初始化 动作统计
				chuActionInfos = getSameList(chuPlayers);
				feiChuActionInfos = getSameList(feiChuPlayers);
			
			}

			break;
		// =============================================================>称储
		case Cnst.ACTION_CHENGCHU:
			if (room.getCanDuChuId() != null && room.getCanDuChuId().equals(userId)) {
				// 增加底分
				room.setDiFen(room.getDiFen()*4);
				fanInfo.put(String.valueOf(action), 4);
				room.setChuType(1);
				room.setHasChengChu(userId);
				// 之前设置的储信息清除
				players = RedisUtil.getPlayerList(room);
				chuPlayers.clear();
				feiChuPlayers.clear();
				for (int i = 0; i < players.size(); i++) {
					Long thisUserId = players.get(i).getUserId();
					if (thisUserId.equals(userId)) {
						chuPlayers.add(userId);
					} else {
						feiChuPlayers.add(thisUserId);
					}
				}
				//更新chu feichu
				room.setChuPlayers(chuPlayers);
				
				room.setFeiChuPlayers(feiChuPlayers);
				room.setChuColor(card.getType());
				if (type == Cnst.ROOM_PALYTYPE_MING) {
					playStatus = Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_BAOCHUAI);
					nextActionUserId = feiChuPlayers.get(0);
					room.setAskChuaiNum(1);
					// 初始化 动作统计
					feiChuActionInfos = getSameList(feiChuPlayers);
					feiChuActionInfos.remove(0);
					break;
				}
				if (type == Cnst.ROOM_PALYTYPE_AN) {
					//重新 筛选揪储 的玩家
					List<Long> newjiuChu = new ArrayList<Long>();
					for(int i=0;i<players.size();i++){						
						Long feiChuUserId = players.get(i).userId;
						if(userId.equals(feiChuUserId)){
							continue;
						}
						if(GameUtil.checkHong(players.get(i).getCurrentCardList(), 13)){
							newjiuChu.add(feiChuUserId);						
						}
					}
					room.setJiuChu(newjiuChu);
					if (room.getJiuChu() == null
							|| room.getJiuChu().size() == 0) {
						// 没人可以反踹 开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					} else {
						playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_FANCHUAI);
						nextActionUserId = room.getJiuChu().get(0);

						// 初始化 动作统计
						feiChuActionInfos = getSameList(room.getJiuChu());
						feiChuActionInfos.remove(0);
						//储包踹
						chuActionInfos = new ArrayList<Long>();
						if(GameUtil.checkHong(player.getCurrentCardList(), 13)){
							chuActionInfos.add(userId);
						}
						break;
					}
				}
			}

			break;
		// =============================================================>缴
		case Cnst.ACTION_JIAO:
			if (playStatus != Cnst.ROOM_PALYSTATE_JIAOPAI) {
				System.out.println("======================>此玩家不能缴牌");
				return;
			}
			// 规定是 同伙有一人不缴 则跳过 对方缴牌
			// 独储缴牌
			if (chuPlayers.size() == 1 && chuActionInfos.contains(userId)) {
				// 当局结束
				playStatus = Cnst.ROOM_PALYSTATE_START;
				room.setState(Cnst.ROOM_STATE_XJS);
				player.setPlayStatus(Cnst.PLAYER_STATE_JIAO);
				break;
			}
			// 储方缴牌 第一个人缴牌 如果不缴 直接清除 chuActionInfo
			if (chuActionInfos.size() == 2) {
				ifContinue = 0;
				nextActions.add(Cnst.ACTION_TONGYI);
				nextActions.add(Cnst.ACTION_JUJUE);
				nextActionUserId = chuPlayers.get(1);
				break;
			}
			// 非储方在缴牌 非储方可能有3个人啊
			if (chuActionInfos.size() == 0 && feiChuActionInfos.size() != 0) {
				ifContinue = 0;
				nextActions.add(Cnst.ACTION_TONGYI);
				nextActions.add(Cnst.ACTION_JUJUE);
				nextActionUserId = feiChuPlayers.get(1);// 非储方第二人来选择
				break;
			}
			break;
		// =============================================================>包踹
		case Cnst.ACTION_BAOCHUAI:
			if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI) {
				// 非储方第一人包踹
				ifContinue = 0;
				nextActions.add(Cnst.ACTION_TONGYI);
				nextActions.add(Cnst.ACTION_JUJUE);
				room.setFeiChuBaoChuai(userId);
				nextActionUserId = feiChuActionInfos.get(0);// 非储方储方第二人来选择
				feiChuActionInfos.remove(0);
			} else if (playStatus == Cnst.ROOM_PALYSTATE_CHUBAOCHUAI) {
				// 储方第一人包踹
				ifContinue = 0;
				nextActions.add(Cnst.ACTION_TONGYI);
				nextActions.add(Cnst.ACTION_JUJUE);
				room.setChuBaoChuai(userId);
				nextActionUserId = chuActionInfos.get(0);// 储方第二人来选择
				chuActionInfos.remove(0);
			}
			break;
			// =========================================================>踹
		case Cnst.ACTION_CHUAI:
			if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUCHUAI) {
				// 非储方第一人踹
				if(feiChuActionInfos.size() == 0){
					room.setDiFen(room.getDiFen()*2);
					fanOverValue = String.valueOf(Cnst.ACTION_CHUAI);
					if(fanInfo.get(fanOverValue)==null){
						fanInfo.put(fanOverValue, 2);
					}else{
						fanInfo.put(fanOverValue, StringUtils.parseInt(fanInfo.get(fanOverValue))*2);
					}
					room.setHasChuaiCirle(room.getHasChuaiCirle()==null?1:room.getHasChuaiCirle()+1);
					if(chuPlayers.size() == 1){
						//一人独储 这时直接踹
						playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = chuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						chuActionInfos = getSameList(chuPlayers);
						chuActionInfos.remove(0);
						break;
					}else if(room.getHasChuaiCirle() == 1){
						//第一圈包踹
						playStatus = Cnst.ROOM_PALYSTATE_CHUBAOCHUAI;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_BAOCHUAI);
						nextActionUserId = chuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						chuActionInfos = getSameList(chuPlayers);
						chuActionInfos.remove(0);
						break;
					}else{
						//第二圈直接踹
						playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = chuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						chuActionInfos = getSameList(chuPlayers);
						chuActionInfos.remove(0);
						break;
					}					
				}else{
					room.setPingChuaiFeiChu(userId);
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_TONGYI);
					nextActions.add(Cnst.ACTION_JUJUE);
					nextActionUserId = feiChuActionInfos.get(0);// 非储方第二人来选择
					feiChuActionInfos.remove(0);
				}
				
			} else if (playStatus == Cnst.ROOM_PALYSTATE_CHUCHUAI) {
				//独储
				if(chuPlayers.size() == 1){
					room.setDiFen(room.getDiFen()*2);
					fanOverValue  = String.valueOf(Cnst.ACTION_CHUAI);
					if(fanInfo.get(fanOverValue)==null){
						fanInfo.put(fanOverValue, 2);
					}else{
						fanInfo.put(fanOverValue, StringUtils.parseInt(fanInfo.get(fanOverValue))*2);
					}
					if (room.getChuaiCircle() == room.getHasChuaiCirle()) {
						// 开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}
					if (room.getChuaiCircle() > room.getHasChuaiCirle()) {
						// 非储继续踹
						playStatus = Cnst.ROOM_PALYSTATE_FEICHUCHUAI;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = feiChuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						feiChuActionInfos = getSameList(feiChuPlayers);
						feiChuActionInfos.remove(0);
						//给前端 第二 三回合圈数
						info.put("pingChuaiCircle", room.getHasChuaiCirle()+1);
						break;
					}
				}else{
					// 储方第一人踹
					room.setPingChuaiChu(userId);
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_TONGYI);
					nextActions.add(Cnst.ACTION_JUJUE);
					nextActionUserId = chuActionInfos.get(0);// 储方第二人来选择
					chuActionInfos.remove(0);
				}				
			}
			break;
		// =========================================================>不踹
		case Cnst.ACTION_BUCHUAI:
			if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUCHUAI) {
				// 非储方第一人踹
				if(feiChuActionInfos.size() == 0){
					//有人包踹
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				}else{
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_TONGYI);
					nextActions.add(Cnst.ACTION_JUJUE);
					nextActionUserId = feiChuActionInfos.get(0);// 非储方第二人来选择
					feiChuActionInfos.remove(0);
					break;
				}
				
			} else if (playStatus == Cnst.ROOM_PALYSTATE_CHUCHUAI) {
				// 储方第一人踹
				if(chuActionInfos.size() == 0){
					//有人包踹
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				}else{
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_TONGYI);
					nextActions.add(Cnst.ACTION_JUJUE);
					nextActionUserId = chuActionInfos.get(0);// 非储方第二人来选择
					chuActionInfos.remove(0);
					break;
				}
			}
			break;
		// =============================================================>同意
		case Cnst.ACTION_TONGYI:
			// 缴牌
			if (playStatus == Cnst.ROOM_PALYSTATE_JIAOPAI) {
				// 储方缴牌
				if (chuActionInfos.size() != 0) {
					// 游戏结束
					for (int i = 0; i < chuPlayers.size(); i++) {
						// 设置两个状态为 jiao
						Player p1 = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers
								.get(i)));
						if(p1.getUserId().equals(userId)){
							p1 = player;
						}
						p1.setPlayStatus(Cnst.PLAYER_STATE_JIAO);
						RedisUtil.updateRedisData(null, p1);
					}
					playStatus = Cnst.ROOM_PALYSTATE_START;
					room.setState(Cnst.ROOM_STATE_XJS);
					break;
				}
				// 非储方缴牌 2人
				if (chuActionInfos.size() == 0 && feiChuActionInfos.size() == 2) {
					// 游戏结束
					for (int i = 0; i < feiChuPlayers.size(); i++) {
						// 设置两个状态为 jiao
						Player p1 = RedisUtil.getPlayerByUserId(String.valueOf(feiChuPlayers
								.get(i)));
						if(p1.getUserId().equals(userId)){
							p1 = player;
						}
						p1.setPlayStatus(Cnst.PLAYER_STATE_JIAO);
						RedisUtil.updateRedisData(null, p1);
					}
					playStatus = Cnst.ROOM_PALYSTATE_START;
					room.setState(Cnst.ROOM_STATE_XJS);
					break;
				}
				// 非储方 缴牌3人 第二人同意
				if (chuActionInfos.size() == 0 && feiChuActionInfos.size() == 3) {
					// 第二人同意
					if (feiChuActionInfos.get(1).equals(userId)) {
						ifContinue = 0;
						nextActions.add(Cnst.ACTION_TONGYI);
						nextActions.add(Cnst.ACTION_JUJUE);
						nextActionUserId = feiChuPlayers.get(2);// 非储方第三人来选择
						break;
					}
					// 第三人同意
					if (feiChuActionInfos.get(2).equals(userId)) {
						// 游戏结束 rs.size();i++){

						for (int i = 0; i < feiChuPlayers.size(); i++) {
							// 设置两个状态为 jiao
							Player p1 = RedisUtil
									.getPlayerByUserId(String.valueOf(feiChuPlayers.get(i)));
							if(p1.getUserId().equals(userId)){
								p1 = player;
							}
							p1.setPlayStatus(Cnst.PLAYER_STATE_JIAO);
							RedisUtil.updateRedisData(null, p1);
						}
						playStatus = Cnst.ROOM_PALYSTATE_START;
						room.setState(Cnst.ROOM_STATE_XJS);
						break;

					}
				}
			}
			// 非储包踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI) {
				if (feiChuActionInfos.size() == 0) {
					// 所有人同意 更新缴牌人集合 从出牌集合中移除
					List<Long> jiaoList = getSameList(feiChuPlayers);
					jiaoList.remove(room.getFeiChuBaoChuai());
					info.put("jiaoList", jiaoList);
					feiChuPlayers.clear();
					feiChuPlayers.add(room.getFeiChuBaoChuai());
					room.setFeiChuJiaoList(jiaoList);
					for (Long id : jiaoList) {
						room.getRealPlayerIds().remove(id);
						// 是否把玩家players设置为缴
						Player p1 = RedisUtil.getPlayerByUserId(String.valueOf(id));
						if(id.equals(userId)){
							p1 = player;
						}
						p1.setPlayStatus(Cnst.PLAYER_STATE_JIAO);
						RedisUtil.updateRedisData(null, p1);
					}

					// 设置baoChuaiState
					room.setBaoChuaiState(1);
					// 增加底分
					room.setDiFen(room.getDiFen() * 4);
					// 添加 包踹人的fanInfo
					Player baoChuai = RedisUtil.getPlayerByUserId(String.valueOf(room
							.getFeiChuBaoChuai()));
					baoChuai.getFanInfo().put(String.valueOf(Cnst.ACTION_BAOCHUAI), 4);
					RedisUtil.updateRedisData(null, baoChuai);
					//重新 筛选揪储 的玩家
					List<Long> newjiuChu = new ArrayList<Long>();
					if(GameUtil.checkHong(baoChuai.getCurrentCardList(), 13)){
						newjiuChu.add(baoChuai.getUserId());						
					}								
					room.setJiuChu(newjiuChu);
					if (type == Cnst.ROOM_PALYTYPE_MING) {
						// 平踹圈数
						if (room.getChuaiCircle() == 0) {
							// 直接开局
							playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
							nextActionUserId = room.getHongXinThree();
							break;
						} else {
							playStatus = Cnst.ROOM_PALYSTATE_FEICHUCHUAI;
							nextActions.add(Cnst.ACTION_CHUAI);
							nextActions.add(Cnst.ACTION_BUCHUAI);

							nextActionUserId = feiChuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							feiChuActionInfos = getSameList(feiChuPlayers);
							feiChuActionInfos.remove(0);
							info.put("pingChuaiCircle", 1);
							break;
						}
					} else {
						// 暗储
						if(chuPlayers.size() == 1){
							//独储 跳过包踹
							chuActionInfos.clear();
							//判断储方是否可以反踹
							for(int i=0;i<chuPlayers.size();i++){
								Player p = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers.get(i)));
								if(GameUtil.checkHong(p.getCurrentCardList(), 13)){
									chuActionInfos.add(p.getUserId());
								}
							}
							if(chuActionInfos.size() == 0){
								// 开局
								playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
								nextActionUserId = room.getHongXinThree();
								break;
							}else{
								//储方先反踹
								playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
								nextActions.add(Cnst.ACTION_GUO);
								nextActions.add(Cnst.ACTION_FANCHUAI);
								nextActionUserId = chuActionInfos.get(0);
								chuActionInfos.remove(0);
								feiChuActionInfos = getSameList(room.getJiuChu());//反踹用
								break;
							}
						}else{
							playStatus = Cnst.ROOM_PALYSTATE_CHUBAOCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_BAOCHUAI);
							nextActionUserId = chuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							chuActionInfos = getSameList(chuPlayers);
							chuActionInfos.remove(0);
							break;
						}					
					}

				} else {
					// 继续下一个人询问
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_TONGYI);
					nextActions.add(Cnst.ACTION_JUJUE);
					nextActionUserId = feiChuActionInfos.get(0);// 非储方第三人来选择
					feiChuActionInfos.remove(0);
					break;
				}
			}
			// 非储踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUCHUAI) {
				if (feiChuActionInfos.size() == 0) {
					// 同意分两种情况 踹和不踹
					if (room.getLastAction() == Cnst.ACTION_CHUAI
							|| (room.getLastAction() == Cnst.ACTION_TONGYI && room.getPingChuaiFeiChu()!=null)) {
						// 所有人同意
						room.setFeiChuChuai(true);
						// 增加底分
						room.setDiFen(room.getDiFen() * 2);
						// 增加平踹圈数
						room.setHasChuaiCirle(room.getHasChuaiCirle()==null?1:room.getHasChuaiCirle()+1);
						// 添加 fanInfo
						
						Player p = RedisUtil.getPlayerByUserId(String.valueOf(room.getPingChuaiFeiChu()));
						fanOverValue = String.valueOf(Cnst.ACTION_CHUAI);
						if(p.getFanInfo().get(fanOverValue)==null){
							p.getFanInfo().put(fanOverValue, 2);
						}else{
							p.getFanInfo().put(fanOverValue, StringUtils.parseInt(p.getFanInfo().get(fanOverValue))*2);
						}
						RedisUtil.updateRedisData(null, p);
						
						if(chuPlayers.size() == 1){
							//一人独储 这时直接踹
							playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
							nextActions.add(Cnst.ACTION_CHUAI);
							nextActions.add(Cnst.ACTION_BUCHUAI);
							nextActionUserId = chuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							chuActionInfos = getSameList(chuPlayers);
							chuActionInfos.remove(0);
							break;
						}else if(room.getHasChuaiCirle() == 1){
							//第一圈包踹
							playStatus = Cnst.ROOM_PALYSTATE_CHUBAOCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_BAOCHUAI);
							nextActionUserId = chuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							chuActionInfos = getSameList(chuPlayers);
							chuActionInfos.remove(0);
							break;
						}else{
							//第二圈直接踹
							playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
							nextActions.add(Cnst.ACTION_CHUAI);
							nextActions.add(Cnst.ACTION_BUCHUAI);
							nextActionUserId = chuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							chuActionInfos = getSameList(chuPlayers);
							chuActionInfos.remove(0);
							break;
						}					
					} else {
						// 不踹 直接开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}
				} else {
					// 继续下一个人询问
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_TONGYI);
					nextActions.add(Cnst.ACTION_JUJUE);
					nextActionUserId = feiChuActionInfos.get(0);// 非储方第三人来选择
					feiChuActionInfos.remove(0);
					break;
				}
			}
			// 储包踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_CHUBAOCHUAI) {
				// 储最多俩人
				List<Long> jiaoList = getSameList(chuPlayers);
				jiaoList.remove(room.getChuBaoChuai());
				info.put("jiaoList", jiaoList);
				// jiaoPaiPlayers.put(2, jiaoList);
				chuPlayers.clear();
				chuPlayers.add(room.getChuBaoChuai());
				room.setChuJiaoList(jiaoList);
				for (Long id : jiaoList) {
					room.getRealPlayerIds().remove(id);
					// 是否把玩家players设置为缴
					Player p1 = RedisUtil.getPlayerByUserId(String.valueOf(id));
					if(id.equals(userId)){
						p1 = player;
					}
					p1.setPlayStatus(Cnst.PLAYER_STATE_JIAO);
					RedisUtil.updateRedisData(null, p1);
				}
				if (room.getBaoChuaiState()!=null && room.getBaoChuaiState() == 1) {
					room.setBaoChuaiState(3);
				} else {
					room.setBaoChuaiState(2);
				}
				// 增加底分
				room.setDiFen(room.getDiFen() * 4);
				// 添加 包踹人的fanInfo
				Player baoChuai = RedisUtil.getPlayerByUserId(String.valueOf(room
						.getChuBaoChuai()));
				baoChuai.getFanInfo().put(String.valueOf(Cnst.ACTION_BAOCHUAI), 4);
				RedisUtil.updateRedisData(null, baoChuai);

				if (type == Cnst.ROOM_PALYTYPE_MING) {

					// 储踹的状态
					playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
					nextActions.add(Cnst.ACTION_CHUAI);
					nextActions.add(Cnst.ACTION_BUCHUAI);
					nextActionUserId = chuPlayers.get(0);
					room.setAskChuaiNum(1);
					// 初始化 动作统计
					chuActionInfos = getSameList(chuPlayers);
					chuActionInfos.remove(0);
					break;
				} else {
					chuActionInfos.clear();
					//判断储方是否可以反踹
					for(int i=0;i<chuPlayers.size();i++){
						Player p = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers.get(i)));
						if(GameUtil.checkHong(p.getCurrentCardList(), 13)){
							chuActionInfos.add(p.getUserId());
						}
					}
					if(chuActionInfos.size() == 0){
						// 开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}else{
						//储方先反踹
						playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_FANCHUAI);
						nextActionUserId = chuActionInfos.get(0);
						chuActionInfos.remove(0);
						feiChuActionInfos = getSameList(room.getJiuChu());//反踹用
						break;
					}
				}
			}
			// 储踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_CHUCHUAI) {
				// 同意的是踹 还是不踹
				if (room.getLastAction() == Cnst.ACTION_CHUAI) {

					// 增加底分
					room.setDiFen(room.getDiFen() * 2);
				
					Player p = RedisUtil.getPlayerByUserId(String.valueOf(room.getPingChuaiChu()));
					fanOverValue = String.valueOf(Cnst.ACTION_CHUAI);
					if(p.getFanInfo().get(fanOverValue)==null){
						p.getFanInfo().put(fanOverValue, 2);
					}else{
						p.getFanInfo().put(fanOverValue, StringUtils.parseInt(p.getFanInfo().get(fanOverValue))*2);
					}
					RedisUtil.updateRedisData(null, p);					

					if (room.getChuaiCircle() == room.getHasChuaiCirle()) {
						// 开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}
					if (room.getChuaiCircle() > room.getHasChuaiCirle()) {
						// 非储继续踹
						playStatus = Cnst.ROOM_PALYSTATE_FEICHUCHUAI;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = feiChuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						feiChuActionInfos = getSameList(feiChuPlayers);
						feiChuActionInfos.remove(0);
						//给前端 第二 三回合圈数
						info.put("pingChuaiCircle", room.getHasChuaiCirle()+1);
						break;
					}
				} else {
					// 开局
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				}
			}
			break;
		// =============================================================>拒绝
		case Cnst.ACTION_JUJUE:
			// 缴牌状态
			if (playStatus == Cnst.ROOM_PALYSTATE_JIAOPAI) {
				// 储方缴牌
				if (chuActionInfos.size() != 0) {
					chuActionInfos.clear();// 清空动作统计
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_JIAO);

					nextActionUserId = feiChuPlayers.get(0);// 非储方第一人来选择
					break;
				} else {
					// 非储方有两人或者三人都无所谓 直接结束 下一环节
					feiChuActionInfos.clear();
					// 结束缴牌环节
					if (type == Cnst.ROOM_PALYTYPE_MING) {

						// 双方已轮训完 判断是否有人可以撑储 没有则包踹环节

						if (room.getChengChu() == 1 && room.getChuPlayers().size()>1
								&& room.getCanDuChuId() != null && feiChuPlayers.contains(room.getCanDuChuId())) {
							// 撑储环节
							playStatus = Cnst.ROOM_PALYSTATE_CHENGCHU;
							nextActionUserId = room.getCanDuChuId();
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_CHENGCHU);
							break;

						} else {
							// 进行包踹环节 非储方
							playStatus = Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_BAOCHUAI);
							nextActionUserId = feiChuPlayers.get(0);
							room.setAskChuaiNum(1);// 初始化轮训次数
							// 初始化 动作统计
							feiChuActionInfos = getSameList(feiChuPlayers);
							feiChuActionInfos.remove(0);
							break;
						}

					} else {
						// 暗储
						playStatus = Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_BAOCHUAI);
						nextActionUserId = feiChuPlayers.get(0);
						room.setAskChuaiNum(1);// 初始化轮训次数
						// 初始化 动作统计
						feiChuActionInfos = getSameList(feiChuPlayers);
						feiChuActionInfos.remove(0);
						break;
					}
				}
			}
			// 非储方包踹状态
			else if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI) {
				// 拒绝后 换另外一个人开始询问 是否包踹 根据房间lastAction 进行统计判断
				if (room.getAskChuaiNum() == room.getFeiChuPlayers().size()) {
					// 全部拒绝 结束 进行踹环节
					if (type == Cnst.ROOM_PALYTYPE_MING) {
						// 平踹圈数
						if (room.getChuaiCircle() == 0) {
							// 直接开局
							playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
							nextActionUserId = room.getHongXinThree();
							break;
						} else {
							playStatus = Cnst.ROOM_PALYSTATE_FEICHUCHUAI;
							nextActions.add(Cnst.ACTION_CHUAI);
							nextActions.add(Cnst.ACTION_BUCHUAI);

							nextActionUserId = feiChuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							feiChuActionInfos = getSameList(feiChuPlayers);
							feiChuActionInfos.remove(0);
							info.put("pingChuaiCircle", 1);
							break;
						}

					} else {
						// 暗储
						if(chuPlayers.size() == 1){
							//独储 跳过包踹
							chuActionInfos.clear();
							//判断储方是否可以反踹
							for(int i=0;i<chuPlayers.size();i++){
								Player p = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers.get(i)));
								if(GameUtil.checkHong(p.getCurrentCardList(), 13)){
									chuActionInfos.add(p.getUserId());
								}
							}
							if(chuActionInfos.size() == 0){
								// 开局
								playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
								nextActionUserId = room.getHongXinThree();
								break;
							}else{
								//储方先反踹
								playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
								nextActions.add(Cnst.ACTION_GUO);
								nextActions.add(Cnst.ACTION_FANCHUAI);
								nextActionUserId = chuActionInfos.get(0);
								chuActionInfos.remove(0);
								break;
							}
						}else{
							playStatus = Cnst.ROOM_PALYSTATE_CHUBAOCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_BAOCHUAI);
							nextActionUserId = chuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							chuActionInfos = getSameList(chuPlayers);
							chuActionInfos.remove(0);
							break;
						}	
					}
				} else {
					// 重置动作统计 ,下一轮询问
					feiChuActionInfos = getSameList(feiChuPlayers);
					if(room.getAskChuaiNum() == 2 && room.getFeiChuPlayers().size() == 3){
						//最后一个先询问 第一个 第二个
						feiChuActionInfos.remove(2);
						feiChuActionInfos.add(0,feiChuPlayers.get(2));
					}else{
						feiChuActionInfos.remove(1);
						feiChuActionInfos.add(0,feiChuPlayers.get(1));// 添至最后一位询问
					}									
					room.setAskChuaiNum(room.getAskChuaiNum() + 1);
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_BAOCHUAI);
					nextActionUserId = feiChuActionInfos.get(0);
					feiChuActionInfos.remove(0);
					break;
				}
			}
			// 非储方踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUCHUAI) {
				if(room.getLastAction() == Cnst.ACTION_BUCHUAI || (room.getLastAction() == Cnst.ACTION_TONGYI && room.getPingChuaiFeiChu()==null)){
					//如果拒绝不踹 继续询问
					if(room.getAskChuaiNum() == room.getFeiChuPlayers().size()){
						//应该直接开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}else{
						feiChuActionInfos = getSameList(feiChuPlayers);
						if(room.getAskChuaiNum() == 2 && room.getFeiChuPlayers().size() == 3){
							//最后一个先询问 第一个 第二个
							feiChuActionInfos.remove(2);
							feiChuActionInfos.add(0,feiChuPlayers.get(2));
						}else{
							feiChuActionInfos.remove(1);
							feiChuActionInfos.add(0,feiChuPlayers.get(1));// 添至最后一位询问
						}
						room.setAskChuaiNum(room.getAskChuaiNum() + 1);
						ifContinue = 0;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = feiChuActionInfos.get(0);
						feiChuActionInfos.remove(0);
						break;
					}
				}else{
					//应该直接开局
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				}
				

			}
			// 储包踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_CHUBAOCHUAI) {
				if (room.getAskChuaiNum() == room.getChuPlayers().size()) {
					// 全部拒绝 结束 进行踹环节
					if (type == Cnst.ROOM_PALYTYPE_MING) {
						// 储踹的状态
						playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = chuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						chuActionInfos = getSameList(chuPlayers);
						chuActionInfos.remove(0);
						break;
					} else {
						chuActionInfos.clear();
						//判断储方是否可以反踹
						for(int i=0;i<chuPlayers.size();i++){
							Player p = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers.get(i)));
							if(GameUtil.checkHong(p.getCurrentCardList(), 13)){
								chuActionInfos.add(p.getUserId());
							}
						}
						if(chuActionInfos.size() == 0){
							// 开局
							playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
							nextActionUserId = room.getHongXinThree();
							break;
						}else{
							//储方先反踹
							playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_FANCHUAI);
							nextActionUserId = chuActionInfos.get(0);
							chuActionInfos.remove(0);
							feiChuActionInfos = getSameList(room.getJiuChu());//反踹用
							break;
						}
					}
				} else {
					// 重置动作统计 ,下一轮询问
					chuActionInfos = getSameList(chuPlayers);
					room.setAskChuaiNum(room.getAskChuaiNum() + 1);
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_BAOCHUAI);
					nextActionUserId = chuActionInfos.get(1);
					chuActionInfos.remove(1);
					break;
				}
			}
			// 储方踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_CHUCHUAI) {
				// 有人拒绝后 可以直接进行下一环节
				if(room.getLastAction() == Cnst.ACTION_BUCHUAI){
					if (room.getAskChuaiNum() == chuPlayers.size()) {
						// 开局发牌
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					} else {
						chuActionInfos = getSameList(chuPlayers);
						chuActionInfos.remove(room.getAskChuaiNum());
						chuActionInfos.add(0,chuActionInfos.get(room.getAskChuaiNum()));// 添至最后一位询问
						room.setAskChuaiNum(room.getAskChuaiNum() + 1);
						ifContinue = 0;
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = chuActionInfos.get(0);
						chuActionInfos.remove(0);
						break;
					}
				}else{
					// 开局发牌
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				}
			}
			break;
		// =============================================================>过
		case Cnst.ACTION_GUO:
			// 梦宣
			if (playStatus == Cnst.ROOM_PALYSTATE_MENGXUAN) {
				players = RedisUtil.getPlayerList(room);
				canMengXuan.remove(userId);
				if (canMengXuan.size() == 0) {
					// 放弃梦宣
					chuPlayers = new ArrayList<Long>();
					feiChuPlayers = new ArrayList<Long>();
					room.setChuColor(room.getLastFaCard().getType());
					chuColor = room.getChuColor();
					for (int i = 0; i < players.size(); i++) {
						currentCardList = players.get(i).getCurrentCardList();
						Long thisUserId = players.get(i).getUserId();
						in: for (Card c : currentCardList) {
							if (chuColor == 1 || chuColor == 3) {
								if (c.getSymble() == 1
										&& (c.getType() == 1 || c.getType() == 3)) {
									// 设置成储方
									chuPlayers.add(thisUserId);
									break in;
								}
							}
							if (chuColor == 2 || chuColor == 4) {
								if (c.getSymble() == 1
										&& (c.getType() == 2 || c.getType() == 4)) {
									// 设置成储方
									chuPlayers.add(thisUserId);
									break in;
								}
							}
						}
						if (!chuPlayers.contains(thisUserId)) {
							// 设置成非储
							feiChuPlayers.add(thisUserId);
						}
						// 没人梦宣 是否有人可以撑储 有人梦宣 则在执行梦宣动作 时 在判断一次
						if (!chuPlayers.contains(thisUserId) &&
								GameUtil.checkDuChu(currentCardList,room.getChuColor()) ) {
							room.setCanDuChuId(thisUserId);
						}

					}
					room.setChuPlayers(chuPlayers);
					room.setFeiChuPlayers(feiChuPlayers);
					// 更新players
					if (canMengChuai == null || canMengChuai.size() == 0) {
						// 这里要扣除 梦踹到自己身上的分数

						// 进入缴牌状态
						playStatus = Cnst.ROOM_PALYSTATE_JIAOPAI;
						nextActionUserId = chuPlayers.get(0);
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_JIAO);

						// 初始化 动作统计
						chuActionInfos = getSameList(chuPlayers);
						feiChuActionInfos = getSameList(feiChuPlayers);
						break;
					} else {
						// 梦踹状态
						playStatus = Cnst.ROOM_PALYSTATE_MENGCHUAI;
						nextActionUserId = canMengChuai.get(0);
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_MENGCHUAI);
						break;
					}
				} else {
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_MENGXUAN);

					nextActionUserId = canMengXuan.get(0);
					break;
				}
			}
			// 梦踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_MENGCHUAI) {
				canMengChuai.remove(userId);
				if (canMengChuai.size() == 0) {
					// 进入缴牌状态
					playStatus = Cnst.ROOM_PALYSTATE_JIAOPAI;
					nextActionUserId = chuPlayers.get(0);
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_JIAO);

					// 初始化 动作统计
					chuActionInfos = getSameList(chuPlayers);
					feiChuActionInfos = getSameList(feiChuPlayers);
					break;
				} else {
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_MENGCHUAI);

					nextActionUserId = canMengChuai.get(0);
					break;
				}
			}
			// 撑储
			else if (playStatus == Cnst.ROOM_PALYSTATE_CHENGCHU) {
				
				if (type == Cnst.ROOM_PALYTYPE_MING) {
					playStatus = Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_BAOCHUAI);
					nextActionUserId = feiChuPlayers.get(0);
					room.setAskChuaiNum(1);
					// 初始化 动作统计
					feiChuActionInfos = getSameList(feiChuPlayers);
					feiChuActionInfos.remove(0);
					break;
				}
				if (type == Cnst.ROOM_PALYTYPE_AN) {
					// 判断有人是否可以揪储
					room.setCanDuChuId(null);
					if (room.getJiuChu() == null
							|| room.getJiuChu().size() == 0) {
						// 没人可以揪储 开局
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					} else {
						playStatus = Cnst.ROOM_PALYSTATE_JIUCHU;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_JIUCHU);
						nextActionUserId = room.getJiuChu().get(0);

						// 初始化 动作统计
						feiChuActionInfos = getSameList(room.getJiuChu());
						feiChuActionInfos.remove(0);
						
						break;
					}
				}
				break;
			}
			// 缴牌状态
			else if (playStatus == Cnst.ROOM_PALYSTATE_JIAOPAI) {
				// 储方缴牌
				if (chuActionInfos.size() != 0) {
					chuActionInfos.clear();// 清空动作统计
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_JIAO);

					nextActionUserId = feiChuPlayers.get(0);// 非储方第一人来选择
					break;
				} else {
					// 非储方有两人或者三人都无所谓 直接结束 下一环节
					feiChuActionInfos.clear();
					// 结束缴牌环节
					if (type == Cnst.ROOM_PALYTYPE_MING) {

						// 双方已轮训完 判断是否有人可以撑储 没有则包踹环节

						if (room.getChengChu() == 1 && chuPlayers.size()>1
								&& room.getCanDuChuId() != null && feiChuPlayers.contains(room.getCanDuChuId())) {
							// 撑储环节
							playStatus = Cnst.ROOM_PALYSTATE_CHENGCHU;
							nextActionUserId = room.getCanDuChuId();
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_CHENGCHU);
							break;
						} else {
							// 进行包踹环节 非储方
							playStatus = Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_BAOCHUAI);
							nextActionUserId = feiChuPlayers.get(0);
							room.setAskChuaiNum(1);// 初始化轮训次数
							// 初始化 动作统计
							feiChuActionInfos = getSameList(feiChuPlayers);
							feiChuActionInfos.remove(0);
							break;
						}

					} else {
						// 暗储
						playStatus = Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_BAOCHUAI);
						nextActionUserId = feiChuPlayers.get(0);
						room.setAskChuaiNum(1);// 初始化轮训次数
						// 初始化 动作统计
						feiChuActionInfos = getSameList(feiChuPlayers);
						feiChuActionInfos.remove(0);
						break;
					}
				}
			}
			// 非储包踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_FEICHUBAOCHUAI) {
				if (room.getAskChuaiNum() == room.getFeiChuPlayers().size()) {
					// 全部拒绝 结束 进行踹环节
					if (type == Cnst.ROOM_PALYTYPE_MING) {
						// 平踹圈数
						if (room.getChuaiCircle() == 0) {
							// 直接开局
							playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
							nextActionUserId = room.getHongXinThree();
							break;
						} else {
							// 可以平踹
							playStatus = Cnst.ROOM_PALYSTATE_FEICHUCHUAI;
							nextActions.add(Cnst.ACTION_CHUAI);
							nextActions.add(Cnst.ACTION_BUCHUAI);
							nextActionUserId = feiChuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							feiChuActionInfos = getSameList(feiChuPlayers);
							feiChuActionInfos.remove(0);
							info.put("pingChuaiCircle", 1);
							break;
						}
					} else {
						// 暗储
						if(chuPlayers.size() == 1){
							//独储 跳过包踹
							chuActionInfos.clear();
							//判断储方是否可以反踹
							for(int i=0;i<chuPlayers.size();i++){
								Player p = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers.get(i)));
								if(GameUtil.checkHong(p.getCurrentCardList(), 13)){
									chuActionInfos.add(p.getUserId());
								}
							}
							if(chuActionInfos.size() == 0){
								// 开局
								playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
								nextActionUserId = room.getHongXinThree();
								break;
							}else{
								//储方先反踹
								playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
								nextActions.add(Cnst.ACTION_GUO);
								nextActions.add(Cnst.ACTION_FANCHUAI);
								nextActionUserId = chuActionInfos.get(0);
								chuActionInfos.remove(0);
								feiChuActionInfos = getSameList(room.getJiuChu());//反踹用
								break;
							}
						}else{
							playStatus = Cnst.ROOM_PALYSTATE_CHUBAOCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_BAOCHUAI);
							nextActionUserId = chuPlayers.get(0);
							room.setAskChuaiNum(1);
							// 初始化 动作统计
							chuActionInfos = getSameList(chuPlayers);
							chuActionInfos.remove(0);
							break;
						}
						
					}

				} else {
					// 重置动作统计 ,下一轮询问

					feiChuActionInfos = getSameList(feiChuPlayers);
					if(room.getAskChuaiNum() == 2 && room.getFeiChuPlayers().size() == 3){
						//最后一个先询问 第一个 第二个
						feiChuActionInfos.remove(2);
						feiChuActionInfos.add(0,feiChuPlayers.get(2));
					}else{
						feiChuActionInfos.remove(1);
						feiChuActionInfos.add(0,feiChuPlayers.get(1));// 添至最后一位询问
					}
					room.setAskChuaiNum(room.getAskChuaiNum() + 1);
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_BAOCHUAI);
					nextActionUserId = feiChuActionInfos.get(0);
					feiChuActionInfos.remove(0);
					break;
				}
			}

			// 储包踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_CHUBAOCHUAI) {
				if (room.getAskChuaiNum() >= room.getChuPlayers().size()) {
					// 全部拒绝 结束 进行踹环节
					if (type == Cnst.ROOM_PALYTYPE_MING) {

						// 储踹的状态
						playStatus = Cnst.ROOM_PALYSTATE_CHUCHUAI;
						
						nextActions.add(Cnst.ACTION_CHUAI);
						nextActions.add(Cnst.ACTION_BUCHUAI);
						nextActionUserId = chuPlayers.get(0);
						room.setAskChuaiNum(1);
						// 初始化 动作统计
						chuActionInfos = getSameList(chuPlayers);
						chuActionInfos.remove(0);
						break;
					} else {
						chuActionInfos.clear();
						//判断储方是否可以反踹
						for(int i=0;i<chuPlayers.size();i++){
							Player p = RedisUtil.getPlayerByUserId(String.valueOf(chuPlayers.get(i)));
							if(GameUtil.checkHong(p.getCurrentCardList(), 13)){
								chuActionInfos.add(p.getUserId());
							}
						}
						if(chuActionInfos.size() == 0){
							// 开局
							playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
							nextActionUserId = room.getHongXinThree();
							break;
						}else{
							//储方先反踹
							playStatus = Cnst.ROOM_PALYSTATE_FANCHUAI;
							nextActions.add(Cnst.ACTION_GUO);
							nextActions.add(Cnst.ACTION_FANCHUAI);
							nextActionUserId = chuActionInfos.get(0);
							chuActionInfos.remove(0);
							feiChuActionInfos = getSameList(room.getJiuChu());//反踹用
							break;
						}
					}
				} else { 
					// 重置动作统计 ,下一轮询问  询问第二个人
					chuActionInfos = getSameList(chuPlayers);
					room.setAskChuaiNum(room.getAskChuaiNum() + 1);
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_BAOCHUAI);
					nextActionUserId = chuActionInfos.get(1);
					chuActionInfos.remove(1);
					break;
				}
			}
			// 反踹
			else if (playStatus == Cnst.ROOM_PALYSTATE_FANCHUAI) {
	
				if(chuPlayers.contains(userId)){
					//储反踹
					if(chuActionInfos.size()!=0){
						ifContinue = 0;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_FANCHUAI);
						nextActionUserId = chuActionInfos.get(0);
						chuActionInfos.remove(0);
						break;
					}else{
						// 什么也不用处理 跳过开局就行
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}
				}else{
					if(feiChuActionInfos.size()!=0){
						ifContinue = 0;
						nextActions.add(Cnst.ACTION_GUO);
						nextActions.add(Cnst.ACTION_FANCHUAI);
						nextActionUserId = feiChuActionInfos.get(0);
						feiChuActionInfos.remove(0);
						break;
					}else{
						// 什么也不用处理 跳过开局就行
						playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
						nextActionUserId = room.getHongXinThree();
						break;
					}
				}
			}
			// 揪储
			else if (playStatus == Cnst.ROOM_PALYSTATE_JIUCHU) {
				if (feiChuActionInfos.size() == 0) {
					// 无人揪储 开局
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				} else {
					// 下个人揪储
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_JIUCHU);
					nextActionUserId = feiChuActionInfos.get(0);
					feiChuActionInfos.remove(0);
					break;
				}
			}
			break;
		// =============================================================>反踹
		case Cnst.ACTION_FANCHUAI:
			List<Integer> fanChuaiPai = room.getFanChuaiPai();
			if(fanChuaiPai == null){
				fanChuaiPai = new ArrayList<Integer>();
			}
			if(actionPai == 0 || fanChuaiPai.contains(actionPai%100)){
				illegalRequest(interfaceId, session);
				return;
			}
			room.setDiFen(room.getDiFen() * 2);
			fanInfo.put(String.valueOf(action), 2);
			currentCardList = player.getCurrentCardList();			
			if(actionPai != 0){
				fanChuaiPai.add(actionPai%100);
				room.setFanChuaiPai(fanChuaiPai);
			}
			if (chuPlayers.contains(userId)) {
				//储踹
				List<Card> newCards = new ArrayList<Card>();
				for (int i = 0; i < 13; i++) {
					if (fanChuaiPai.contains(currentCardList.get(i).getSymble())) {
						continue;
					}
					newCards.add(currentCardList.get(i));
				}
				// 重新检测可以继续反踹
				if (GameUtil.checkHong(newCards, newCards.size())) {
					chuActionInfos.add(0,userId);// 添加到 下一轮反踹集合中
				}
				// 检测非储方是否可以反踹
				if (feiChuActionInfos.size() == 0) {
					// 开局
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				} else {
					// 非储方继续反踹
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_FANCHUAI);
					nextActionUserId = feiChuActionInfos.get(0);
					feiChuActionInfos.remove(0);
					break;
				}

			} else {
				// 非储方反踹
				// 判断此玩家是否可以继续反踹 没有则移出队列
				List<Card> newCards = new ArrayList<Card>();
				for (int i = 0; i < 13; i++) {
					if (fanChuaiPai.contains(currentCardList.get(i).getSymble()) ) {
						continue;
					}
					newCards.add(currentCardList.get(i));
				}
				// 重新检测可以继续反踹
				if (GameUtil.checkHong(newCards, newCards.size())) {
					feiChuActionInfos.add(0,userId);// 添加到 下一轮反踹集合中
				}
				if (chuActionInfos.size() == 0) {
					// 储方不能踹
					// 开局
					playStatus = Cnst.ROOM_PALYSTATE_CHUPAI;
					nextActionUserId = room.getHongXinThree();
					break;
				} else {
					// 轮到 储方反踹
					ifContinue = 0;
					nextActions.add(Cnst.ACTION_GUO);
					nextActions.add(Cnst.ACTION_FANCHUAI);
					nextActionUserId = chuActionInfos.get(0);
					chuActionInfos.remove(0);
					break;
				}
			}
		// =============================================================>揪储
		case Cnst.ACTION_JIUCHU:
			// 增加底分
			room.setDiFen(room.getDiFen()*2);
			fanInfo.put(String.valueOf(action), 2);
			// 清空动作
			feiChuActionInfos.clear();
			room.setIfJiu(true);
			// 进入缴牌状态
			playStatus = Cnst.ROOM_PALYSTATE_JIAOPAI;
			nextActionUserId = chuPlayers.get(0);
			nextActions.add(Cnst.ACTION_GUO);
			nextActions.add(Cnst.ACTION_JIAO);

			// 初始化 动作统计
			chuActionInfos = getSameList(chuPlayers);
			feiChuActionInfos = getSameList(feiChuPlayers);
			break;
		}
 		currAction = nextActions;
		currActionUser = nextActionUserId;
		info.put("continue", ifContinue);
		info.put("nextAction", nextActions);
		info.put("nextActionUserId", nextActionUserId);

		info.put("action", action);
		info.put("actionPai", actionPais);
		info.put("userId", userId);
	
		if(room.getType() == Cnst.ROOM_PALYTYPE_MING){
			if(playStatus < Cnst.ROOM_PALYSTATE_JIAOPAI){
				if(room.getMengXuan() != null){
				JSONArray chu = new JSONArray();
				chu.add(room.getMengXuan());
				info.put("chuPlayers", chu);
				info.put("chuColors", room.getChuColor());	
				}
			}else{
				info.put("chuPlayers", room.getChuPlayers());
				info.put("chuColors", room.getChuColor());	
			}
		}else{
			if(room.getIfJiu() == null || room.getIfJiu() == false){
				
			}else{
				info.put("chuPlayers", room.getChuPlayers());
			}
			info.put("chuColors", room.getChuColor());
		}
		
		info.put("diFen", room.getDiFen());

		// 更新缓存
		player.setFanInfo(fanInfo);
		room.setCurrAction(currAction);
		room.setCurrActionUser(currActionUser);
		room.setLastAction(newLastAction);
		room.setLastActionUser(newLastActionUserId);
		room.setLastActionPai(actionPais);
		room.setPlayStatus(playStatus);
		room.setChuActionInfos(chuActionInfos);
		room.setFeiChuActionInfos(feiChuActionInfos);
		room.setCanMengXuan(canMengXuan);
		room.setCanMengChuai(canMengChuai);
		RedisUtil.updateRedisData(room, player);
		//回放用新的状态。。暂时没啥好想法
		info.put("fileBackPlayStatus",room.getPlayStatus());
		BackFileUtil.write(null, interfaceId,room,null,getNewObj(info));//写入文件内容
		
		if (room.getState() == Cnst.ROOM_STATE_XJS) {
			JieSuan.xiaoJieSuan(String.valueOf(roomId));
		}
		// 动作推送
		MessageFunctions.interface_100104(info, room, 100104);
		
		//缴牌前吧梦踹到自己身上的分去掉
		if (room.getMengChuai() != null && room.getMengChuai().size() != 0
				&& playStatus == Cnst.ROOM_PALYSTATE_JIAOPAI) {
			Set<Long> mengChuai = new HashSet<Long>(room.getMengChuai());
			for (int i = 0; i < room.getChuPlayers().size(); i++) {
				if (mengChuai.contains(room.getChuPlayers().get(i))) {
					Player p = RedisUtil.getPlayerByUserId(String.valueOf(room
							.getChuPlayers().get(i)));
					Integer fan = StringUtils.parseInt(p.getFanInfo().get(
							String.valueOf(Cnst.ACTION_MENGCHUAI)));
					room.setDiFen(room.getDiFen() / fan);
					mengChuai.remove(room.getChuPlayers().get(i));
					p.getFanInfo()
							.remove(String.valueOf(Cnst.ACTION_MENGCHUAI));
					RedisUtil.updateRedisData(null, p);
				}
			}
			room.setMengChuai(null);
			RedisUtil.updateRedisData(room, null);
		}
	}

	/**
	 * 查看包踹人牌
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100208(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("查看包踹人牌,interfaceId -> 100208");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		// String roomId = StringUtils.toString((readData.get("roomSn")));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId));
		if(p.getPlayStatus() != Cnst.PLAYER_STATE_JIAO){
			return;
		}
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(p.getRoomId()));
		Player baoChuaiPlayer = null;
		List<Integer> pais = new ArrayList<Integer>();
		JSONObject info = new JSONObject();
		if(room.getFeiChuJiaoList()!=null && room.getFeiChuJiaoList().contains(userId)){
			baoChuaiPlayer = RedisUtil.getPlayerByUserId(String.valueOf(room.getFeiChuBaoChuai()));
			for (Card c : baoChuaiPlayer.getCurrentCardList()) {
				pais.add(c.getOrigin());
			}
			info.put("userId", room.getFeiChuBaoChuai());
		}
		if(room.getChuJiaoList()!=null && room.getChuJiaoList().contains(userId)){
			baoChuaiPlayer = RedisUtil.getPlayerByUserId(String.valueOf(room.getChuBaoChuai()));
			for (Card c : baoChuaiPlayer.getCurrentCardList()) {
				pais.add(c.getOrigin());
			}
			info.put("userId", room.getChuBaoChuai());
		}		
		
	
		info.put("pais", pais);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 玩家申请解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public synchronized static void interface_100203(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("玩家请求解散房间,interfaceId -> 100203");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room.getDissolveRoom() != null) {
			return;
		}
		DissolveRoom dis = new DissolveRoom();
		dis.setDissolveTime(new Date().getTime());
		dis.setUserId(userId);
		List<Map<String, Object>> othersAgree = new ArrayList<>();
		List<Player> players = RedisUtil.getPlayerList(room);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				Map<String, Object> map = new HashMap<>();
				map.put("userId", p.getUserId());
				map.put("agree", 0);// 1同意；2解散；0等待
				othersAgree.add(map);
			}
		}
		dis.setOthersAgree(othersAgree);
		room.setDissolveRoom(dis);

		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", dis.getDissolveTime());
		info.put("userId", dis.getUserId());
		info.put("othersAgree", dis.getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions()
					.get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		for (Player p : players) {
			RedisUtil.updateRedisData(null, p);
		}

		RedisUtil.updateRedisData(room, null);
		
		//解散房间超时任务开启
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_2);
	}

	/**
	 * 同意或者拒绝解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */

	public synchronized static void interface_100204(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("同意或者拒绝解散房间,interfaceId -> 100203");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer userAgree = StringUtils.parseInt(readData.get("userAgree"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null) {// 房间已经自动解散
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_4);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		if (room.getDissolveRoom() == null) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_7);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		List<Map<String, Object>> othersAgree = room.getDissolveRoom()
				.getOthersAgree();
		for (Map<String, Object> m : othersAgree) {
			if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))) {
				m.put("agree", userAgree);
				break;
			}
		}
		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
		info.put("userId", room.getDissolveRoom().getUserId());
		info.put("othersAgree", room.getDissolveRoom().getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (userAgree == 2) {
			//有玩家拒绝解散房间
			room.setDissolveRoom(null);
			RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)), room,
					Cnst.ROOM_LIFE_TIME_CREAT);
		}
		int agreeNum = 0;
		int rejectNunm = 0;

		for (Map<String, Object> m : othersAgree) {
			if (m.get("agree").equals(1)) {
				agreeNum++;
			} else if (m.get("agree").equals(2)) {
				rejectNunm++;
			}
		}
		RedisUtil.updateRedisData(room, null);

		List<Player> players = RedisUtil.getPlayerList(room);

		if (agreeNum == 3 || rejectNunm >= 1) {
			if (agreeNum == 3) {
				//解散房间是 xiaoJSInfo 写入0
				if(room.getState() == Cnst.ROOM_STATE_GAMIING){
					//中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for(int i=0;i<4;i++){
						xiaoJSInfo.add(0);
					}
					room.addXiaoJSInfo(xiaoJSInfo);
				}
				
				room.setState(Cnst.ROOM_STATE_YJS);
				
				MessageFunctions.updateDatabasePlayRecord(room);
				for (Player p : players) {
					p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0, 0, 0, 0, 0,
							0, 0, 0);
				}
				room.setDissolveRoom(null);
				RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)),
						room, Cnst.ROOM_LIFE_TIME_DIS);
				RedisUtil.setPlayersList(players);
				//关闭解散房间计时任务
		        notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_2,false);
				// BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
			}
		}
		

		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions()
					.get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

	}

	/**
	 * 退出房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public synchronized static void interface_100205(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100205");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}
		if (room.getState() == Cnst.ROOM_STATE_CREATED) {
			List<Player> players = RedisUtil.getPlayerList(room);
			Map<String, Object> info = new HashMap<>();
			info.put("userId", userId);
			if (room.getCreateId().equals(userId)) {// 房主退出，
				if (room.getRoomType().equals(Cnst.ROOM_TYPE_1)) {// 房主模式
					int circle = room.getCircleNum();

				
					info.put("type", Cnst.EXIST_TYPE_DISSOLVE);

					for (Player p : players) {
						if (p.getUserId().equals(userId)) {
							p.setMoney(p.getMoney() + Cnst.moneyMap.get(circle));
							break;
						}
					}

					RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP
							.concat(String.valueOf(roomId)));

					for (Player p : players) {
						p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0, 0, 0,
								0, 0, 0, 0, 0);
					}
					 //关闭解散房间计时任务
                    notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,false);
				} else {// 自由模式，走正常退出
					info.put("type", Cnst.EXIST_TYPE_EXIST);
					existRoom(room, players, userId);
					RedisUtil.updateRedisData(room, null);
				}
			} else {// 正常退出
				info.put("type", Cnst.EXIST_TYPE_EXIST);
				existRoom(room, players, userId);
				RedisUtil.updateRedisData(room, null);
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());

			for (Player p : players) {
				RedisUtil.updateRedisData(null, p);
			}

			for (Player p : players) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}

		} else {
			roomIsGaming(interfaceId, session);
		}
	}

	private static void existRoom(RoomResp room, List<Player> players,
			Long userId) {
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0, 0, 0, 0, 0, 0,
						0, 0);
				break;
			}
		}
		Long[] pids = room.getPlayerIds();
		if (pids != null) {
			for (int i = 0; i < pids.length; i++) {
				if (userId.equals(pids[i])) {
					pids[i] = null;
					break;
				}
			}
		}
	}

	/**
	 * 语音表情
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100206(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100206");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		String userId = String.valueOf(readData.get("userId"));
		String type = String.valueOf(readData.get("type"));
		String idx = String.valueOf(readData.get("idx"));

		Map<String, Object> info = new HashMap<>();
		info.put("roomId", roomId);
		info.put("userId", userId);
		info.put("type", type);
		info.put("idx", idx);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = RedisUtil.getPlayerList(roomId);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}
		}
	}

	public static synchronized List<Long> getSameList(List<Long> list) {
		List<Long> list1 = new ArrayList<Long>();
		if (list == null) {
			return list1;
		} else {
			for (int i = 0; i < list.size(); i++) {
				list1.add(list.get(i));
			}
			return list1;
		}
	}
	public static synchronized List<Long> getAllExistList(List<Long> list1,List<Long> list2){
		List<Long> list3 = new ArrayList<Long>();
		if(list1 == null || list1.size() == 0){
			return list3;
		}
		if(list2 == null || list2.size() == 0){
			return list3;
		}
		for(int i=0;i<list1.size();i++){
			if(list2.contains(list1.get(i))){
				list3.add(list1.get(i));
			}
		}
		return list3;
	}
}
