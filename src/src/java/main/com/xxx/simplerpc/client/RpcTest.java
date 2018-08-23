package com.xxx.simplerpc.client;

import com.xxx.simplerpc.EchoService;
import com.xxx.simplerpc.EchoServiceImpl;
import com.xxx.simplerpc.server.RpcExporter;

import java.net.InetSocketAddress;

public class RpcTest {
    public static void main(String[] args){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RpcExporter.exporter("localhost", 8088);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        RpcImporter<EchoService> importer = new RpcImporter<>();
        EchoService echoService = importer.importer(EchoServiceImpl.class,
                new InetSocketAddress("localhost", 8088));
        System.out.println(echoService.echo("Are you ok?"));
    }
}
