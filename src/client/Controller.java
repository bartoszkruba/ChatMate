package client;

import client.clientApp.Client;
import client.clientApp.MessageInboxHandler;
import client.clientApp.Sender;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import models.*;

import java.util.ArrayList;
import java.util.Comparator;
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
   public ListView channelList;

   private LinkedBlockingDeque<Sendable> messageHandlerQueue;
   private LinkedBlockingDeque<Sendable> senderQueue;

   public ObservableList<Channel> channels;

   public void initialize() {
      client = Client.getInstance();
      messageHandlerQueue = new LinkedBlockingDeque<>();
      senderQueue = new LinkedBlockingDeque<>();
      client.setMessageHandlerQueue(messageHandlerQueue);
      client.setSenderQueue(senderQueue);
      new MessageInboxHandler(messageHandlerQueue, senderQueue, this).start();

      channels = FXCollections.observableArrayList();

      SortedList<Channel> sortedList = new SortedList<>(channels, Comparator.comparing(Channel::getName));

      channelList.setItems(sortedList);
      channelList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

   }

   @FXML
   private void joinRoomBtnPressed() {
      String channel = roomField.getText();
      if (!channel.trim().equals("")) {
         if (channels.stream().filter(c -> c.getName().equals(channel)).toArray(Channel[]::new).length == 0) {
            Sendable message = new Message(MessageType.JOIN_CHANNEL);
            ((Message) message).CHANNEL = channel;
            senderQueue.add(message);
            if (client.getCurrentChannel() == null) {
               client.setCurrentChannel(channel);
            }
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

