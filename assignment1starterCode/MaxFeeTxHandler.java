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
     * finds a set of transactions with maximum total transaction fees -- i.e. maximize the sum over all transactions in the set of (sum of input values - sum of output values)).
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	Set<Transaction> res = new HashSet<Transaction>();
    	for(Transaction tx : possibleTxs) {
    		Map<String, Double> map = getInOut(tx);
    		Double tx_in = map.get("in");
    		Double tx_out = map.get("out");
    		
    		if(tx_out < tx_in)
    			res.add(tx);
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
}
