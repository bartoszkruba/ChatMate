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

   @FXML
   public TextArea textArea;

   @FXML
   public TextField chattField;

   @FXML
   public Button sendButton;

   @FXML
   private TextField roomField;

   @FXML
   private Button joinRoomBtn;

   private LinkedBlockingDeque<Sendable> messageHandlerQueue;
   private LinkedBlockingDeque<Sendable> senderQueue;

   public void initialize() {
      client = Client.getInstance();
      messageHandlerQueue = new LinkedBlockingDeque<>();
      senderQueue = new LinkedBlockingDeque<>();
      client.setMessageHandlerQueue(messageHandlerQueue);
      client.setSenderQueue(senderQueue);
      new MessageInboxHandler(messageHandlerQueue, senderQueue, this).start();
   }

   @FXML
   private void joinRoomBtnPressed() {
      String channel = roomField.getText();
      if (!channel.trim().equals("")) {
         Sendable message = new Message(MessageType.JOIN_CHANNEL);
         ((Message) message).CHANNEL = channel;
         senderQueue.add(message);
         if (client.getCurrentChannel() == null) {
            client.setCurrentChannel(channel);
         }
         roomField.clear();
      }
   }

   @FXML
   private void sendButtonPressed() {
      String content = chattField.getText();
      if (!content.trim().equals("")) {
         Sendable message = new Message(MessageType.CHANNEL_MESSAGE);
         ((Message) message).CHANNEL = client.getCurrentChannel();
         ((Message) message).TEXT_CONTENT = content;
         senderQueue.add(message);
         chattField.clear();
      }
   }


}

