package com.w3engineers.eth.data.remote;

import android.content.Context;
import android.net.Network;
import android.util.Log;

import com.w3engineers.eth.contracts.RaidenMicroTransferChannels;
import com.w3engineers.eth.contracts.TmeshToken;

import org.reactivestreams.Subscription;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint192;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;

public class BlockRequest {
    private Context mContext;
    private ExecutorService callableExecutor;
    private ContractGasProvider contractGasProvider;
    private Web3j web3j;
    private Credentials credentials;
    private String TAG = "BlockRequest";
    private String tokenAddress = null;
    private String channelAddress = null;
    private String web3jRpcURL = "";
    private BlockTransactionObserver transactionObserver;
    private ConcurrentHashMap<String, Runnable> eventRequests;
    private long mGasPrice = 0;
    private long mGasLimit = 0;
    Subscription balanceApproveObserver, channelCreateObserver, channelCloseObserver, channelTopupObserver, channelWithdrawnObserver, tokenMintedObserver, tokenTransferredObserver;
    private HttpService mHttpService;


    public BlockRequest(String tokenAddress, String channelAddress, String rpcUrl, Context mContext, long gasPrice, long gasLimit, BlockTransactionObserver transactionObserver){

        this.tokenAddress = tokenAddress;
        this.channelAddress = channelAddress;
        this.web3jRpcURL = rpcUrl;
        this.mGasPrice = gasPrice;
        this.mGasLimit = gasLimit;
        this.mContext = mContext;
        this.transactionObserver = transactionObserver;
        this.eventRequests = new ConcurrentHashMap<>();

        callableExecutor = Executors.newFixedThreadPool(1);
    }

    public void setCredential(Credentials credential) {
        this.credentials = credential;
    }

    private void initWeb3j() {
        if (mHttpService != null) {

            web3j = Web3j.build(mHttpService);
            contractGasProvider = new ContractGasProvider() {
                @Override
                public BigInteger getGasPrice(String contractFunc) {
                    return BigInteger.valueOf(mGasPrice);
                }

                @Override
                public BigInteger getGasPrice() {
                    return BigInteger.valueOf(mGasPrice);
                }

                @Override
                public BigInteger getGasLimit(String contractFunc) {
                    return BigInteger.valueOf(mGasLimit);
                }

                @Override
                public BigInteger getGasLimit() {
                    return BigInteger.valueOf(mGasLimit);
                }
            };

            for (String key : eventRequests.keySet()) {
                eventRequests.get(key).run();
            }

        }
    }

    public TmeshToken loadTmeshToken() {
        return TmeshToken.load(tokenAddress, web3j, credentials, contractGasProvider);
    }

    public RaidenMicroTransferChannels loadChannelManager() {
        return RaidenMicroTransferChannels.load(channelAddress, web3j, credentials, contractGasProvider);
    }

    public Double getUserTokenAllowance(final String owner) throws ExecutionException, InterruptedException {
        if (mHttpService == null)
            return  null;

        Future<Double> future = callableExecutor.submit(new Callable() {
            @Override
            public Double call() {
                TmeshToken tmeshToken = loadTmeshToken();
                try {
                    BigInteger allowance = tmeshToken.allowance(owner, channelAddress).send();
                    if (allowance != null) {
                        double tokenValue = getETHorTOKEN(allowance);
                        return tokenValue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        });
        return future.get();
    }

    public Tuple2<Double, Double> getChannelInfo(final String sender, final String receiver, final long blockNumber) throws ExecutionException, InterruptedException {

        Future<Tuple2<Double, Double>> future = callableExecutor.submit(new Callable<Tuple2<Double, Double>>() {
            @Override
            public Tuple2<Double, Double> call() throws Exception {
                RaidenMicroTransferChannels channelManager = loadChannelManager();
                Tuple5<byte[], BigInteger, BigInteger, BigInteger, BigInteger> values = null;

                try {
                    values = channelManager.getChannelInfo(sender, receiver, BigInteger.valueOf(blockNumber)).send();
                    if (values != null) {
                        double deposit = getETHorTOKEN(values.getValue2());
                        double withdraw = getETHorTOKEN(values.getValue5());
                        Tuple2<Double, Double> output= new Tuple2<>(deposit, withdraw);
                        return output;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return future.get();
    }
    public String getBalanceProof(String receiver, long blockNumber, double balance) {
        List<String> labels = Arrays.asList(
                "string message_id",
                "address receiver",
                "uint32 block_created",
                "uint192 balance",
                "address contract"
        );

        int l = 0;
        for (String item : labels) {
            l += item.getBytes().length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(l);

        for (String a : labels) {
            buffer.put(a.getBytes());
        }
        byte[] array = buffer.array();

        List<byte[]> values = Arrays.asList(
                "Sender balance proof signature".getBytes(),
                Numeric.hexStringToByteArray(receiver),
                Numeric.toBytesPadded(BigInteger.valueOf(blockNumber), 4),
                Numeric.toBytesPadded(getWeiValue(balance), 24),
                Numeric.hexStringToByteArray(channelAddress)
        );

        //ByteBuffer bufferValues = ByteBuffer.allocate(values.stream().mapToInt(a -> a.length).sum());
        //Support Lower API Level
        int v = 0;
        for (byte[] item : values) {
            v += item.length;
        }
        ByteBuffer bufferValues = ByteBuffer.allocate(v);

        for (byte[] a : values) {
            bufferValues.put(a);
        }
        byte[] arrayValues = bufferValues.array();

        ByteBuffer byteArrayBuffer = ByteBuffer.allocate(64);
        byteArrayBuffer.put(Hash.sha3(array));
        byteArrayBuffer.put(Hash.sha3(arrayValues));
        byte[] arrayValuesBuffer = byteArrayBuffer.array();

        Sign.SignatureData signature = Sign.signMessage(arrayValuesBuffer, credentials.getEcKeyPair());

        //Balance Proof Signature
        ByteBuffer sigBuffer = ByteBuffer.allocate(signature.getR().length + signature.getS().length + 1);
        sigBuffer.put(signature.getR());
        sigBuffer.put(signature.getS());
        sigBuffer.put(signature.getV());
        String balanceProofSignature = Numeric.toHexString(sigBuffer.array());
        Log.i(TAG, "BalanceProofSig: " + balanceProofSignature);

        return balanceProofSignature;
    }

    public String verifyBalanceProofSignature(final String receiver, final long blockNumber, final double balance, final String balanceProof) throws ExecutionException, InterruptedException {

        Future<String> future = callableExecutor.submit(new Callable() {
            @Override
            public String call() {
                try {
                    RaidenMicroTransferChannels channelManager = loadChannelManager();
                    String sender = channelManager.extractBalanceProofSignature(receiver, BigInteger.valueOf(blockNumber), getWeiValue(balance), Numeric.hexStringToByteArray(balanceProof)).send();
                    if (sender != null) {
                        return sender;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        });
        return future.get();
    }

    public String getClosingHash(String sender, long blockNumber, double balance) {
        List<String> labels = Arrays.asList(
                "string message_id",
                "address sender",
                "uint32 block_created",
                "uint192 balance",
                "address contract"
        );

        // ByteBuffer buffer = ByteBuffer.allocate(labels.stream().mapToInt(a -> a.getBytes().length).sum());
        // Support Lower API Level
        int l = 0;
        for (String item : labels) {
            l += item.getBytes().length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(l);

        for (String a : labels) {
            buffer.put(a.getBytes());
        }
        byte[] array = buffer.array();

        List<byte[]> values = Arrays.asList(
                "Receiver closing signature".getBytes(),
                Numeric.hexStringToByteArray(sender),
                Numeric.toBytesPadded(BigInteger.valueOf(blockNumber), 4),
                Numeric.toBytesPadded(getWeiValue(balance), 24),
                Numeric.hexStringToByteArray(channelAddress)
        );

        //ByteBuffer bufferValues = ByteBuffer.allocate(values.stream().mapToInt(a -> a.length).sum());
        //Support Lower API Level
        int v = 0;
        for (byte[] item : values) {
            v += item.length;
        }
        ByteBuffer bufferValues = ByteBuffer.allocate(v);

        for (byte[] a : values) {
            bufferValues.put(a);
        }
        byte[] arrayValues = bufferValues.array();

        ByteBuffer byteArrayBuffer = ByteBuffer.allocate(64);
        byteArrayBuffer.put(Hash.sha3(array));
        byteArrayBuffer.put(Hash.sha3(arrayValues));
        byte[] arrayValuesBuffer = byteArrayBuffer.array();

        Sign.SignatureData signature = Sign.signMessage(arrayValuesBuffer, credentials.getEcKeyPair());

        //Closing Signature
        ByteBuffer sigBuffer = ByteBuffer.allocate(signature.getR().length + signature.getS().length + 1);
        sigBuffer.put(signature.getR());
        sigBuffer.put(signature.getS());
        sigBuffer.put(signature.getV());
        String closingHashSignature = Numeric.toHexString(sigBuffer.array());
        Log.i(TAG, "ClosingHashSig: " + closingHashSignature);

        return closingHashSignature;
    }


    public Double getUserTokenBalance(String address) throws ExecutionException, InterruptedException {
        if (mHttpService == null)
            return  null;

        Future<Double> future = callableExecutor.submit(new Callable() {
            @Override
            public Double call() {
                TmeshToken tmeshToken = loadTmeshToken();
                try {
                    BigInteger balance = tmeshToken.balanceOf(address).send();
                    if (balance != null) {
                        double tokenValue = getETHorTOKEN(balance);
                        return tokenValue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        });

        return future.get();
    }

    public Double getUserEthBalance(String address) throws ExecutionException, InterruptedException{
        if (mHttpService == null)
            return  null;

        Future<Double> future = callableExecutor.submit(new Callable() {
            @Override
            public Double call() {
                try {
                    EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                    if (ethGetBalance != null) {
                        double ethValue = getETHorTOKEN(ethGetBalance.getBalance());
                        return ethValue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        });

        return future.get();
    }

    public Integer getUserNonce(String address) throws ExecutionException, InterruptedException{
        if (mHttpService == null)
            return  null;

        Future<Integer> future = callableExecutor.submit(new Callable() {
            @Override
            public Integer call() {
                try {
                    EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                    if (nonce != null) {
                        return nonce.intValue();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return future.get();
    }

    public interface SubmitRequestListener {
        void onRequestSubmitted(String hash, int forRId);

        void onRequestSubmitError(String msg, int forRId);
    }

    public void submitRequest(String hexValue, int forRId, SubmitRequestListener listener) {
        Log.i(TAG, "submitRequest: " + hexValue);

        callableExecutor.submit(new Callable() {
            @Override
            public Integer call() {
                EthSendTransaction transactionResponse = null;
                try {
                    transactionResponse = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
                    if (transactionResponse != null) {
                        String hash = transactionResponse.getTransactionHash();
                        Log.i(TAG, "submitted hash : " + hash);
                        listener.onRequestSubmitted(hash, forRId);
                    } else {
                        listener.onRequestSubmitError("error occurred in library layer.", forRId);
                    }
                } catch (InterruptedException e) {
                    listener.onRequestSubmitError(e.getMessage(), forRId);
                } catch (ExecutionException e) {
                    listener.onRequestSubmitError(e.getMessage(), forRId);
                }
                return 0;
            }
        });

    }

    public BigInteger getWeiValue(double value) {
        BigDecimal weiTokenValue = Convert.toWei(BigDecimal.valueOf(value), Convert.Unit.ETHER);
        BigInteger b = weiTokenValue.toBigInteger();
        Log.i(TAG, "weiTokenValue: " + b.toString());
        return b;
    }

    public double getETHorTOKEN(BigInteger value) {
        BigDecimal tokenValue = Convert.fromWei(new BigDecimal(value), Convert.Unit.ETHER);
        Log.i(TAG, "tokenValue: " + tokenValue.doubleValue());
        return tokenValue.doubleValue();
    }
    //    public void verifyClosingHashSignature(final String sender, final int blockNumber, final int balance, final String closingSig, final VerifyClosingHash listener) {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    RaidenMicroTransferChannels channelManager = loadChannelManager();
//                    String receiver = channelManager.extractClosingSignature(sender, BigInteger.valueOf(blockNumber), BigInteger.valueOf(balance), Numeric.hexStringToByteArray(closingSig)).send();
//                    listener.onClosingHashVerified(receiver);
//                } catch (Exception e) {
//
//                }
//            }
//        });
//    }

    public interface BlockTransactionObserver {
        void onBalanceApprovedLog(TmeshToken.ApprovalEventResponse typedResponse);

        void onChannelCreatedLog(RaidenMicroTransferChannels.ChannelCreatedEventResponse typedResponse);

        void onChannelToppedUpLog(RaidenMicroTransferChannels.ChannelToppedUpEventResponse typedResponse);

        void onChannelClosedLog(RaidenMicroTransferChannels.ChannelSettledEventResponse typedResponse);

        void onChannelWithdrawnLog(RaidenMicroTransferChannels.ChannelWithdrawEventResponse typedResponse);

        void onTokenMintedLog(TmeshToken.MintedEventResponse typedResponse);

        void onTokenTransferredLog(TmeshToken.TransferEventResponse typedResponse);

    }

    public void logBalanceApproved(long blockNumber) {
        Log.i(TAG, "logBalanceApproved: " + blockNumber);
        if (web3j == null){
            eventRequests.put("logBalanceApproved", ()->logBalanceApproved(blockNumber));
            return;
        }

        if (balanceApproveObserver == null) {

            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        TmeshToken tmeshToken = loadTmeshToken();

                        balanceApproveObserver = (Subscription) tmeshToken.approvalEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "approvalEventFlowable: " + log.log.getTransactionHash());
                            if (transactionObserver != null) {
                                transactionObserver.onBalanceApprovedLog(log);
                            } else {
                                Log.i(TAG, "approvalEventFlowable: listener not found");
                            }
                        }, throwable -> {
                            Log.i(TAG, "throwable Error5: " + throwable.getMessage());
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }
    }

    public void logTokenMinted(long blockNumber) {
        Log.i(TAG, "logTokenMinted: " + blockNumber);

        if (web3j == null){
            eventRequests.put("logTokenMinted", ()->logTokenMinted(blockNumber));
            return;
        }

        if (tokenMintedObserver == null) {
            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        TmeshToken tmeshToken = loadTmeshToken();

                        tokenMintedObserver = (Subscription) tmeshToken.mintedEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "mintedEventFlowable: " + log.log.getTransactionHash());
                            if (transactionObserver != null) {
                                transactionObserver.onTokenMintedLog(log);
                            } else {
                                Log.i(TAG, "mintedEventFlowable: listener not found");
                            }
                        }, throwable -> {
                            Log.i(TAG, "throwable Error6: " + throwable.getMessage());
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }
    }

    public void logTokenTransferred(long blockNumber) {
        Log.i(TAG, "logTokenMinted: " + blockNumber);

        if (web3j == null){
            eventRequests.put("logTokenTransferred", ()->logTokenTransferred(blockNumber));
            return;
        }

        if (tokenTransferredObserver == null) {
            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        TmeshToken tmeshToken = loadTmeshToken();

                        tokenTransferredObserver = (Subscription) tmeshToken.transferEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "transferredEventFlowable: " + log.log.getTransactionHash());
                            if (transactionObserver != null) {
                                transactionObserver.onTokenTransferredLog(log);
                            } else {
                                Log.i(TAG, "transferEventFlowable: listener not found");
                            }
                        }, throwable -> {
                            Log.i(TAG, "throwable Error7: " + throwable.getMessage());
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }
    }

    public void logChannelCreated(long blockNumber) {
        Log.i(TAG, "logChannelCreated: " + blockNumber);

        if (web3j == null){
            eventRequests.put("logChannelCreated", ()->logChannelCreated(blockNumber));
            return;
        }


        if (channelCreateObserver == null) {
            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        RaidenMicroTransferChannels channelManager = loadChannelManager();
                        channelCreateObserver = (Subscription) channelManager.channelCreatedEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "channelCreatedEventFlowable: " + log.log.getTransactionHash() + " " + log.log.getBlockNumber().toString());

                            if (transactionObserver != null) {
                                transactionObserver.onChannelCreatedLog(log);
                            } else {
                                Log.i(TAG, "run: listener not found");
                            }
                        }, throwable -> {
                            Log.i(TAG, "throwable Error1: " + throwable.getMessage());
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }

    }

    public void logChannelToppedUp(long blockNumber) {
        Log.i(TAG, "logChannelToppedUp: " + blockNumber);

        if (web3j == null){
            eventRequests.put("logChannelToppedUp", ()->logChannelToppedUp(blockNumber));
            return;
        }

        if (channelTopupObserver == null) {
            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        RaidenMicroTransferChannels channelManager = loadChannelManager();
                        channelTopupObserver = (Subscription) channelManager.channelToppedUpEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "channelToppedUpEventFlowable: " + log.log.getTransactionHash());
                            transactionObserver.onChannelToppedUpLog(log);
                        }, throwable -> {
                            Log.i(TAG, "throwable Error2: " + throwable.getMessage());
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }
    }

    public void logChannelClosed(long blockNumber) {
        Log.i(TAG, "logChannelClosed: " + blockNumber);

        if (web3j == null){
            eventRequests.put("logChannelClosed", ()->logChannelClosed(blockNumber));
            return;
        }

        if (channelCloseObserver == null) {
            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        Log.i(TAG, "run: trying to close");
                        RaidenMicroTransferChannels channelManager = loadChannelManager();
                        channelCloseObserver = (Subscription) channelManager.channelSettledEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "channelCloseRequestedEventFlowable: " + log.log.getTransactionHash());
                            ;
                            if (transactionObserver != null) {
                                transactionObserver.onChannelClosedLog(log);
                            } else {
                                Log.i(TAG, "run: listener not found");
                            }
                        }, throwable -> {
                            Log.i(TAG, "throwable Error3: " + throwable.getMessage());
                        });

                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }

    }

    public void logChannelWithdrawn(long blockNumber) {
        Log.i(TAG, "logChannelWithdrawn: " + blockNumber);

        if (web3j == null){
            eventRequests.put("logChannelWithdrawn", ()->logChannelWithdrawn(blockNumber));
            return;
        }
        if (channelWithdrawnObserver == null) {
            callableExecutor.submit(new Callable() {
                @Override
                public Integer call() {
                    try {
                        RaidenMicroTransferChannels channelManager = loadChannelManager();
                        channelWithdrawnObserver = (Subscription) channelManager.channelWithdrawEventFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), DefaultBlockParameterName.LATEST).subscribe(log -> {
                            Log.i(TAG, "channelWithdrawEventFlowable: " + log.log.getTransactionHash());
                            transactionObserver.onChannelWithdrawnLog(log);
                        }, throwable -> {
                            Log.i(TAG, "throwable Error4: " + throwable.getMessage());
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception: " + e.getMessage());
                    }
                    return 0;
                }
            });
        }
    }

    public String approve(double value, int nonce) throws Exception {
        Log.i(TAG, "approve: " + value + " " + nonce);

        final Function approveFunc = new Function(
                TmeshToken.FUNC_APPROVE,
                Arrays.<Type>asList(new Address(channelAddress),
                        new Uint256(getWeiValue(value))),
                Collections.<TypeReference<?>>emptyList());

        return signMessage(approveFunc, tokenAddress, nonce, null);
    }

    public String createChannel(String sellerAddress, double deposit, int nonce) throws Exception {
        Log.i(TAG, "createChannel: " + sellerAddress + "  " + deposit + " " + nonce);

        Function createFunc = new Function(
                RaidenMicroTransferChannels.FUNC_CREATECHANNEL,
                Arrays.asList(new Address(sellerAddress), new Uint192(getWeiValue(deposit))),
                Collections.emptyList());

        return signMessage(createFunc, channelAddress, nonce, null);
    }

    public String topup(String receiver, long blockNumber, double added_deposit, int nonce) throws Exception {

        Log.i(TAG, "topup: " + receiver + "  " + added_deposit + " " + nonce + " " + blockNumber);

        final Function topupFunc = new Function(
                RaidenMicroTransferChannels.FUNC_TOPUP,
                Arrays.<Type>asList(new Address(receiver),
                        new Uint32(blockNumber),
                        new Uint192(getWeiValue(added_deposit))),
                Collections.<TypeReference<?>>emptyList());

        return signMessage(topupFunc, channelAddress, nonce, null);
    }

    public String close(String receiver, long block_number, double balance, String balance_signature, String closing_signature, int nonce) throws Exception {

        Log.i(TAG, "close: " + receiver + "  " + block_number + " " + nonce + " " + balance);

        final Function closeFunc = new Function(
                RaidenMicroTransferChannels.FUNC_COOPERATIVECLOSE,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(receiver),
                        new Uint32(block_number),
                        new Uint192(getWeiValue(balance)),
                        new DynamicBytes(Numeric.hexStringToByteArray(balance_signature)),
                        new DynamicBytes(Numeric.hexStringToByteArray(closing_signature))),
                Collections.<TypeReference<?>>emptyList());

        return signMessage(closeFunc, channelAddress, nonce, null);
    }

    public String withdraw(long block_number, double balance, String balance_signature, int nonce) throws Exception {

        Log.i(TAG, "withdraw: " + block_number + " " + nonce + " " + balance);

        final Function withdrawFunc = new Function(
                RaidenMicroTransferChannels.FUNC_WITHDRAW,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint32(block_number),
                        new org.web3j.abi.datatypes.generated.Uint192(getWeiValue(balance)),
                        new org.web3j.abi.datatypes.DynamicBytes(Numeric.hexStringToByteArray(balance_signature))),
                Collections.<TypeReference<?>>emptyList());

        return signMessage(withdrawFunc, channelAddress, nonce, null);
    }

    public String buyToken(double value, int nonce) throws Exception {

        Log.i(TAG, "buyToken: " + value + "  " + nonce);

        Function buyFunc = new Function(
                "mint",
                Arrays.<Type>asList(),
                Collections.<TypeReference<?>>emptyList());

        return signMessage(buyFunc, tokenAddress, nonce, getWeiValue(value));
    }

    private String signMessage(Function function, String contractAddress, int nonce, BigInteger value) throws Exception {

        String encodedFunction = FunctionEncoder.encode(function);
        Log.i(TAG, "encodedFunction: " + encodedFunction);
        RawTransaction rawTransaction = null;

        if (value == null) {
            rawTransaction = RawTransaction.createTransaction(
                    BigInteger.valueOf(nonce),
                    BigInteger.valueOf(mGasPrice),
                    BigInteger.valueOf(mGasLimit),
                    contractAddress,
                    encodedFunction);
        } else {
            rawTransaction = RawTransaction.createTransaction(
                    BigInteger.valueOf(nonce),
                    BigInteger.valueOf(mGasPrice),
                    BigInteger.valueOf(mGasLimit),
                    tokenAddress,
                    value,
                    encodedFunction);
        }
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        Log.i(TAG, "signedMessage: " + hexValue);
        return hexValue;
    }

    public void setNetworkInterface(Network network) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().socketFactory(network.getSocketFactory()).build();

        mHttpService = new HttpService(web3jRpcURL, okHttpClient);

        Log.v(TAG, "onAvailable: " + mHttpService.toString());

        initWeb3j();
    }

    public TransactionReceipt getTransactionReceiptByHash(String hash) throws Exception {

        Future<TransactionReceipt> future = callableExecutor.submit((Callable) () -> {

            try {

                EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(hash).sendAsync().get();
                return ethGetTransactionReceipt.getResult();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

        return future.get();
    }

}
