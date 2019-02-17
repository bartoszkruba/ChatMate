package client.clientApp;

import client.Controller;
import client.Main;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
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
         // Handle things here
      }
   }
}
