package client.clientApp;

import client.Controller;
import client.Main;
import models.Channel;
import models.Message;
import models.Sendable;
import models.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;

import static client.Main.primaryStage;

public class Receiver extends Thread {
   private Socket socket;
   private ObjectInputStream objectInputStream;

   public Receiver(Socket socket) {
      this.socket = socket;
      try {
         this.objectInputStream = new ObjectInputStream(socket.getInputStream());
      } catch (IOException e) {
         System.out.println("failed to create input stream");
         Client.getInstance().isRunning = false;
         // todo kolla om server är död, prova återanslutning
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void run() {
      while (Client.getInstance().isRunning) {
         try {
            Sendable inData = (Sendable) objectInputStream.readObject();
            Client.getInstance().getMessageHandlerQueue().add(inData);
         } catch (IOException e) {
            System.out.println("Read Error");
            Client.getInstance().isRunning = false;
            // todo kolla om server är död, prova återanslutning
         } catch (ClassNotFoundException e) {
            System.out.println("Message Error");
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
