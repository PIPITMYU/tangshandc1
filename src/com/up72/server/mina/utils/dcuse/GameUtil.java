package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.RoomResp;

public class GameUtil {
	// 判断是否有轰(反踹用)
	public static boolean checkHong(List<Card> playCard, Integer num) {
		Map<Integer, Integer> mapCard = new HashMap<Integer, Integer>();
		for (int i = 0; i < num; i++) {
			int sortNum = playCard.get(i).getSymble();
			if (mapCard.get(sortNum) == null) {
				mapCard.put(sortNum, 1);
			} else {
				mapCard.put(sortNum, mapCard.get(sortNum) + 1);
			}
		}
		Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry = it.next();
			// 轰
			if (entry.getValue() == 3 || entry.getValue() == 4) {
				return true;
			}
		}
		return false;
	}
	// 判断梦踹 
	public static boolean checkMengC(List<Card> playCard, Integer num) {
		Map<Integer, Integer> mapCard = new HashMap<Integer, Integer>();
		for (int i = 0; i < num; i++) {
			int sortNum = playCard.get(i).getSymble();
			if (mapCard.get(sortNum) == null) {
				mapCard.put(sortNum, 1);
			} else {
				mapCard.put(sortNum, mapCard.get(sortNum) + 1);
			}
		}
		Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry = it.next();
			// 轰
			if (entry.getKey()!=1 && (entry.getValue() == 3 || entry.getValue() == 4)) {
				return true;
			}
		}
		return false;
	}

	public static boolean check4Hong(List<Card> playCard, Integer num) {
		Map<Integer, Integer> mapCard = new HashMap<Integer, Integer>();
		for (int i = 0; i < num; i++) {
			int sortNum = playCard.get(i).getSymble();
			if (mapCard.get(sortNum) == null) {
				mapCard.put(sortNum, 1);
			} else {
				mapCard.put(sortNum, mapCard.get(sortNum) + 1);
			}
		}
		Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry = it.next();
			// 轰
			if (entry.getValue() == 4) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean check3Hong(List<Card> playCard, Integer num) {
		Map<Integer, Integer> mapCard = new HashMap<Integer, Integer>();
		for (int i = 0; i < num; i++) {
			int sortNum = playCard.get(i).getSymble();
			if (mapCard.get(sortNum) == null) {
				mapCard.put(sortNum, 1);
			} else {
				mapCard.put(sortNum, mapCard.get(sortNum) + 1);
			}
		}
		Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry = it.next();
			// 轰
			if (entry.getValue() == 3) {
				return true;
			}
		}
		return false;
	}

	// 数组转List
	public static List<Long> changeList(Long[] ids) {
		List<Long> list = new ArrayList<Long>();
		for (Long l : ids) {
			list.add(l);
		}
		return list;
	}

	// 判断是否有独储
	public static boolean checkDuChu(List<Card> playCard, Integer colorChu) {
		int count = 0;
		if (colorChu == 1 || colorChu == 3) {
			for (int i = 0; i < playCard.size(); i++) {
				Card card = playCard.get(i);
				if (card.getSymble() == 1
						&& (card.getType() == 2 || card.getType() == 4)) {
					count++;
				}
			}
		}
		if (colorChu == 2 || colorChu == 4) {
			for (int i = 0; i < playCard.size(); i++) {
				Card card = playCard.get(i);
				if (card.getSymble() == 1
						&& (card.getType() == 1 || card.getType() == 3)) {
					count++;
				}
			}
		}
		if (count == 2) {
			return true;
		}
		return false;
	}

	// 检测顺子
	public static boolean checkShun(List<Card> cards, RoomResp room) {
		if (cards == null || cards.size() < 3) {
			return false;
		} else {
			Map<Integer, Integer> cardMap = getPaiMap(cards);
			Set<Integer> keySet = cardMap.keySet();
			if (keySet.size() != cards.size()) {
				return false;
			}
			List<Integer> keyList = new ArrayList<Integer>(keySet);
			if (!checkHasA(cards,1)) {
				return checkListAdd(keyList);
			} else {
				// 包括A 1--->a23算顺
				keyList.remove(new Integer(1));
				if (checkListAdd(keyList)) {
					if (keyList.contains(2)) {
						if (room.getA23() == 1) {
							return true;
						} else {
							if(keyList.contains(13)){
								//2-13-1
								return true;
							}
							return false;
						}
					} else if (keyList.contains(13)) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}
	}

	// 检测双顺
	public static boolean checkShuangShun(List<Card> cards, RoomResp room) {
		if (room.getShuangShun() == 0 || cards.size() < 6) {
			return false;
		}
		Map<Integer, Integer> cardMap = getPaiMap(cards);
		Set<Integer> keySet = cardMap.keySet();
		for (Integer num : keySet) {
			if (cardMap.get(num) != 2) {
				return false;
			}
		}
		List<Integer> keyList = new ArrayList<Integer>(keySet);
		if (!checkHasA(cards,1)) {
			return checkListAdd(keyList);
		} else {
			// 包括A 1--->a23算顺
			keyList.remove(new Integer(1));
			if (checkListAdd(keyList)) {
				if (keyList.contains(2)) {
					if (room.getA23() == 1) {
						return true;
					} else {
						if(keyList.contains(13)){
							//2-13-1
							return true;
						}
						return false;
					}
				} else if (keyList.contains(13)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	// 检测list是否为递增
	public static boolean checkListAdd(List<Integer> keyList) {
		int firstNum = keyList.get(0);
		for (int i = 1; i < keyList.size(); i++) {
			if (firstNum + i != keyList.get(i)) {
				return false;
			}
		}
		return true;
	}

	// 检测牌中是否含有n
	public static boolean checkHasA(List<Card> cards,Integer n) {
		if (cards == null) {
			return false;
		}
		for (Card c : cards) {
			if (c.getSymble() == n)
				return true;
		}
		return false;
	}

	// 得到全部牌个数的map
	public static Map<Integer, Integer> getPaiMap(List<Card> cards) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if (cards == null || cards.size() == 0) {
			return map;
		}
		for (int i = 0; i < cards.size(); i++) {
			int num = cards.get(i).getSymble();
			if (map.get(num) == null) {
				map.put(num, 1);
			} else {
				map.put(num, map.get(num) + 1);
			}
		}
		return map;
	}
	//获取牌的全部点数 symble和  含2 排序比大小时用
	public static Integer getSumSymble(List<Card> cards){
		int sum = 0;
		for(Card c:cards){
			sum += c.getSymble();
		}
		return sum;
	}
	
	// 判断 是否能要的起
	public static boolean checkYao(List<Card> chuCards, RoomResp room) {
		List<Card> lastCards = room.getLastChuPai();
		if(chuCards!=null){
			Collections.sort(chuCards); 
		}
		if(lastCards != null){
			Collections.sort(lastCards);
		}
		//撑储不能单独出
		if(room.getChuType() == 1 && chuCards.get(0).getSortNum() == 16){
			if(chuCards.size()!=2){
				return false;
			}else{
				if(chuCards.get(1).getSortNum() != 16){
					return false;
				}
			}
		}
		if (lastCards == null || lastCards.size() == 0) {
			// 第一个出
			if(chuCards.size() == 1){
				return true;
			}else if(chuCards.size() == 2){
				if(chuCards.get(0).getSymble() == chuCards.get(1).getSymble()){
					return true;
				}
				return false;
			}else if(chuCards.size() == 3){
				if(checkShun(chuCards, room)){
					return true;
				}else if(check3Hong(chuCards, 3)){
					return true;
				}else{
					return false;
				}
			}else if(chuCards.size() == 4){
				if(checkShun(chuCards, room)){
					return true;
				}else if(check4Hong(chuCards, 4)){
					return true;
				}else{
					return false;
				}
			}else{
				if(checkShun(chuCards, room)){
					return true;
				}else if(checkShuangShun(chuCards, room)){
					return true;
				}else{
					return false;
				}
			}
		} else {
			//独储管一切
			if(room.getChuPlayers().size() == 1 && chuCards.size() == 2 && 
					chuCards.get(0).getSortNum() == 16 && chuCards.get(1).getSortNum() == 16){
				return true;
			}
			if(lastCards.size() == 1){
				if(chuCards.size() == 1){
					if(chuCards.get(0).getSortNum()>lastCards.get(0).getSortNum()){
						return true;
					}
					return false;
				}else if(chuCards.size() == 3 && check3Hong(chuCards, 3)){
					return true;
				}else if(chuCards.size() == 4 && check4Hong(chuCards, 4)){
					return true;
				}else{
					return false;
				}
			}else if(lastCards.size() == 2){
				if(chuCards.size() == 2){
					if(chuCards.get(0).getSymble() == chuCards.get(1).getSymble()){
						if(chuCards.get(0).getDoubleSortNum()>lastCards.get(0).getDoubleSortNum()){
							return true;	
						}
					}
					return false;					
				}else if(chuCards.size() == 3 && check3Hong(chuCards, 3)){
					return true;
				}else if(chuCards.size() == 4 && check4Hong(chuCards, 4)){
					return true;
				}else{
					return false;
				}
			}else if(lastCards.size() == 3){
				if(chuCards.size() == 4 && check4Hong(chuCards, 4)){
					return true;
				}
				if(check3Hong(lastCards, 3) && check3Hong(chuCards, 3)){
					if(chuCards.get(0).getDoubleSortNum()>lastCards.get(0).getDoubleSortNum()){
						return true;
					}
				}else if(checkShun(lastCards, room)){
					if(check3Hong(chuCards, 3)){
						return true;
					}else if(checkShun(chuCards, room)){
						//是否含 2 
						if(checkHasA(lastCards, 2)){
							//含2的话用点数总和比 包括123这种情况
							if(getSumSymble(lastCards)<getSumSymble(chuCards)){
								return true;
							}
						}else{
							if(chuCards.get(chuCards.size()-1).getDoubleSortNum() > lastCards.get(lastCards.size()-1).getDoubleSortNum()){
								return true;
							}
						}						
						return false;
					}
				}
				return false;
			}else if(lastCards.size() == 4){
				if(check4Hong(lastCards, 4) && chuCards.size() == 4 && check4Hong(chuCards, 4)){
					if(chuCards.get(0).getDoubleSortNum() > lastCards.get(0).getDoubleSortNum()){
						return true;
					}
					return false;
				}else if(checkShun(lastCards, room)){
					if(chuCards.size() == 3 && check3Hong(chuCards, 3)){
						return true;
					}else if(chuCards.size() == 4){
						if(check4Hong(chuCards, 4)){
							return true;
						}
						//是否含 2 
						if(checkHasA(lastCards, 2)){
							//含2的话用点数总和比 包括123这种情况
							if(getSumSymble(lastCards)<getSumSymble(chuCards)){
								return true;
							}
						}else{
							if(chuCards.get(chuCards.size()-1).getDoubleSortNum() > lastCards.get(lastCards.size()-1).getDoubleSortNum()){
								return true;
							}
						}						
						return false;
					}
				}else{
					return false;
				}
			}else{
				if(chuCards.size() == 3 && check3Hong(chuCards, 3)){
					return true;
				}else if(chuCards.size() == 4 && check4Hong(chuCards, 4)){
					return true;
				}else{
					if(chuCards.size()!=lastCards.size()){
						return false;
					}else{
						if(checkShun(lastCards, room) && checkShun(chuCards, room)){
							//是否含 2 
							if(checkHasA(lastCards, 2)){
								//含2的话用点数总和比 包括123这种情况
								if(getSumSymble(lastCards)<getSumSymble(chuCards)){
									return true;
								}
								if(checkHasA(lastCards, 13)){
									if(chuCards.get(0).getSymble() == 1 && chuCards.get(chuCards.size()-1).getSymble() == 3){
										return true;
									}
								}
							}else{
								if(chuCards.get(chuCards.size()-1).getDoubleSortNum() > lastCards.get(lastCards.size()-1).getDoubleSortNum()){
									return true;
								}
							}						
							return false;
						}else if(checkShuangShun(lastCards, room) && checkShuangShun(chuCards, room)){
							//是否含 2 
							if(checkHasA(lastCards, 2)){
								//含2的话用点数总和比 包括123这种情况
								if(getSumSymble(lastCards)<getSumSymble(chuCards)){
									return true;
								}
							}else{
								if(chuCards.get(chuCards.size()-1).getDoubleSortNum() > lastCards.get(lastCards.size()-1).getDoubleSortNum()){
									return true;
								}
							}						
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	// 明储 特殊牌型
	public static void mingTestPai(Integer i, List<Card> card) {
//		if (i == 0) {
//			card.add(new Card(101));
//			card.add(new Card(201));
//			card.add(new Card(102));
//			card.add(new Card(202));
//			card.add(new Card(103));
//			card.add(new Card(203));
//			card.add(new Card(104));
//			card.add(new Card(204));
//			card.add(new Card(105));
//			card.add(new Card(205));
//			card.add(new Card(106));
//			card.add(new Card(206));
//			card.add(new Card(107));
//		}
//		if (i == 1) {
//			card.add(new Card(301));
//			card.add(new Card(401));
//			card.add(new Card(302));
//			card.add(new Card(402));
//			card.add(new Card(303));
//			card.add(new Card(403));
//			card.add(new Card(304));
//			card.add(new Card(404));
//			card.add(new Card(305));
//			card.add(new Card(405));
//			card.add(new Card(306));
//			card.add(new Card(406));
//			card.add(new Card(207));
//		}
//		if (i == 2) {
//			card.add(new Card(108));
//			card.add(new Card(208));
//			card.add(new Card(109));
//			card.add(new Card(209));
//			card.add(new Card(110));
//			card.add(new Card(210));
//			card.add(new Card(111));
//			card.add(new Card(211));
//			card.add(new Card(112));
//			card.add(new Card(212));
//			card.add(new Card(113));
//			card.add(new Card(213));
//			card.add(new Card(307));
//		}
//		if (i == 3) {
//			card.add(new Card(308));
//			card.add(new Card(408));
//			card.add(new Card(309));
//			card.add(new Card(409));
//			card.add(new Card(310));
//			card.add(new Card(410));
//			card.add(new Card(311));
//			card.add(new Card(411));
//			card.add(new Card(312));
//			card.add(new Card(412));
//			card.add(new Card(313));
//			card.add(new Card(413));
//			card.add(new Card(407));
//		}
//		if (i == 0) {
//			card.add(new Card(102));
//			card.add(new Card(103));
//			card.add(new Card(104));
//			card.add(new Card(105));
//			card.add(new Card(106));
//			card.add(new Card(101));
//			card.add(new Card(107));
//			card.add(new Card(108));
//			card.add(new Card(109));
//			card.add(new Card(110));
//			card.add(new Card(111));
//			card.add(new Card(112));
//			card.add(new Card(113));
//		}
//		if (i == 1) {
//			card.add(new Card(202));
//			card.add(new Card(203));
//			card.add(new Card(204));
//			card.add(new Card(205));
//			card.add(new Card(206));
//			card.add(new Card(201));
//			card.add(new Card(207));
//			card.add(new Card(208));
//			card.add(new Card(209));
//			card.add(new Card(210));
//			card.add(new Card(211));
//			card.add(new Card(212));
//			card.add(new Card(213));
//		}
//		if (i == 2) {
//			card.add(new Card(302));
//			card.add(new Card(303));
//			card.add(new Card(304));
//			card.add(new Card(305));
//			card.add(new Card(306));
//			card.add(new Card(301));
//			card.add(new Card(307));
//			card.add(new Card(308));
//			card.add(new Card(309));
//			card.add(new Card(310));
//			card.add(new Card(311));
//			card.add(new Card(312));
//			card.add(new Card(313));
//		}
//		if (i == 3) {
//			card.add(new Card(402));
//			card.add(new Card(403));
//			card.add(new Card(404));
//			card.add(new Card(405));
//			card.add(new Card(406));
//			card.add(new Card(401));
//			card.add(new Card(407));
//			card.add(new Card(408));
//			card.add(new Card(409));
//			card.add(new Card(410));
//			card.add(new Card(411));
//			card.add(new Card(412));
//			card.add(new Card(413));
//		}
		//储A 卡死
		if (i == 0) {
			card.add(new Card(401));
			card.add(new Card(201));
			card.add(new Card(209));
			card.add(new Card(312));
			card.add(new Card(103));
			card.add(new Card(102));
			card.add(new Card(107));
			card.add(new Card(211));
			card.add(new Card(313));
			card.add(new Card(410));
			card.add(new Card(403));
			card.add(new Card(405));
			card.add(new Card(213));
		}
		if (i == 1) {
			card.add(new Card(301));
			card.add(new Card(113));
			card.add(new Card(204));
			card.add(new Card(206));
			card.add(new Card(409));
			card.add(new Card(205));
			card.add(new Card(108));
			card.add(new Card(105));
			card.add(new Card(208));
			card.add(new Card(411));
			card.add(new Card(306));
			card.add(new Card(413));
			card.add(new Card(112));
		}
		if (i == 2) {
			card.add(new Card(111));
			card.add(new Card(309));
			card.add(new Card(109));
			card.add(new Card(305));
			card.add(new Card(106));
			card.add(new Card(101));
			card.add(new Card(202));
			card.add(new Card(203));
			card.add(new Card(408));
			card.add(new Card(104));
			card.add(new Card(308));
			card.add(new Card(407));
			card.add(new Card(307));
		}
		if (i == 3) {
			card.add(new Card(412));
			card.add(new Card(304));
			card.add(new Card(207));
			card.add(new Card(210));
			card.add(new Card(310));
			card.add(new Card(404));
			card.add(new Card(406));
			card.add(new Card(302));
			card.add(new Card(212));
			card.add(new Card(110));
			card.add(new Card(311));
			card.add(new Card(402));
			card.add(new Card(303));
		}
	}
}
