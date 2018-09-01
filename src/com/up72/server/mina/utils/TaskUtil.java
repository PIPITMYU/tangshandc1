package com.up72.server.mina.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPFunctionExecutor;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/28.
 */
public class TaskUtil implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8155411098353016824L;

	public static void initTaskSchdual() {
        //加载spring的定时配置文件，然后，启动定时任务
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"com/up72/server/mina/utils/applicationContext.xml"});
        System.out.println("加载spring的定时配置文件完成");
    }

    public class DissolveRoomTask implements Runnable,Serializable{

        /**
		 * 
		 */
		private static final long serialVersionUID = 5796473046207010831L;
		private int roomId;
        private int type;//1创建房间40分钟解散任务；2申请解散房间5分钟解散；
        private int taskId = 0;
        
        public DissolveRoomTask(int roomId,int type,int taskId){
            synchronized (this){
                this.roomId = roomId;
                this.type = type;
                this.taskId = taskId;
            }
        }

        @Override
        public void run() {
            synchronized (this){
                try {
                    
                    if (this.type == Cnst.DIS_ROOM_TYPE_1){
                        this.wait(Cnst.ROOM_CREATE_DIS_TIME);
                    }else if(this.type == Cnst.DIS_ROOM_TYPE_2){
                        this.wait(Cnst.ROOM_DIS_TIME);
                    }
                    
                    RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId));
                    if (room==null || room.getState() == Cnst.ROOM_STATE_YJS){
                        return ;
                    }
                    if (this.type == Cnst.DIS_ROOM_TYPE_1){
                        taskId = room.getCreateDisId();
                    }else if(this.type == Cnst.DIS_ROOM_TYPE_2){
                        taskId = room.getApplyDisId();
                    }
                    
                    Integer resultInfo = TCPGameFunctions.disRoomIdResultInfo.get(taskId);
                    if (resultInfo!=null){//notify的，需要终止直接解散
                        System.out.println(roomId+"通知我终止解散房间");
                    }else{//非notify的，直接解散
                    	if (room.getDissolveRoom()==null) {//创建房间超过四十分钟，自动解散
                    		List<Player> players = RedisUtil.getPlayerList(room);
                    		
                    		if (players!=null&&players.size()>0) {
                    			for(Player p:players){
                    				 p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
    	        			        RedisUtil.updateRedisData(null, p);
    	        		        }
							}
                    		
                    		Player fangzhu = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()));
                    		if (fangzhu!=null) {
								fangzhu.setMoney(fangzhu.getMoney()+Cnst.moneyMap.get(room.getCircleNum()));
								RedisUtil.updateRedisData(null, fangzhu);
							}
                    		
                    		RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
                    		
                    		//通知房间内用户房间被解散
                    		MessageFunctions.interface_100111(Cnst.REQ_STATE_13, players,room.getRoomId());
						}else{//房间内申请解散超过5分钟自动解散
							List<Map<String, Object>> otherAgreeList = room.getDissolveRoom().getOthersAgree();
	                    	if (otherAgreeList!=null&&otherAgreeList.size()>0) {
	                    		for(int i=0;i<otherAgreeList.size();i++){
	                    			otherAgreeList.get(i).put("agree", 1);
	                    		}
							}
	                    	
	                    	Map<String,Object> info = new HashMap<>();
	                        info.put("dissolveTime",room.getDissolveRoom().getDissolveTime());
	                        info.put("userId",room.getDissolveRoom().getUserId());
	                        info.put("othersAgree",otherAgreeList);
	                        JSONObject result = TCPGameFunctions.getJSONObj(100204,1,info);
	                        ProtocolData pd = new ProtocolData(100204, result.toJSONString());

	                        List<Player> players = RedisUtil.getPlayerList(room);
	                        
	                        room.setState(Cnst.ROOM_STATE_YJS);

	        				for(Player p:players){
	        					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
	        		            if(se!=null&&se.isConnected()){
	        		                se.write(pd);
	        		            }
	        		            p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0,0,0,0,0,0,0,0);
	        			        RedisUtil.updateRedisData(null, p);
	        		        }
	        				RedisUtil.updateRedisData(room, null);
						}
                    }
                    TCPGameFunctions.disRoomIdMap.remove(taskId);
                    TCPGameFunctions.disRoomIdResultInfo.remove(taskId);
                } catch (Exception e){
                    e.printStackTrace();
                }finally{
                    TCPGameFunctions.disRoomIdMap.remove(taskId);
                    TCPGameFunctions.disRoomIdResultInfo.remove(taskId);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


}
