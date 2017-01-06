package com.btc.assignment2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	private boolean[] followees;
	private Set<Transaction> pendingTransactions;
	private double p_malicious;
	private Map<Transaction, Set<Integer>> potentialTransactions; //transaction -> set of nodes that sent the transaction
	private Map<Transaction, Set<Integer>> potentialTransactions_previous_round;
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    	this.p_malicious = p_malicious;
    	potentialTransactions = new HashMap<Transaction, Set<Integer>>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
    	Set<Transaction> result = new HashSet<Transaction>();
    	result.addAll(pendingTransactions);
    	
    	//potentialTransactions
    	for(Transaction tx : potentialTransactions.keySet()) {
    		Set<Integer> nodes = potentialTransactions.get(tx);
    		Set<Integer> nodes_prev = potentialTransactions_previous_round.get(tx);
    		int nodes_prev_size = (nodes_prev == null) ? 0 : nodes_prev.size();
    		if(nodes.size() >= nodes_prev_size)
    			result.add(tx);
    	}
    	
    	return result;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	potentialTransactions_previous_round = potentialTransactions;
    	potentialTransactions = new HashMap<Transaction, Set<Integer>>();
    	
    	Set<Transaction> tx_follow = new HashSet<Transaction>();
    	Set<Transaction> tx_not_follow = new HashSet<Transaction>();
    	Set<Transaction> tx_all = new HashSet<Transaction>();
    	Map<Integer, Set<Transaction>> node_tx_map = new HashMap<Integer, Set<Transaction>>();
        Map<Transaction, Integer> tx_node_count = new HashMap<Transaction, Integer>();
    	
        for(Candidate c : candidates) {
        	tx_all.add(c.tx);
        	if(followees[c.sender])
        		tx_follow.add(c.tx);
        	else
        		tx_not_follow.add(c.tx);
        	
        	Set<Transaction> tx_map = node_tx_map.get(c.sender);
        	if(tx_map == null) {
        		tx_map = new HashSet<Transaction>();
        		node_tx_map.put(c.sender, tx_map);
        	}
        	tx_map.add(c.tx);
        	
        	Integer node_count = tx_node_count.get(c.tx);
        	if(node_count == null)
        		node_count = 0;
        	tx_node_count.put(c.tx, node_count+1);
            /*
        	//if(followees[c.sender] && !pendingTransactions.contains(c.tx)) {
        	if(!pendingTransactions.contains(c.tx)) {
        		pendingTransactions.add(c.tx); //todo?
        		continue;
        	}
        	
        	Set<Integer> nodes = potentialTransactions.get(c.tx);
        	if(nodes == null)
        		nodes = new HashSet<Integer>();
        	nodes.add(c.sender);
        	potentialTransactions.put(c.tx, nodes);
        	*/
        }
        
        Set<Transaction> pendingTransactions_prev = pendingTransactions;
        //pendingTransactions.clear();
        pendingTransactions = new HashSet<Transaction>();
        //pendingTransactions.addAll(tx_follow);

        //for(Integer node : node_tx_map.keySet()) {}

        int num_nodes = node_tx_map.keySet().size() + 1;
        for(Transaction tx : tx_node_count.keySet()) {
        	int node_count = tx_node_count.get(tx);
        	if(pendingTransactions_prev.contains(tx))
        		++node_count;
        	if((double)node_count/(double)num_nodes >= p_malicious)
        	//if((double)node_count/(double)num_nodes >= 0.5)
        		pendingTransactions.add(tx);
        }

        /*
        for(Transaction tx : tx_all) {
        	boolean tx_in_all_nodes = true;
        	
        	for(Integer node : node_tx_map.keySet()) {
        		Set<Transaction> node_tx = node_tx_map.get(node);
        		if(node_tx.size() == 0)
        			continue;
        		
        		if(!node_tx.contains(tx)) {
        			tx_in_all_nodes = false;
        			break;
        		}
        	}
        	
        	if(tx_in_all_nodes)
        		pendingTransactions.add(tx);
        }
        */
    }
}
