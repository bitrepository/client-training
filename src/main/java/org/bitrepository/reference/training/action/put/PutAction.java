package org.bitrepository.reference.training.action.put;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.digest.DigestUtils;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.reference.training.CliOptions;
import org.bitrepository.reference.training.action.ClientAction;
import org.bitrepository.reference.training.util.BitmagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PutAction implements ClientAction {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private PutFileClient putClient;
    private String collectionID;
    private String fileID;
    private Path file;

    public PutAction(CommandLine cmd, PutFileClient putClient) {
        this.putClient = putClient;
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        fileID = cmd.getOptionValue(CliOptions.FILEID_OPT);
        file = Paths.get(cmd.getOptionValue(CliOptions.FILE_OPT));
    }
    
    @Override
    public void performAction() {
        try {
            String checksum = calculateChecksum();
            URL fileExchangeURL = buildFileExchangeUrl(checksum);
            PutFileEventHandler eventHandler = new PutFileEventHandler(BitmagUtils.getFileExchange(), 
                    fileID, fileExchangeURL, file);
            
            putClient.putFile(collectionID, fileExchangeURL, fileID, 0, 
                    BitmagUtils.getChecksum(checksum), null, eventHandler, 
                    "Put of new file as part of training client");

            eventHandler.waitForFinish();
            
            if(eventHandler.hasFailed()) {
                System.out.println("PutFile operation failed :(");
            } else {
                System.out.println("PutFile operation succeeded :)");
            }
        } catch (IOException e) {
            log.error("Failed to calculate checksum", e);
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete", e);
        }
    }
    
    
    // Simple checksum calculation. Assuming that the file actually exists otherwise IOExceptions will be thrown.
    private String calculateChecksum() throws IOException {
        String fileChecksum;
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ))) {
            fileChecksum = DigestUtils.md5Hex(is);
        }
        return fileChecksum;
    }
    
    // Build a semi-unique file address (won't work with checksum collisions, or simultaneous uploads of the same file)
    private URL buildFileExchangeUrl(String checksum) throws MalformedURLException {
        URL baseurl = BitmagUtils.getFileExchangeBaseURL();
        return new URL(baseurl.toString() + checksum);
    }
    
    
}
