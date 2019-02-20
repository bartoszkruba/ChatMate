package client.clientApp;

import client.Controller;
import javafx.application.Platform;
import models.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;

public class MessageInboxHandler extends Thread {
   private LinkedBlockingDeque<Sendable> messages;
   private LinkedBlockingDeque<Sendable> senderQueue;
   private Controller controller;

   public MessageInboxHandler(LinkedBlockingDeque<Sendable> messages, LinkedBlockingDeque<Sendable> senderQueue, Controller controller) {
      this.messages = messages;
      this.controller = controller;
      this.senderQueue = senderQueue;
   }

   @Override
   public void run() {
      while (Client.getInstance().isRunning) {
         if (messages.size() > 0) {
            handleMessages();
         }
         try {
            Thread.sleep(100);

         } catch (InterruptedException e) {
         }
      }
   }

   private void handleMessages() {
      while (messages.size() > 0) {
         Sendable s = messages.removeFirst();
         if (s instanceof Message) {
            Message m = (Message) s;
            switch (m.TYPE) {
               case CHANNEL_MESSAGE:
                  process_CHANNEL_MESSAGE(m);
                  break;
               case JOIN_CHANNEL:
                  process_JOIN_CHANNEL(m);
                  break;
               case LEAVE_CHANNEL:
                  process_LEAVE_CHANNEL(m);
                  break;
               case DISCONNECT:
                  process_DISCONNECT(m);
                  break;
               case CONNECT: {
                  process_CONNECT(m);
                  break;
               }
               case NICKNAME_CHANGE:
                  process_NICKNAME_CHANGE(m);
                  break;
            }
         } else if (s instanceof Channel) {
            Channel channel = (Channel) s;
            process_CHANNEL(channel);
         }
      }
   }


   private void process_CHANNEL_MESSAGE(Message m) {
      ArrayList<String> channel = Client.getInstance().getChannelMessages().get(m.CHANNEL);
      if (channel != null) {
         String user;
         if (m.SENDER.toString().equals(Client.getInstance().getID())) {
            user = "You: ";
         } else {
            user = "Someone: ";
            for (User u : Client.getInstance().channelUsers.get(m.CHANNEL)) {
               if (u.getID().toString().equals(m.SENDER.toString())) {
                  user = u.getNickName() + ": ";
               }
            }
         }
         String message = "\n" + user + m.TEXT_CONTENT;
         channel.add(message);
         if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
            controller.textArea.appendText(message);
         }
      }
   }

   private void process_JOIN_CHANNEL(Message m) {
      Platform.runLater(() -> {
         ArrayList<String> channel = Client.getInstance().getChannelMessages().get(m.CHANNEL);
         if (channel != null) {
            String message = "\n" + m.NICKNAME + " joined";
            channel.add(message);
            User user = new User(m.NICKNAME, m.SENDER);
            Client.getInstance().channelUsers.get(m.CHANNEL).add(user);
            if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
               controller.users.add(user);
               controller.textArea.appendText(message);
            }
         }
      });
   }

   private void process_LEAVE_CHANNEL(Message m) {
      ArrayList<String> channel = Client.getInstance().getChannelMessages().get(m.CHANNEL);
      if (channel != null) {
         String message = "\n" + m.NICKNAME + " left";
         channel.add(message);
         Client.getInstance().channelUsers.get(m.CHANNEL).remove(new User("", m.SENDER));
         if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
            controller.users.remove(new User("", m.SENDER));
            controller.textArea.appendText(message);
         }
      }
   }


   private void process_DISCONNECT(Message m) {
      String message = "\n" + m.TEXT_CONTENT + " disconnected";
      Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
      Client.getInstance().channelUsers.forEach((key, value) -> {
         value.remove(new User("", m.SENDER));
      });
      if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
         controller.users.remove(new User("", m.SENDER));
         controller.textArea.appendText(message);
      }
   }

   private void process_CONNECT(Message m) {
      Client.getInstance().setNickname(m.NICKNAME);
      Client.getInstance().setID(m.SENDER.toString());
   }

   private void process_NICKNAME_CHANGE(Message m) {
      if (Client.getInstance().getID().equals(m.SENDER.toString())) {
         Client.getInstance().setNickname(m.NICKNAME);
      }

      User user = new User(m.NICKNAME, m.SENDER);
      int i = controller.users.indexOf(user);

      if (i >= 0) {
         Platform.runLater(() -> {
            controller.users.remove(i);
            controller.users.add(user);
         });
      }

      Client.getInstance().channelUsers.values().forEach(v -> {
         v.remove(user);
         v.add(user);
      });

   }

   private void process_CHANNEL(Channel channel) {

      try {
         Platform.runLater(() -> {
            controller.channels.add(channel);
            ArrayList<String> messages = Client.getInstance().getChannelMessages().getOrDefault(channel.getName(), new ArrayList<>());
            Client.getInstance().getChannelMessages().put(channel.getName(), messages);
            Client.getInstance().channelUsers.put(channel.getName(), new ConcurrentSkipListSet<>(channel.getUsers()));
            if (controller.channels.size() == 1) {
               controller.users.addAll(channel.getUsers());
               controller.channelList.getSelectionModel().selectFirst();
               Client.getInstance().setCurrentChannel(channel.getName());
            }
         });
      } catch (Exception e) {

      }
   }

}
