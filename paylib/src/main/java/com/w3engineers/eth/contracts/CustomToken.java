package com.w3engineers.eth.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.2.0.
 */
public class CustomToken extends Contract {
    private static final String BINARY = "60c0604052600460808190527f48302e310000000000000000000000000000000000000000000000000000000060a090815261003e91600691906100f8565b5034801561004b57600080fd5b50604051610bf1380380610bf18339810160409081528151602080840151928401516060850151938501805193959094910192909161008f916007918601906100f8565b506009805460ff191660ff8316908117909155600a90810a905581516100bc9060089060208501906100f8565b5050600b8054600160a060020a031916331790819055600160a060020a0316600090815260046020526040812084905592909255506101939050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061013957805160ff1916838001178555610166565b82800160010185558215610166579182015b8281111561016657825182559160200191906001019061014b565b50610172929150610176565b5090565b61019091905b80821115610172576000815560010161017c565b90565b610a4f806101a26000396000f3006080604052600436106100da5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde0381146100df578063095ea7b3146101695780631249c58b146101a157806318160ddd146101ab5780631b3ed722146101d257806323b872dd146101e7578063313ce567146102115780633c68eb811461023c57806354fd4d501461025157806370a082311461026657806380edef8e1461028757806395d89b41146102b8578063a9059cbb146102cd578063be45fd62146102f1578063dd62ed3e1461035a575b600080fd5b3480156100eb57600080fd5b506100f4610381565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561012e578181015183820152602001610116565b50505050905090810190601f16801561015b5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561017557600080fd5b5061018d600160a060020a036004351660243561040f565b604080519115158252519081900360200190f35b6101a96104c6565b005b3480156101b757600080fd5b506101c0610552565b60408051918252519081900360200190f35b3480156101de57600080fd5b506101c0610558565b3480156101f357600080fd5b5061018d600160a060020a036004358116906024351660443561055e565b34801561021d57600080fd5b506102266106a4565b6040805160ff9092168252519081900360200190f35b34801561024857600080fd5b506101a96106ad565b34801561025d57600080fd5b506100f4610718565b34801561027257600080fd5b506101c0600160a060020a0360043516610773565b34801561029357600080fd5b5061029c61078e565b60408051600160a060020a039092168252519081900360200190f35b3480156102c457600080fd5b506100f461079d565b3480156102d957600080fd5b5061018d600160a060020a03600435166024356107f8565b3480156102fd57600080fd5b50604080516020600460443581810135601f810184900484028501840190955284845261018d948235600160a060020a03169460248035953695946064949201919081908401838280828437509497506108d69650505050505050565b34801561036657600080fd5b506101c0600160a060020a03600435811690602435166109f8565b6007805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104075780601f106103dc57610100808354040283529160200191610407565b820191906000526020600020905b8154815290600101906020018083116103ea57829003601f168201915b505050505081565b6000600160a060020a038316151561042657600080fd5b8115806104545750336000908152600560209081526040808320600160a060020a0387168452909152902054155b151561045f57600080fd5b336000818152600560209081526040808320600160a060020a03881680855290835292819020869055805186815290519293927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925929181900390910190a350600192915050565b600067016345785d8a00003410156104dd57600080fd5b50600a5433600081815260046020526040808220805460329095029485019055815484018255518392917f30385c845b448a36257a6a1716e6ad2e1bc2cbe333cde1e69fe849ad6511adfe91a33360009081526004602052604090205481111561054357fe5b60005481111561054f57fe5b50565b60005481565b600a5481565b6000600160a060020a038416151561057557600080fd5b600160a060020a038316151561058a57600080fd5b600160a060020a0383163014156105a057600080fd5b600160a060020a0384166000908152600460205260409020548211156105c557600080fd5b600160a060020a03841660009081526005602090815260408083203384529091529020548211156105f557600080fd5b600160a060020a038316600090815260046020526040902054828101101561061c57600080fd5b600160a060020a03808416600081815260046020908152604080832080548801905593881680835284832080548890039055600582528483203384528252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060019392505050565b60095460ff1681565b600b54600160a060020a031633146106c457600080fd5b60003031116106d257600080fd5b600b54604051600160a060020a0390911690303180156108fc02916000818181858888f1935050505015801561070c573d6000803e3d6000fd5b5030311561071657fe5b565b6006805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104075780601f106103dc57610100808354040283529160200191610407565b600160a060020a031660009081526004602052604090205490565b600b54600160a060020a031681565b6008805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156104075780601f106103dc57610100808354040283529160200191610407565b6000600160a060020a038316151561080f57600080fd5b600160a060020a03831630141561082557600080fd5b3360009081526004602052604090205482111561084157600080fd5b600160a060020a038316600090815260046020526040902054828101101561086857600080fd5b33600081815260046020908152604080832080548790039055600160a060020a03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a350600192915050565b60008060006108e586866107f8565b15156108f057600080fd5b853b915060008211156109ec57506040517fc0ee0b8a0000000000000000000000000000000000000000000000000000000081523360048201818152602483018790526060604484019081528651606485015286518994600160a060020a0386169463c0ee0b8a9490938b938b9360840190602085019080838360005b8381101561098557818101518382015260200161096d565b50505050905090810190601f1680156109b25780820380516001836020036101000a031916815260200191505b50945050505050600060405180830381600087803b1580156109d357600080fd5b505af11580156109e7573d6000803e3d6000fd5b505050505b50600195945050505050565b600160a060020a039182166000908152600560209081526040808320939094168252919091522054905600a165627a7a723058207db1b1572dbeffcfc852adb237918a3baa16673a5600ae732e3b0d502d958bd30029";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_MULTIPLIER = "multiplier";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_TRANSFERFUNDS = "transferFunds";

    public static final String FUNC_VERSION = "version";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_OWNER_ADDRESS = "owner_address";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final Event MINTED_EVENT = new Event("Minted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event APPROVAL_EVENT = new Event("Approval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected CustomToken(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CustomToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CustomToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CustomToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> approve(String _spender, BigInteger _value) {
        final Function function = new Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_spender), 
                new org.web3j.abi.datatypes.generated.Uint256(_value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> mint(BigInteger weiValue) {
        final Function function = new Function(
                FUNC_MINT, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> multiplier() {
        final Function function = new Function(FUNC_MULTIPLIER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transferFrom(String _from, String _to, BigInteger _value) {
        final Function function = new Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_from), 
                new org.web3j.abi.datatypes.Address(_to), 
                new org.web3j.abi.datatypes.generated.Uint256(_value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transferFunds() {
        final Function function = new Function(
                FUNC_TRANSFERFUNDS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> version() {
        final Function function = new Function(FUNC_VERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> balanceOf(String _owner) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_owner)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> owner_address() {
        final Function function = new Function(FUNC_OWNER_ADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> transfer(String _to, BigInteger _value) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_to), 
                new org.web3j.abi.datatypes.generated.Uint256(_value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> transfer(String _to, BigInteger _value, byte[] _data) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_to), 
                new org.web3j.abi.datatypes.generated.Uint256(_value), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> allowance(String _owner, String _spender) {
        final Function function = new Function(FUNC_ALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_owner), 
                new org.web3j.abi.datatypes.Address(_spender)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public List<MintedEventResponse> getMintedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(MINTED_EVENT, transactionReceipt);
        ArrayList<MintedEventResponse> responses = new ArrayList<MintedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MintedEventResponse typedResponse = new MintedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._to = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._num = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<MintedEventResponse> mintedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, MintedEventResponse>() {
            @Override
            public MintedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(MINTED_EVENT, log);
                MintedEventResponse typedResponse = new MintedEventResponse();
                typedResponse.log = log;
                typedResponse._to = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._num = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<MintedEventResponse> mintedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MINTED_EVENT));
        return mintedEventFlowable(filter);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRANSFER_EVENT, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._to = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._spender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, ApprovalEventResponse>() {
            @Override
            public ApprovalEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(APPROVAL_EVENT, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse.log = log;
                typedResponse._owner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._spender = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    @Deprecated
    public static CustomToken load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CustomToken(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CustomToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CustomToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CustomToken load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CustomToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CustomToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CustomToken(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CustomToken> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, BigInteger initial_supply, String token_name, String token_symbol, BigInteger decimal_units) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initial_supply), 
                new org.web3j.abi.datatypes.Utf8String(token_name), 
                new org.web3j.abi.datatypes.Utf8String(token_symbol), 
                new org.web3j.abi.datatypes.generated.Uint8(decimal_units)));
        return deployRemoteCall(CustomToken.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<CustomToken> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, BigInteger initial_supply, String token_name, String token_symbol, BigInteger decimal_units) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initial_supply), 
                new org.web3j.abi.datatypes.Utf8String(token_name), 
                new org.web3j.abi.datatypes.Utf8String(token_symbol), 
                new org.web3j.abi.datatypes.generated.Uint8(decimal_units)));
        return deployRemoteCall(CustomToken.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<CustomToken> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initial_supply, String token_name, String token_symbol, BigInteger decimal_units) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initial_supply), 
                new org.web3j.abi.datatypes.Utf8String(token_name), 
                new org.web3j.abi.datatypes.Utf8String(token_symbol), 
                new org.web3j.abi.datatypes.generated.Uint8(decimal_units)));
        return deployRemoteCall(CustomToken.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<CustomToken> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initial_supply, String token_name, String token_symbol, BigInteger decimal_units) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(initial_supply), 
                new org.web3j.abi.datatypes.Utf8String(token_name), 
                new org.web3j.abi.datatypes.Utf8String(token_symbol), 
                new org.web3j.abi.datatypes.generated.Uint8(decimal_units)));
        return deployRemoteCall(CustomToken.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class MintedEventResponse {
        public Log log;

        public String _to;

        public BigInteger _num;
    }

    public static class TransferEventResponse {
        public Log log;

        public String _from;

        public String _to;

        public BigInteger _value;
    }

    public static class ApprovalEventResponse {
        public Log log;

        public String _owner;

        public String _spender;

        public BigInteger _value;
    }
}
