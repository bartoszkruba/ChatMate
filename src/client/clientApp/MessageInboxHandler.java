package client.clientApp;

import client.Controller;
import client.Main;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import models.Message;
import models.MessageType;
import models.Sendable;

public class MessageInboxHandler {
   private static MessageInboxHandler ourInstance = new MessageInboxHandler();

   public static MessageInboxHandler getInstance() {
      return ourInstance;
   }

}
