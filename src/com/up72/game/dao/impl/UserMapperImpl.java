package com.up72.game.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.UserMapper;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.SystemMessage;
import com.up72.game.model.User;
import com.up72.server.mina.utils.MyBatisUtils;

/**
 * Created by admin on 2017/6/22.
 */
public class UserMapperImpl implements UserMapper {

    @Override
    public void insert(Player entity) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insert";
                session.insert(sqlName, entity);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public void update(User entity) {

    }

    @Override
    public void updateMoney(Integer money, String userId) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".updateMoney";
                Map<Object, Object> map =new HashMap<>();
                map.put("money",money);
                map.put("userId",userId);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public Player findById(Long id) {
        Player result = null;
        System.out.println("findById id -> " + id);
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName =  UserMapper.class.getName()+".findById";
                System.out.println("sql name ==>> " + sqlName);
                result = session.selectOne(sqlName, id);
                session.close();
            }
        } catch (Exception e) {
            System.out.println("数据库操作出错！");
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    @Override
    public Player findByOpenId(String openId,String cid){
        Player result = null;
        System.out.println("findByOpenId openId" + openId);
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".findByOpenId";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("openId",openId);
                map.put("cid",cid);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findByOpenId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start,Integer limit) {
        List<PlayerRecord> result = new ArrayList<>();
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".findPlayerRecordByUserId";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("start",start);
                map.put("limit",limit);
                result = session.selectList(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findPlayerRecordByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public void userFeedback(Feedback feedback) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".userFeedback";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",feedback.getUserId());
                map.put("tel",feedback.getTel());
                map.put("content",feedback.getContent());
                map.put("createTime",feedback.getCreateTime());
                session.insert(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("userFeedback数据库操作出错！");
        }finally {
            session.close();
        }
    }

    @Override
    public Integer isExistUserId(String userId) {
        Integer result = null;
        System.out.println("isExistUserId openId" + userId);
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".isExistUserId";
                System.out.println("sql name ==>>" + sqlName);
                Map map =new HashMap<>();
                map.put("userId",userId);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("isExistUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public void updateUserAgree(Long userId) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".updateUserAgree";
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public String getNotice() {
        String result = "";
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getNotice";
                System.out.println("sql name ==>>" + sqlName);
                result = session.selectOne(sqlName);
                session.close();
            }
        }catch (Exception e){
            System.out.println("getNotice数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }
    
    @Override
    public String getConectUs() {
        String result = "";
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getConectUs";
                System.out.println("sql name ==>>" + sqlName);
                result = session.selectOne(sqlName);
                session.close();
            }
        }catch (Exception e){
            System.out.println("getConectUs数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }



    @Override
    public List<SystemMessage> getSystemMessage(Long userId, Integer start, Integer limit) {
        List<SystemMessage> result = new ArrayList<>();
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getSystemMessage";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<Object, Object>();
                map.put("userId",userId);
                map.put("start",start);
                map.put("limit",limit);
                result = session.selectList(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("getSystemMessage！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public void insertPlayRecord(Map<String,String> playRecord) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insertPlayRecord";
                session.insert(sqlName, playRecord);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    
    
    
    @Override
    public void insertPlayerMoneyRecord(PlayerMoneyRecord mr) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insertPlayerMoneyRecord";
                session.insert(sqlName, mr);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    
    @Override
    public Integer getUserMoneyByUserId(Long userId) {
        Integer result = null;
        System.out.println("getUserMoneyByUserId userId -> " + userId);
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName =  UserMapper.class.getName()+".getUserMoneyByUserId";
                System.out.println("sql name ==>> " + sqlName);
                result = session.selectOne(sqlName, userId);
                session.close();
            }
        } catch (Exception e) {
            System.out.println("数据库操作出错！getUserMoneyByUserId");
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

	@Override
	public void updateIpAndLastTime(String openId,String ip) {
		 SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = UserMapper.class.getName() + ".updateIpAndLastTime";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("openId", openId);
	                map.put("lastLoginTime",System.currentTimeMillis());
	                map.put("ip",ip);
	                session.update(sqlName,map);
	                session.commit();
//	                MyBatisUtils.closeSessionAndCommit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            session.close();
	        }
	}

	@Override
	public String findIpByUserId(Long userId) {
			String result = null;
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = UserMapper.class.getName()+".findIpByUserId";
	                System.out.println("sql name ==>>" + sqlName);
	                Map<Object,Object> map =new HashMap<>();
	                map.put("userId",userId);
	                result = session.selectOne(sqlName, map);
	                session.close();
	            }
	        }catch (Exception e){
	            System.out.println("findIpByUserId数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return result;
	}

	@Override
	public Integer findTotalGameNum(Long userId) {
		Integer result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".findTotalGameNum";
                System.out.println("sql name ==>>" + sqlName);
                Map<Object,Object> map =new HashMap<>();
                map.put("userId",userId);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
            System.out.println("findIpByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
	}
    
    
}
