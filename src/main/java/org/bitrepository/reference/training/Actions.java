package org.bitrepository.reference.training;

public class Actions {
    /**
     * Action types for the available actions.  
     */
    public static enum Action {
        PUT("put"), 
        GET("get"),
        GETFILEIDS("getfileids"),
        GETCHECKSUMS("getchecksums");
        
        private String action;
        
        Action(String action) {
            this.action = action;
        }
        
        public String toString() {
            return action;
        }
        
        public static Action fromString(String action) {
            if (action != null) {
              for (Action a : Action.values()) {
                if (action.equalsIgnoreCase(a.action)) {
                  return a;
                }
              }
            }
            return null;
          }
        
    }
}
