package org.bitrepository.reference.training.action.getfileids;

import java.util.List;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler to handle results from a GetFileIDsrequest made to a single pillar. 
 */
public class GetFileIDsEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean partialResults = false;
    private final Object finishLock = new Object();
    private boolean finished = false;
    private boolean failed = false;
    private ResultingFileIDs fileIDs;
    
    private String pillarID;
    
    /**
     * Constructor
     * @param pillarID The pillar which is expected to deliver the results.  
     */
    public GetFileIDsEventHandler(String pillarID) {
        this.pillarID = pillarID;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        switch(event.getEventType()) {
        case COMPONENT_COMPLETE:
            log.debug("Got COMPONENT_COMPLETE event {}", event);
            if(event instanceof FileIDsCompletePillarEvent) {
                FileIDsCompletePillarEvent fileIDsEvent = (FileIDsCompletePillarEvent) event;
                if(fileIDsEvent.getContributorID().equals(pillarID)) {
                    fileIDs = fileIDsEvent.getFileIDs();
                    partialResults = fileIDsEvent.isPartialResult();
                } else {
                    log.warn("Got an event from an unexpected contributor '{}' expected '{}'", 
                            fileIDsEvent.getContributorID(), pillarID);
                }
            }
            case COMPLETE:
                log.info("Finished get fileIDs for file '{}'", event.getFileID());
                finish();
                break;
            case FAILED:
                log.warn("Failed get fileIDs for file '{}'", event.getFileID());
                failed = true;
                finish();
                break;
            default:
                break;
            }    
    }
    
    /**
     * Method to obtain the received fileIDs data. 
     * The method should not be called prior to a call to {@link #waitForFinish()} have returned. 
     * @return The checksum data if it have been returned by the pillar, otherwise null.   
     */
    public List<FileIDsDataItem> getFileIDsData() {
        return fileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem();
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
     * Method to determine if the received results is a partial result set. I.e. should
     * the client send a new request to get more results. 
     * The method should not be called prior to a call to {@link #waitForFinish()} have returned.
     * @return true if the results are partial   
     */
    public boolean partialResults() {
        return partialResults;
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
