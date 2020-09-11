# client-training

Repository containing a bit of code for training in the use of the bitrepository clients to produce a simple commandline client. 

It does not cover all clients but revolve around the Put, Get and GetFileIDs operations. 

## Building
The main code is in a buildable state, but will not do much. For a working example see the branch solution-proposal.
To do anything actual settings (RepositorySettings, ReferenceSettings and a matching certificate) will be needed. Those files are not included within this repository.

To build the code, use OpenJDK 1.8 and maven. 
Run: `mvn clean package`

The build will produce a tar-ball with the client java libraies and shell-sripts for running the commandline client. 
