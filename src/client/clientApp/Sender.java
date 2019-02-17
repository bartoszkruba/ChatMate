package client.clientApp;

import models.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender extends Thread {
   private Socket socket;
   private static ObjectOutputStream objectOutputStream;

   public Sender(Socket socket) {
      this.socket = socket;
      try {
         objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      } catch (IOException e) {
         System.out.println("failed to create stream");
         Client.getInstance().isRunning = false;
         // todo kolla om server är död, prova återanslutning
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void sendToServer(Object o) {
      try {
         objectOutputStream.writeObject(o);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void run() {
      while (Client.getInstance().isRunning) {
         try {
            objectOutputStream.writeObject(new Message());
            Thread.sleep(100);
         } catch (Exception e) {

            // todo kolla om server är död, prova återanslutning
         }
      }

   }

}
