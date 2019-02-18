package client.clientApp;

import client.Controller;
import javafx.application.Platform;
import models.*;

import java.awt.*;
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
            Platform.runLater(() -> {
               switch (m.TYPE) {
                  case CHANNEL_MESSAGE: {
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
                     Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
                     if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                        controller.textArea.appendText(message);
                     }
                     break;
                  }
                  case JOIN_CHANNEL: {
                     Platform.runLater(() -> {
                        String message = "\n" + m.NICKNAME + " joined";
                        Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
                        User user = new User(m.NICKNAME, m.SENDER);
                        Client.getInstance().channelUsers.get(m.CHANNEL).add(user);
                        if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                           controller.users.add(user);
                           controller.textArea.appendText(message);
                        }
                     });
                     break;
                  }
                  case DISCONNECT: {
                     String message = "\n" + m.TEXT_CONTENT + " disconnected";
                     Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
                     Client.getInstance().channelUsers.forEach((key, value) -> {
                        value.remove(new User("", m.SENDER));
                     });
                     if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                        controller.users.remove(new User("", m.SENDER));
                        controller.textArea.appendText(message);
                     }
                     break;
                  }
                  case CONNECT: {
                     Client.getInstance().setNickname(m.NICKNAME);
                     Client.getInstance().setID(m.SENDER.toString());
                  }
               }
            });
         } else if (s instanceof Channel) {
            try {
               Platform.runLater(() -> {
                  controller.channels.add((Channel) s);
                  ArrayList<String> messages = Client.getInstance().getChannelMessages().getOrDefault(((Channel) s).getName(), new ArrayList<>());
                  Client.getInstance().getChannelMessages().put(((Channel) s).getName(), messages);
                  Client.getInstance().channelUsers.put(((Channel) s).getName(), new ConcurrentSkipListSet<>(((Channel) s).getUsers()));
                  if (controller.channels.size() == 1) {
                     controller.users.addAll(((Channel) s).getUsers());
                     controller.channelList.getSelectionModel().selectFirst();
                     Client.getInstance().setCurrentChannel(((Channel) s).getName());
                  }
               });
            } catch (Exception e) {

            }
         }
      }
   }
}
