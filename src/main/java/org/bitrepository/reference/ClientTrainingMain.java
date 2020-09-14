package org.bitrepository.reference;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.jms.JMSException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bitrepository.reference.training.Actions.Action;
import org.bitrepository.reference.training.CliOptions;
import org.bitrepository.reference.training.action.ClientAction;
import org.bitrepository.reference.training.action.get.GetAction;
import org.bitrepository.reference.training.action.getchecksums.GetChecksumsAction;
import org.bitrepository.reference.training.action.getfileids.GetFileIDsAction;
import org.bitrepository.reference.training.action.put.PutAction;
import org.bitrepository.reference.training.util.BitmagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTrainingMain {
    
    final static Logger log = LoggerFactory.getLogger(ClientTrainingMain.class);
    private final static String CLIENT_CERTIFICATE_FILE = "client-certificate.pem";

    public static void main(String[] args) throws IOException {
        String applicationConfig = System.getProperty("dk.kb.applicationConfig");
        
        int exitStatus = 0;
        String scriptName = "start-script.sh";
        Path configDir = Paths.get(applicationConfig);
        Path clientCertificate = configDir.resolve(CLIENT_CERTIFICATE_FILE);
        BitmagUtils.initialize(configDir, clientCertificate);
        try {
            Options options = CliOptions.getAllOptions();
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            Action action = Action.fromString(cmd.getOptionValue(CliOptions.ACTION_OPT));
            if(action == null) {
                CliOptions.printActionHelp(scriptName);
                System.exit(2);
            }
            if(cmd.hasOption(CliOptions.HELP_OPT)) {
                CliOptions.printHelp(scriptName, CliOptions.getActionOptions(action));
                System.exit(0);
            }

            ClientAction ca = null;
            try {
                // Parsing a second time to get action specific options.
                cmd = parser.parse(CliOptions.getActionOptions(action), args);    
            } catch (ParseException e) {
                CliOptions.printHelp(scriptName, CliOptions.getActionOptions(action));
                System.exit(2);
            } 
            try {
                switch(action) {
                case GET:
                    ca = new GetAction();
                    break;
                case PUT:
                    ca = new PutAction();
                    break;
                case GETFILEIDS: 
                    ca = new GetFileIDsAction(cmd, BitmagUtils.getFileIDsClient());
                    break;
                case GETCHECKSUMS: 
                    ca = new GetChecksumsAction(cmd, BitmagUtils.getChecksumsClient());
                    break;
                default:
                    throw new RuntimeException("Unknown action: '" + action + "'");
                }
                
                ca.performAction();
            } finally {
                try {
                    BitmagUtils.shutdown();
                } catch (JMSException e) {
                    log.error("Caught an error shutting down bitrepository", e);
                    System.err.println(e.getMessage());
                    exitStatus = 1;
                }
            }
        } catch (MissingOptionException e) {
            CliOptions.printHelp(scriptName, CliOptions.getAllOptions());
            exitStatus = 2;
        } catch (MissingArgumentException e) {
            CliOptions.printActionHelp(scriptName);
            exitStatus = 2;
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            exitStatus = 3;
        } catch (RuntimeException e) {
            log.error("Caught RuntimeException", e);
            System.err.println(e.getMessage());
            exitStatus = 1;
        }
        
        System.exit(exitStatus);
        
    }

}
