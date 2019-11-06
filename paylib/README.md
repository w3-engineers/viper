# What this project about

This project is implementation of Microraiden Smart Contract Transactions.

# How to get env ready

Install solc

    sudo npm install solc@0.4.25
    
Install web3j command line

    wget https://github.com/web3j/web3j/releases/download/v4.2.0/web3j-4.2.0.zip
    unzip web3j-4.2.0.zip 
    ./web3j-4.2.0/bin/web3j
    sudo mv ./web3j-4.2.0/bin/web3j /usr/bin/web3j
    sudo mv ./web3j-4.2.0/lib/console-4.2.0-all.jar /usr/lib/console-4.2.0-all.jar
    web3j --version
    sudo rm web3j-4.2.0.zip
    
To generate java class ofthe smart contract execute

    bash contract.sh
    
For windows

    contract


# How this project is organized

  Ropsten tokenAddress: 0xff24d15afb9eb080c089053be99881dd18aa1090
  Ropsten contractAddress: "0x74434527b8e6c8296506d61d0faf3d18c9e4649a

# Reference

- https://stackoverflow.com/questions/53976057/source-file-requires-different-compiler-version-truffle