package client;

import client.clientApp.Client;
import client.clientApp.MessageInboxHandler;
import client.clientApp.Sender;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import models.Message;
import models.MessageType;
import models.Sendable;
import models.User;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Controller {

   Client client;


   public void initialize() {
      client = Client.getInstance();
      LinkedBlockingDeque<Sendable> messageHandlerQueue = new LinkedBlockingDeque<Sendable>();
      LinkedBlockingDeque<Sendable> senderQueue = new LinkedBlockingDeque<>();
      client.setMessageHandlerQueue(messageHandlerQueue);
      client.setSenderQueue(senderQueue);
      new MessageInboxHandler(messageHandlerQueue, senderQueue, this).start();
   }


}

