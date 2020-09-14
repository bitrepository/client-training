package org.bitrepository.reference.training.action.put;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.protocol.FileExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler to handle a PutFile operation.
 * Optimizing by only uploading a file to the FileExchange if any pillar needs it.  
 */
public class PutFileEventHandler implements EventHandler {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Object finishLock = new Object();
    private boolean finished = false;
    private boolean failed = false;

    private String fileID;
    private URL fileUrl;
    private FileExchange fileExchange;
    private Path file;

    
    /**
     * Constructor
     * @param pillarID The pillar which is expected to deliver the results.  
     */
    public PutFileEventHandler(FileExchange fileExchange, String fileID, URL fileUrl, Path file) {
        this.fileExchange = fileExchange;
        this.fileID = fileID;
        this.fileUrl = fileUrl;
        this.file = file;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        switch(event.getEventType()) {
            case IDENTIFICATION_COMPLETE:
                uploadFile();
                break;
            case COMPLETE:
                log.info("Finished get fileIDs for file '{}'", event.getFileID());
                cleanupFileExchange();
                finish();
                break;
            case FAILED:
                log.warn("Failed get fileIDs for file '{}'", event.getFileID());
                failed = true;
                cleanupFileExchange();
                finish();
                break;
            default:
                break;
            }    
    }
    
    
    private void uploadFile() {
        try {
            fileExchange.putFile(Files.newInputStream(file), fileUrl);
            log.debug("Finished upload of file {}.", fileID);
        } catch (IOException e) {
            log.error("Failed to upload file {}.", fileID, e);
            failed = true;
            finish();
        }        
    }
    
    private void cleanupFileExchange() {
        try {
            fileExchange.deleteFile(fileUrl);
            log.debug("Finished removing file {} from file exchange.", fileUrl);
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to remove file '{}' from file exchange.", fileUrl, e);
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
