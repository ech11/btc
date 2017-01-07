package com.btc.assignment2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private boolean[] followees;
    private Set<Transaction> pendingTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        Set<Transaction> tx_follow = new HashSet<Transaction>();
        Map<Integer, Set<Transaction>> node_tx_map = new HashMap<Integer, Set<Transaction>>();

        for (Candidate c : candidates) {
            if (!followees[c.sender])
                continue;

            tx_follow.add(c.tx);

            Set<Transaction> tx_map = node_tx_map.get(c.sender);
            if (tx_map == null) {
                tx_map = new HashSet<Transaction>();
                node_tx_map.put(c.sender, tx_map);
            }
            tx_map.add(c.tx);
        }

        // remove malicious nodes
        for (int i = 0; i < followees.length; ++i) {
            if (followees[i] && !node_tx_map.containsKey(i))
                followees[i] = false;
        }

        pendingTransactions.addAll(tx_follow);
    }
}
