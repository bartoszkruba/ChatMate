package client.clientApp;

import models.Message;
import models.Sendable;
import models.User;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;

public class Client {
   private static Client ourInstance = new Client();

   ///todo Make private
   public static Client getInstance() {
      return ourInstance;
   }

   boolean isRunning;
   Socket socket;
   public Sender sender;
   public Receiver reciever;
   public ConcurrentSkipListMap<String, ConcurrentSkipListSet<User>> channelList = new ConcurrentSkipListMap<>();
   private ConcurrentHashMap<String, ArrayList<String>> channelMessages = new ConcurrentHashMap<>();
   private String currentChannel;
   private String nickname;
   private LinkedBlockingDeque<Sendable> messageHandlerQueue;
   private LinkedBlockingDeque<Sendable> senderQueue;

   private Client() {
      try {
         socket = new Socket("localhost", 54321);
         isRunning = true;
         sender = new Sender(socket);
         reciever = new Receiver(socket);
         sender.start();
         reciever.start();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void kill() {
      isRunning = false;

   }

   public String getNickname() {
      return nickname;
   }

   public void setNickname(String nickname) {
      this.nickname = nickname;
   }

   public ConcurrentHashMap<String, ArrayList<String>> getChannelMessages() {
      return channelMessages;
   }


   public String getCurrentChannel() {
      return currentChannel;
   }

   public void setCurrentChannel(String currentChannel) {
      this.currentChannel = currentChannel;
   }

   public LinkedBlockingDeque<Sendable> getMessageHandlerQueue() {
      return messageHandlerQueue;
   }

   public void setMessageHandlerQueue(LinkedBlockingDeque<Sendable> messageHandlerQueue) {
      this.messageHandlerQueue = messageHandlerQueue;
   }

   public LinkedBlockingDeque<Sendable> getSenderQueue() {
      return senderQueue;
   }

   public void setSenderQueue(LinkedBlockingDeque<Sendable> senderQueue) {
      this.senderQueue = senderQueue;
   }
}
