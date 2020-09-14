package org.bitrepository.reference.training.action.get;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler to handle a GetFile operation. 
 */
public class GetFileEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Object finishLock = new Object();
    private boolean finished = false;
    private boolean failed = false;
    
    /**
     * Constructor
     * @param pillarID The pillar which is expected to deliver the results.  
     */
    public GetFileEventHandler() {
        
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        switch(event.getEventType()) {
            case COMPLETE:
                log.info("Finished getFile for file '{}'", event.getFileID());
                finish();
                break;
            case FAILED:
                log.warn("Failed getFile for file '{}'", event.getFileID());
                failed = true;
                finish();
                break;
            default:
                break;
            }    
    }
    
    /**
     * Method to indicate the operation have finished (regardless if is successful or not). 
     */
    private void finish() {
        log.trace("Finish method invoked");
        synchronized (finishLock) {
            log.trace("Finish method entered synchronized block");
            finished = true;
            finishLock.notifyAll();
            log.trace("Finish method notified All");            
        }
    }

    /**
     * Method to wait for the operation to complete. The method is blocking.  
     * @throws InterruptedException if the thread is interrupted
     */
    public void waitForFinish() throws InterruptedException {
        synchronized (finishLock) {
            if(finished == false) {
                log.trace("Thread waiting for client to finish");
                finishLock.wait();
            }
            log.trace("Client have indicated it's finished.");
        }
    }
       
    /**
     * Method to determine if the operation was successful.
     * The method should not be called prior to a call to {@link #waitForFinish()} have returned. 
     * @return true if the operation succeeded, otherwise false. 
     */
    public boolean hasFailed() {
        return failed;
    }
 
}
