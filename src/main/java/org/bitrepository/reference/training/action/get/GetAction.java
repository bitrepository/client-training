package org.bitrepository.reference.training.action.get;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.RandomStringUtils;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.reference.training.CliOptions;
import org.bitrepository.reference.training.action.ClientAction;
import org.bitrepository.reference.training.util.BitmagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAction implements ClientAction {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private GetFileClient getFileClient;
    private String collectionID;
    private String fileID;
    private Path file;


    public GetAction(CommandLine cmd, GetFileClient getFileClient) {
        this.getFileClient = getFileClient;
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        fileID = cmd.getOptionValue(CliOptions.FILEID_OPT);
        file = Paths.get(cmd.getOptionValue(CliOptions.FILE_OPT));
    }

    @Override
    public void performAction() {
        if(file.toFile().exists()) {
            System.out.println("Output file already exists, won't overwrite it. Exiting.");
            System.exit(1);
        }
        
        try {
            URL fileExchangeUrl = buildFileExchangeUrl(fileID);
            GetFileEventHandler eventHandler = new GetFileEventHandler();
            getFileClient.getFileFromFastestPillar(collectionID, fileID, null, fileExchangeUrl, eventHandler, "Training fetch");
            eventHandler.waitForFinish();
            
            if(eventHandler.hasFailed()) {
                System.out.println("Failed to get file"); 
            } else {
                try (OutputStream outputStream = Files.newOutputStream(file)) {
                    BitmagUtils.getFileExchange().getFile(outputStream, fileExchangeUrl);
                    BitmagUtils.getFileExchange().deleteFile(fileExchangeUrl);
                } 
                System.out.println("File obtained, can be found at '" + file.toString() + "'");
            }
                        
        } catch (MalformedURLException e) {
            log.error("Failed to build url", e);
        } catch (IOException e) {
            log.error("Failed to get file from fileexchange", e );
        } catch (URISyntaxException e) {
            log.error("Failed to cleanup on fileexchange", e);
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete", e);
        } 

        
    }

    
 // Build a unique file address to avoid collitions
    private URL buildFileExchangeUrl(String fileID) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        return new URL(baseurl.toString() + fileID + RandomStringUtils.randomAlphabetic(8));
    }
}
