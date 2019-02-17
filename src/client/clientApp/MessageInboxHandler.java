package client.clientApp;

import client.Controller;
import client.Main;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import models.Channel;
import models.Message;
import models.MessageType;
import models.Sendable;

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
                  if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                     controller.textArea.appendText("\n" + m.CHANNEL + ": " + m.TEXT_CONTENT);
                  }
                  break;
               case JOIN_CHANNEL:
                  if (m.CHANNEL.equals(Client.getInstance().getCurrentChannel())) {
                     controller.textArea.appendText("\n" + "User " + m.SENDER + " joined channel " + m.CHANNEL);
                  }
            }
         } else if (s instanceof Channel) {
            try {
               controller.channels.add((Channel) s);
               if (controller.channels.size() == 1) {
                  controller.channelList.getSelectionModel().selectFirst();
               }
            } catch (IllegalStateException e) {

            }
         }
      }
   }
}
