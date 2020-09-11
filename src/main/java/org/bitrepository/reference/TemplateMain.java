package org.bitrepository.reference;

import java.io.IOException;
import java.util.Arrays;

public class TemplateMain {

    public static void main(String[] args) throws IOException {
        HelloWorld helloWorld = new HelloWorld();

        String applicationConfig = System.getProperty("dk.kb.applicationConfig");
        System.out.println("Application config can be found in: '" + applicationConfig + "'");
        
        System.out.println("Arguments passed by commandline is: " + Arrays.asList(args));
        
        String speaker = "speaker";
        
        System.out.println(helloWorld.sayHello(speaker));

    }

}
