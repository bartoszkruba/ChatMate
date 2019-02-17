package models;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class Channel implements Comparable<Channel>, Sendable, Serializable {

   private String name;
   private ConcurrentSkipListSet<User> users;

   public Channel(String name) {
      this.name = name;
      this.users = new ConcurrentSkipListSet<>();
   }

   public String getName() {
      return name;
   }

   public SortedSet<User> getUsers() {
      return Collections.unmodifiableSortedSet(this.users);
   }


   public void setName(String name) {
      this.name = name;
   }

   public boolean addUser(User user) {
      return this.users.add(user);
   }

   public boolean removeUser(User user) {
      return this.users.remove(user);
   }

   // TODO: 2019-02-14 Implement getSorted method

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }


      if (obj instanceof Channel) {
         Channel c = (Channel) obj;
         return this.name.equals(c.name);
      }

      return false;
   }

   @Override
   public int compareTo(Channel o) {
      return this.name.compareTo(o.name);
   }

   @Override
   public String toString() {
      return this.name;
   }
}
















