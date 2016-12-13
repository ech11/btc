import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {
	
	private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(final UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	double sum_output = 0.0;
    	double sum_input = 0.0;
    	
    	//1, 2, 3
    	Set<UTXO> used_inputs = new HashSet<UTXO>();
    	for(int i = 0; i < tx.getInputs().size(); ++i) {
    		Transaction.Input ti = tx.getInput(i);
    		
    		UTXO utxo = new UTXO(ti.prevTxHash, ti.outputIndex);
    		//1
    		if(!utxoPool.contains(utxo))
    			return false;
    		
    		//3
    		if(used_inputs.contains(utxo))
    			return false;
    		used_inputs.add(utxo);
    		
    		Transaction.Output to = utxoPool.getTxOutput(utxo);
    		sum_input += to.value;

    		PublicKey pubKey = to.address;
    		byte[] message = tx.getRawDataToSign(i);
    		byte[] signature = ti.signature;
    		//2
    		if(Crypto.verifySignature(pubKey, message, signature) == false)
    			return false;
    	}
    	
        //4, 5
    	for(int i = 0; i < tx.getOutputs().size(); ++i) {
    		Transaction.Output txOut = tx.getOutput(i);
    		
    		//4
    		if(txOut.value < 0.0)
    			return false;
    		sum_output += txOut.value;
    	}
    	
    	//5
    	if(sum_input < sum_output)
    		return false;
    	
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	Set<Transaction> valid_txs = new HashSet<Transaction>();
    	for(Transaction tx : possibleTxs) {
    		if(!isValidTx(tx))
    			continue;
    		
    		//update utxoPool for valid tx
    		updatePool(tx);
    		
    		valid_txs.add(tx);
    	}
    	
    	return valid_txs.toArray(new Transaction[valid_txs.size()]);
    }

    private void updatePool(final Transaction tx) {
    	//remove inputs
    	for(int i = 0; i < tx.getInputs().size(); ++i) {
    		Transaction.Input ti = tx.getInput(i);
    		
    		UTXO utxo = new UTXO(ti.prevTxHash, ti.outputIndex);

    		utxoPool.removeUTXO(utxo);
    	}
    	
    	//add outputs
    	for(int i = 0; i < tx.getOutputs().size(); ++i) {
    		Transaction.Output txOut = tx.getOutput(i);
    		
    		UTXO utxo = new UTXO(tx.getHash(), i);

    		utxoPool.addUTXO(utxo, txOut);
    	}
    }
}
