package com.up72.server.mina.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/28.
 */
@Component()
public class ScheduledTask {
	
	
//    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = Cnst.CLEAN_3)
//    public void testTaskWithDate() {
//        System.out.println("每天凌晨3点清理任务开始执行 ");
        
        
        
        
//        MessageFunctions.updateDatabasePlayRecord(room);//解散房间
//        TCPGameFunctions.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
//        //通知在线玩家房间被解散
//        MessageFunctions.interface_100111(Cnst.REQ_STATE_12, players,Integer.valueOf(roomId));
        
//        System.out.println("每天3点任务执行结束");
//    }
    
//    @Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = Cnst.CLEAN_EVERY_HOUR)
    public void JVMCount() {
        System.out.println("每小时清理任务开始…… ");
        try {
        } catch (Exception e) {
        	System.out.println("每小时清理任务异常");
        } finally {
            cleanUserEveryHour();
//            testTaskWithDate();
            BackFileUtil.deletePlayRecord();
            cleanPlayRecord();//需要加上清理战绩的逻辑
            cleanPlayDaiKaiRecord();//清理代开战绩的逻辑
        }
        System.out.println("每小时清理任务结束");
    }
    
    @Scheduled(cron = Cnst.COUNT_EVERY_TEN_MINUTE)
    public void onlineNumCount(){
    	Map<String,Object> ipcounts = new HashMap<String, Object>();
    	Map<String,Object> temp = new HashMap<String, Object>();
    	int count = MinaServerManager.tcpServer.getSessions().size();
    	temp.put("onlineNum", String.valueOf(count));
    	List<Long> userIds = new ArrayList<Long>();
    	
    	Iterator<IoSession> iterator = MinaServerManager.tcpServer.getSessions().values().iterator();
    	while(iterator.hasNext()){
    		IoSession se = iterator.next();
    		try {
    			Object o = se.getAttribute(Cnst.USER_SESSION_USER_ID);
        		if (o!=null) {
        			Long userId = Long.parseLong(String.valueOf(o));
        			userIds.add(userId);
				}else{
					se.close(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	temp.put("userIds", userIds);
    	
    	ipcounts.put(Cnst.SERVER_IP,temp);
    	RedisUtil.setObject(Cnst.REDIS_ONLINE_NUM_COUNT.concat(Cnst.SERVER_IP), JSONObject.toJSONString(ipcounts),null);
    	System.out.println("每分钟统计在线人数完成");
    }
    
    public static void cleanPlayRecord(){
    	int cleanNum = 0;
		Set<String> recordKeys = RedisUtil.getKeys(Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat("*"));
		if (recordKeys!=null&&recordKeys.size()>0) {
			long ct = System.currentTimeMillis();
			boolean go = true;
			for(String key:recordKeys){
				go = true;
				while(go){
					go = false;
					String record = RedisUtil.rpop(key);//这个只是战绩表hash的field
		    		if (record!=null) {
//						Map<String,Object> recordMap = JSONObject.parseObject(RedisUtil.hget(Cnst.REDIS_PLAY_RECORD_PREFIX, record),Map.class);
						try {
//							Long createTime = (Long) recordMap.get("createTime");
							Long createTime = Long.parseLong(record.split("-")[1]);
							if ((ct-createTime)<Cnst.BACKFILE_STORE_TIME) {//如果没有过期，需要把记录放回原位
								RedisUtil.rpush(key, null, record);
							}else{//继续删除
//								RedisUtil.hdel(Cnst.REDIS_PLAY_RECORD_PREFIX, record);
//								recordMap = null;
								record = null;
								go = true;
								cleanNum++;
							}
						} catch (Exception e) {
							//maybe will 
						}
					}
				}
			}
		}
    	System.out.println("每小时清理战绩完成，共清理过期记录"+cleanNum+"条");
    }
    
    public static void cleanPlayDaiKaiRecord(){
    	int cleanNum = 0;
		Set<String> recordKeys = RedisUtil.getKeys(Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat("*"));
		if (recordKeys!=null&&recordKeys.size()>0) {
			long ct = System.currentTimeMillis();
			boolean go = true;
			for(String key:recordKeys){
				go = true;
				while(go){
					go = false;
					String record = RedisUtil.rpop(key);//这个只是战绩表hash的field
		    		if (record!=null) {
						try {
							Long createTime = Long.parseLong(record.split("-")[1]);
							if ((ct-createTime)<Cnst.BACKFILE_STORE_TIME) {//如果没有过期，需要把记录放回原位
								RedisUtil.rpush(key, null, record);
							}else{//继续删除
								record = null;
								go = true;
								cleanNum++;
							}
						} catch (Exception e) {
							//maybe will 
						}
					}
				}
			}
		}
    	System.out.println("每小时清理代开战绩完成，共清理过期记录"+cleanNum+"条");
    }
    public static void cleanUserEveryHour(){
    	
    }
    

}