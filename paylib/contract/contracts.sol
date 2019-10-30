pragma solidity ^0.4.17;

library ECVerify {

    function ecverify(bytes32 hash, bytes signature) internal pure returns (address signature_address) {
        require(signature.length == 65);

        bytes32 r;
        bytes32 s;
        uint8 v;

        // The signature format is a compact form of:
        //   {bytes32 r}{bytes32 s}{uint8 v}
        // Compact means, uint8 is not padded to 32 bytes.
        assembly {
            r := mload(add(signature, 32))
            s := mload(add(signature, 64))

        // Here we are loading the last 32 bytes, including 31 bytes of 's'.
            v := byte(0, mload(add(signature, 96)))
        }

        // Version of signature should be 27 or 28, but 0 and 1 are also possible
        if (v < 27) {
            v += 27;
        }

        require(v == 27 || v == 28);

        signature_address = ecrecover(hash, v, r, s);

        // ecrecover returns zero on error
        require(signature_address != 0x0);

        return signature_address;
    }
}

/*
* Contract that is working with ERC223 tokens
* https://github.com/ethereum/EIPs/issues/223
*/

/// @title ERC223ReceivingContract - contract implementation for compatibility with ERC223 tokens.
contract ERC223ReceivingContract {

    /// @dev Function that is called when a user or another contract wants to transfer funds.
    /// @param _from Transaction initiator, analogue of msg.sender
    /// @param _value Number of tokens to transfer.
    /// @param _data Data containig a function signature and/or parameters
    function tokenFallback(address _from, uint256 _value, bytes _data) public;
}

contract Token {
    /*
     * Implements ERC 20 standard.
     * https://github.com/ethereum/EIPs/blob/f90864a3d2b2b45c4decf95efd26b3f0c276051a/EIPS/eip-20-token-standard.md
     * https://github.com/ethereum/EIPs/issues/20
     *
     *  Added support for the ERC 223 "tokenFallback" method in a "transfer" function with a payload.
     *  https://github.com/ethereum/EIPs/issues/223
     */

    /*
     * This is a slight change to the ERC20 base standard.
     * function totalSupply() constant returns (uint256 supply);
     * is replaced with:
     * uint256 public totalSupply;
     * This automatically creates a getter function for the totalSupply.
     * This is moved to the base contract since public getter functions are not
     * currently recognised as an implementation of the matching abstract
     * function by the compiler.
     */
    uint256 public totalSupply;

    /*
     * NOTE:
     * The following variables were optional. Now, they are included in ERC 223 interface.
     * They allow one to customise the token contract & in no way influences the core functionality.
     */
    string public name;                   //fancy name: eg Simon Bucks
    uint8 public decimals;                //How many decimals to show. ie. There could 1000 base units with 3 decimals. Meaning 0.980 SBX = 980 base units. It's like comparing 1 wei to 1 ether.
    string public symbol;                 //An identifier: eg SBX


    /// @param _owner The address from which the balance will be retrieved.
    /// @return The balance.
    function balanceOf(address _owner) public constant returns (uint256 balance);

    /// @notice send `_value` token to `_to` from `msg.sender`.
    /// @param _to The address of the recipient.
    /// @param _value The amount of token to be transferred.
    /// @param _data Data to be sent to `tokenFallback.
    /// @return Returns success of function call.
    function transfer(address _to, uint256 _value, bytes _data) public returns (bool success);

    /// @notice send `_value` token to `_to` from `msg.sender`.
    /// @param _to The address of the recipient.
    /// @param _value The amount of token to be transferred.
    /// @return Whether the transfer was successful or not.
    function transfer(address _to, uint256 _value) public returns (bool success);

    /// @notice send `_value` token to `_to` from `_from` on the condition it is approved by `_from`.
    /// @param _from The address of the sender.
    /// @param _to The address of the recipient.
    /// @param _value The amount of token to be transferred.
    /// @return Whether the transfer was successful or not.
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success);

    /// @notice `msg.sender` approves `_spender` to spend `_value` tokens.
    /// @param _spender The address of the account able to transfer the tokens.
    /// @param _value The amount of tokens to be approved for transfer.
    /// @return Whether the approval was successful or not.
    function approve(address _spender, uint256 _value) public returns (bool success);

    /// @param _owner The address of the account owning tokens.
    /// @param _spender The address of the account able to transfer the tokens.
    /// @return Amount of remaining tokens allowed to spent.
    function allowance(address _owner, address _spender) public constant returns (uint256 remaining);

    /*
     * Events
     */
    event Transfer(address indexed _from, address indexed _to, uint256 _value);
    event Approval(address indexed _owner, address indexed _spender, uint256 _value);

    // There is no ERC223 compatible Transfer event, with `_data` included.
}

/// @title Standard token contract - Standard token implementation.
contract StandardToken is Token {

    /*
     * Data structures
     */
    mapping(address => uint256) balances;
    mapping(address => mapping(address => uint256)) allowed;

    /*
     * Public functions
     */
    /// @notice Send `_value` tokens to `_to` from `msg.sender`.
    /// @dev Transfers sender's tokens to a given address. Returns success.
    /// @param _to Address of token receiver.
    /// @param _value Number of tokens to transfer.
    /// @return Returns success of function call.
    function transfer(address _to, uint256 _value) public returns (bool) {
        require(_to != 0x0);
        require(_to != address(this));
        require(balances[msg.sender] >= _value);
        require(balances[_to] + _value >= balances[_to]);

        balances[msg.sender] -= _value;
        balances[_to] += _value;

        Transfer(msg.sender, _to, _value);

        return true;
    }

    /// @notice Send `_value` tokens to `_to` from `msg.sender` and trigger
    /// tokenFallback if sender is a contract.
    /// @dev Function that is called when a user or another contract wants to transfer funds.
    /// @param _to Address of token receiver.
    /// @param _value Number of tokens to transfer.
    /// @param _data Data to be sent to tokenFallback
    /// @return Returns success of function call.
    function transfer(
        address _to,
        uint256 _value,
        bytes _data)
    public
    returns (bool)
    {
        require(transfer(_to, _value));

        uint codeLength;

        assembly {
        // Retrieve the size of the code on target address, this needs assembly.
            codeLength := extcodesize(_to)
        }

        if (codeLength > 0) {
            ERC223ReceivingContract receiver = ERC223ReceivingContract(_to);
            receiver.tokenFallback(msg.sender, _value, _data);
        }

        return true;
    }

    /// @notice Transfer `_value` tokens from `_from` to `_to` if `msg.sender` is allowed.
    /// @dev Allows for an approved third party to transfer tokens from one
    /// address to another. Returns success.
    /// @param _from Address from where tokens are withdrawn.
    /// @param _to Address to where tokens are sent.
    /// @param _value Number of tokens to transfer.
    /// @return Returns success of function call.
    function transferFrom(address _from, address _to, uint256 _value)
    public
    returns (bool)
    {
        require(_from != 0x0);
        require(_to != 0x0);
        require(_to != address(this));
        require(balances[_from] >= _value);
        require(allowed[_from][msg.sender] >= _value);
        require(balances[_to] + _value >= balances[_to]);

        balances[_to] += _value;
        balances[_from] -= _value;
        allowed[_from][msg.sender] -= _value;

        Transfer(_from, _to, _value);

        return true;
    }

    /// @notice Allows `_spender` to transfer `_value` tokens from `msg.sender` to any address.
    /// @dev Sets approved amount of tokens for spender. Returns success.
    /// @param _spender Address of allowed account.
    /// @param _value Number of approved tokens.
    /// @return Returns success of function call.
    function approve(address _spender, uint256 _value) public returns (bool) {
        require(_spender != 0x0);

        // To change the approve amount you first have to reduce the addresses`
        // allowance to zero by calling `approve(_spender, 0)` if it is not
        // already 0 to mitigate the race condition described here:
        // https://github.com/ethereum/EIPs/issues/20#issuecomment-263524729
        require(_value == 0 || allowed[msg.sender][_spender] == 0);

        allowed[msg.sender][_spender] = _value;
        Approval(msg.sender, _spender, _value);
        return true;
    }

    /*
     * Read functions
     */
    /// @dev Returns number of allowed tokens that a spender can transfer on
    /// behalf of a token owner.
    /// @param _owner Address of token owner.
    /// @param _spender Address of token spender.
    /// @return Returns remaining allowance for spender.
    function allowance(address _owner, address _spender)
    constant
    public
    returns (uint256)
    {
        return allowed[_owner][_spender];
    }

    /// @dev Returns number of tokens owned by the given address.
    /// @param _owner Address of token owner.
    /// @return Returns balance of owner.
    function balanceOf(address _owner) constant public returns (uint256) {
        return balances[_owner];
    }
}

/// @title CustomToken
contract CustomToken is StandardToken {

    /*
     *  Token metadata
     */
    string public version = 'H0.1';       //human 0.1 standard. Just an arbitrary versioning scheme.
    string public name;
    string public symbol;
    uint8 public decimals;
    uint256 public multiplier;

    address public owner_address;

    /*
     * Events
     */
    event Minted(address indexed _to, uint256 indexed _num);

    /*
     *  Public functions
     */
    /// @dev Contract constructor function.
    /// @param initial_supply Initial supply of tokens.
    /// @param token_name Token name for display.
    /// @param token_symbol Token symbol.
    /// @param decimal_units Number of token decimals.
    function CustomToken(
        uint256 initial_supply,
        string token_name,
        string token_symbol,
        uint8 decimal_units)
    public
    {
        // Set the name for display purposes
        name = token_name;

        // Amount of decimals for display purposes
        decimals = decimal_units;
        multiplier = 10 ** (uint256(decimal_units));

        // Set the symbol for display purposes
        symbol = token_symbol;

        // Initial supply is assigned to the owner
        owner_address = msg.sender;
        balances[owner_address] = initial_supply;
        totalSupply = initial_supply;
    }

    /// @notice Allows tokens to be minted and assigned to `msg.sender`
    /// For `msg.value >= 100 finney`, the sender receives 50 tokens
    function mint() public payable {
        require(msg.value >= 100 finney);

        // Assign 50 tokens to msg.sender
        uint256 num = 50 * multiplier;
        balances[msg.sender] += num;
        totalSupply += num;

        Minted(msg.sender, num);

        assert(balances[msg.sender] >= num);
        assert(totalSupply >= num);
    }

    /// @notice Transfers the collected ETH to the contract owner.
    function transferFunds() public {
        require(msg.sender == owner_address);
        require(this.balance > 0);

        owner_address.transfer(this.balance);
        assert(this.balance == 0);
    }
}

/// @title Raiden MicroTransfer Channels Contract.
contract RaidenMicroTransferChannels {

    /*
     *  Data structures
     */

    // The only role of the owner_address is to add or remove trusted contracts
    address public owner_address;

    // Number of blocks to wait from an uncooperativeClose initiated by the sender
    // in order to give the receiver a chance to respond with a balance proof
    // in case the sender cheats. After the challenge period, the sender can settle
    // and delete the channel.
    uint32 public challenge_period;

    // Contract semantic version
    string public constant version = '0.2.0';

    // We temporarily limit total token deposits in a channel to 100 tokens with 18 decimals.
    // This was calculated just for RDN with its current (as of 30/11/2017) price and should
    // not be considered to be the same for other tokens.
    // This is just for the bug bounty release, as a safety measure.
    uint256 public constant channel_deposit_bugbounty_limit = 10 ** 18 * 100;

    Token public token;

    mapping(bytes32 => Channel) public channels;
    mapping(bytes32 => ClosingRequest) public closing_requests;
    mapping(address => bool) public trusted_contracts;
    mapping(bytes32 => uint192) public withdrawn_balances;

    // 24 bytes (deposit) + 4 bytes (block number)
    struct Channel {
        // uint192 is the maximum uint size needed for deposit based on a
        // 10^8 * 10^18 token totalSupply.
        uint192 deposit;

        // Block number at which the channel was opened. Used in creating
        // a unique identifier for the channel between a sender and receiver.
        // Supports creation of multiple channels between the 2 parties and prevents
        // replay of messages in later channels.
        uint32 open_block_number;
    }

    // 24 bytes (deposit) + 4 bytes (block number)
    struct ClosingRequest {
        // Number of tokens owed by the sender when closing the channel.
        uint192 closing_balance;

        // Block number at which the challenge period ends, in case it has been initiated.
        uint32 settle_block_number;
    }

    /*
     * Modifiers
     */

    modifier isOwner() {
        require(msg.sender == owner_address);
        _;
    }

    modifier isTrustedContract() {
        require(trusted_contracts[msg.sender]);
        _;
    }

    /*
     *  Events
     */

    event ChannelCreated(
        address indexed _sender_address,
        address indexed _receiver_address,
        uint192 _deposit);
    event ChannelToppedUp (
        address indexed _sender_address,
        address indexed _receiver_address,
        uint32 indexed _open_block_number,
        uint192 _added_deposit);
    event ChannelCloseRequested(
        address indexed _sender_address,
        address indexed _receiver_address,
        uint32 indexed _open_block_number,
        uint192 _balance);
    event ChannelSettled(
        address indexed _sender_address,
        address indexed _receiver_address,
        uint32 indexed _open_block_number,
        uint192 _balance,
        uint192 _receiver_tokens);
    event ChannelWithdraw(
        address indexed _sender_address,
        address indexed _receiver_address,
        uint32 indexed _open_block_number,
        uint192 _withdrawn_balance);
    event TrustedContract(
        address indexed _trusted_contract_address,
        bool _trusted_status);


    /*
     *  Constructor
     */

    /// @notice Constructor for creating the uRaiden microtransfer channels contract.
    /// @param _token_address The address of the Token used by the uRaiden contract.
    /// @param _challenge_period A fixed number of blocks representing the challenge period.
    /// We enforce a minimum of 500 blocks waiting period.
    /// after a sender requests the closing of the channel without the receiver's signature.
    /// @param _trusted_contracts Array of contract addresses that can be trusted to
    /// open and top up channels on behalf of a sender.
    function RaidenMicroTransferChannels(
        address _token_address,
        uint32 _challenge_period,
        address[] _trusted_contracts)
    public
    {
        require(_token_address != 0x0);
        require(addressHasCode(_token_address));
        require(_challenge_period >= 500);

        token = Token(_token_address);

        // Check if the contract is indeed a token contract
        require(token.totalSupply() > 0);

        challenge_period = _challenge_period;
        owner_address = msg.sender;
        addTrustedContracts(_trusted_contracts);
    }

    /*
     *  External functions
     */

    /// @notice Opens a new channel or tops up an existing one, compatibility with ERC 223.
    /// @dev Can only be called from the trusted Token contract.
    /// @param _sender_address The address that sent the tokens to this contract.
    /// @param _deposit The amount of tokens that the sender escrows.
    /// @param _data Data needed for either creating a channel or topping it up.
    /// It always contains the sender and receiver addresses +/- a block number.
    function tokenFallback(address _sender_address, uint256 _deposit, bytes _data) external {
        // Make sure we trust the token
        require(msg.sender == address(token));

        uint192 deposit = uint192(_deposit);
        require(deposit == _deposit);

        // Create channel - sender address + receiver address = 2 * 20 bytes
        // Top up channel - sender address + receiver address + block number = 2 * 20 + 4 bytes
        uint length = _data.length;
        require(length == 40 || length == 44);

        // Offset of 32 bytes, representing _data.length
        address channel_sender_address = address(addressFromBytes(_data, 0x20));

        // The channel can be opened by the sender or by a trusted contract
        require(_sender_address == channel_sender_address || trusted_contracts[_sender_address]);

        // Offset of 32 bytes (data.length) + 20 bytes (sender address)
        address channel_receiver_address = address(addressFromBytes(_data, 0x34));

        if (length == 40) {
            createChannelPrivate(channel_sender_address, channel_receiver_address, deposit);
        } else {
            // Offset of: 32 bytes (_data.length) + 20 bytes (sender address)
            // + 20 bytes (receiver address)
            uint32 open_block_number = uint32(blockNumberFromBytes(_data, 0x48));
            updateInternalBalanceStructs(
                channel_sender_address,
                channel_receiver_address,
                open_block_number,
                deposit
            );
        }
    }

    /// @notice Creates a new channel between `msg.sender` and `_receiver_address` and transfers
    /// the `_deposit` token deposit to this contract. Compatibility with ERC20 tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _deposit The amount of tokens that the sender escrows.
    function createChannel(address _receiver_address, uint192 _deposit) external {
        createChannelPrivate(msg.sender, _receiver_address, _deposit);

        // transferFrom deposit from msg.sender to contract
        // ! needs prior approval from msg.sender
        require(token.transferFrom(msg.sender, address(this), _deposit));
    }

    /// @notice Function that allows a delegate contract to create a new channel between
    /// `_sender_address` and `_receiver_address` and transfers the token deposit to this contract.
    /// Can only be called by a trusted contract. Compatibility with ERC20 tokens.
    /// @param _sender_address The sender's address in behalf of whom the delegate sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _deposit The amount of tokens that the sender escrows.
    function createChannelDelegate(
        address _sender_address,
        address _receiver_address,
        uint192 _deposit)
    isTrustedContract
    external
    {
        createChannelPrivate(_sender_address, _receiver_address, _deposit);

        // transferFrom deposit from msg.sender to contract
        // ! needs prior approval from msg.sender
        require(token.transferFrom(msg.sender, address(this), _deposit));
    }

    /// @notice Increase the channel deposit with `_added_deposit`.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _added_deposit The added token deposit with which the current deposit is increased.
    function topUp(
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _added_deposit)
    external
    {
        updateInternalBalanceStructs(
            msg.sender,
            _receiver_address,
            _open_block_number,
            _added_deposit
        );

        // transferFrom deposit from msg.sender to contract
        // ! needs prior approval from msg.sender
        // Do transfer after any state change
        require(token.transferFrom(msg.sender, address(this), _added_deposit));
    }

    /// @notice Function that allows a delegate contract to increase the channel deposit
    /// with `_added_deposit`. Can only be called by a trusted contract. Compatibility with ERC20 tokens.
    /// @param _sender_address The sender's address in behalf of whom the delegate sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _added_deposit The added token deposit with which the current deposit is increased.
    function topUpDelegate(
        address _sender_address,
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _added_deposit)
    isTrustedContract
    external
    {
        updateInternalBalanceStructs(
            _sender_address,
            _receiver_address,
            _open_block_number,
            _added_deposit
        );

        // transferFrom deposit from msg.sender to contract
        // ! needs prior approval from the trusted contract
        // Do transfer after any state change
        require(token.transferFrom(msg.sender, address(this), _added_deposit));
    }

    /// @notice Allows channel receiver to withdraw tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _balance Partial or total amount of tokens owed by the sender to the receiver.
    /// Has to be smaller or equal to the channel deposit. Has to match the balance value from
    /// `_balance_msg_sig` - the balance message signed by the sender.
    /// Has to be smaller or equal to the channel deposit.
    /// @param _balance_msg_sig The balance message signed by the sender.
    function withdraw(
        uint32 _open_block_number,
        uint192 _balance,
        bytes _balance_msg_sig)
    external
    {
        require(_balance > 0);

        // Derive sender address from signed balance proof
        address sender_address = extractBalanceProofSignature(
            msg.sender,
            _open_block_number,
            _balance,
            _balance_msg_sig
        );

        bytes32 key = getKey(sender_address, msg.sender, _open_block_number);

        // Make sure the channel exists
        require(channels[key].open_block_number > 0);

        // Make sure the channel is not in the challenge period
        require(closing_requests[key].settle_block_number == 0);

        require(_balance <= channels[key].deposit);
        require(withdrawn_balances[key] < _balance);

        uint192 remaining_balance = _balance - withdrawn_balances[key];
        withdrawn_balances[key] = _balance;

        // Send the remaining balance to the receiver
        require(token.transfer(msg.sender, remaining_balance));

        ChannelWithdraw(sender_address, msg.sender, _open_block_number, remaining_balance);
    }

    /// @notice Function called by the sender, receiver or a delegate, with all the needed
    /// signatures to close the channel and settle immediately.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _balance The amount of tokens owed by the sender to the receiver.
    /// @param _balance_msg_sig The balance message signed by the sender.
    /// @param _closing_sig The receiver's signed balance message, containing the sender's address.
    function cooperativeClose(
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _balance,
        bytes _balance_msg_sig,
        bytes _closing_sig)
    external
    {
        // Derive sender address from signed balance proof
        address sender = extractBalanceProofSignature(
            _receiver_address,
            _open_block_number,
            _balance,
            _balance_msg_sig
        );

        // Derive receiver address from closing signature
        address receiver = extractClosingSignature(
            sender,
            _open_block_number,
            _balance,
            _closing_sig
        );
        require(receiver == _receiver_address);

        // Both signatures have been verified and the channel can be settled.
        settleChannel(sender, receiver, _open_block_number, _balance);
    }

    /// @notice Sender requests the closing of the channel and starts the challenge period.
    /// This can only happen once.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between
    /// the sender and receiver was created.
    /// @param _balance The amount of tokens owed by the sender to the receiver.
    function uncooperativeClose(
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _balance)
    external
    {
        bytes32 key = getKey(msg.sender, _receiver_address, _open_block_number);

        require(channels[key].open_block_number > 0);
        require(closing_requests[key].settle_block_number == 0);
        require(_balance <= channels[key].deposit);

        // Mark channel as closed
        closing_requests[key].settle_block_number = uint32(block.number) + challenge_period;
        require(closing_requests[key].settle_block_number > block.number);
        closing_requests[key].closing_balance = _balance;
        ChannelCloseRequested(msg.sender, _receiver_address, _open_block_number, _balance);
    }


    /// @notice Function called by the sender after the challenge period has ended, in order to
    /// settle and delete the channel, in case the receiver has not closed the channel himself.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between
    /// the sender and receiver was created.
    function settle(address _receiver_address, uint32 _open_block_number) external {
        bytes32 key = getKey(msg.sender, _receiver_address, _open_block_number);

        // Make sure an uncooperativeClose has been initiated
        require(closing_requests[key].settle_block_number > 0);

        // Make sure the challenge_period has ended
        require(block.number > closing_requests[key].settle_block_number);

        settleChannel(msg.sender, _receiver_address, _open_block_number,
            closing_requests[key].closing_balance
        );
    }

    /// @notice Function for retrieving information about a channel.
    /// @param _sender_address The address that sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @return Channel information: unique_identifier, deposit, settle_block_number,
    /// closing_balance, withdrawn balance).
    function getChannelInfo(
        address _sender_address,
        address _receiver_address,
        uint32 _open_block_number)
    external
    view
    returns (bytes32, uint192, uint32, uint192, uint192)
    {
        bytes32 key = getKey(_sender_address, _receiver_address, _open_block_number);
        require(channels[key].open_block_number > 0);

        return (
        key,
        channels[key].deposit,
        closing_requests[key].settle_block_number,
        closing_requests[key].closing_balance,
        withdrawn_balances[key]
        );
    }

    /*
     *  Public functions
     */

    /// @notice Function for adding trusted contracts. Can only be called by owner_address.
    /// @param _trusted_contracts Array of contract addresses that can be trusted to
    /// open and top up channels on behalf of a sender.
    function addTrustedContracts(address[] _trusted_contracts) isOwner public {
        for (uint256 i = 0; i < _trusted_contracts.length; i++) {
            if (addressHasCode(_trusted_contracts[i])) {
                trusted_contracts[_trusted_contracts[i]] = true;
                TrustedContract(_trusted_contracts[i], true);
            }
        }
    }

    /// @notice Function for removing trusted contracts. Can only be called by owner_address.
    /// @param _trusted_contracts Array of contract addresses to be removed from
    /// the trusted_contracts mapping.
    function removeTrustedContracts(address[] _trusted_contracts) isOwner public {
        for (uint256 i = 0; i < _trusted_contracts.length; i++) {
            if (trusted_contracts[_trusted_contracts[i]]) {
                trusted_contracts[_trusted_contracts[i]] = false;
                TrustedContract(_trusted_contracts[i], false);
            }
        }
    }

    /// @notice Returns the sender address extracted from the balance proof.
    /// dev Works with eth_signTypedData https://github.com/ethereum/EIPs/pull/712.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _balance The amount of tokens owed by the sender to the receiver.
    /// @param _balance_msg_sig The balance message signed by the sender.
    /// @return Address of the balance proof signer.
    function extractBalanceProofSignature(
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _balance,
        bytes _balance_msg_sig)
    public
    view
    returns (address)
    {
        // The variable names from below will be shown to the sender when signing
        // the balance proof, so they have to be kept in sync with the Dapp client.
        // The hashed strings should be kept in sync with this function's parameters
        // (variable names and types).
        // ! Note that EIP712 might change how hashing is done, triggering a
        // new contract deployment with updated code.
        bytes32 message_hash = keccak256(
            keccak256(
                'string message_id',
                'address receiver',
                'uint32 block_created',
                'uint192 balance',
                'address contract'
            ),
            keccak256(
                'Sender balance proof signature',
                _receiver_address,
                _open_block_number,
                _balance,
                address(this)
            )
        );

        // Derive address from signature
        address signer = ECVerify.ecverify(message_hash, _balance_msg_sig);
        return signer;
    }

    /// @dev Returns the receiver address extracted from the closing signature.
    /// Works with eth_signTypedData https://github.com/ethereum/EIPs/pull/712.
    /// @param _sender_address The address that sends tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _balance The amount of tokens owed by the sender to the receiver.
    /// @param _closing_sig The receiver's signed balance message, containing the sender's address.
    /// @return Address of the closing signature signer.
    function extractClosingSignature(
        address _sender_address,
        uint32 _open_block_number,
        uint192 _balance,
        bytes _closing_sig)
    public
    view
    returns (address)
    {
        // The variable names from below will be shown to the sender when signing
        // the balance proof, so they have to be kept in sync with the Dapp client.
        // The hashed strings should be kept in sync with this function's parameters
        // (variable names and types).
        // ! Note that EIP712 might change how hashing is done, triggering a
        // new contract deployment with updated code.
        bytes32 message_hash = keccak256(
            keccak256(
                'string message_id',
                'address sender',
                'uint32 block_created',
                'uint192 balance',
                'address contract'
            ),
            keccak256(
                'Receiver closing signature',
                _sender_address,
                _open_block_number,
                _balance,
                address(this)
            )
        );

        // Derive address from signature
        address signer = ECVerify.ecverify(message_hash, _closing_sig);
        return signer;
    }

    /// @notice Returns the unique channel identifier used in the contract.
    /// @param _sender_address The address that sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @return Unique channel identifier.
    function getKey(
        address _sender_address,
        address _receiver_address,
        uint32 _open_block_number)
    public
    pure
    returns (bytes32 data)
    {
        return keccak256(_sender_address, _receiver_address, _open_block_number);
    }

    /*
     *  Private functions
     */

    /// @dev Creates a new channel between a sender and a receiver.
    /// @param _sender_address The address that sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _deposit The amount of tokens that the sender escrows.
    function createChannelPrivate(
        address _sender_address,
        address _receiver_address,
        uint192 _deposit)
    private
    {
        require(_deposit <= channel_deposit_bugbounty_limit);

        uint32 open_block_number = uint32(block.number);

        // Create unique identifier from sender, receiver and current block number
        bytes32 key = getKey(_sender_address, _receiver_address, open_block_number);

        require(channels[key].deposit == 0);
        require(channels[key].open_block_number == 0);
        require(closing_requests[key].settle_block_number == 0);

        // Store channel information
        channels[key] = Channel({deposit : _deposit, open_block_number : open_block_number});
        ChannelCreated(_sender_address, _receiver_address, _deposit);
    }

    /// @dev Updates internal balance Structures when the sender adds tokens to the channel.
    /// @param _sender_address The address that sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _added_deposit The added token deposit with which the current deposit is increased.
    function updateInternalBalanceStructs(
        address _sender_address,
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _added_deposit)
    private
    {
        require(_added_deposit > 0);
        require(_open_block_number > 0);

        bytes32 key = getKey(_sender_address, _receiver_address, _open_block_number);

        require(channels[key].open_block_number > 0);
        require(closing_requests[key].settle_block_number == 0);
        require(channels[key].deposit + _added_deposit <= channel_deposit_bugbounty_limit);

        channels[key].deposit += _added_deposit;
        assert(channels[key].deposit >= _added_deposit);
        ChannelToppedUp(_sender_address, _receiver_address, _open_block_number, _added_deposit);
    }

    /// @dev Deletes the channel and settles by transfering the balance to the receiver
    /// and the rest of the deposit back to the sender.
    /// @param _sender_address The address that sends tokens.
    /// @param _receiver_address The address that receives tokens.
    /// @param _open_block_number The block number at which a channel between the
    /// sender and receiver was created.
    /// @param _balance The amount of tokens owed by the sender to the receiver.
    function settleChannel(
        address _sender_address,
        address _receiver_address,
        uint32 _open_block_number,
        uint192 _balance)
    private
    {
        bytes32 key = getKey(_sender_address, _receiver_address, _open_block_number);
        Channel memory channel = channels[key];

        require(channel.open_block_number > 0);
        require(_balance <= channel.deposit);
        require(withdrawn_balances[key] <= _balance);

        // Remove closed channel structures
        // channel.open_block_number will become 0
        // Change state before transfer call
        delete channels[key];
        delete closing_requests[key];

        // Send the unwithdrawn _balance to the receiver
        uint192 receiver_remaining_tokens = _balance - withdrawn_balances[key];
        require(token.transfer(_receiver_address, receiver_remaining_tokens));

        // Send deposit - balance back to sender
        require(token.transfer(_sender_address, channel.deposit - _balance));

        ChannelSettled(
            _sender_address,
            _receiver_address,
            _open_block_number,
            _balance,
            receiver_remaining_tokens
        );
    }

    /*
     *  Internal functions
     */

    /// @dev Internal function for getting an address from tokenFallback data bytes.
    /// @param data Bytes received.
    /// @param offset Number of bytes to offset.
    /// @return Extracted address.
    function addressFromBytes(bytes data, uint256 offset) internal pure returns (address) {
        bytes20 extracted_address;
        assembly {
            extracted_address := mload(add(data, offset))
        }
        return address(extracted_address);
    }

    /// @dev Internal function for getting the block number from tokenFallback data bytes.
    /// @param data Bytes received.
    /// @param offset Number of bytes to offset.
    /// @return Block number.
    function blockNumberFromBytes(bytes data, uint256 offset) internal pure returns (uint32) {
        bytes4 block_number;
        assembly {
            block_number := mload(add(data, offset))
        }
        return uint32(block_number);
    }

    /// @dev Check if a contract exists.
    /// @param _contract The address of the contract to check for.
    /// @return True if a contract exists, false otherwise.
    function addressHasCode(address _contract) internal view returns (bool) {
        uint size;
        assembly {
            size := extcodesize(_contract)
        }

        return size > 0;
    }
}

/*
 * This is a contract used for testing RaidenMicroTransferChannels.
 */

contract Delegate {

    RaidenMicroTransferChannels public microraiden;
    Token public token;

    function setup(address _token_address, address _microraiden_contract) external {
        require(_microraiden_contract != 0x0);
        require(_token_address != 0x0);
        microraiden = RaidenMicroTransferChannels(_microraiden_contract);
        token = Token(_token_address);
    }

    function createChannelERC20(address _sender_address, address _receiver_address, uint192 _deposit) external {
        token.approve(address(microraiden), _deposit);
        microraiden.createChannelDelegate(_sender_address, _receiver_address, _deposit);
    }

    function createChannelERC223(uint192 _deposit, bytes _data) external {
        token.transfer(address(microraiden), _deposit, _data);
    }

    function topUpERC20(address _sender_address, address _receiver_address, uint32 _open_block_number, uint192 _deposit) external {
        token.approve(address(microraiden), _deposit);
        microraiden.topUpDelegate(_sender_address, _receiver_address, _open_block_number, _deposit);
    }

    function topUpERC223(uint192 _deposit, bytes _data) external {
        token.transfer(address(microraiden), _deposit, _data);
    }
}
