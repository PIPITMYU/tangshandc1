package com.up72.game.dto.resp;

import java.util.ArrayList;

import java.util.List;


import com.up72.game.constant.Cnst;
import com.up72.game.model.Room;
import com.up72.server.mina.bean.DissolveRoom;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RoomResp extends Room {
	
	
	private List<Card> currentCardList = new ArrayList<Card>();//房间内剩余牌集合；
    private List<Long> chuPlayers ;//储方
    private List<Long> feiChuPlayers ;//非储方
    private Integer chuColor;//储的颜色
    private Integer state;//本房间状态，0等待玩家入坐；1人满等待；2游戏中；3小结算
    private Integer lastNum;//房间剩余局数
    private Integer totalNum;//当前第几局
    private Integer diFen;//底分
    private DissolveRoom dissolveRoom;//申请解散信息
    private List<Card> lastChuPai;//最后出的牌
    private Long lastUserId;//最后出牌的玩家
    private Long xjst;//小局开始时间
    private Long lastActionUser;//最后动作人
    private Integer lastAction;//最后动作人动作
    private List<Integer> lastActionPai;//最后动作人牌
    private Long currActionUser;//下个动作人
    private List<Integer> currAction;//下个动作人动作    
    private Integer playStatus;//游戏中房间状态
    private Integer createDisId;
    private Integer applyDisId;
    private Integer outNum;//请求大接口的玩家次数

    private Integer wsw_sole_main_id;//大接口id 暂时没用
    private Integer wsw_sole_action_id;//吃碰杠出牌发牌id
    
    private String openName;//房主id
    private Long[] playerIds;//玩家id集合
    private Integer xiaoJuNum;//每次小局，这个字段++，回放用
    
    private Long mengXuan;//梦宣的玩家id
    private List<Long> mengChuai;//梦踹的玩家集合
    private boolean feiChuChuai;//非储方是否踹过
    private Integer hasChuaiCirle;//已经平踹的圈数
    private List<Long> canMengXuan;//可以梦宣的玩家
    private List<Long> canMengChuai;//可以梦踹的玩家
    private Card lastFaCard;//最后一张的牌
    private List<Long> chuActionInfos;// 用于标识储家 缴 包踹 踹 的统计 
    private List<Long> feiChuActionInfos;// 用于标识非储家 缴 包踹 踹 的统计
    private Long canDuChuId;//是否可以独储的id
    private Long chuBaoChuai;//储方发起包踹的id
    private Long feiChuBaoChuai;//非储方发起包踹的id
    private List<Long> realPlayerIds;//有同意包踹的玩家或出完牌的玩家  出牌按这个走
    private List<Long> hasEndPlayerIds;//出完牌的用户id
    private List<Long> jiuChu;//可以揪储的id
    private Boolean ifJiu;//是否揪储
    private Integer baoChuaiState;//包踹情况 0是没人包踹 1是非储包踹 2是储包踹 3是非储储 都有人包踹
    private Long hongXinThree;//第一个出牌的玩家
    private List<Long> liangChuIds;//已亮储的玩家id
    private Integer askChuaiNum;//包踹轮训次数
    private boolean jieFeng;//开始接风
    private Integer jieFengNum;//接风数
//  private Integer anChuHasFaPai;//暗储是否已发牌
    private List<Long> chuJiaoList;//储包踹缴牌
    private List<Long> feiChuJiaoList;//非储包踹缴牌
    private Integer chuType;//储类型 0 普通 1 撑储
    private Long pingChuaiChu;//平踹的储玩家
    private Long pingChuaiFeiChu;//平踹的非储玩家
    private List<Integer> fanChuaiPai;//反踹牌 的点数
    private List<Integer> mengChuaiPai;//梦踹牌的点数
    private Long hasChengChu;//撑储人的id
    private Integer addIsChu;//设置储牌标识
    private List<List<Integer>> xiaoJSInfo = new ArrayList<List<Integer>>();//回放用的小结算信息
    public void initRoom(){
    	this.currentCardList = new ArrayList<Card>();
    	this.chuPlayers = null;
    	this.feiChuPlayers = null;
    	this.chuColor = null;
//    	this.diFen = 1;
    	this.lastChuPai = null;
    	this.lastChuPai = null;
    	this.xjst = null;
    	this.lastAction = null;
    	this.lastActionUser = null;
    	this.lastActionPai = null;
    	this.currAction = null;
    	this.currActionUser = null;
    	this.playStatus = Cnst.ROOM_PALYSTATE_START;
    	this.mengXuan = null;
    	this.mengChuai = null;
    	this.feiChuChuai = false;
    	this.hasChuaiCirle = 0;
    	this.canMengXuan = null;
    	this.canMengChuai = null;
    	this.lastFaCard = null;
    	this.chuActionInfos = null;
    	this.feiChuActionInfos = null;
    	this.canDuChuId = null;
    	this.chuBaoChuai = null;
    	this.feiChuBaoChuai = null;
    	this.realPlayerIds = null;
    	this.hasEndPlayerIds = new ArrayList<Long>();
    	this.jiuChu = null;
    	this.ifJiu = false;
    	this.baoChuaiState = 0;
    	this.hongXinThree = null;
    	this.liangChuIds = null;
    	this.jieFeng = false;
    	this.jieFengNum = null;
    	this.chuJiaoList = null;
    	this.feiChuJiaoList = null;
    	this.chuType = 0;
    	this.pingChuaiChu = null;
    	this.pingChuaiFeiChu = null;
    	this.fanChuaiPai = null;
    	this.mengChuaiPai = null;
    	this.hasChengChu = null;
    	this.addIsChu = 0;
    }
	public List<Card> getCurrentCardList() {
		return currentCardList;
	}
	public void setCurrentCardList(List<Card> currentCardList) {
		this.currentCardList = currentCardList;
	}
	public List<Long> getChuPlayers() {
		return chuPlayers;
	}
	public void setChuPlayers(List<Long> chuPlayers) {
		this.chuPlayers = chuPlayers;
	}
	public List<Long> getFeiChuPlayers() {
		return feiChuPlayers;
	}
	public void setFeiChuPlayers(List<Long> feiChuPlayers) {
		this.feiChuPlayers = feiChuPlayers;
	}
	public Integer getChuColor() {
		return chuColor;
	}
	public void setChuColor(Integer chuColor) {
		this.chuColor = chuColor;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Integer getLastNum() {
		return lastNum;
	}
	public void setLastNum(Integer lastNum) {
		this.lastNum = lastNum;
	}
	public Integer getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}
	public Integer getDiFen() {
		return diFen;
	}
	public void setDiFen(Integer diFen) {
		this.diFen = diFen;
	}
	public DissolveRoom getDissolveRoom() {
		return dissolveRoom;
	}
	public void setDissolveRoom(DissolveRoom dissolveRoom) {
		this.dissolveRoom = dissolveRoom;
	}
	public List<Card> getLastChuPai() {
		return lastChuPai;
	}
	public void setLastChuPai(List<Card> lastChuPai) {
		this.lastChuPai = lastChuPai;
	}
	public Long getLastUserId() {
		return lastUserId;
	}
	public void setLastUserId(Long lastUserId) {
		this.lastUserId = lastUserId;
	}
	public Long getXjst() {
		return xjst;
	}
	public void setXjst(Long xjst) {
		this.xjst = xjst;
	}
	public Long getLastActionUser() {
		return lastActionUser;
	}
	public void setLastActionUser(Long lastActionUser) {
		this.lastActionUser = lastActionUser;
	}
	public Integer getLastAction() {
		return lastAction;
	}
	public void setLastAction(Integer lastAction) {
		this.lastAction = lastAction;
	}
	public Long getCurrActionUser() {
		return currActionUser;
	}
	public void setCurrActionUser(Long currActionUser) {
		this.currActionUser = currActionUser;
	}
	
	public List<Integer> getCurrAction() {
		return currAction;
	}
	public void setCurrAction(List<Integer> currAction) {
		this.currAction = currAction;
	}
	public Integer getPlayStatus() {
		return playStatus;
	}
	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}
	public Integer getCreateDisId() {
		return createDisId;
	}
	public void setCreateDisId(Integer createDisId) {
		this.createDisId = createDisId;
	}
	public Integer getApplyDisId() {
		return applyDisId;
	}
	public void setApplyDisId(Integer applyDisId) {
		this.applyDisId = applyDisId;
	}
	public Integer getOutNum() {
		return outNum;
	}
	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}
	public Integer getWsw_sole_main_id() {
		return wsw_sole_main_id;
	}
	public void setWsw_sole_main_id(Integer wsw_sole_main_id) {
		this.wsw_sole_main_id = wsw_sole_main_id;
	}
	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}
	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}
	public String getOpenName() {
		return openName;
	}
	public void setOpenName(String openName) {
		this.openName = openName;
	}
	public Long[] getPlayerIds() {
		return playerIds;
	}
	public void setPlayerIds(Long[] playerIds) {
		this.playerIds = playerIds;
	}
	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}
	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}
	public Long getMengXuan() {
		return mengXuan;
	}
	public void setMengXuan(Long mengXuan) {
		this.mengXuan = mengXuan;
	}
	public List<Long> getMengChuai() {
		return mengChuai;
	}
	public void setMengChuai(List<Long> mengChuai) {
		this.mengChuai = mengChuai;
	}
	public boolean isFeiChuChuai() {
		return feiChuChuai;
	}
	public void setFeiChuChuai(boolean feiChuChuai) {
		this.feiChuChuai = feiChuChuai;
	}
	public Integer getHasChuaiCirle() {
		return hasChuaiCirle;
	}
	public void setHasChuaiCirle(Integer hasChuaiCirle) {
		this.hasChuaiCirle = hasChuaiCirle;
	}
	public List<Long> getCanMengXuan() {
		return canMengXuan;
	}
	public void setCanMengXuan(List<Long> canMengXuan) {
		this.canMengXuan = canMengXuan;
	}
	public List<Long> getCanMengChuai() {
		return canMengChuai;
	}
	public void setCanMengChuai(List<Long> canMengChuai) {
		this.canMengChuai = canMengChuai;
	}
	public Card getLastFaCard() {
		return lastFaCard;
	}
	public void setLastFaCard(Card lastFaCard) {
		this.lastFaCard = lastFaCard;
	}
	public List<Long> getChuActionInfos() {
		return chuActionInfos;
	}
	public void setChuActionInfos(List<Long> chuActionInfos) {
		this.chuActionInfos = chuActionInfos;
	}
	public List<Long> getFeiChuActionInfos() {
		return feiChuActionInfos;
	}
	public void setFeiChuActionInfos(List<Long> feiChuActionInfos) {
		this.feiChuActionInfos = feiChuActionInfos;
	}
	public Long getCanDuChuId() {
		return canDuChuId;
	}
	public void setCanDuChuId(Long canDuChuId) {
		this.canDuChuId = canDuChuId;
	}
	public Long getChuBaoChuai() {
		return chuBaoChuai;
	}
	public void setChuBaoChuai(Long chuBaoChuai) {
		this.chuBaoChuai = chuBaoChuai;
	}
	public Long getFeiChuBaoChuai() {
		return feiChuBaoChuai;
	}
	public void setFeiChuBaoChuai(Long feiChuBaoChuai) {
		this.feiChuBaoChuai = feiChuBaoChuai;
	}
	public List<Long> getRealPlayerIds() {
		return realPlayerIds;
	}
	public void setRealPlayerIds(List<Long> realPlayerIds) {
		this.realPlayerIds = realPlayerIds;
	}
	public List<Long> getHasEndPlayerIds() {
		return hasEndPlayerIds;
	}
	public void setHasEndPlayerIds(List<Long> hasEndPlayerIds) {
		this.hasEndPlayerIds = hasEndPlayerIds;
	}
	public List<Long> getJiuChu() {
		return jiuChu;
	}
	public void setJiuChu(List<Long> jiuChu) {
		this.jiuChu = jiuChu;
	}
	public Boolean getIfJiu() {
		return ifJiu;
	}
	public void setIfJiu(Boolean ifJiu) {
		this.ifJiu = ifJiu;
	}
	public Integer getBaoChuaiState() {
		return baoChuaiState;
	}
	public void setBaoChuaiState(Integer baoChuaiState) {
		this.baoChuaiState = baoChuaiState;
	}
	public Long getHongXinThree() {
		return hongXinThree;
	}
	public void setHongXinThree(Long hongXinThree) {
		this.hongXinThree = hongXinThree;
	}
	public List<Long> getLiangChuIds() {
		return liangChuIds;
	}
	public void setLiangChuIds(List<Long> liangChuIds) {
		this.liangChuIds = liangChuIds;
	}
	
	
	public boolean isJieFeng() {
		return jieFeng;
	}
	public void setJieFeng(boolean jieFeng) {
		this.jieFeng = jieFeng;
	}
	public Integer getJieFengNum() {
		return jieFengNum;
	}
	public void setJieFengNum(Integer jieFengNum) {
		this.jieFengNum = jieFengNum;
	}
	
	//初始化房间手牌
    public void initCurrentCardList() {
		List<Card> cards = new ArrayList<Card>();
		for (int i = 0; i < Cnst.CARD_ARRAY.length; i++) {
			cards.add(new Card(Cnst.CARD_ARRAY[i]));
		}
		this.currentCardList = cards;
	}
    //发牌
    public void dealCard(int num,Player player){
    	for(int i=1;i<=num;i++){
    		Card card = currentCardList.get(getRandomVal());
    		player.dealCard(card);
    		currentCardList.remove(card);
        }
    }
    //获取发牌随机数
    public int getRandomVal(){
		return (int) (Math.random() * (currentCardList.size()));
	}
	public Integer getAskChuaiNum() {
		return askChuaiNum;
	}
	public void setAskChuaiNum(Integer askChuaiNum) {
		this.askChuaiNum = askChuaiNum;
	}
	public List<Long> getChuJiaoList() {
		return chuJiaoList;
	}
	public void setChuJiaoList(List<Long> chuJiaoList) {
		this.chuJiaoList = chuJiaoList;
	}
	public List<Long> getFeiChuJiaoList() {
		return feiChuJiaoList;
	}
	public void setFeiChuJiaoList(List<Long> feiChuJiaoList) {
		this.feiChuJiaoList = feiChuJiaoList;
	}
	public Integer getChuType() {
		return chuType;
	}
	public void setChuType(Integer chuType) {
		this.chuType = chuType;
	}
	public Long getPingChuaiChu() {
		return pingChuaiChu;
	}
	public void setPingChuaiChu(Long pingChuaiChu) {
		this.pingChuaiChu = pingChuaiChu;
	}
	public Long getPingChuaiFeiChu() {
		return pingChuaiFeiChu;
	}
	public void setPingChuaiFeiChu(Long pingChuaiFeiChu) {
		this.pingChuaiFeiChu = pingChuaiFeiChu;
	}
	public List<Integer> getFanChuaiPai() {
		return fanChuaiPai;
	}
	public void setFanChuaiPai(List<Integer> fanChuaiPai) {
		this.fanChuaiPai = fanChuaiPai;
	}
	public List<Integer> getMengChuaiPai() {
		return mengChuaiPai;
	}
	public void setMengChuaiPai(List<Integer> mengChuaiPai) {
		this.mengChuaiPai = mengChuaiPai;
	}
	public Long getHasChengChu() {
		return hasChengChu;
	}
	public void setHasChengChu(Long hasChengChu) {
		this.hasChengChu = hasChengChu;
	}
	public List<Integer> getLastActionPai() {
		return lastActionPai;
	}
	public void setLastActionPai(List<Integer> lastActionPai) {
		this.lastActionPai = lastActionPai;
	}
	public Integer getAddIsChu() {
		return addIsChu;
	}
	public void setAddIsChu(Integer addIsChu) {
		this.addIsChu = addIsChu;
	}
	public List<List<Integer>> getXiaoJSInfo() {
		return xiaoJSInfo;
	}
	public void setXiaoJSInfo(List<List<Integer>> xiaoJSInfo) {
		this.xiaoJSInfo = xiaoJSInfo;
	}
	public void addXiaoJSInfo(List<Integer> xiaoJS){
		this.xiaoJSInfo.add(xiaoJS);
	}
}
