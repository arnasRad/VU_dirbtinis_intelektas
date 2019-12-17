package com.arnasrad.fbchaining.model;

import com.arnasrad.fbchaining.utility.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class ProductionSystem {

    private ArrayList<Rule> rules;
    private ArrayList<String> facts;
    private ArrayList<String> originalFacts; // facts initially loaded from file
    private String target;

    public ProductionSystem() {

        this.rules = new ArrayList<>();
        this.facts = new ArrayList<>();
        this.originalFacts = new ArrayList<>();
        this.target = null;
    }

    public ProductionSystem(ArrayList<Rule> rules, String[] facts, String target) {

        this.rules = new ArrayList<>(rules);
        this.facts = new ArrayList<>(Arrays.asList(facts));
        this.originalFacts = new ArrayList<>(Arrays.asList(facts));
        this.target = target;
    }

    public void reset() {

        this.facts = new ArrayList<>(originalFacts);
    }

    public void addRule(Rule rule) {

        this.rules.add(rule);
    }

    public void addFact(String fact) {

        this.facts.add(fact);
    }

    public void removeFacts(ArrayList<String> facts) {

        this.facts.removeAll(facts);
    }

    public void setTarget(String target) {

        this.target = target;
    }

    public String getTarget() {

        return this.target;
    }

    public ArrayList<String> getFacts() {

        return new ArrayList<>(this.facts);
    }

    public ArrayList<String> getInitialFacts() {

        return new ArrayList<>(originalFacts);
    }

    public String getInitialFactsString() {

        return Utils.getListString(originalFacts, ", ");
    }

    /**
     * Checks whether the production system already has a fact
     *      that the specified rule produces
     * Sets rule flag to value 2 if its result exists in production system facts list
     * @param rule to check for result
     * @return true if fact exists; false otherwise
     */
    public boolean resultInFacts(Rule rule) {

        if (this.facts.contains(rule.getResult())) {

            rule.setFlag((byte) 2);
            return true;
        }

        return false;
    }

    /**
     * Applies the specified rule to production system if it's possible to be applied
     * Add rule result to production systems' facts list
     * raises flag1 on the rule on successful rule application
     *
     * @param rule to apply to production system
     * @return list of absent facts need to apply the rule
     * @throws Exception rule is not present in production system
     */
    public ArrayList<String> applyRule(Rule rule) throws Exception{

        if (!rules.contains(rule)) {

            throw new Exception("Rule " + rule + " is not present in production " +
                    "system. Cannot apply the rule.");
        }

        ArrayList<String> absentFacts = getAbsentFacts(rule);
        if (absentFacts.size() != 0) {

            return absentFacts;
        }

        addFact(rule.getResult());
        rule.setFlag((byte) 1);

        return new ArrayList<>();
    }

    /**
     * Checks whether target fact is present in production system facts list
     * @return true if target exists; false otherwise
     */
    public boolean isTargetReached() {

        return facts.contains(target);
    }

    /**
     * Determines whether it's possible to use a given rule using
     * current production system facts
     * @param rule to check for usage
     * @return true if rule can be used; false otherwise
     */
    public boolean canUse(Rule rule) {

        ArrayList<String> ruleFacts = rule.getFacts();
        for(String ruleFact : ruleFacts) {
            if (!this.facts.contains(ruleFact)) {

                return false;
            }
        }

        return true;
    }

    /**
     * Returns the absent facts for a rule to be applied
     * @param rule to check for usage
     * @return list of absent facts; empty list is return if there are no
     *          absent facts (it is possible to apply the rule)
     */
    private ArrayList<String> getAbsentFacts(Rule rule) {

        ArrayList<String> ruleFacts = rule.getFacts();
        ArrayList<String> absentFacts = new ArrayList<>();
        for(String ruleFact : ruleFacts) {
            if (!this.facts.contains(ruleFact)) {

                absentFacts.add(ruleFact);
            }
        }

        return absentFacts;
    }

    public ArrayList<Rule> getRules() {

        return new ArrayList<>(rules);
    }

    public String getRulesString() {

        if (rules == null || rules.size() == 0) {

            return "";
        }

        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(Rule rule : rules) {

            sb.append("\tR").append(i).append(": ").append(rule).append("\n");
        }

        return sb.toString();
    }

    public String getFactsString() {

        StringBuilder sb = new StringBuilder();

        for(String fact : facts) {

            sb.append(fact).append(", ");
        }

        String factsString = sb.toString();
        return factsString.substring(0, factsString.length() - 2);
    }

    public int getFactsCount() {

        return this.facts.size();
    }
}
