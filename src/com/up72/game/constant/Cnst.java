package com.up72.game.constant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.up72.server.mina.utils.ProjectInfoPropertyUtil;

/**
 * 常量
 */
public class Cnst {
	
	// 获取项目版本信息
    public static final String version = ProjectInfoPropertyUtil.getProperty("project_version", "1.5");
    public static Boolean isTest = true;//是否是测试环境


    public static final String p_name = ProjectInfoPropertyUtil.getProperty("p_name", "wsw_X1");
    public static final String o_name = ProjectInfoPropertyUtil.getProperty("o_name", "u_consume");
    public static final String gm_url = ProjectInfoPropertyUtil.getProperty("gm_url", "");
    
    //回放配置
    public static final String BACK_FILE_PATH = ProjectInfoPropertyUtil.getProperty("backFilePath", "1.5");
    public static final String FILE_ROOT_PATH = ProjectInfoPropertyUtil.getProperty("fileRootPath", "1.5");
    public static String SERVER_IP = getLocalAddress();
    public static String HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");
    public static String getLocalAddress(){
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
    
    
    public static final String cid = ProjectInfoPropertyUtil.getProperty("cid", "7");;
    //redis配置
    public static final String REDIS_HOST = ProjectInfoPropertyUtil.getProperty("redis.host", "");
    public static final String REDIS_PORT = ProjectInfoPropertyUtil.getProperty("redis.port", "");
    public static final String REDIS_PASSWORD = ProjectInfoPropertyUtil.getProperty("redis.password", "");

    //mina的端口
    public static final String MINA_PORT = ProjectInfoPropertyUtil.getProperty("mina.port", "");
    //mina
    public static final int Session_Read_BufferSize = 2048 * 10;
    public static final int Session_life = 60;
    public static final int WriteTimeOut = 500;
    
    public static final String rootPath = ProjectInfoPropertyUtil.getProperty("rootPath", "");

    public static final long HEART_TIME = 9000;//心跳时间，前端定义为8s，避免网络问题延时，后端计算是以9s计算
    public static final int MONEY_INIT = 4;//初始赠送给用户的房卡数
    //开房选项中的是否
    public static final int YES = 1;
    public static final int NO = 0;
    

    public static final long ROOM_OVER_TIME = 5*60*60*1000;//房间定时5小时解散
    public static final long ROOM_CREATE_DIS_TIME = 40*60*1000;//创建房间之后，40分钟解散
    public static final long ROOM_DIS_TIME = 5*60*1000;//玩家发起解散房间之后，5分钟自动解散
    public static final String CLEAN_3 = "0 0 3 * * ?";
    public static final String CLEAN_EVERY_HOUR = "0 0 0/1 * * ?";
    public static final String COUNT_EVERY_TEN_MINUTE = "0 0/1 * * * ?";
    public static final long BACKFILE_STORE_TIME = 3*24*60*60*1000;//回放文件保存时间
    
    
    //测试时间
//    public static final long ROOM_OVER_TIME = 60*1000;//
//    public static final long ROOM_CREATE_DIS_TIME = 20*1000;
//    public static final long ROOM_DIS_TIME = 10*1000;
//	public static final String CLEAN_3 = "0/5 * * * * ?";
//	public static final String CLEAN_EVERY_HOUR = "0/5 * * * * ?";
//    public static final String COUNT_EVERY_TEN_MINUTE = "0/1 * * * * ?";
//    public static final long BACKFILE_STORE_TIME = 60*1000;//回放文件保存时间
    
    

    public static final int ROOM_LIFE_TIME_CREAT = (int) (ROOM_OVER_TIME/1000);//创建时，5小时，redis用
    public static final int ROOM_LIFE_TIME_DIS = (int) ((ROOM_DIS_TIME/1000)-200);//解散房间时，300s，redis用
    public static final int ROOM_LIFE_TIME_COMMON = (int) ((ROOM_CREATE_DIS_TIME/1000)+200);//正常开局存活时间，redis用
    public static final int OVERINFO_LIFE_TIME_COMMON = (int) (10*60);//大结算 overInfo 存活时间
    public static final int PLAYOVER_LIFEE_TIME =3*24*60*60*1000;//战绩保存时间
    
    public static final int DIS_ROOM_RESULT = 1;

    public static final int DIS_ROOM_TYPE_1 = 1;//创建房间40分钟解散类型
    public static final int DIS_ROOM_TYPE_2 = 2;//玩家点击解散房间类型

    public static final int PAGE_SIZE = 10;
    
    public static final int DI_XIAN_PAI = 14;//房间默认的底线牌

    //风向表示
    public static final int WIND_EAST = 1;
    public static final int WIND_SOUTH = 2;
    public static final int WIND_WEST = 3;
    public static final int WIND_NORTH = 4;

    public static final String USER_SESSION_USER_ID = "user_id";
    public static final String USER_SESSION_IP = "ip";
    
    

    //房间信息中的state
    // 1等待玩家入坐；2游戏中；3小结算
    public static final int ROOM_STATE_CREATED = 1;
    public static final int ROOM_STATE_GAMIING = 2;
    public static final int ROOM_STATE_XJS = 3;
    public static final int ROOM_STATE_YJS = 4;
    //房间中的playState state = 2时有效
    public static final int ROOM_PALYSTATE_START = 0;//开局
    public static final int ROOM_PALYSTATE_MENGXUAN = 1;//梦宣
    public static final int ROOM_PALYSTATE_MENGCHUAI = 2;//梦踹
    public static final int ROOM_PALYSTATE_PINGXUAN = 3;//平宣
    public static final int ROOM_PALYSTATE_JIAOPAI = 4;//缴牌
    public static final int ROOM_PALYSTATE_CHENGCHU = 5;//撑储
    public static final int ROOM_PALYSTATE_FEICHUBAOCHUAI = 6;//非储方包踹
    public static final int ROOM_PALYSTATE_FEICHUCHUAI = 7;//非储方踹
    public static final int ROOM_PALYSTATE_CHUBAOCHUAI = 8;//储方包踹
    public static final int ROOM_PALYSTATE_CHUCHUAI = 9;//储方踹
    public static final int ROOM_PALYSTATE_JIUCHU = 10;//揪储
    public static final int ROOM_PALYSTATE_FANCHUAI = 11;//反储
    public static final int ROOM_PALYSTATE_CHUPAI = 12;//出牌

    //房间类型
    public static final int ROOM_TYPE_1 = 1;//房主模式
    public static final int ROOM_TYPE_2 = 2;//自由模式
    public static final int ROOM_TYPE_3 = 3;//AA

    public static final int ROOM_PALYTYPE_MING = 1; //明储
    public static final int ROOM_PALYTYPE_AN = 2; //暗储
    //小局结算时的
    public static final int OVER_TYPE_1 = 1;//全歼
    public static final int OVER_TYPE_2 = 2;//半奸
    public static final int OVER_TYPE_3 = 3;//和局
    public static final int OVER_TYPE_4 = 4;//被全歼
    public static final int OVER_TYPE_5 = 5;//被半奸


    //开房的局数对应消耗的房卡数
    public static final Map<Integer,Integer> moneyMap = new HashMap<>();
    static {
        moneyMap.put(8,4);
        moneyMap.put(12,4);
        moneyMap.put(24,4);
        moneyMap.put(32,12);
    }
    //玩家在线状态 state 
    public static final int PLAYER_LINE_STATE_INLINE = 1;//"inline"
    public static final int PLAYER_LINE_STATE_OUT = 2;//"out"

    //玩家状态
    public static final int PLAYER_STATE_DATING = 1;//"dating"
    public static final int PLAYER_STATE_IN = 2;//"in"
    public static final int PLAYER_STATE_PREPARED = 3;//"prepared"
    public static final int PLAYER_STATE_GAME = 4;//"game"
    public static final int PLAYER_STATE_JIAO = 5;//"jiao"
    public static final int PLAYER_STATE_over = 6;//"over"
    public static final int PLAYER_STATE_XJS = 7;//"xjs"
    
    //输赢结果
    public static final int GAME_RESULT_QUANJIAN = 1;
    public static final int GAME_RESULT_BANJIAN = 2;
    public static final int GAME_RESULT_HEJU = 3;
    public static final int GAME_RESULT_BBANJIAN = 4;
    public static final int GAME_RESULT_BQUANJIAN = 5;

    //请求状态
    public static final int REQ_STATE_FUYI = -1;//敬请期待
    public static final int REQ_STATE_0 = 0;//非法请求
    public static final int REQ_STATE_1 = 1;//正常
    public static final int REQ_STATE_2 = 2;//余额不足
    public static final int REQ_STATE_3 = 3;//已经在其他房间中
    public static final int REQ_STATE_4 = 4;//房间不存在
    public static final int REQ_STATE_5 = 5;//房间人员已满
    public static final int REQ_STATE_6 = 6;//游戏中，不能退出房间
    public static final int REQ_STATE_7 = 7;//有玩家拒绝解散房间
    public static final int REQ_STATE_8 = 8;//玩家不存在（代开模式中，房主踢人用的）
    public static final int REQ_STATE_9 = 9;//接口id不符合，需请求大接口
    public static final int REQ_STATE_10 = 10;//代开房间创建成功
    public static final int REQ_STATE_11 = 11;//已经代开过10个了，不能再代开了
    public static final int REQ_STATE_12 = 12;//房间存在超过24小时解散的提示
    public static final int REQ_STATE_13 = 13;//房间40分钟未开局解散提示
    public static final int REQ_STATE_14 = 14;//ip不一致

    //动作列表
    public static final int ACTION_BUYAO = 13;//出牌：不要
    public static final int ACTION_YAO = 12;//出牌：要
    public static final int ACTION_BUCHUAI = 11;//不踹
    public static final int ACTION_JIUCHU = 10;//揪储
    public static final int ACTION_FANCHUAI = 9;//反踹
    public static final int ACTION_JUJUE = 8;//拒绝
    public static final int ACTION_TONGYI = 7;//同意
    public static final int ACTION_JIAO = 6;//缴牌
    public static final int ACTION_CHUAI = 5;//平踹
    public static final int ACTION_BAOCHUAI = 4;//包踹
    public static final int ACTION_CHENGCHU = 3;//称储
    public static final int ACTION_MENGCHUAI = 2;//梦踹
    public static final int ACTION_MENGXUAN = 1;//梦宣
    public static final int ACTION_GUO = 0;//过

    //牌局底分
    public static final int SCORE_BASE = 1;

    //退出类型
    public static final int EXIST_TYPE_EXIST = 1;//"exist"
    public static final int EXIST_TYPE_DISSOLVE = 2;//"dissolve";

    // 项目根路径
    public static String ROOTPATH = "";
    
    //redis存储的key的不同类型的前缀
    public static final String REDIS_PREFIX_ROOMMAP = "TSDACHU_ROOM_MAP_";//房间信息
    public static final String REDIS_PREFIX_OPENIDUSERMAP = "TSDACHU_OPENID_USERID_MAP_";//openId-user数据
    public static final String REDIS_PREFIX_USER_ID_USER_MAP = "TSDACHU_USER_ID_USER_MAP_";//通过userId获取用户
    //redis中通知的key
    public static final String NOTICE_KEY = "TSDACHU_NOTICE_KEY";
    
    public static final String PROJECT_PREFIX = "TSDACHU_*";
    
    public static final String REDIS_ONLINE_NUM_COUNT = "TSDACHU_ONLINE_NUM_";
    
    public static final String REDIS_HEART_PREFIX = "TSDACHU_HEART_USERS_MAP";
    
    //这个字段不清理，存放玩家战绩，定时任务定期清理内容
    public static final String REDIS_PLAY_RECORD_PREFIX = "TSDACHU_PLAY_RECORD_";//房间战绩
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_USER = "TSDACHU_PLAY_RECORD_FOR_USER_";//玩家字段
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI = "TSDACHU_PLAY_RECORD_FOR_DAIKAI_";//代开房间
    public static final String REDIS_PLAY_RECORD_PREFIX_OVERINFO = "TSDACHU_PLAY_RECORD_OVERINFO_";//大结算
    
    public static Map<String,String> ROUTE_MAP = new ConcurrentHashMap<String, String>();
    static{
    	ROUTE_MAP.put("a","interfaceId");
    	ROUTE_MAP.put("b","state");
    	ROUTE_MAP.put("c","message");
    	ROUTE_MAP.put("d","info");
    	ROUTE_MAP.put("e","others");
    	ROUTE_MAP.put("f","page");
    	ROUTE_MAP.put("g","infos");
    	ROUTE_MAP.put("h","roomId");
    	ROUTE_MAP.put("i","createTime");
    	ROUTE_MAP.put("j","userInfos");
    	ROUTE_MAP.put("k","pages");
    	ROUTE_MAP.put("l","connectionInfo");
    	ROUTE_MAP.put("m","circleNum");
    	ROUTE_MAP.put("n","roomType");
    	ROUTE_MAP.put("o","type");
    	ROUTE_MAP.put("p","xuanNum");
    	ROUTE_MAP.put("q","chuaiCircle");
    	ROUTE_MAP.put("r","chengChu");
    	ROUTE_MAP.put("s","shuangShun");
    	ROUTE_MAP.put("t","A23");
    	ROUTE_MAP.put("u","gongDan");
    	ROUTE_MAP.put("v","reqState");
    	ROUTE_MAP.put("w","playerNum");
    	ROUTE_MAP.put("x","money");
    	ROUTE_MAP.put("y","roomSn");
    	ROUTE_MAP.put("z","playerInfo");
    	ROUTE_MAP.put("A","position");
    	ROUTE_MAP.put("B","openName");
    	ROUTE_MAP.put("C","openImg");
    	ROUTE_MAP.put("D","roomInfo");
    	ROUTE_MAP.put("E","score");
    	ROUTE_MAP.put("F","openId");
    	ROUTE_MAP.put("G","cId");
    	ROUTE_MAP.put("H","wsw_sole_main_id");
    	ROUTE_MAP.put("I","wsw_sole_action_id");
    	ROUTE_MAP.put("J","userId");
    	ROUTE_MAP.put("K","lastNum");
    	ROUTE_MAP.put("L","totalNum");
    	ROUTE_MAP.put("M","lastPai");
    	ROUTE_MAP.put("N","lastUserId");
    	ROUTE_MAP.put("O","xjst");
    	ROUTE_MAP.put("P","dissolveRoom");
    	ROUTE_MAP.put("Q","dissolveTime");
    	ROUTE_MAP.put("R","othersAgree");
    	ROUTE_MAP.put("S","agree");
    	ROUTE_MAP.put("T","currentUser");
    	ROUTE_MAP.put("U","playStatus");
    	ROUTE_MAP.put("V","version");
    	ROUTE_MAP.put("W","gender");
    	ROUTE_MAP.put("X","ip");
    	ROUTE_MAP.put("Y","joinIndex");
    	ROUTE_MAP.put("Z","userAgree");
    	ROUTE_MAP.put("aa","notice");
    	ROUTE_MAP.put("ab","roomInfos");
    	ROUTE_MAP.put("ac","ChuColor");
    	ROUTE_MAP.put("ad","ChuPlayers");
    	ROUTE_MAP.put("ae","diFen");
    	ROUTE_MAP.put("af","actionUser");
    	ROUTE_MAP.put("ag","action");
    	ROUTE_MAP.put("ah","actionPai");
    	ROUTE_MAP.put("ai","pais");
    	ROUTE_MAP.put("aj","isYao");
    	ROUTE_MAP.put("ak","idx");
    	ROUTE_MAP.put("al","continue");
    	ROUTE_MAP.put("am","nextActionUserId");
    	ROUTE_MAP.put("an","nextAction");
    	ROUTE_MAP.put("ao","paiNum");
    	ROUTE_MAP.put("ap","nextChuPlayerId");
    	ROUTE_MAP.put("aq","endNum");// 几跑 paoIndex
    	ROUTE_MAP.put("ar","totalFan");
    	ROUTE_MAP.put("as","Chu");
    	ROUTE_MAP.put("at","finalScore");
    	ROUTE_MAP.put("au","fanInfo");
    	ROUTE_MAP.put("av","quanJ");
    	ROUTE_MAP.put("aw","banJ");
    	ROUTE_MAP.put("ax","heJ");
    	ROUTE_MAP.put("ay","chuNum");
    	ROUTE_MAP.put("az","duChuNum");
    	ROUTE_MAP.put("aA","XiaoJuNum");// xjn
    	ROUTE_MAP.put("aB","lastChuPai");
    	ROUTE_MAP.put("aC","lastActionUser");//lastActionPlayer
    	ROUTE_MAP.put("aD","lastAction");
    	ROUTE_MAP.put("aE","lastActionPai");
    	ROUTE_MAP.put("aF","currActionUser");//currActionPlayer
    	ROUTE_MAP.put("aG","currAction");
    	ROUTE_MAP.put("aH","currActionPai");
    	ROUTE_MAP.put("aI","tiShi");
    	ROUTE_MAP.put("aJ","paiInfos");//paiInfos
    	ROUTE_MAP.put("aK","chuaiPais");
    	ROUTE_MAP.put("aL","juNum");
    	ROUTE_MAP.put("aM","backUrl");
    	ROUTE_MAP.put("aN","sessionId");
    	ROUTE_MAP.put("aO","bQuanJ");
    	ROUTE_MAP.put("aP","bBanJ");
    	ROUTE_MAP.put("aQ","winType");
    	ROUTE_MAP.put("aR","anotherUsers");
    	ROUTE_MAP.put("aS","openName");
    	
    	ROUTE_MAP.put("interfaceId","a");
    	ROUTE_MAP.put("state","b");
    	ROUTE_MAP.put("message","c");
    	ROUTE_MAP.put("info","d");
    	ROUTE_MAP.put("others","e");
    	ROUTE_MAP.put("page","f");
    	ROUTE_MAP.put("infos","g");
    	ROUTE_MAP.put("roomId","h");
    	ROUTE_MAP.put("createTime","i");
    	ROUTE_MAP.put("userInfos","j");
    	ROUTE_MAP.put("pages","k");
    	ROUTE_MAP.put("connectionInfo","l");
    	ROUTE_MAP.put("circleNum","m");
    	ROUTE_MAP.put("roomType","n");
    	ROUTE_MAP.put("type","o");
    	ROUTE_MAP.put("xuanNum","p");
    	ROUTE_MAP.put("chuaiCircle","q");
    	ROUTE_MAP.put("chengChu","r");
    	ROUTE_MAP.put("shuangShun","s");
    	ROUTE_MAP.put("A23","t");
    	ROUTE_MAP.put("gongDan","u");
    	ROUTE_MAP.put("reqState","v");
    	ROUTE_MAP.put("playerNum","w");
    	ROUTE_MAP.put("money","x");
    	ROUTE_MAP.put("roomSn","y");
    	ROUTE_MAP.put("playerInfo","z");
    	ROUTE_MAP.put("position","A");
    	ROUTE_MAP.put("userName","B");
    	ROUTE_MAP.put("userImg","C");
    	ROUTE_MAP.put("roomInfo","D");
    	ROUTE_MAP.put("score","E");
    	ROUTE_MAP.put("openId","F");
    	ROUTE_MAP.put("cId","G");
    	ROUTE_MAP.put("wsw_sole_main_id","H");
    	ROUTE_MAP.put("wsw_sole_action_id","I");
    	ROUTE_MAP.put("userId","J");
    	ROUTE_MAP.put("lastNum","K");
    	ROUTE_MAP.put("totalNum","L");
    	ROUTE_MAP.put("lastPai","M");
    	ROUTE_MAP.put("lastUserId","N");
    	ROUTE_MAP.put("xjst","O");
    	ROUTE_MAP.put("dissolveRoom","P");
    	ROUTE_MAP.put("dissolveTime","Q");
    	ROUTE_MAP.put("othersAgree","R");
    	ROUTE_MAP.put("agree","S");
    	ROUTE_MAP.put("currentUser","T");
    	ROUTE_MAP.put("playStatus","U");
    	ROUTE_MAP.put("version","V");
    	ROUTE_MAP.put("gender","W");
    	ROUTE_MAP.put("ip","X");
    	ROUTE_MAP.put("joinIndex","Y");
    	ROUTE_MAP.put("userAgree","Z");
    	ROUTE_MAP.put("notice","aa");
    	ROUTE_MAP.put("roomInfos","ab");
    	ROUTE_MAP.put("chuColors","ac");//
    	ROUTE_MAP.put("chuPlayers","ad");
    	ROUTE_MAP.put("diFen","ae");
    	ROUTE_MAP.put("actionUser","af");
    	ROUTE_MAP.put("action","ag");
    	ROUTE_MAP.put("actionPai","ah");
    	ROUTE_MAP.put("pais","ai");
    	ROUTE_MAP.put("isYao","aj");
    	ROUTE_MAP.put("idx","ak");
    	ROUTE_MAP.put("continue","al");
    	ROUTE_MAP.put("nextActionUserId","am");//nextActionUserId
    	ROUTE_MAP.put("nextAction","an");
    	ROUTE_MAP.put("paiNum","ao");
    	ROUTE_MAP.put("nextChuPlayerId","ap");
    	ROUTE_MAP.put("endNum","aq");//几跑
    	ROUTE_MAP.put("totalFan","ar");
    	ROUTE_MAP.put("Chu","as");
    	ROUTE_MAP.put("finalScore","at");
    	ROUTE_MAP.put("fanInfo","au");
    	ROUTE_MAP.put("quanJ","av");
    	ROUTE_MAP.put("banJ","aw");
    	ROUTE_MAP.put("heJ","ax");
    	ROUTE_MAP.put("chuNum","ay");
    	ROUTE_MAP.put("duChuNum","az");
    	ROUTE_MAP.put("XiaoJuNum","aA");
    	ROUTE_MAP.put("lastChuPai","aB");
    	ROUTE_MAP.put("lastActionUser","aC");//lastActionPlayer
    	ROUTE_MAP.put("lastAction","aD");
    	ROUTE_MAP.put("lastActionPai","aE");
    	ROUTE_MAP.put("currActionUser","aF");//currActionPlayer
    	ROUTE_MAP.put("currAction","aG");
    	ROUTE_MAP.put("currActionPai","aH");
    	ROUTE_MAP.put("tiShi","aI");
    	ROUTE_MAP.put("paiInfos","aJ");//paiInfos
    	ROUTE_MAP.put("chuaiPais","aK");
    	ROUTE_MAP.put("juNum","aL");
    	ROUTE_MAP.put("backUrl","aM");
    	ROUTE_MAP.put("sessionId","aN");
    	ROUTE_MAP.put("bQuanJ","aO");
    	ROUTE_MAP.put("bBanJ","aP");
    	ROUTE_MAP.put("winType","aQ");
    	ROUTE_MAP.put("anotherUsers","aR");
    	ROUTE_MAP.put("openName","aS");
    	ROUTE_MAP.put("eastUserName","aT");
    	ROUTE_MAP.put("eastUserImg","aU");
    	ROUTE_MAP.put("eastUserMoneyRecord","aV");
    	ROUTE_MAP.put("southUserName","aW");
    	ROUTE_MAP.put("southUserImg","aX");
    	ROUTE_MAP.put("southUserMoneyRecord","aY");
    	ROUTE_MAP.put("westUserName","aZ");
    	ROUTE_MAP.put("westUserImg","ba");
    	ROUTE_MAP.put("westUserMoneyRecord","bb");
    	ROUTE_MAP.put("northUserName","bc");
    	ROUTE_MAP.put("northUserImg","bd");
    	ROUTE_MAP.put("northUserMoneyRecord","be");
    	ROUTE_MAP.put("chuType", "bf");
    	ROUTE_MAP.put("pingChuaiCircle", "bg");
    	ROUTE_MAP.put("actionStartId", "bh");
    	ROUTE_MAP.put("jiaoList", "bi");
    	ROUTE_MAP.put("xiaoJuInfo", "bj");
    	
    	ROUTE_MAP.put("id","Ba");
    	ROUTE_MAP.put("playerIds","Bb");
    	ROUTE_MAP.put("fileBackPlayStatus", "zz");
    }

    

    public final static int[] CARD_ARRAY = { 101, 201, 301, 401, 102, 202, 302, 402, 103, 203, 303,
    	403, 104, 204, 304, 404, 105, 205, 305, 405, 106, 206, 306, 406, 107, 207, 307, 407, 108, 208,
    	308, 408, 109, 209, 309, 409, 110, 210, 310, 410, 111, 211, 311, 411, 112, 212, 312, 412, 113, 
    	213, 313, 413 };
    
    public final static String[] INIT_HALL_USER= {"roomId","currentCardList","startCardList","chu",
    	"endNum","score","joinIndex","position","quanJ","banJ","heJ","bQuanJ","bBanJ",
    	"chuNum","duChuNum","thisScore","winType","fanInfo"};
}
