package com.chgreen.app;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;


public class Router 
{
    public static void main( String[] args ) throws Exception
    {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel
        .open();
        String host = "localhost";
        int portBroker = 5000;
        int portMarket = 5001;
        InetSocketAddress sAddr = new InetSocketAddress(host, portBroker);       
        server.bind(sAddr);      
        System.out.format("Server is listening at %s%n", sAddr);
        Attachment attach = new Attachment();
        attach.server = server;
        server.accept(attach, new ConnectionHandler());
        Thread.currentThread().join();
        
        
        
        
        
        
        /*int totalBrokers = 0;
        int totalMarkets = 0;
        ServerSocket serverSocketBroker = new ServerSocket(5000);
        Socket socketBroker = serverSocketBroker.accept();
        System.out.println( "when does this  happen?" );
        InputStreamReader IRBroker = new InputStreamReader(socketBroker.getInputStream());
        BufferedReader BRBroker = new BufferedReader(IRBroker);

        while(true){
            String message = BRBroker.readLine();
            System.out.println(message);
            if (message.equalsIgnoreCase("exit")){
                serverSocketBroker.close();
                return;
            }

            if (message != null)
            {
                PrintStream PSBroker = new PrintStream(socketBroker.getOutputStream());
                PSBroker.println("Message received!");
            }
        }*/
    }

    private int validateCheckSum(){

        return(0);
    }

    private int generateID(){

        return(0);
    } 
}

class Attachment {
    AsynchronousServerSocketChannel server;
    AsynchronousSocketChannel client;
    ByteBuffer buffer;
    SocketAddress clientAddr;
    boolean isRead;
}

class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Attachment>{

    @Override
    public void completed(AsynchronousSocketChannel client, Attachment attach) {
        try{
            SocketAddress clientAddr = client.getRemoteAddress();
            System.out.format("Accepted a  connection from  %s%n", clientAddr);
            attach.server.accept(attach, this);
            ReadWriteHandler rwHandler = new ReadWriteHandler();
            Attachment newAttach = new Attachment();
            newAttach.server = attach.server;
            newAttach.client = client;
            newAttach.buffer = ByteBuffer.allocate(2048);
            newAttach.isRead = true;
            newAttach.clientAddr = clientAddr;
            client.read(newAttach.buffer, newAttach, rwHandler);            
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void failed(Throwable exc, Attachment attachment) {
            System.out.println("Failed to accept a connection.");
            exc.printStackTrace();
    }

}

class ReadWriteHandler implements CompletionHandler<Integer, Attachment>{

    @Override
    public void completed(Integer result, Attachment attach) {
        if (result == -1){
            try{
                attach.client.close();
                System.out.format("Stopped   listening to the   client %s%n",attach.clientAddr);
            }
            catch(IOException e){
                e.printStackTrace();
            }
            return;
        }

        if (attach.isRead){
            attach.buffer.flip();
            int limits = attach.buffer.limit();
            byte[] bytes = new byte[limits];
            attach.buffer.get(bytes, 0, limits);
            Charset cs = Charset.forName("UTF-8");
            String msg = new String(bytes, cs);
            System.out.format("Client at  %s  says: %s%n", attach.clientAddr, msg);
            attach.isRead = false;
            attach.buffer.rewind();
        }
        else {
            attach.client.write(attach.buffer, attach, this);
            attach.isRead = true;
            attach.buffer.clear();
            attach.client.read(attach.buffer, attach, this);
        }

    }

    @Override
    public void failed(Throwable exc, Attachment attach) {
            exc.printStackTrace();
    }

}