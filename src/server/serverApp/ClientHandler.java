package server.serverApp;

import models.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class ClientHandler implements Runnable {

   private ObjectOutputStream streamOut;
   private ObjectInputStream streamIn;
   private Socket socket;
   private ServerApp serverApp;
   private boolean isRunning = false;
   private final int TIMEOUT_MS = 50;
   private final User user;
   private final LinkedBlockingDeque<Sendable> userOutbox;
   private final LinkedBlockingQueue<Sendable> messageHandlerQueue;

   public ClientHandler(Socket socket, ServerApp serverApp, LinkedBlockingQueue<Sendable> messageHandlerQueue) {
      this.socket = socket;
      this.serverApp = serverApp;
      this.user = new User("Unknown");
      this.userOutbox = new LinkedBlockingDeque<>();
      this.messageHandlerQueue = messageHandlerQueue;

      ActiveUserController.getInstance().addUser(this.user, this.userOutbox);
      try {
         streamIn = new ObjectInputStream(socket.getInputStream());
         streamOut = new ObjectOutputStream(socket.getOutputStream());
         //Flush?
         socket.setSoTimeout(TIMEOUT_MS);
         isRunning = true;
      } catch (IOException e) {
         System.out.println("failed to create streams ");
      } catch (Exception e) {
         e.printStackTrace();
      }

      Message m = new Message(MessageType.CONNECT);
      m.SENDER = this.user.getID();
      m.NICKNAME = this.user.getNickName();

      this.userOutbox.add(m);

      System.out.println(socket.getInetAddress().toString() + " connected");
   }

   private void readMessage() {
      // TODO: 2019-02-14 try reconnect 
      try {
         Object object = streamIn.readObject();
         if (object instanceof Message) {
            Message message = (Message) object;
            message.SENDER = this.user.getID();
            // System.out.println(message);//Debug
            messageHandlerQueue.add(message);
            streamOut.reset();
         }
      } catch (SocketTimeoutException e) {
      } catch (IOException e) {
         // Kolla om möjlig återanslutning till server
         System.out.println("Error in Clientreader");
         tryDisconnect();
         // todo kolla om klienten är död, prova återanslutning
      } catch (ClassNotFoundException e) {
         System.out.println("Felaktig klass skickad");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void writeMessage() {
      int stop = 1;
      for (int i = 0; i < stop; i++) {
         if (clientHasMessages()) {
            Sendable m = this.userOutbox.getFirst();
            try {

               if (m instanceof Channel) {
                  Channel channel = (Channel) m;
                  System.out.println(channel.getName());
                  channel.getUsers().forEach(user -> {
                     System.out.println(user.getNickName());
                  });
               }

               streamOut.writeObject(m);

               // TODO: 2019-02-17 Bug - Removing first message from stack is not always right should check index instead
               this.userOutbox.removeFirst();
               i = 10;
            } catch (IOException e) {
               System.out.println("Error in clientWriter");
               stop = 10;
               try {
                  Thread.sleep(100);
               } catch (Exception exception) {
               }
               if (i == stop - 1) {
                  tryDisconnect();
                  i = 10;
               }
               // todo kolla om klienten är död, prova återanslutning
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

      }
   }

   private boolean clientHasMessages() {
      return userOutbox.size() > 0;
   }

   private void tryDisconnect() {
      cleanUpAfterUser();
      this.isRunning = false;
      System.out.println("Connection to " + socket.getInetAddress() + " lost");
   }

   private void cleanUpAfterUser() {
      String[] userChannels = ActiveChannelController.getInstance().getChannelsForUser(this.user);
      Stream.of(userChannels).forEach(c -> {
         Message message = new Message(MessageType.DISCONNECT);
         message.CHANNEL = c;
         message.SENDER = this.user.getID();
         message.TEXT_CONTENT = this.user.getNickName();
         this.messageHandlerQueue.add(message);
      });
      ActiveUserController.getInstance().removeUser(this.user);
      try {
         this.socket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void run() {
      while (serverApp.isRunning() && this.isRunning) {
         readMessage();
         writeMessage();
      }
   }
}
