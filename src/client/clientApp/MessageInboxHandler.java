package client.clientApp;

import client.Controller;
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
               case CHANNEL_MESSAGE: {
                  String message = "\n" + m.TEXT_CONTENT;
                  Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
                  if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                     controller.textArea.appendText(message);
                  }
                  break;
               }
               case JOIN_CHANNEL: {
                  String message = "\n" + m.TEXT_CONTENT + " joined";
                  Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
                  if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                     controller.textArea.appendText(message);
                  }
                  break;
               }
               case DISCONNECT: {
                  String message = "\n" + m.TEXT_CONTENT + " disconnected";
                  Client.getInstance().getChannelMessages().get(m.CHANNEL).add(message);
                  if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                     controller.textArea.appendText(message);
                  }
                  break;
               }
            }
         } else if (s instanceof Channel) {
            try {
               controller.channels.add((Channel) s);
               ArrayList<String> messages = Client.getInstance().getChannelMessages().getOrDefault(((Channel) s).getName(), new ArrayList<>());
               Client.getInstance().getChannelMessages().put(((Channel) s).getName(), messages);
               Client.getInstance().channelList.put(((Channel) s).getName(), new ConcurrentSkipListSet<>(((Channel) s).getUsers()));
               if (controller.channels.size() == 1) {
                  controller.channelList.getSelectionModel().selectFirst();
                  Client.getInstance().setCurrentChannel(((Channel) s).getName());
               }
            } catch (IllegalStateException e) {

            }
         }
      }
   }
}
