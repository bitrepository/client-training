package org.bitrepository.reference.training;

import java.util.Arrays;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.bitrepository.reference.training.Actions.Action;

public class CliOptions {
    public final static String ACTION_OPT = "a";
    public final static String COLLECTION_OPT = "c";
    public final static String FILE_OPT = "f";
    public final static String FILEID_OPT = "i";
    public final static String PILLAR_OPT = "p";
    public final static String HELP_OPT = "h";
    
    private final static Option actionOption;
    private final static Option actionDetailedOption;
    private final static Option collectionOption 
        = new Option(COLLECTION_OPT, "collection", true, "Collection to work on");
    private final static Option fileOption 
        = new Option(FILE_OPT, "file", true, "File to work on");
    private final static Option fileIdOption 
        = new Option(FILEID_OPT, "fileID", true, "FileID to be processed to work on");
    private final static Option pillarOption 
        = new Option(PILLAR_OPT, "pillar", true, "Pillar to perform the action on");
    private final static Option helpOption = new Option(HELP_OPT, "help", false, "Prints help and usage information");
    
    static {
        actionOption = new Option(ACTION_OPT, "action", true, "Action to perform");
        actionOption.setRequired(true);
        actionDetailedOption = new Option(ACTION_OPT, "action", true, "Possible actions: " + Arrays.asList(Action.values()));
        actionDetailedOption.setRequired(true);
    }
    
    /**
     * Method to print the help message
     * @param scriptName The name of the program as it was invoked
     * @param options The options to include in the help message 
     */
    public static void printHelp(String scriptName, Options options) {
        String header = "Training client to work with bitrepository.org bitrepository\n\n";
                
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(scriptName, header, options, null, true);
    }
    
    /**
     * Method to print the help information when no action is specified.  
     * @param scriptName The name of the program as it was invoked
     */
    public static void printActionHelp(String scriptName) {
        Options opts = new Options();
        opts.addOption(actionDetailedOption);
        printHelp(scriptName, opts);
        
    }
    
    /**
     * Method to retrieve all the various options available for usage
     * @return The options for use in the client.  
     */
    public static Options getAllOptions() {
        Options options = new Options();
        options.addOption(actionOption);
        options.addOption(collectionOption);
        options.addOption(fileIdOption);
        options.addOption(fileOption);
        options.addOption(pillarOption);
        options.addOption(helpOption);
        return options;
    }
    
    /**
     * Method to retrieve the options that are relevant for a specific Action
     * @param action The {@link Action} to obtain options for
     * @return The options relevant for the given Action 
     */
    public static Options getActionOptions(Action action) {
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(actionOption);
        
        switch(action) {
        case PUT:
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(fileIdOption));
            options.addOption(makeOptionRequired(fileOption));
            break;
        case GET: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(fileIdOption));
            options.addOption(makeOptionRequired(fileOption));
            break;
        case GETFILEIDS: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(pillarOption));
            break;
        case GETCHECKSUMS: 
            options.addOption(makeOptionRequired(collectionOption));
            options.addOption(makeOptionRequired(pillarOption));
            options.addOption(makeOptionRequired(fileIdOption));
            break;
        }
        
        return options;
    }
    
    /**
     * Helper method to clone and make an option required for the specific use.
     * @param opt The option to deliver a clone marked as required
     * @return clone of the inputtet option marked as required  
     */
    private static Option makeOptionRequired(Option opt) {
        opt.setRequired(true);
        return opt;
    }
}
