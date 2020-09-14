# client-training

Repository containing a bit of code for training in the use of the bitrepository clients to produce a simple commandline client. 

It does not cover all clients but revolve around the Put, Get, GetFileIDs and GetChecksums operations. 

## Building
The main code is in a buildable state, but will not do much. For a working example see the branch solution-proposal.
To do anything actual settings (RepositorySettings, ReferenceSettings and a matching certificate) will be needed. Those files are not included within this repository.

To build the code, use OpenJDK 1.8 and maven. 
Run: `mvn clean package`

The build will produce a tar-ball with the client java libraies and shell-sripts for running the commandline client. 

## Running
Extract the tar-ball that was produced by the build. To run the program run `bin/start-script.sh`, it will print help information detailing the further use. 

Before running the files `RepositorySettings.xml`, `ReferenceSettings.xml` and `client-certificate.pem` should be placed in the `conf/` directory.

## Tasks

1. Implement the PutFile action. Focus points:
  * Calculation of checksum
  * Upload of file to FileExchange, but *not* before the identification phase is done 
  * Handling of returned events (failures, partial results, retry)
  * Cleanup after operation

2. Implement the GetFile action. Focus points:
  * Specification of FileExchange
  * Choice of pillar, fastets or named?
  * Handling of returned events
  * Cleanup after operation

3. Implement the GetFileIDS action. Focus:
  * Pagination of data 

