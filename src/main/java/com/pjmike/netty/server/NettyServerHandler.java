package com.pjmike.netty.server;

import com.pjmike.netty.protocol.message.HeartbeatResponsePacket;
import com.pjmike.netty.protocol.protobuf.MessageBase;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author pjmike
 * @create 2018-10-24 15:43
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<MessageBase.Message> {

    @Autowired
    ChannelRepository channelRepository;

    public static Map<String, HttpServletResponse> responseMap =new HashMap<>();
    public static  Map<String, CountDownLatch> countDownLatchMap = new HashMap<>();
    public static Map<String,Object> resultMap = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageBase.Message msg) throws Exception {
        if (msg.getCmd().equals(MessageBase.Message.CommandType.HEARTBEAT_REQUEST)) {
            log.info("收到客户端发来的心跳消息：{}", msg.toString());
            //回应pong
            ctx.writeAndFlush(new HeartbeatResponsePacket());
        } else if (msg.getCmd().equals(MessageBase.Message.CommandType.NORMAL)) {
            log.info("收到客户端的业务消息：{}",msg.toString());

            if (channelRepository.get("666666") != null){
                if (responseMap.containsKey(msg.getRequestId()) && countDownLatchMap.containsKey(msg.getRequestId())){
                    HttpServletResponse response = responseMap.get(msg.getRequestId());
                    PrintWriter printWriter = response.getWriter();
                    printWriter.println(msg.toString());
                    printWriter.flush();
                    resultMap.put(msg.getRequestId(),msg.toString());
                    countDownLatchMap.get(msg.getRequestId()).countDown();
                }
            }

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelRepository.put("666666", ctx.channel());
        System.out.println("channelActive");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println("channelRegistered");
    }

    public void push(String id,MessageBase.Message message){
        if (Objects.nonNull(channelRepository.get(id))){
            channelRepository.get(id).writeAndFlush(message);
        }
    }
}
