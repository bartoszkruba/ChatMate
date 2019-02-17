package server.serverApp;


import models.Message;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerApp {

   private ServerSocket serverSocket;
   private final int PORT = 54321;
   private ExecutorService clientHandlers;
   private MessageHandler messageHandler;
   private boolean running = false;
   private final LinkedBlockingQueue<Message> messageHandlerQueue;

   public ServerApp() {
      try {
         serverSocket = new ServerSocket(PORT);
         running = true;
      } catch (IOException e) {
         System.out.println("Could not create a server");
      } catch (Exception e) {
         e.printStackTrace();
      }

      clientHandlers = Executors.newCachedThreadPool();
      messageHandlerQueue = new LinkedBlockingQueue<>();

      messageHandler = new MessageHandler(messageHandlerQueue);

   }

   public boolean isRunning() {
      return running;
   }



   void kill() {
      clientHandlers.shutdown();
      running = false;
   }

   public void run() {
      if (running) {
         new Thread(messageHandler).start();
         System.out.println("Server is running on port " + PORT);
      }
      while (running) {
         try {
            Socket socket = serverSocket.accept();
            clientHandlers.submit(new ClientHandler(socket, this, messageHandlerQueue));
         } catch (IOException e) {
            System.out.println("Fail to connect to client");
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}


















