package server.serverApp;


import models.*;

import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class MessageHandler implements Runnable {

   private LinkedBlockingQueue<Sendable> messages;

   public MessageHandler(LinkedBlockingQueue<Sendable> messages) {
      this.messages = messages;
   }

   @Override
   public void run() {
      // TODO: 2019-02-14 change true to isrunning
      while (true) {
         if (messages.size() > 0) {
            processMessages();
         }
         try {
            Thread.sleep(10);
         } catch (InterruptedException e) {
         }

      }
   }

   private void processMessages() {
      Sendable s = this.messages.remove();
      if (s instanceof Message) {
         Message m = (Message) s;

         switch (m.TYPE) {
            case DISCONNECT:
               sendToChannel(m);
               break;
            case CHANNEL_MESSAGE:
               sendToChannel(m);
               break;
            case JOIN_CHANNEL:
               addUserToChannel(m);
               break;
            case LEAVE_CHANNEL:
               removeUserFromChannel(m);
               break;
            case WHISPER_MESSAGE:
               sendToUser(m);
               break;
            case NICKNAME_CHANGE:
               changeNickname(m);
               break;
         }
      }
   }

   private void sendToChannel(Message m) {
      if (m.CHANNEL == null || m.CHANNEL.equals("")) {
         return;
      }
      Channel channel = ActiveChannelController.getInstance().getChannel(m.CHANNEL);
      if (channel != null) {
         SortedSet<User> users = channel.getUsers();
         if (users != null) {
            users.forEach(u -> {
               try {
                  ActiveUserController.getInstance().getUserOutbox(u).add(m);
               } catch (Exception e) {
               }
            });
         }
      }
   }

   private void addUserToChannel(Message m) {
      System.out.println("Adding User " + m.SENDER + " to channel " + m.CHANNEL);
      String channel = m.CHANNEL;
      UUID userID = m.SENDER;
      if (channel != null && userID != null) {
         User u = ActiveUserController.getInstance().getUser(userID);
         if (u != null) {
            if (ActiveChannelController.getInstance().getChannel(channel) == null) {
               ActiveChannelController.getInstance().addUserToChannel(u, channel);
               sendToUser(u, ActiveChannelController.getInstance().getChannel(channel));
            } else {
               m.NICKNAME = u.getNickName();
               sendToChannel(m);
               ActiveChannelController.getInstance().addUserToChannel(u, channel);
               sendToUser(u, ActiveChannelController.getInstance().getChannel(channel));
            }
         }
      }
      System.out.println("Users connected to " + m.CHANNEL + ": " + ActiveChannelController.getInstance().getChannel(m.CHANNEL).getUsers().size());
   }

   private void removeUserFromChannel(Message m) {
      if (m.SENDER != null && m.CHANNEL != null && !m.CHANNEL.equals("")) {
         User u = ActiveUserController.getInstance().getUser(m.SENDER);
         if (u != null) {
            ActiveChannelController.getInstance().getChannel(m.CHANNEL).removeUser(ActiveUserController.getInstance().getUser(m.SENDER));
            m.NICKNAME = u.getNickName();
            this.sendToChannel(m);
            System.out.println("Users connected to " + m.CHANNEL + ": " + ActiveChannelController.getInstance().getChannel(m.CHANNEL).getUsers().size());
         }
      }
   }

   private void sendToUser(Message m) {
      System.out.println("Sending message to " + m.RECEIVER);
      LinkedBlockingDeque<Message> outbox = ActiveUserController.getInstance().getUserOutbox(m.RECEIVER);
      outbox.add(m);
   }

   private void sendToUser(User u, Sendable s) {
      ActiveUserController.getInstance().getUserOutbox(u).add(s);
   }

   private void changeNickname(Message message) {
      User user = ActiveUserController.getInstance().getUser(message.SENDER);

      if (user != null) {
         user.setNickName(message.NICKNAME);

         String[] userChannels = ActiveChannelController.getInstance().getChannelsForUser(user);
         if (userChannels.length > 0) {
            Stream.of(userChannels).forEach(c -> {
               Message m = new Message(MessageType.NICKNAME_CHANGE);
               m.CHANNEL = c;
               m.SENDER = user.getID();
               m.NICKNAME = user.getNickName();
               sendToChannel(m);
            });
         } else {
            Message m = new Message(MessageType.NICKNAME_CHANGE);
            m.SENDER = user.getID();
            m.NICKNAME = user.getNickName();
            sendToUser(user, m);
         }
      }
   }

}
