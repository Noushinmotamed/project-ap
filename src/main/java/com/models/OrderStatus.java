package com.models;

/**
 *           - submitted
 *           - unpaid and cancelled
 *           - waiting vendor
 *           - cancelled
 *           - finding courier
 *           - on the way
 *           - completed
 */

public enum OrderStatus {
   submitted("submitted"),
   unpaidAndCancelled("unpaid and cancelled"),
   waitingVendor("waiting vendor"),
   cancelled("cancelled"),
   findingCourier("finding courier"),
   onTheWay("on the way"),
   complete("complete");

   private final String displayName;

   OrderStatus(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return displayName;
   }
}
