package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.redis.RedisUtil;

public class JieSuan {
	public static void xiaoJieSuan(String roomId){
		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId);
		List<Player> players = RedisUtil.getPlayerList(room);
		//判断 hasEndPlayerIds  0人 缴牌
		List<Long> hasEndPlayerIds = room.getHasEndPlayerIds();
		List<Long> chuPlayers = room.getChuPlayers();
		List<Long> feiChuPlayers = room.getFeiChuPlayers();
		Integer BaoChuaiState = room.getBaoChuaiState();
		List<Long> feiChuJiaoList = room.getFeiChuJiaoList();
		List<Long> chuJiaoList = room.getChuJiaoList();
 		
		//设置储个数
		for(Player player : players){
			if(chuPlayers.contains(player.getUserId())){
				player.setChu(true);
				player.setChuNum(player.getChuNum()+1);				
			}
			if(feiChuPlayers.contains(player.getUserId())){
				player.setChu(false);
			}
			if(feiChuJiaoList != null && feiChuJiaoList.contains(player.getUserId())){
				player.setChu(false);
			}
			if(chuJiaoList != null && chuJiaoList.contains(player.getUserId())){
				player.setChu(true);
				player.setChuNum(player.getChuNum()+1);	
			}
		}
		
		//设置独储
		if(chuPlayers.size()==1 && (chuJiaoList == null || chuJiaoList.size() == 0)){
      		in:for(int i=0;i<players.size();i++){
      			Player p = players.get(i);
          		Long thisUserId = p.getUserId();
      			if(thisUserId.equals(chuPlayers.get(0))){
             		p.setDuChuNum(p.getDuChuNum()+1);
      				break in;
      			}
      		}
      	}	
		Integer result = -1 ;// 1 储全歼 2 储半奸 3 和局 4 储被半奸 5 储被全歼 0 缴牌  
		//6 储全歼  7 储被全歼   2v2 非储包踹 不公担
		//8 储全歼    9 储被全歼 2v2 各有人包踹 不公担
		//6 储全歼  3和局  7储被全歼 2v2 非储包踹 不公担
		//10储全歼 3 和局  11 储被全歼 2v2 储包踹 不公担
		
		// 1v3 12 全歼 13 半奸 14 被全歼 3和局
		//1v3 非储包踹  共担全歼 12 不供担 15  共担被全歼 14 不公担被全歼 16
		if(hasEndPlayerIds == null || hasEndPlayerIds.size() == 0){
			//缴牌			
			result = 0;		
		}else{
			//1v3
			if(chuPlayers.size() == 1){
				//独储
				if(BaoChuaiState == null || BaoChuaiState == 0){
					//全歼
					if(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 0){
						result = 12;
					}
					//半奸
					if(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 1){
						result = 13;
					}
					//和局
					if(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 2){
						result = 3;
					}
					//被全奸
					if(!hasEndPlayerIds.contains(chuPlayers.get(0))){
						result = 14;
					}	
				}
				
				else{
					if(BaoChuaiState == 1){
						//有非储包踹
						//全歼
						if(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 0){						
							if(room.getGongDan() == 1){
								//共担
								result = 12;
							}else{
								//不公担
								result = 15;
							}
							
						}
						//被全奸
						else{
							if(room.getGongDan() == 1){
								//共担
								result = 14;
							}else{
								//不公担
								result = 16;
							}
						}
					}
					if(BaoChuaiState == 2){
						//储包踹
						//全歼
						if(hasEndPlayerIds.get(0).equals(chuPlayers.get(0))){
							//不公担
							if(room.getGongDan() == 0){
								result = 10;
							}else{
								result = 1;
							}
						}
						//和局
						if(hasEndPlayerIds.size() == 2 && hasEndPlayerIds.get(1).equals(chuPlayers.get(0))){
							result = 3;
						}
						//被全歼
						if(!hasEndPlayerIds.contains(chuPlayers.get(0))){
							//不公担
							if(room.getGongDan() == 0){
								result = 11;
							}else{
								result = 5;
							}
						}
					}
					if(BaoChuaiState == 3){
						Long firstUser = hasEndPlayerIds.get(0);
						//全歼
						if(chuPlayers.contains(firstUser)){							
							if(room.getGongDan() == 0){
								//不公担
								result = 8;
								
							}else{
								//共担
								result = 1;
							}		
						}
						//被全歼
						else{
							if(room.getGongDan() == 0){
								//不公担
								result = 9;
							}else{
								//共担
								result = 5;
							}	
						}
					}
				}
			}
			//2v2
			if(chuPlayers.size() == 2){
				//无人包踹 
				if(BaoChuaiState == null || BaoChuaiState == 0){
					//全歼
					if(hasEndPlayerIds.contains(chuPlayers.get(0)) && hasEndPlayerIds.contains(chuPlayers.get(1))){
						result = 1;
					}
					//半奸
					if((hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 0 && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 2) || 
							(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 2 && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 0)){
						result = 2;
					}
					//和局
					if((hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 0 && !hasEndPlayerIds.contains(chuPlayers.get(1))) || 
							(!hasEndPlayerIds.contains(chuPlayers.get(0)) && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 0) ||
							(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 1 && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 2) ||
							(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 2 && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 1)){
						result = 3;
					}
					//被半奸
					if((hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 1 && !hasEndPlayerIds.contains(chuPlayers.get(1))) || 
							(!hasEndPlayerIds.contains(chuPlayers.get(0)) && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 1)){
						result = 4;
					}
					//被全歼
					if(!hasEndPlayerIds.contains(chuPlayers.get(0)) && !hasEndPlayerIds.contains(chuPlayers.get(1))){
						result = 5;
					}
				}
				//有人包踹
				else{	
					//非储包踹
					if(BaoChuaiState == 1){
						//全歼
						if((hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 0 && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 1) || 							
							(hasEndPlayerIds.indexOf(chuPlayers.get(0)) == 1 && hasEndPlayerIds.indexOf(chuPlayers.get(1)) == 0)){
							//不公担
							if(room.getGongDan() == 0){
								result = 6;
							}else{
								result = 1;
							}
						}
						//和局
						if(hasEndPlayerIds.size()==2 && hasEndPlayerIds.get(1).equals(feiChuPlayers.get(0))){
							result = 3;
						}
						//被全歼
						if(hasEndPlayerIds.get(0).equals(feiChuPlayers.get(0))){
							//不公担
							if(room.getGongDan() == 0){
								result = 7;
							}else{
								result = 5;
							}
						}
					}
				
				}
			}
		}
		//结果算分
		switch(result){
			case 0:
				for(Player player:players){
					if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
						//独储缴 输3家
						if(chuPlayers.size()==1 && chuPlayers.contains(player.getUserId())){
							player.setThisScore(-room.getDiFen()*3);
							player.setScore(player.getScore()+player.getThisScore());
							player.setbBanJ(player.getbBanJ()+1);
							player.setWinType(Cnst.GAME_RESULT_BBANJIAN);
						}else{
							player.setThisScore(-room.getDiFen());
							player.setScore(player.getScore()+player.getThisScore());
							player.setbBanJ(player.getbBanJ()+1);
							player.setWinType(Cnst.GAME_RESULT_BBANJIAN);
						}
						
						
					}else{
						if(chuPlayers.size()==1 && chuPlayers.contains(player.getUserId())){
							player.setThisScore(room.getDiFen()*3);
							player.setScore(player.getScore()+player.getThisScore());
							player.setBanJ(player.getBanJ()+1);
							player.setWinType(Cnst.GAME_RESULT_BANJIAN);	
						}else{
							player.setThisScore(room.getDiFen());
							player.setScore(player.getScore()+player.getThisScore());
							player.setBanJ(player.getBanJ()+1);
							player.setWinType(Cnst.GAME_RESULT_BANJIAN);	
						}
											
					}
				}
				break;
			case 1:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}else{
						player.setThisScore(-room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}
				}
				break;
			case 2:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(room.getDiFen());
						player.setScore(player.getScore()+player.getThisScore());
						player.setBanJ(player.getBanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BANJIAN);
					}else{
						player.setThisScore(-room.getDiFen());
						player.setScore(player.getScore()+player.getThisScore());
						player.setbBanJ(player.getbBanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BBANJIAN);
					}
				}
				break;
			case 3:
				for(Player player:players){
					player.setThisScore(0);
					player.setScore(player.getScore());
					player.setHeJ(player.getHeJ()+1);
					player.setWinType(Cnst.GAME_RESULT_HEJU);
				}
				break;
			case 4:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(-room.getDiFen());
						player.setScore(player.getScore()+player.getThisScore());
						player.setbBanJ(player.getbBanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BBANJIAN);
					}else{
						player.setThisScore(room.getDiFen());
						player.setScore(player.getScore()+player.getThisScore());
						player.setBanJ(player.getBanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BANJIAN);
					}
				}
				break;
			case 5:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(-room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}else{
						player.setThisScore(room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}
				}
				break;
			case 6:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}else{
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(-room.getDiFen()*2*2);
							
						}
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}
				}
				break;
			case 7:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(-room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}else{
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(room.getDiFen()*2*2);
						}						
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}
				}
				break;
			case 8:
				for(Player player:players){
					if(player.getChu() == true){
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(room.getDiFen()*2*2);
						}						
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}else{
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(-room.getDiFen()*2*2);
							
						}
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}
				}
				break;
			case 9:
				for(Player player:players){									
					if(player.getChu() == true){
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(-room.getDiFen()*2*2);
							
						}
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}else{
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(room.getDiFen()*2*2);
						}						
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}
				}
				break;
			case 10:
				for(Player player:players){
					if(player.getChu() == true){
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(room.getDiFen()*2*2);
						}						
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}else{
						player.setThisScore(-room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}
				}
				break;
			case 11:
				for(Player player:players){									
					if(player.getChu() == true){
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(-room.getDiFen()*2*2);
							
						}
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}else{
						player.setThisScore(room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}
				}
				break;
			case 12:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(room.getDiFen()*2*3);
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}else{
						player.setThisScore(-room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}
				}
				break;
			case 13:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(room.getDiFen()*3);
						player.setScore(player.getScore()+player.getThisScore());
						player.setBanJ(player.getBanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BANJIAN);
					}else{
						player.setThisScore(-room.getDiFen());
						player.setScore(player.getScore()+player.getThisScore());
						player.setbBanJ(player.getbBanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BBANJIAN);
					}
				}
				break;
			case 14:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(-room.getDiFen()*2*3);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbBanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}else{
						player.setThisScore(room.getDiFen()*2);
						player.setScore(player.getScore()+player.getThisScore());
						player.setBanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}
				}
				break;
			case 15:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(room.getDiFen()*2*3);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}else{
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(-room.getDiFen()*2*3);
						}						
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}
				}
				break;
			case 16:
				for(Player player:players){
					if(player.getChu() == true){
						player.setThisScore(-room.getDiFen()*2*3);
						player.setScore(player.getScore()+player.getThisScore());
						player.setbQuanJ(player.getbQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_BQUANJIAN);
					}else{
						if(player.getPlayStatus() == Cnst.PLAYER_STATE_JIAO){
							player.setThisScore(0);
						}else{
							player.setThisScore(room.getDiFen()*2*3);
						}						
						player.setScore(player.getScore()+player.getThisScore());
						player.setQuanJ(player.getQuanJ()+1);
						player.setWinType(Cnst.GAME_RESULT_QUANJIAN);
					}
				}
				break;
			default:
				break;
		}
		
		//修改圈数 
		room.setLastNum(room.getLastNum()-1);
		room.setTotalNum(room.getTotalNum()+1);
		//初始化房间
		room.initRoom();
		List<Integer> xiaoJS = new ArrayList<Integer>();
		for(Player p:players){
			p.setPlayStatus(Cnst.PLAYER_STATE_IN);
			xiaoJS.add(p.getThisScore());
		}
		room.addXiaoJSInfo(xiaoJS);
		RedisUtil.updateRedisData(room, null);
		RedisUtil.setPlayersList(players);
		//写入文件
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
		BackFileUtil.write(null, 100102, room, null, TCPGameFunctions.getNewObj(info));
		
		if(room.getXiaoJuNum() == room.getCircleNum()){
			//最后一局 大结算 
			room = RedisUtil.getRoomRespByRoomId(roomId);
			room.setState(Cnst.ROOM_STATE_YJS);
			//这里更新数据库吧			
			TCPGameFunctions.updateDatabasePlayRecord(room);
		}
	}
}
