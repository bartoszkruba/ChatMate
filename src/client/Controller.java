package client;

import client.clientApp.Client;
import client.clientApp.Sender;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import models.Message;
import models.MessageType;
import models.User;

import java.util.Iterator;

public class Controller {

   Client client;


   public void initialize() {
      client = Client.getInstance();
   }


}

