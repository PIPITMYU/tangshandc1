package com.up72.game.dto.resp;

import com.up72.game.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/26.
 */
public class Player extends User {

	private Integer roomId;// 房间密码，也是roomSn
	private Integer state;// out离开状态（断线）;inline正常在线；
	private List<Card> currentCardList;// 用户手中当前的牌
	private List<Card> startCardList;//用户最初手牌
	private Integer position;// 位置信息；详见Cnst
	private Boolean chu;// 是否是储
	private String ip;//所在服务器ip 需与加入房间ip一致
	private Integer endNum;//第几个跑的
	private Integer score;// 玩家积分；初始为1000，待定
	private Integer thisScore;//当局得分
	private String notice;// 跑马灯信息
	private Integer playStatus;// dating用户在大厅中; in刚进入房间，等待状态; prepared准备状态; 缴牌状态（非出牌状态;跑了状态（非出牌状态）
	private Integer joinIndex;// 加入顺序
	private Long sessionId;
	private Integer quanJ;//全歼次数
	private Integer banJ;//半奸次数
	private Integer heJ;//和局次数
	private Integer bQuanJ;//被全歼次数
	private Integer bBanJ;//被半奸次数
	private Integer chuNum;//储的次数
	private Integer duChuNum;//独储的次数
	private Integer winType;//赢的类型
	private Map<String,Object> fanInfo;//动作番数
	private boolean anHasFaPai;//暗储是否发过牌
	
	public void initPlayer(Integer roomId,Integer playStatus,Integer score,Integer quanJ,Integer banJ,Integer heJ,
			Integer bBanJ,Integer bQuanJ,Integer chuNum,Integer duChuNum){
		if(roomId == null){
			this.position = null;
			this.joinIndex = null;			
		}
		this.roomId = roomId;
		this.chu = false;
		this.winType = null;
		this.currentCardList = new ArrayList<Card>();
		this.fanInfo = new LinkedHashMap<String,Object>();
		this.playStatus = playStatus;
		this.thisScore = 0;
		this.score = score;
		this.quanJ = quanJ;
		this.banJ = banJ;
		this.heJ = heJ;
		this.bBanJ = bBanJ;
		this.bQuanJ = bQuanJ;
		this.chuNum = chuNum;
		this.duChuNum = duChuNum;
		this.endNum = null;
		this.anHasFaPai = false;
	}
	public Integer getRoomId() {
		return roomId;
	}


	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	public List<Card> getCurrentCardList() {
		return currentCardList;
	}


	public void setCurrentCardList(List<Card> currentCardList) {
		this.currentCardList = currentCardList;
	}


	public List<Card> getStartCardList() {
		return startCardList;
	}


	public void setStartCardList(List<Card> startCardList) {
		this.startCardList = startCardList;
	}


	public Integer getPosition() {
		return position;
	}


	public void setPosition(Integer position) {
		this.position = position;
	}


	public Boolean getChu() {
		return chu;
	}


	public void setChu(Boolean chu) {
		this.chu = chu;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public Integer getEndNum() {
		return endNum;
	}


	public void setEndNum(Integer endNum) {
		this.endNum = endNum;
	}


	public Integer getScore() {
		return score;
	}


	public void setScore(Integer score) {
		this.score = score;
	}


	public Integer getThisScore() {
		return thisScore;
	}


	public void setThisScore(Integer thisScore) {
		this.thisScore = thisScore;
	}


	public String getNotice() {
		return notice;
	}


	public void setNotice(String notice) {
		this.notice = notice;
	}


	public Integer getPlayStatus() {
		return playStatus;
	}


	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}


	public Integer getJoinIndex() {
		return joinIndex;
	}


	public void setJoinIndex(Integer joinIndex) {
		this.joinIndex = joinIndex;
	}


	public Long getSessionId() {
		return sessionId;
	}


	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}


	public Integer getQuanJ() {
		return quanJ;
	}


	public void setQuanJ(Integer quanJ) {
		this.quanJ = quanJ;
	}


	public Integer getBanJ() {
		return banJ;
	}


	public void setBanJ(Integer banJ) {
		this.banJ = banJ;
	}


	public Integer getHeJ() {
		return heJ;
	}


	public void setHeJ(Integer heJ) {
		this.heJ = heJ;
	}


	public Integer getbQuanJ() {
		return bQuanJ;
	}


	public void setbQuanJ(Integer bQuanJ) {
		this.bQuanJ = bQuanJ;
	}


	public Integer getbBanJ() {
		return bBanJ;
	}


	public void setbBanJ(Integer bBanJ) {
		this.bBanJ = bBanJ;
	}


	public Integer getChuNum() {
		return chuNum;
	}


	public void setChuNum(Integer chuNum) {
		this.chuNum = chuNum;
	}


	public Integer getDuChuNum() {
		return duChuNum;
	}


	public void setDuChuNum(Integer duChuNum) {
		this.duChuNum = duChuNum;
	}


	public Integer getWinType() {
		return winType;
	}


	public void setWinType(Integer winType) {
		this.winType = winType;
	}


	public Map<String, Object> getFanInfo() {
		return fanInfo;
	}


	public void setFanInfo(Map<String, Object> fanInfo) {
		this.fanInfo = fanInfo;
	}

	
	@Override
	public String toString() {
		return "Player [roomId=" + roomId + ", state=" + state
				+ ", currentCardList=" + currentCardList + ", startCardList="
				+ startCardList + ", position=" + position + ", chu=" + chu
				+ ", ip=" + ip + ", endNum=" + endNum + ", score=" + score
				+ ", thisScore=" + thisScore + ", notice=" + notice
				+ ", playStatus=" + playStatus 
				+ ", joinIndex=" + joinIndex + ", sessionId=" + sessionId
				+ ", quanJ=" + quanJ + ", banJ=" + banJ + ", heJ=" + heJ
				+ ", bQuanJ=" + bQuanJ + ", bBanJ=" + bBanJ + ", chuNum="
				+ chuNum + ", duChuNum=" + duChuNum + ", winType=" + winType
				+ ", fanInfo=" + fanInfo + "]";
	}


	//发牌
	public void dealCard(Card card){
		this.currentCardList.add(card);
	}
	public boolean isAnHasFaPai() {
		return anHasFaPai;
	}
	public void setAnHasFaPai(boolean anHasFaPai) {
		this.anHasFaPai = anHasFaPai;
	}

	
	
	
}
