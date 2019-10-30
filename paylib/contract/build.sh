#!/bin/bash

# solcjs contracts.sol --bin --abi --optimize --overwrite -o build
web3j solidity generate -b build/CustomToken.bin -a build/CustomToken.abi -o ../src/main/java -p com.w3engineers.eth.contracts
web3j solidity generate -b build/RaidenMicroTransferChannels.bin -a build/RaidenMicroTransferChannels.abi -o ../src/main/java -p com.w3engineers.eth.contracts

