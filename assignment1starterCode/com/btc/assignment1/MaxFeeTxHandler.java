package com.btc.assignment1;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MaxFeeTxHandler {
	
	private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(final UTXOPool utxoPool) {
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
     * finds a set of transactions with maximum total transaction fees -- i.e. maximize the sum over all transactions in the set of (sum of input values - sum of output values)).
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	Set<Transaction> res = new HashSet<Transaction>();
    	for(Transaction tx : possibleTxs) {
    		if(!isValidTx(tx))
    			continue;
    		
    		Map<String, Double> map = getInOut(tx);
    		Double tx_in = map.get("in");
    		Double tx_out = map.get("out");
    		
    		if(tx_out <= tx_in)
    			res.add(tx);
    		else
    			; //todo - remove from pool?
    		
    		//update utxoPool for valid tx
    		if(tx_out <= tx_in)
    		updatePool(tx);
    	}
    	
    	return res.toArray(new Transaction[res.size()]);
    }

    private Map<String, Double> getInOut(final Transaction tx) {
    	Map<String, Double> res = new HashMap<String, Double>();
    	

    	double sum_input = 0.0;
    	for(int i = 0; i < tx.getInputs().size(); ++i) {
    		Transaction.Input ti = tx.getInput(i);
    		
    		UTXO utxo = new UTXO(ti.prevTxHash, ti.outputIndex);
    		if(!utxoPool.contains(utxo))
    			continue;
    		
    		Transaction.Output to = utxoPool.getTxOutput(utxo);
    		sum_input += to.value;
    	}
    	
    	double sum_output = 0.0;
    	for(int i = 0; i < tx.getOutputs().size(); ++i) {
    	    Transaction.Output txOut = tx.getOutput(i);
    	    sum_output += txOut.value;
    	}
    			
    	res.put("in", sum_input);
    	res.put("out", sum_output);
    	
    	return res;
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
