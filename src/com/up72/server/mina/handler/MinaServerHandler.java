package com.up72.server.mina.handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketAcceptor;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPFunctionExecutor;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.tcp.MinaTCPServer;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.WebSocketUtil;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 *
 * IoHandler是Mina实现其业务逻辑的顶级接口；在IoHandler中定义了7个方法，根据I/O事件来触发对应的方法：
 *
 */
public class MinaServerHandler extends IoHandlerAdapter {
	
    private static final MyLog logger = MyLog.getLogger(MinaServerHandler.class);
    private static final MyLog pressure = MyLog.getPressureLogger(MinaServerHandler.class);
    public static SocketAcceptor acceptor;
    private int count = 0;//0314001

    public MinaServerHandler() {
        super();
    }

    public MinaServerHandler(SocketAcceptor acceptor) {
        super();
        this.acceptor = acceptor;
    }

    public static ExecutorService workService = Executors.newFixedThreadPool(500);

    /**
     *
     * 单开线程处理客户端请求信息
     *
     */
    static class ReceivedTask implements Runnable {
        IoSession session;
        Object message;
        public ReceivedTask(IoSession session, Object message) {
            super();
            this.session = session;
            this.message = message;
        }
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            ProtocolData data = (ProtocolData) message;
            try {
            	if (data.getInterfaceId()==0) {
					session.close(true);
				}else{
					TCPFunctionExecutor.execute(session, (ProtocolData) message);
				}
            	
            } catch (Exception e) {
                logger.E("业务处理异常", e);
                //session.close(true);
                JSONObject obj = JSONObject.parseObject(data.getJsonString());
                Integer interfaceId = obj.getInteger("a");
                JSONObject result = TCPGameFunctions.getJSONObj(interfaceId,0,null);
                result.put("c","服务器异常"+interfaceId);
                ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
                session.write(pd);
            } finally {
                pressure.I("TCP 函数 [ " + data.getInterfaceId() + " ] 请求处理时间 -->> " + (System.currentTimeMillis() - now));
                Thread.currentThread().interrupt();
            }
        }
    }
    
    static String userMatcher = "[0-9]{6}";

    static class ClosedTask implements Runnable {
        IoSession session;
        public ClosedTask(IoSession session) {
            super();
            this.session = session;
        }
        @Override
        public void run() {
            try{
            	String userId = String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID));
            	if (userId!=null&&userId.matches(userMatcher)) {
                	RedisUtil.hdel(Cnst.REDIS_HEART_PREFIX, userId);//删除心跳
                	Player cp = RedisUtil.getPlayerByUserId(userId);
                	if (cp!=null&&cp.getRoomId()!=null) {
                		List<Player> players = RedisUtil.getPlayerList(cp.getRoomId());
                		List<Long> userIds = new ArrayList<Long>();
						userIds.add(cp.getUserId());
						List<Integer> status = new ArrayList<Integer>();
						status.add(Cnst.PLAYER_LINE_STATE_OUT);
						MessageFunctions.interface_100109(players, status,userIds,2);
					}
				}
            	session.close(true);
            }catch (Exception e){

            }
        }
    }
    /**
     *
     * 当实现IoHandler的类抛出异常时调用；
     *
     * @param session
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.E("会话发生异常 exceptionCaught，服务器 关闭 会话", cause);
        logger.I(" 会话 [" + session.getId() + "] 客户端关闭，服务器  关闭 会话");
        if (session!=null) {
            session.close(true);
		}
    }

    /**
     * 当一个消息被(IoSession#write)发送出去后调用；
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {// 当信息已经传送给客户端后触发此方法 0314001
//                System.out.println("信息已经传送给到达客户端void messageSent........");

        //-----super.messageSent(session, message);
    }

    /**
     *
     * 当接收了一个消息时调用；
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {// 当客户端发送的消息到达
    	try {
        	String clientIP = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        	session.setAttribute(Cnst.USER_SESSION_IP, clientIP);
		} catch (Exception e) {
			session.setAttribute(Cnst.USER_SESSION_IP, "");
		}
    	
        if(message instanceof  ProtocolData){
            ProtocolData data = (ProtocolData) message;
            if (data.isWebAccept()) {
                ProtocolData sendMessage = data;
                sendMessage.setJsonString(WebSocketUtil.getSecWebSocketAccept(data
                        .getJsonString()));
                session.write(sendMessage);
            } else {
            	ProtocolData pd = (ProtocolData) message;
            	if (pd.getJsonString().contains(MinaTCPServer.HEART_BEAT)) {
            		//以防万一，接收到的心跳数据乱了
					TCPFunctionExecutor.heart(session, (ProtocolData)message);
				}else{
	       		 	workService.execute(new ReceivedTask(session, message));
				}          
            }
        }
    }

    /**
     *
     * 当连接关闭时调用；
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("链接关闭，id为"+session.getId());
       // 当一个客户端关闭时
        workService.execute(new ClosedTask(session));
    }

    /**
     * 当连接进入空闲状态时调用；
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {// 当连接空闲时触发此方法 0314001
        System.out.println("连接空闲");
        super.sessionIdle(session, status);
        if (session!=null) {
            session.close(true);
		}
    }


    /**
     * 当一个新的连接建立时，由I/O processor thread调用；
     * @param session
     * @throws Exception
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("新客户端连接,id="+session.getId());//0314 当一个新客户端连接后触发此方法.
		super.sessionCreated(session);
    }

    /**
     * 当连接打开是调用；
     * @param session
     * @throws Exception
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        // set idle time to 60 seconds
        //当一个客端端连接进入时
        count++;
        System.out.println("第 " + count + " 个 client 登陆！address： : "
                + session.getRemoteAddress());//0314001
		//-----super.sessionOpened(session);
        session.getConfig().setBothIdleTime(Cnst.Session_life);
        logger.I(" session Opened ,连接数 -> " + acceptor.getManagedSessionCount());
        
    }
}
