package com.pjmike.netty.controller;

import com.pjmike.netty.protocol.protobuf.MessageBase;
import com.pjmike.netty.server.NettyServerHandler;
import com.pjmike.netty.utils.TaskExecutorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * @author pjmike
 * @create 2018-10-24 16:47
 */
@RestController
public class ConsumerController {
    @Autowired
    private NettyServerHandler nettyServerHandler;

    @GetMapping("/send")
    public void send(HttpServletResponse response,String deviceId,String content) throws InterruptedException {
        response.setCharacterEncoding("utf-8");
        String uuid = UUID.randomUUID().toString().replace("-", "");
        MessageBase.Message message = new MessageBase.Message()
                .toBuilder().setCmd(MessageBase.Message.CommandType.NORMAL)
                .setContent(content)
                .setRequestId(uuid).build();
        //发送给设备测
        nettyServerHandler.push(deviceId, message);


        //同步等待设备测的返回数据
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NettyServerHandler.responseMap.put(uuid, response);
        NettyServerHandler.countDownLatchMap.put(uuid, countDownLatch);

        //设置5秒超时
        TaskExecutorUtil.getTaskExecutor().execute(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (NettyServerHandler.resultMap.containsKey(uuid)){
                System.out.println("response has handled");
                NettyServerHandler.resultMap.remove(uuid);
            }else {
                System.out.println("处理超时");
                try {
                    response.getWriter().println("process time out");
                } catch (IOException e) {
                }
            }
            countDownLatch.countDown();

        });
        countDownLatch.await();
        NettyServerHandler.responseMap.remove(uuid);
        NettyServerHandler.countDownLatchMap.remove(uuid);
        System.out.println("process end ");
    }
}
