package com.w3engineers.eth.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint192;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple5;
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
public class RaidenMicroTransferChannels extends Contract {
    private static final String BINARY = "60806040523480156200001157600080fd5b50604051620020a6380380620020a68339810160409081528151602083015191830151909201600160a060020a03831615156200004d57600080fd5b620000618364010000000062000192810204565b15156200006d57600080fd5b6101f463ffffffff831610156200008357600080fd5b60018054600160a060020a031916600160a060020a038581169190911791829055604080517f18160ddd0000000000000000000000000000000000000000000000000000000081529051600093909216916318160ddd9160048082019260209290919082900301818787803b158015620000fc57600080fd5b505af115801562000111573d6000803e3d6000fd5b505050506040513d60208110156200012857600080fd5b5051116200013557600080fd5b600080543360a060020a63ffffffff02199091167401000000000000000000000000000000000000000063ffffffff86160217600160a060020a03191617905562000189816401000000006200019a810204565b505050620002a7565b6000903b1190565b60008054600160a060020a03163314620001b357600080fd5b5060005b8151811015620002a357620001f38282815181101515620001d457fe5b9060200190602002015162000192640100000000026401000000009004565b156200029a5760016004600084848151811015156200020e57fe5b602090810291909101810151600160a060020a03168252810191909152604001600020805460ff191691151591909117905581518290829081106200024f57fe5b602090810290910181015160408051600181529051600160a060020a03909216927fe2ad9d0600e2a93ef46991efd2c22f65f9ebe472487cc7551647bc52d793289992918290030190a25b600101620001b7565b5050565b611def80620002b76000396000f3006080604052600436106101245763ffffffff60e060020a600035041663016a8cf681146101295780630a00840c146101615780630eba6b061461018f5780631ac25e99146101fd5780631c6f609b146102975780631d8ceb44146102e75780631f52cc351461036557806322a3eab5146103ba578063323cb59b146103ee57806354fd4d50146104245780635a8e9d66146104ae5780636108b5ff146104d857806377c13323146104ff57806379694f081461053f5780637a7ebd7b1461057b578063803c83831461059357806380edef8e146105c35780638c76b4b7146105d8578063990030cc1461062d578063a6b7fa3614610662578063a6d1596314610699578063c0ee0b8a146106c6578063fc0c546a146106f7578063fcff5ed61461070c575b600080fd5b34801561013557600080fd5b5061015f600160a060020a036004351663ffffffff60243516600160c060020a036044351661073f565b005b34801561016d57600080fd5b50610176610802565b6040805163ffffffff9092168252519081900360200190f35b34801561019b57600080fd5b506101bf600160a060020a036004358116906024351663ffffffff60443516610826565b60408051958652600160c060020a03948516602087015263ffffffff9093168584015290831660608501529091166080830152519081900360a00190f35b34801561020957600080fd5b50604080516020601f60643560048181013592830184900484028501840190955281845261027b94600160a060020a038135169463ffffffff602480359190911695600160c060020a036044351695369560849493019181908401838280828437509497506108b49650505050505050565b60408051600160a060020a039092168252519081900360200190f35b3480156102a357600080fd5b5061015f60048035600160a060020a0316906024803563ffffffff1691604435600160c060020a031691606435808201929081013591608435908101910135610a2a565b3480156102f357600080fd5b50604080516020601f60643560048181013592830184900484028501840190955281845261027b94600160a060020a038135169463ffffffff602480359190911695600160c060020a03604435169536956084949301918190840183828082843750949750610ad89650505050505050565b34801561037157600080fd5b506040805160206004803580820135838102808601850190965280855261015f95369593946024949385019291829185019084908082843750949750610c439650505050505050565b3480156103c657600080fd5b506103d2600435610d50565b60408051600160c060020a039092168252519081900360200190f35b3480156103fa57600080fd5b5061015f600160a060020a036004351663ffffffff60243516600160c060020a0360443516610d6b565b34801561043057600080fd5b50610439610ef3565b6040805160208082528351818301528351919283929083019185019080838360005b8381101561047357818101518382015260200161045b565b50505050905090810190601f1680156104a05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b3480156104ba57600080fd5b5061015f600160a060020a036004351663ffffffff60243516610f2a565b3480156104e457600080fd5b506104ed610faf565b60408051918252519081900360200190f35b34801561050b57600080fd5b50610517600435610fbc565b60408051600160c060020a03909316835263ffffffff90911660208301528051918290030190f35b34801561054b57600080fd5b5061015f600160a060020a036004358116906024351663ffffffff60443516600160c060020a0360643516610fe6565b34801561058757600080fd5b506105176004356110c8565b34801561059f57600080fd5b506104ed600160a060020a036004358116906024351663ffffffff604435166110f2565b3480156105cf57600080fd5b5061027b61113f565b3480156105e457600080fd5b506040805160206004803580820135838102808601850190965280855261015f9536959394602494938501929182918501908490808284375094975061114e9650505050505050565b34801561063957600080fd5b5061064e600160a060020a036004351661123f565b604080519115158252519081900360200190f35b34801561066e57600080fd5b5061015f6004803563ffffffff169060248035600160c060020a031691604435918201910135611254565b3480156106a557600080fd5b5061015f600160a060020a0360043516600160c060020a0360243516611494565b3480156106d257600080fd5b5061015f60048035600160a060020a0316906024803591604435918201910135611551565b34801561070357600080fd5b5061027b6116ca565b34801561071857600080fd5b5061015f600160a060020a0360043581169060243516600160c060020a03604435166116d9565b61074b33848484611702565b600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152336004820152306024820152600160c060020a03841660448201529051600160a060020a03909216916323b872dd916064808201926020929091908290030181600087803b1580156107c657600080fd5b505af11580156107da573d6000803e3d6000fd5b505050506040513d60208110156107f057600080fd5b505115156107fd57600080fd5b505050565b60005474010000000000000000000000000000000000000000900463ffffffff1681565b60008060008060008061083a8989896110f2565b60008181526002602052604081205491925060c060020a90910463ffffffff161161086457600080fd5b60008181526002602090815260408083205460038352818420546005909352922054929b600160c060020a039283169b5060c060020a820463ffffffff169a509082169850911695509350505050565b604080517f737472696e67206d6573736167655f696400000000000000000000000000000081527f616464726573732072656365697665720000000000000000000000000000000060118201527f75696e74333220626c6f636b5f6372656174656400000000000000000000000060218201527f75696e743139322062616c616e6365000000000000000000000000000000000060358201527f6164647265737320636f6e747261637400000000000000000000000000000000604482015281519081900360540181207f53656e6465722062616c616e63652070726f6f66207369676e6174757265000082526c01000000000000000000000000600160a060020a0388168102601e84015260e060020a63ffffffff881602603284015268010000000000000000600160c060020a0387160260368401523002604e83015282519182900360620182209082526020820152815190819003909101902060009081610a1f8285611873565b979650505050505050565b600080610a6989898989898080601f016020809104026020016040519081016040528093929190818152602001838380828437506108b4945050505050565b9150610aa782898987878080601f01602080910402602001604051908101604052809392919081815260200183838082843750610ad8945050505050565b9050600160a060020a03808216908a1614610ac157600080fd5b610acd82828a8a611953565b505050505050505050565b604080517f737472696e67206d6573736167655f696400000000000000000000000000000081527f616464726573732073656e64657200000000000000000000000000000000000060118201527f75696e74333220626c6f636b5f63726561746564000000000000000000000000601f8201527f75696e743139322062616c616e6365000000000000000000000000000000000060338201527f6164647265737320636f6e747261637400000000000000000000000000000000604282015281519081900360520181207f526563656976657220636c6f73696e67207369676e617475726500000000000082526c01000000000000000000000000600160a060020a0388168102601a84015260e060020a63ffffffff881602602e84015268010000000000000000600160c060020a0387160260328401523002604a830152825191829003605e0182209082526020820152815190819003909101902060009081610a1f8285611873565b60008054600160a060020a03163314610c5b57600080fd5b5060005b8151811015610d4c57600460008383815181101515610c7a57fe5b6020908102909101810151600160a060020a031682528101919091526040016000205460ff1615610d44576000600460008484815181101515610cb957fe5b602090810291909101810151600160a060020a03168252810191909152604001600020805460ff19169115159190911790558151829082908110610cf957fe5b602090810290910181015160408051600081529051600160a060020a03909216927fe2ad9d0600e2a93ef46991efd2c22f65f9ebe472487cc7551647bc52d793289992918290030190a25b600101610c5f565b5050565b600560205260009081526040902054600160c060020a031681565b6000610d783385856110f2565b60008181526002602052604081205491925060c060020a90910463ffffffff1611610da257600080fd5b60008181526003602052604090205460c060020a900463ffffffff1615610dc857600080fd5b600081815260026020526040902054600160c060020a039081169083161115610df057600080fd5b600080548282526003602052604090912080544363ffffffff7401000000000000000000000000000000000000000090940484168101841660c060020a9081027bffffffff000000000000000000000000000000000000000000000000199093169290921792839055910490911611610e6857600080fd5b600081815260036020908152604091829020805477ffffffffffffffffffffffffffffffffffffffffffffffff1916600160c060020a0386169081179091558251908152915163ffffffff861692600160a060020a0388169233927f960e55a871be40c817ffd64e2117c513e42f047ccfdcbc5454e68dfc65e9a9b09281900390910190a450505050565b60408051808201909152600581527f302e322e30000000000000000000000000000000000000000000000000000000602082015281565b6000610f373384846110f2565b60008181526003602052604081205491925060c060020a90910463ffffffff1611610f6157600080fd5b60008181526003602052604090205460c060020a900463ffffffff164311610f8857600080fd5b6000818152600360205260409020546107fd90339085908590600160c060020a0316611953565b68056bc75e2d6310000081565b600360205260009081526040902054600160c060020a0381169060c060020a900463ffffffff1682565b3360009081526004602052604090205460ff16151561100457600080fd5b61101084848484611702565b600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152336004820152306024820152600160c060020a03841660448201529051600160a060020a03909216916323b872dd916064808201926020929091908290030181600087803b15801561108b57600080fd5b505af115801561109f573d6000803e3d6000fd5b505050506040513d60208110156110b557600080fd5b505115156110c257600080fd5b50505050565b600260205260009081526040902054600160c060020a0381169060c060020a900463ffffffff1682565b604080516c01000000000000000000000000600160a060020a0380871682028352851602601482015260e060020a63ffffffff8416026028820152905190819003602c0190209392505050565b600054600160a060020a031681565b60008054600160a060020a0316331461116657600080fd5b5060005b8151811015610d4c57611193828281518110151561118457fe5b90602001906020020151611c10565b156112375760016004600084848151811015156111ac57fe5b602090810291909101810151600160a060020a03168252810191909152604001600020805460ff191691151591909117905581518290829081106111ec57fe5b602090810290910181015160408051600181529051600160a060020a03909216927fe2ad9d0600e2a93ef46991efd2c22f65f9ebe472487cc7551647bc52d793289992918290030190a25b60010161116a565b60046020526000908152604090205460ff1681565b60008080600160c060020a038616811061126d57600080fd5b6112a933888888888080601f016020809104026020016040519081016040528093929190818152602001838380828437506108b4945050505050565b92506112b68333896110f2565b60008181526002602052604081205491935060c060020a90910463ffffffff16116112e057600080fd5b60008281526003602052604090205460c060020a900463ffffffff161561130657600080fd5b600082815260026020526040902054600160c060020a03908116908716111561132e57600080fd5b600082815260056020526040902054600160c060020a0380881691161061135457600080fd5b5060008181526005602090815260408083208054600160c060020a038a811677ffffffffffffffffffffffffffffffffffffffffffffffff1983161790925560015483517fa9059cbb0000000000000000000000000000000000000000000000000000000081523360048201529183168b03928316602483015292519194600160a060020a039093169363a9059cbb9360448084019492938390030190829087803b15801561140257600080fd5b505af1158015611416573d6000803e3d6000fd5b505050506040513d602081101561142c57600080fd5b5051151561143957600080fd5b60408051600160c060020a0383168152905163ffffffff8916913391600160a060020a038716917fe588a2bb9921c62e0da981f10952a3bb82216bf11a1f731da610fd2542eef27d919081900360200190a450505050505050565b61149f338383611c18565b600154604080517f23b872dd000000000000000000000000000000000000000000000000000000008152336004820152306024820152600160c060020a03841660448201529051600160a060020a03909216916323b872dd916064808201926020929091908290030181600087803b15801561151a57600080fd5b505af115801561152e573d6000803e3d6000fd5b505050506040513d602081101561154457600080fd5b50511515610d4c57600080fd5b6001546000908190819081908190600160a060020a0316331461157357600080fd5b879450600160c060020a038516851461158b57600080fd5b859350602884148061159d575083602c145b15156115a857600080fd5b6115e487878080601f0160208091040260200160405190810160405280939291908181526020018383808284375060209450611d8b9350505050565b925082600160a060020a031689600160a060020a0316148061161e5750600160a060020a03891660009081526004602052604090205460ff165b151561162957600080fd5b61166587878080601f0160208091040260200160405190810160405280939291908181526020018383808284375060349450611d8b9350505050565b915083602814156116805761167b838387611c18565b610acd565b6116bc87878080601f0160208091040260200160405190810160405280939291908181526020018383808284375060489450611da09350505050565b9050610acd83838388611702565b600154600160a060020a031681565b3360009081526004602052604090205460ff1615156116f757600080fd5b61074b838383611c18565b6000600160c060020a038216811061171957600080fd5b600063ffffffff84161161172c57600080fd5b6117378585856110f2565b60008181526002602052604081205491925060c060020a90910463ffffffff161161176157600080fd5b60008181526003602052604090205460c060020a900463ffffffff161561178757600080fd5b60008181526002602052604090205468056bc75e2d63100000600160c060020a03918216840190911611156117bb57600080fd5b6000818152600260205260409020805477ffffffffffffffffffffffffffffffffffffffffffffffff198116600160c060020a039182168501821617918290558381169116101561180857fe5b8263ffffffff1684600160a060020a031686600160a060020a03167f283bcbed58779cdfe40c216a69673863430a43dbf7fe557730c0498890e55126856040518082600160c060020a0316600160c060020a0316815260200191505060405180910390a45050505050565b6000806000808451604114151561188957600080fd5b50505060208201516040830151606084015160001a601b60ff821610156118ae57601b015b8060ff16601b14806118c357508060ff16601c145b15156118ce57600080fd5b60408051600080825260208083018085528a905260ff8516838501526060830187905260808301869052925160019360a0808501949193601f19840193928390039091019190865af1158015611928573d6000803e3d6000fd5b5050604051601f190151945050600160a060020a038416151561194a57600080fd5b50505092915050565b600061195d611dac565b600061196a8787876110f2565b6000818152600260209081526040808320815180830190925254600160c060020a038116825260c060020a900463ffffffff16918101829052929550919350106119b357600080fd5b8151600160c060020a0390811690851611156119ce57600080fd5b600083815260056020526040902054600160c060020a03808616911611156119f557600080fd5b50600082815260026020908152604080832080547fffffffff000000000000000000000000000000000000000000000000000000009081169091556003835281842080549091169055600582528083205460015482517fa9059cbb000000000000000000000000000000000000000000000000000000008152600160a060020a038b81166004830152600160c060020a039384168a03938416602483015293519295939091169363a9059cbb9360448084019492938390030190829087803b158015611ac057600080fd5b505af1158015611ad4573d6000803e3d6000fd5b505050506040513d6020811015611aea57600080fd5b50511515611af757600080fd5b6001548251604080517fa9059cbb000000000000000000000000000000000000000000000000000000008152600160a060020a038b8116600483015292889003600160c060020a031660248201529051919092169163a9059cbb9160448083019260209291908290030181600087803b158015611b7357600080fd5b505af1158015611b87573d6000803e3d6000fd5b505050506040513d6020811015611b9d57600080fd5b50511515611baa57600080fd5b60408051600160c060020a03808716825283166020820152815163ffffffff881692600160a060020a03808b1693908c16927f5f32714de7650ec742b858687d8db145623b99b0748db73df3ffc4d718867a8d929181900390910190a450505050505050565b6000903b1190565b60008068056bc75e2d63100000600160c060020a0384161115611c3a57600080fd5b439150611c488585846110f2565b600081815260026020526040902054909150600160c060020a031615611c6d57600080fd5b60008181526002602052604090205460c060020a900463ffffffff1615611c9357600080fd5b60008181526003602052604090205460c060020a900463ffffffff1615611cb957600080fd5b604080518082018252600160c060020a0385811680835263ffffffff868116602080860191825260008881526002825287902095518654925177ffffffffffffffffffffffffffffffffffffffffffffffff199093169516949094177bffffffff000000000000000000000000000000000000000000000000191660c060020a91909216021790925582519182529151600160a060020a0387811693908916927f986876e67d288f7b8bc5229976a1d5710be919feb66d2e1aec1bf3eadebba207929081900390910190a35050505050565b01516c01000000000000000000000000900490565b015160e060020a900490565b6040805180820190915260008082526020820152905600a165627a7a72305820fa29137291383dadc564a828c472e57358c534c3565886a82fbaacc044ad35500029";

    public static final String FUNC_TOPUP = "topUp";

    public static final String FUNC_CHALLENGE_PERIOD = "challenge_period";

    public static final String FUNC_GETCHANNELINFO = "getChannelInfo";

    public static final String FUNC_EXTRACTBALANCEPROOFSIGNATURE = "extractBalanceProofSignature";

    public static final String FUNC_COOPERATIVECLOSE = "cooperativeClose";

    public static final String FUNC_EXTRACTCLOSINGSIGNATURE = "extractClosingSignature";

    public static final String FUNC_REMOVETRUSTEDCONTRACTS = "removeTrustedContracts";

    public static final String FUNC_WITHDRAWN_BALANCES = "withdrawn_balances";

    public static final String FUNC_UNCOOPERATIVECLOSE = "uncooperativeClose";

    public static final String FUNC_VERSION = "version";

    public static final String FUNC_SETTLE = "settle";

    public static final String FUNC_CHANNEL_DEPOSIT_BUGBOUNTY_LIMIT = "channel_deposit_bugbounty_limit";

    public static final String FUNC_CLOSING_REQUESTS = "closing_requests";

    public static final String FUNC_TOPUPDELEGATE = "topUpDelegate";

    public static final String FUNC_CHANNELS = "channels";

    public static final String FUNC_GETKEY = "getKey";

    public static final String FUNC_OWNER_ADDRESS = "owner_address";

    public static final String FUNC_ADDTRUSTEDCONTRACTS = "addTrustedContracts";

    public static final String FUNC_TRUSTED_CONTRACTS = "trusted_contracts";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final String FUNC_CREATECHANNEL = "createChannel";

    public static final String FUNC_TOKENFALLBACK = "tokenFallback";

    public static final String FUNC_TOKEN = "token";

    public static final String FUNC_CREATECHANNELDELEGATE = "createChannelDelegate";

    public static final Event CHANNELCREATED_EVENT = new Event("ChannelCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint192>() {}));
    ;

    public static final Event CHANNELTOPPEDUP_EVENT = new Event("ChannelToppedUp", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint32>(true) {}, new TypeReference<Uint192>() {}));
    ;

    public static final Event CHANNELCLOSEREQUESTED_EVENT = new Event("ChannelCloseRequested", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint32>(true) {}, new TypeReference<Uint192>() {}));
    ;

    public static final Event CHANNELSETTLED_EVENT = new Event("ChannelSettled", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint32>(true) {}, new TypeReference<Uint192>() {}, new TypeReference<Uint192>() {}));
    ;

    public static final Event CHANNELWITHDRAW_EVENT = new Event("ChannelWithdraw", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint32>(true) {}, new TypeReference<Uint192>() {}));
    ;

    public static final Event TRUSTEDCONTRACT_EVENT = new Event("TrustedContract", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}));
    ;

    @Deprecated
    protected RaidenMicroTransferChannels(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected RaidenMicroTransferChannels(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected RaidenMicroTransferChannels(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected RaidenMicroTransferChannels(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> topUp(String _receiver_address, BigInteger _open_block_number, BigInteger _added_deposit) {
        final Function function = new Function(
                FUNC_TOPUP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_added_deposit)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> challenge_period() {
        final Function function = new Function(FUNC_CHALLENGE_PERIOD, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint32>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Tuple5<byte[], BigInteger, BigInteger, BigInteger, BigInteger>> getChannelInfo(String _sender_address, String _receiver_address, BigInteger _open_block_number) {
        final Function function = new Function(FUNC_GETCHANNELINFO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_sender_address), 
                new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint192>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint192>() {}, new TypeReference<Uint192>() {}));
        return new RemoteCall<Tuple5<byte[], BigInteger, BigInteger, BigInteger, BigInteger>>(
                new Callable<Tuple5<byte[], BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple5<byte[], BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<byte[], BigInteger, BigInteger, BigInteger, BigInteger>(
                                (byte[]) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public RemoteCall<String> extractBalanceProofSignature(String _receiver_address, BigInteger _open_block_number, BigInteger _balance, byte[] _balance_msg_sig) {
        final Function function = new Function(FUNC_EXTRACTBALANCEPROOFSIGNATURE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_balance), 
                new org.web3j.abi.datatypes.DynamicBytes(_balance_msg_sig)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> cooperativeClose(String _receiver_address, BigInteger _open_block_number, BigInteger _balance, byte[] _balance_msg_sig, byte[] _closing_sig) {
        final Function function = new Function(
                FUNC_COOPERATIVECLOSE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_balance), 
                new org.web3j.abi.datatypes.DynamicBytes(_balance_msg_sig), 
                new org.web3j.abi.datatypes.DynamicBytes(_closing_sig)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> extractClosingSignature(String _sender_address, BigInteger _open_block_number, BigInteger _balance, byte[] _closing_sig) {
        final Function function = new Function(FUNC_EXTRACTCLOSINGSIGNATURE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_sender_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_balance), 
                new org.web3j.abi.datatypes.DynamicBytes(_closing_sig)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> removeTrustedContracts(List<String> _trusted_contracts) {
        final Function function = new Function(
                FUNC_REMOVETRUSTEDCONTRACTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.datatypes.Address.class,
                        org.web3j.abi.Utils.typeMap(_trusted_contracts, org.web3j.abi.datatypes.Address.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> withdrawn_balances(byte[] param0) {
        final Function function = new Function(FUNC_WITHDRAWN_BALANCES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint192>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> uncooperativeClose(String _receiver_address, BigInteger _open_block_number, BigInteger _balance) {
        final Function function = new Function(
                FUNC_UNCOOPERATIVECLOSE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_balance)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> version() {
        final Function function = new Function(FUNC_VERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> settle(String _receiver_address, BigInteger _open_block_number) {
        final Function function = new Function(
                FUNC_SETTLE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> channel_deposit_bugbounty_limit() {
        final Function function = new Function(FUNC_CHANNEL_DEPOSIT_BUGBOUNTY_LIMIT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Tuple2<BigInteger, BigInteger>> closing_requests(byte[] param0) {
        final Function function = new Function(FUNC_CLOSING_REQUESTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint192>() {}, new TypeReference<Uint32>() {}));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> topUpDelegate(String _sender_address, String _receiver_address, BigInteger _open_block_number, BigInteger _added_deposit) {
        final Function function = new Function(
                FUNC_TOPUPDELEGATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_sender_address), 
                new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_added_deposit)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple2<BigInteger, BigInteger>> channels(byte[] param0) {
        final Function function = new Function(FUNC_CHANNELS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint192>() {}, new TypeReference<Uint32>() {}));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<byte[]> getKey(String _sender_address, String _receiver_address, BigInteger _open_block_number) {
        final Function function = new Function(FUNC_GETKEY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_sender_address), 
                new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_open_block_number)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<String> owner_address() {
        final Function function = new Function(FUNC_OWNER_ADDRESS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> addTrustedContracts(List<String> _trusted_contracts) {
        final Function function = new Function(
                FUNC_ADDTRUSTEDCONTRACTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.datatypes.Address.class,
                        org.web3j.abi.Utils.typeMap(_trusted_contracts, org.web3j.abi.datatypes.Address.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> trusted_contracts(String param0) {
        final Function function = new Function(FUNC_TRUSTED_CONTRACTS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> withdraw(BigInteger _open_block_number, BigInteger _balance, byte[] _balance_msg_sig) {
        final Function function = new Function(
                FUNC_WITHDRAW, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint32(_open_block_number), 
                new org.web3j.abi.datatypes.generated.Uint192(_balance), 
                new org.web3j.abi.datatypes.DynamicBytes(_balance_msg_sig)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> createChannel(String _receiver_address, BigInteger _deposit) {
        final Function function = new Function(
                FUNC_CREATECHANNEL, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint192(_deposit)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> tokenFallback(String _sender_address, BigInteger _deposit, byte[] _data) {
        final Function function = new Function(
                FUNC_TOKENFALLBACK, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_sender_address), 
                new org.web3j.abi.datatypes.generated.Uint256(_deposit), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> token() {
        final Function function = new Function(FUNC_TOKEN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> createChannelDelegate(String _sender_address, String _receiver_address, BigInteger _deposit) {
        final Function function = new Function(
                FUNC_CREATECHANNELDELEGATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_sender_address), 
                new org.web3j.abi.datatypes.Address(_receiver_address), 
                new org.web3j.abi.datatypes.generated.Uint192(_deposit)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public List<ChannelCreatedEventResponse> getChannelCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CHANNELCREATED_EVENT, transactionReceipt);
        ArrayList<ChannelCreatedEventResponse> responses = new ArrayList<ChannelCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ChannelCreatedEventResponse typedResponse = new ChannelCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._deposit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ChannelCreatedEventResponse> channelCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, ChannelCreatedEventResponse>() {
            @Override
            public ChannelCreatedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CHANNELCREATED_EVENT, log);
                ChannelCreatedEventResponse typedResponse = new ChannelCreatedEventResponse();
                typedResponse.log = log;
                typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._deposit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ChannelCreatedEventResponse> channelCreatedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANNELCREATED_EVENT));
        return channelCreatedEventFlowable(filter);
    }

    public List<ChannelToppedUpEventResponse> getChannelToppedUpEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CHANNELTOPPEDUP_EVENT, transactionReceipt);
        ArrayList<ChannelToppedUpEventResponse> responses = new ArrayList<ChannelToppedUpEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ChannelToppedUpEventResponse typedResponse = new ChannelToppedUpEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse._added_deposit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ChannelToppedUpEventResponse> channelToppedUpEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, ChannelToppedUpEventResponse>() {
            @Override
            public ChannelToppedUpEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CHANNELTOPPEDUP_EVENT, log);
                ChannelToppedUpEventResponse typedResponse = new ChannelToppedUpEventResponse();
                typedResponse.log = log;
                typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                typedResponse._added_deposit = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ChannelToppedUpEventResponse> channelToppedUpEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANNELTOPPEDUP_EVENT));
        return channelToppedUpEventFlowable(filter);
    }

    public List<ChannelCloseRequestedEventResponse> getChannelCloseRequestedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CHANNELCLOSEREQUESTED_EVENT, transactionReceipt);
        ArrayList<ChannelCloseRequestedEventResponse> responses = new ArrayList<ChannelCloseRequestedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ChannelCloseRequestedEventResponse typedResponse = new ChannelCloseRequestedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse._balance = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ChannelCloseRequestedEventResponse> channelCloseRequestedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, ChannelCloseRequestedEventResponse>() {
            @Override
            public ChannelCloseRequestedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CHANNELCLOSEREQUESTED_EVENT, log);
                ChannelCloseRequestedEventResponse typedResponse = new ChannelCloseRequestedEventResponse();
                typedResponse.log = log;
                typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                typedResponse._balance = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ChannelCloseRequestedEventResponse> channelCloseRequestedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANNELCLOSEREQUESTED_EVENT));
        return channelCloseRequestedEventFlowable(filter);
    }

    public List<ChannelSettledEventResponse> getChannelSettledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CHANNELSETTLED_EVENT, transactionReceipt);
        ArrayList<ChannelSettledEventResponse> responses = new ArrayList<ChannelSettledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ChannelSettledEventResponse typedResponse = new ChannelSettledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse._balance = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse._receiver_tokens = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ChannelSettledEventResponse> channelSettledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, ChannelSettledEventResponse>() {
            @Override
            public ChannelSettledEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CHANNELSETTLED_EVENT, log);
                ChannelSettledEventResponse typedResponse = new ChannelSettledEventResponse();
                typedResponse.log = log;
                typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                typedResponse._balance = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse._receiver_tokens = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ChannelSettledEventResponse> channelSettledEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANNELSETTLED_EVENT));
        return channelSettledEventFlowable(filter);
    }

    public List<ChannelWithdrawEventResponse> getChannelWithdrawEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(CHANNELWITHDRAW_EVENT, transactionReceipt);
        ArrayList<ChannelWithdrawEventResponse> responses = new ArrayList<ChannelWithdrawEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ChannelWithdrawEventResponse typedResponse = new ChannelWithdrawEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse._withdrawn_balance = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ChannelWithdrawEventResponse> channelWithdrawEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, ChannelWithdrawEventResponse>() {
            @Override
            public ChannelWithdrawEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CHANNELWITHDRAW_EVENT, log);
                ChannelWithdrawEventResponse typedResponse = new ChannelWithdrawEventResponse();
                typedResponse.log = log;
                typedResponse._sender_address = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._receiver_address = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse._open_block_number = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
                typedResponse._withdrawn_balance = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ChannelWithdrawEventResponse> channelWithdrawEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CHANNELWITHDRAW_EVENT));
        return channelWithdrawEventFlowable(filter);
    }

    public List<TrustedContractEventResponse> getTrustedContractEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRUSTEDCONTRACT_EVENT, transactionReceipt);
        ArrayList<TrustedContractEventResponse> responses = new ArrayList<TrustedContractEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TrustedContractEventResponse typedResponse = new TrustedContractEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._trusted_contract_address = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._trusted_status = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TrustedContractEventResponse> trustedContractEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, TrustedContractEventResponse>() {
            @Override
            public TrustedContractEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRUSTEDCONTRACT_EVENT, log);
                TrustedContractEventResponse typedResponse = new TrustedContractEventResponse();
                typedResponse.log = log;
                typedResponse._trusted_contract_address = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._trusted_status = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TrustedContractEventResponse> trustedContractEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRUSTEDCONTRACT_EVENT));
        return trustedContractEventFlowable(filter);
    }

    @Deprecated
    public static RaidenMicroTransferChannels load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new RaidenMicroTransferChannels(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static RaidenMicroTransferChannels load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new RaidenMicroTransferChannels(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RaidenMicroTransferChannels load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new RaidenMicroTransferChannels(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static RaidenMicroTransferChannels load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new RaidenMicroTransferChannels(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<RaidenMicroTransferChannels> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _token_address, BigInteger _challenge_period, List<String> _trusted_contracts) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_challenge_period), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.datatypes.Address.class,
                        org.web3j.abi.Utils.typeMap(_trusted_contracts, org.web3j.abi.datatypes.Address.class))));
        return deployRemoteCall(RaidenMicroTransferChannels.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<RaidenMicroTransferChannels> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _token_address, BigInteger _challenge_period, List<String> _trusted_contracts) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_challenge_period), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.datatypes.Address.class,
                        org.web3j.abi.Utils.typeMap(_trusted_contracts, org.web3j.abi.datatypes.Address.class))));
        return deployRemoteCall(RaidenMicroTransferChannels.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<RaidenMicroTransferChannels> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _token_address, BigInteger _challenge_period, List<String> _trusted_contracts) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_challenge_period), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.datatypes.Address.class,
                        org.web3j.abi.Utils.typeMap(_trusted_contracts, org.web3j.abi.datatypes.Address.class))));
        return deployRemoteCall(RaidenMicroTransferChannels.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<RaidenMicroTransferChannels> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _token_address, BigInteger _challenge_period, List<String> _trusted_contracts) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token_address), 
                new org.web3j.abi.datatypes.generated.Uint32(_challenge_period), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.Address>(
                        org.web3j.abi.datatypes.Address.class,
                        org.web3j.abi.Utils.typeMap(_trusted_contracts, org.web3j.abi.datatypes.Address.class))));
        return deployRemoteCall(RaidenMicroTransferChannels.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class ChannelCreatedEventResponse {
        public Log log;

        public String _sender_address;

        public String _receiver_address;

        public BigInteger _deposit;
    }

    public static class ChannelToppedUpEventResponse {
        public Log log;

        public String _sender_address;

        public String _receiver_address;

        public BigInteger _open_block_number;

        public BigInteger _added_deposit;
    }

    public static class ChannelCloseRequestedEventResponse {
        public Log log;

        public String _sender_address;

        public String _receiver_address;

        public BigInteger _open_block_number;

        public BigInteger _balance;
    }

    public static class ChannelSettledEventResponse {
        public Log log;

        public String _sender_address;

        public String _receiver_address;

        public BigInteger _open_block_number;

        public BigInteger _balance;

        public BigInteger _receiver_tokens;
    }

    public static class ChannelWithdrawEventResponse {
        public Log log;

        public String _sender_address;

        public String _receiver_address;

        public BigInteger _open_block_number;

        public BigInteger _withdrawn_balance;
    }

    public static class TrustedContractEventResponse {
        public Log log;

        public String _trusted_contract_address;

        public Boolean _trusted_status;
    }
}
