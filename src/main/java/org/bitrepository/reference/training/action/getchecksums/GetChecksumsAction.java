package org.bitrepository.reference.training.action.getchecksums;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.reference.training.CliOptions;
import org.bitrepository.reference.training.action.ClientAction;
import org.bitrepository.reference.training.util.BitmagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetChecksumsAction implements ClientAction {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT);
    
    private GetChecksumsClient checksumsClient;
    private String collectionID;
    private String pillarID;
    private String fileID;
    
    public GetChecksumsAction(CommandLine cmd, GetChecksumsClient checksumsClient) {
        collectionID = cmd.getOptionValue(CliOptions.COLLECTION_OPT);
        pillarID = cmd.getOptionValue(CliOptions.PILLAR_OPT);
        fileID = cmd.getOptionValue(CliOptions.FILE_OPT);
        this.checksumsClient = checksumsClient;
    }

    @Override
    public void performAction() {
        GetChecksumsEventHandler eventHandler = new GetChecksumsEventHandler(pillarID);
        checksumsClient.getChecksums(collectionID, getQuery(pillarID, new Date(0)), fileID, 
                BitmagUtils.getChecksumSpec(ChecksumType.MD5), null, eventHandler, null);
        
        try {
            eventHandler.waitForFinish();
            
            List<ChecksumDataForChecksumSpecTYPE> checksumData = eventHandler.getChecksumData();
            System.out.println("Checksum:\t CalculationTime:\t FileID:");    
            for(ChecksumDataForChecksumSpecTYPE cd : checksumData) {
                Date calculationDate = CalendarUtils.convertFromXMLGregorianCalendar(cd.getCalculationTimestamp());
                String checksum = Base16Utils.decodeBase16(cd.getChecksumValue());
                System.out.println(String.format(Locale.ROOT, "%s\t %s\t %s", checksum, sdf.format(calculationDate), cd.getFileID()));
            }
            
            
        } catch (InterruptedException e) {
            log.error("Got interrupted while waiting for operation to complete");
        }
        
    }
    
    private ContributorQuery[] getQuery(String pillarID, Date minDate) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        res.add(new ContributorQuery(pillarID, minDate, null, 10000));
        return res.toArray(new ContributorQuery[1]);
    } 

}
