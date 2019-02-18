package client;

import client.clientApp.Client;
import client.clientApp.MessageInboxHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import models.*;

import java.time.LocalDate;
import java.util.Comparator;
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
   public ListView<Channel> channelList;

   @FXML
   public ListView<User> usersList;

   @FXML
   private ContextMenu listContextMenu;

   private LinkedBlockingDeque<Sendable> messageHandlerQueue;
   private LinkedBlockingDeque<Sendable> senderQueue;

   public ObservableList<Channel> channels;
   public ObservableList<User> users;

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

      channelList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Channel>() {
         @Override
         public void changed(ObservableValue<? extends Channel> observable, Channel oldValue, Channel newValue) {
            client.setCurrentChannel(newValue.getName());
            textArea.clear();
            client.getChannelMessages().get(newValue.getName()).forEach(textArea::appendText);

            users.clear();
            users.addAll(Client.getInstance().channelUsers.get(newValue.getName()));
         }
      });


      users = FXCollections.observableArrayList();

      SortedList<User> userSortedList = new SortedList<>(users, Comparator.comparing(User::getNickName));

      usersList.setItems(userSortedList);
      usersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

      listContextMenu = new ContextMenu();
      MenuItem deleteMenuItem = new MenuItem("Leave Channel");
      deleteMenuItem.setOnAction((ActionEvent event) -> {
         System.out.println("Leaving " + channelList.getSelectionModel().getSelectedItem().getName());
      });

      listContextMenu.getItems().addAll(deleteMenuItem);

      channelList.setCellFactory(new Callback<ListView<Channel>, ListCell<Channel>>() {
         @Override
         public ListCell<Channel> call(ListView<Channel> param) {
            ListCell<Channel> cell = new ListCell<>() {
               @Override
               protected void updateItem(Channel item, boolean empty) {
                  super.updateItem(item, empty);
                  if (empty) {
                     setText(null);
                  } else {
                     setText(item.toString());
                  }
               }
            };

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
               if (isNowEmpty) {
                  cell.setContextMenu(null);
               } else {
                  cell.setContextMenu(listContextMenu);
               }
            });

            return cell;
         }
      });

   }

   @FXML
   private void joinRoomBtnPressed() {
      String channel = roomField.getText();
      if (!channel.trim().equals("")) {
         if (channels.stream().filter(c -> c.getName().equals(channel)).toArray(Channel[]::new).length == 0) {
            Sendable message = new Message(MessageType.JOIN_CHANNEL);
            ((Message) message).CHANNEL = channel;
            senderQueue.add(message);
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

   @FXML
   private void handleKeyPresseddOnRoomField(KeyEvent e) {
      if (e.getCode().toString().equals("ENTER")) {
         joinRoomBtnPressed();
      }
   }

   @FXML
   private void handleKeyPresseddOnChattField(KeyEvent e) {
      if (e.getCode().toString().equals("ENTER")) {
         sendButtonPressed();
      }
   }

}

