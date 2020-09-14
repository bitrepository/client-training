package org.bitrepository.reference.training.action.getfileids;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.reference.training.CliOptions;
import org.bitrepository.reference.training.action.ClientAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFileIDsAction implements ClientAction {
    private final Logger log = LoggerFactory.getLogger(getClass());    
    private GetFileIDsClient client;
    private String collectionID;
    private String pillarID;
    // Lets limit the page size to force pagination
    private int maxPageSize = 100;
    
    public GetFileIDsAction(CommandLine cmd, GetFileIDsClient client) {
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        pillarID = cmd.getOptionValue(CliOptions.PILLAR_OPT);
        this.client = client;
    }
    

    @Override
    public void performAction() {
        boolean notFinished = false;
        Date latestFileDate = new Date(0);
        Set<String> fileIDs = new HashSet<>();
        
        try {
            do {
                GetFileIDsEventHandler eh = getPageOfIDs(latestFileDate);
                eh.waitForFinish();
                
                List<FileIDsDataItem> fileIDsData = eh.getFileIDsData();
                for(FileIDsDataItem item : fileIDsData) {
                    fileIDs.add(item.getFileID());
                    Date modificationTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime());
                    if(modificationTime.after(latestFileDate)) {
                        latestFileDate = modificationTime;
                    }
                    notFinished = eh.partialResults();
                }
            } while(notFinished);
            
            printResults(fileIDs);
            
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete", e);
        }
    }

    private void printResults(Set<String> fileIDs) {
        System.out.println("FileID:");
        for(String fileID : fileIDs) {
            System.out.println(fileID);
        }
    }
    
    private GetFileIDsEventHandler getPageOfIDs(Date minDate) {
        GetFileIDsEventHandler eventHandler = new GetFileIDsEventHandler(pillarID);
        client.getFileIDs(collectionID, makeQuery(pillarID, minDate), null, null, eventHandler);
        return eventHandler;
    }
    
    private ContributorQuery[] makeQuery(String pillarID, Date minDate) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        res.add(new ContributorQuery(pillarID, minDate, null, maxPageSize));
        return res.toArray(new ContributorQuery[1]);
    }
    
}
