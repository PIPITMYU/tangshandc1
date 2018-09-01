package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.model.SystemMessage;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.MyRedis;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * 临时类
 * @author zc
 *
 */
public class PR_temp extends TCPGameFunctions{
	
	


    /**
     * 大厅查询战绩
     * @param session
     * @param readData
     */
    public static void interface_100002(IoSession session, Map<String,Object> map){
        logger.I("大厅查询战绩,interfaceId -> 100002");
        Integer interfaceId = (Integer) map.get("interfaceId");
        String userId = (String) map.get("userId");
        Integer page = (Integer) map.get("page");
        String key = Cnst.REDIS_PLAY_RECORD_PREFIX.concat(userId);
        
        Long pageSize = RedisUtil.llen(key); 
        int start = 0;
        int end = 0;
        List<String> infos = RedisUtil.lrange(key, start, end);
        
        Map<String,Object> info = new HashMap<>();
        if (infos!=null&&infos.size()>0) {
        	List<String> finalInfo = RedisUtil.hmgetList(key, infos.toArray(new String[]{}));
            info.put("infos",finalInfo);
		}
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
    public static void interface_100003(IoSession session, Map<String,Object> map){
        logger.I("大厅查询系统消息,interfaceId -> 100003");
        Integer interfaceId = (Integer) map.get("interfaceId");
        Integer page = (Integer) map.get("page");
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
    public static void interface_100004(IoSession session, Map<String,Object> map){
        logger.I("大厅请求联系我们,interfaceId -> 100004");
        Integer interfaceId = (Integer) map.get("interfaceId");
        Long userId = (Long) map.get("userId");
        Map<String,String> info = new HashMap<>();
        info.put("connectionInfo",userService.getConectUs());
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    
    
    
    
    public static void main1(String[] args) throws Exception {
    	List<String> ll = new ArrayList<String>();

    	
    	Map<String,Object> ss = new ConcurrentHashMap<String, Object>();
    	ss.put("roomId", "1111");
    	ss.put("createTime", "2222");
    	ss.put("circleNum", "33333");
    	ss.put("lastNum", "4444");
    	ss.put("state", "5555");
    	ss.put("type", "6666");
    	ss.put("xuanNum", "7777");
    	ss.put("chuaiCircle", "8888");
    	ss.put("chengChu", "43344343");
    	ss.put("shuangShun", "434343");
    	ss.put("A23", "4443343");
    	ss.put("gongDan", "323523");
    	ss.put("juNum", "34523453");
    	ss.put("backUrl", "2345235");
    	ss.put("userInfos", "34523523");
    	
    	Map<String,String> mm = new ConcurrentHashMap<String, String>();
    	mm.put("张三", "12");
    	mm.put("李四", "21");
    	mm.put("王五", "12");
    	mm.put("赵六", "-12");
    	ss.put("userInfos", mm);
    	
    	ll.add(ss.toString());
    	
        JSONObject obj = new JSONObject();
        obj.put("interfaceId",100100);
        obj.put("state",1);
        obj.put("message","");
        obj.put("info",ll);
        obj.put("others","");
        Class.forName("com.up72.game.constant.Cnst");
        long l = System.currentTimeMillis();
//        JSONObject temp = getNewObj(obj);
        System.out.println(System.currentTimeMillis()-l);
	}
    
    
    
    
    public static void main2(String[] args) {
    	MyRedis.initRedis();
    	
    	Map<String,String> rm = new ConcurrentHashMap<String, String>();
    	Map<String,String> pm = new ConcurrentHashMap<String, String>();
    	rm.put("playType", "2");
    	rm.put("roomType", "1");
    	rm.put("xuanNum", "3");
    	rm.put("currActionUser", "888989");
    	rm.put("currAction", "[2,3,0]");
    	rm.put("chuPlayers", "[1,2,3]");
    	rm.put("feiChuPlayers", "[4]");
    	rm.put("chuActionInfos", "[2,3]");
    	rm.put("feiChuActionInfos", "[3,2]");
    	rm.put("canMengXuan", "[1,2,3]");
    	rm.put("canMengChuai", "[2,3]");
    	rm.put("chengChu", "2");
    	rm.put("canDuChuId", "212");
    	rm.put("feiChuBaoChuai", "333");
    	rm.put("chuBaoChuai", "444");
    	rm.put("chuaiCircle", "3");
    	rm.put("hasChuaiCirle", "2");
    	rm.put("chuColor", "1");
    	rm.put("diFen", "23");
    	rm.put("status", "2");
    	
    	RedisUtil.hmset(Cnst.REDIS_PREFIX_ROOMMAP.concat("888888"), rm, null);
    	
    	pm.put("currentCardList", "[211,223,112,221,121,212,432,321,321,211,213]");
    	pm.put("playStatus", "chu");
    	pm.put("status", "online");
    	RedisUtil.hmset(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat("156565"), pm, null);

    	
    	
    	
    	
    	
    	
    	long t = System.currentTimeMillis();
    	System.out.println(t);
    	Map<String,Object> readData = new ConcurrentHashMap<String, Object>();
    	readData.put("interfaceId", 100000);
    	readData.put("userId", 156565);
    	readData.put("roomSn", 888888);
    	
    	
    	
    	
    	String[] playerField = new String[]{"currentCardList","playStatus","status"};
    	String[] roomField = new String[]{"playType","roomType","xuanNum",
    			"currActionUser","currAction","chuPlayers","feiChuPlayers","chuActionInfos",
    			"feiChuActionInfos","canMengXuan","canMengChuai","chengChu","canDuChuId",
    			"feiChuBaoChuai","chuBaoChuai","chuaiCircle","hasChuaiCirle","chuColor",
    			"diFen","status"};
    	
    	
    	
    	
    	
    	
    	
    	Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
        Long userId = StringUtils.parseLong(readData.get("userId"));
        Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
        String playerKey = Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(String.valueOf(userId));//redis userkey
        String roomKey = Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId));//redis roomKey
        
        Map<String,String> playerMap = playerMap = RedisUtil.hmget(playerKey,playerField);
        Map<String,String> roomMap= RedisUtil.hmget(roomKey,roomField);
        
        
        Integer playStatus = StringUtils.parseInt(roomMap.get("playType"));//房间状态
        Integer playType = StringUtils.parseInt(roomMap.get("roomType"));//明储 暗储 选项
        Integer xuanNum = StringUtils.parseInt(roomMap.get("xuanNum"));//梦宣选项
        Long currActionUser = StringUtils.parseLong(roomMap.get("currActionUser"));//当前动作人
        Integer chuaiCircle = StringUtils.parseInt(roomMap.get("chuaiCircle"));//可以踹几圈
        Long feiChuBaoChuai = StringUtils.parseLong(roomMap.get("feiChuBaoChuai"));
        Long chuBaoChuai = StringUtils.parseLong(roomMap.get("chuBaoChuai"));
        String feiChuChuai = roomMap.get("feiChuChuai");//非储是否踹过
        Integer hasChuaiCirle = StringUtils.parseInt(roomMap.get("hasChuaiCirle"));//踹过几圈了
		Integer chuColor = StringUtils.parseInt(roomMap.get("chuColor"));
		Integer diFen = StringUtils.parseInt(roomMap.get("diFen"));
		Integer state = StringUtils.parseInt(roomMap.get("state"));

      List<Integer> currAction = JSONObject.parseObject(roomMap.get("currAction"),List.class);//当前动作
      List<Long> chuPlayers = JSONObject.parseObject(roomMap.get("chuPlayers"),List.class);
      List<Long> feiChuPlayers = JSONObject.parseObject(roomMap.get("feiChuPlayers"),List.class);
      List<Long> chuActionInfos = JSONObject.parseObject(roomMap.get("chuActionInfos"),List.class);
      List<Long> feiChuActionInfos = JSONObject.parseObject(roomMap.get("feiChuActionInfos"),List.class);
		List<Long> canMengXuan = JSONObject.parseObject(roomMap.get("canMengXuan"),List.class);
		List<Long> canMengChuai = JSONObject.parseObject(roomMap.get("canMengChuai"),List.class);
    	List<Card> currentCardList = JSONObject.parseObject(playerMap.get("currentCardList"),List.class);
        String playerStatus = String.valueOf(playerMap.get("status"));//玩家在线on out
        String playerPlayStatus = String.valueOf(playerMap.get("playStatus"));// 玩家游戏内状态
        
        
        System.out.println(playStatus+"_"+playType+"_"+xuanNum+"_"+currActionUser+"_"+chuPlayers+"_"+canMengXuan+"_"+canMengChuai+"_"+playerPlayStatus+"_"+currentCardList);
        
        
        long t1 = System.currentTimeMillis();
        System.out.println(t1);
        System.out.println(t1-t);
	}
    
    
    public static void main(String[] args) {
    	MinaServerManager.printStr();
    	
    	
        
        
        
	}
    
    
    
    
    
    
    
	
}
