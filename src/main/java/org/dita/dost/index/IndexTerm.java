/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.index;

import static org.dita.dost.util.Constants.*;

import java.util.*;

import org.dita.dost.util.DITAOTCollator;

/**
 * This class represents an indexterm.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
public final class IndexTerm implements Comparable<IndexTerm> {
    /** The locale of  the indexterm, used for sorting. */
    private static Locale termLocale = null;

    /** The name of the indexterm. */
    private String termName = null;

    /** The target list of the indexterm. */
    private List<IndexTermTarget> targetList = null;

    /** The sorting termKey of the indexterm, default will be the term name. */
    private String termKey = null;

    /** The start attribute. */
    private String start=null;

    /** The end attribute. */
    private String end=null;

    /** The sub indexterms contained by this indexterm. */
    private List<IndexTerm> subTerms = null;

    /** The prefix added to the term name (such as IndexTerm_Prefix_See or IndexTerm_Prefix_See_Also). */
    private String termPrefix = null;

    /** The list of rtl locale.*/
    private static final ArrayList<String> rtlLocaleList;

    /**
     * The boolean to show whether current term is leaf term
     * leaf means the current indexterm element doesn't contains any subterms
     * or only has "index-see" or "index-see-also" subterms.
     */
    private boolean leaf = true;

    //initialization for rtlLocaleList
    static{
        rtlLocaleList = new ArrayList<>(2);
        rtlLocaleList.add("ar_EG");
        rtlLocaleList.add("he_IL");
    }

    /**
     * Constructor.
     */
    public IndexTerm() {
        subTerms = new ArrayList<>(1);
        targetList = new ArrayList<>(1);
    }

    /**
     * Get the global locale of indexterm.
     * 
     * @return Locale language
     */
    public static Locale getTermLocale() {
        return termLocale;
    }

    /**
     * Set the global locale of indexterm.
     * 
     * @param locale locale
     */
    public static void setTermLocale(final Locale locale) {
        termLocale = locale;
    }

    /**
     * Get the index term name.
     * 
     * @return term name
     */
    public String getTermName() {
        return termName;
    }

    /**
     * Set the index term name.
     * 
     * @param name name to set
     */
    public void setTermName(final String name) {
        termName = name;
    }

    /**
     * Get the key used for sorting this term.
     * @return Returns the termKey.
     */
    public String getTermKey() {
        return termKey;
    }

    /**
     * Set the key used for sorting this term.
     * @param key The termKey to set.
     */
    public void setTermKey(final String key) {
        termKey = key;
    }

    /**
     * Get the sub term list.
     * 
     * @return sub term list
     */
    public List<IndexTerm> getSubTerms() {
        return subTerms;
    }

    /**
     * Get the start attribute.
     * @return start attribute
     */
    public String getStartAttribute(){
        return start;
    }

    /**
     * Get the end attribute.
     * @return end attribute
     */
    public String getEndAttribute(){
        return end;
    }

    /**
     * Set the start attribute.
     * @param start attribute
     */
    public void setStartAttribute(final String start){
        this.start=start;
    }

    /**
     * Set the end attribute.
     * @param end attribute
     */

    public void setEndAttribute(final String end){
        this.end=end;
    }
    /**
     * Add a sub term into the sub term list.
     * 
     * @param term index term to be added
     */
    public void addSubTerm(final IndexTerm term) {
        int i = 0;
        final int subTermNum = subTerms.size();

        if (!IndexTerm_Prefix_See.equals(term.getTermPrefix()) &&
                !IndexTerm_Prefix_See_Also.equals(term.getTermPrefix())){
            //if the term is not "index-see" or "index-see-also"
            leaf = false;
        }

        for (; i < subTermNum; i++) {
            final IndexTerm subTerm = subTerms.get(i);

            if (subTerm.equals(term)) {
                return;
            }

            // Add targets when same term name and same term key
            if (subTerm.getTermFullName().equals(term.getTermFullName())
                    && subTerm.getTermKey().equals(term.getTermKey())) {
                subTerm.addTargets(term.getTargetList());
                subTerm.addSubTerms(term.getSubTerms());
                return;
            }
        }

        if (i == subTermNum) {
            subTerms.add(term);
        }
    }

    /**
     * Add all the sub terms in the list.
     * 
     * @param terms terms list
     */
    public void addSubTerms(final List<IndexTerm> terms) {
        int subTermsNum = 0;
        if (terms == null) {
            return;
        }

        subTermsNum = terms.size();
        for (int i = 0; i < subTermsNum; i++) {
            addSubTerm(terms.get(i));
        }
    }

    /**
     * IndexTerm will be equal if they have same name, target and subterms.
     * 
     * @param o object to compare with.
     * @return boolean
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof IndexTerm)) {
            return false;
        } else if (o == this) {
            return true;
        }
        final IndexTerm it = (IndexTerm) o;
        boolean eqTermName;
        boolean eqTermKey;
        boolean eqTargetList;
        boolean eqSubTerms;
        boolean eqTermPrefix;

        eqTermName = Objects.equals(termName, it.getTermName()) || termName != null && termName.equals(it.getTermName());
        eqTermPrefix = Objects.equals(termPrefix, it.getTermPrefix()) || termPrefix != null && termPrefix.equals(it.getTermPrefix());
        eqTermKey = Objects.equals(termKey, it.getTermKey()) || termKey != null && termKey.equals(it.getTermKey());
        eqTargetList = targetList == it.getTargetList() || targetList != null && targetList.equals(it.getTargetList());
        eqSubTerms =  subTerms == it.getSubTerms() || subTerms != null && subTerms.equals(it.getSubTerms());

        return eqTermName && eqTermKey && eqTargetList && eqSubTerms && eqTermPrefix;
    }

    /**
     * Generate hash code for IndexTerm.
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int result = 17;

        result = 37 * result + termName.hashCode();
        result = 37 * result + termKey.hashCode();
        result = 37 * result + targetList.hashCode();
        result = 37 * result + subTerms.hashCode();

        return result;
    }

    /**
     * Sort all the subterms iteratively.
     */
    public void sortSubTerms() {
        final int subTermNum = subTerms.size();

        if (subTerms != null && subTermNum > 0) {
            Collections.sort(subTerms);
            for (final IndexTerm subTerm : subTerms) {
                subTerm.sortSubTerms();
            }
        }
    }

    /**
     * Compare the given indexterm with current term.
     * 
     * @param obj object to compare with
     * @return int
     */
    @Override
    public int compareTo(final IndexTerm obj) {
        return DITAOTCollator.getInstance(termLocale).compare(termKey, obj.getTermKey());
    }

    /**
     * Get the target list of current indexterm.
     * 
     * @return Returns the targetList.
     */
    public List<IndexTermTarget> getTargetList() {
        return targetList;
    }

    /**
     * Add a new indexterm target.
     * 
     * @param target indexterm target
     */
    public void addTarget(final IndexTermTarget target) {
        if (!targetList.contains(target)) {
            targetList.add(target);
        }
    }

    /**
     * Add all the indexterm targets in the list.
     * 
     * @param targets list of targets
     */
    public void addTargets(final List<IndexTermTarget> targets) {
        int targetNum = 0;

        if (targets == null) {
            return;
        }

        targetNum = targets.size();
        for (int i = 0; i < targetNum; i++) {
            addTarget(targets.get(i));
        }
    }

    /**
     * See if this indexterm has sub terms.
     * 
     * @return true if has subterms, false or else.
     */
    public boolean hasSubTerms() {
        return subTerms != null && subTerms.size() > 0;
    }

    /**
     * @see java.lang.Object#toString()
     * @return string
     */
    @Override
    public String toString() {

        return "{Term name: " + termName + ", Term key: " + termKey + ", Target list: " + targetList.toString() + ", Sub-terms: " + subTerms.toString() + "}";
    }

    /**
     * Get the term prefix (such as IndexTerm_Prefix_See_Also).
     * @return term prefix
     */
    public String getTermPrefix() {
        return termPrefix;
    }

    /**
     * Set the term prefix (such as IndexTerm_Prefix_See_Also).
     * @param termPrefix term prefix to set
     */
    public void setTermPrefix(final String termPrefix) {
        this.termPrefix = termPrefix;
    }

    /**
     * Get the full term, with any prefix.
     * @return full term with prefix
     */
    public String getTermFullName(){
        if (termPrefix == null){
            return termName;
        }else{
            if (termLocale == null){
                return termPrefix + STRING_BLANK + termName;
            }else if (rtlLocaleList.contains(termLocale.toString())){
                return termName + STRING_BLANK
                        + Messages.getString("IndexTerm." + termPrefix.toLowerCase().trim().replace(' ', '-'),
                                termLocale);
            }else {
                return Messages.getString("IndexTerm." + termPrefix.toLowerCase().trim().replace(' ', '-'),
                        termLocale)
                        + STRING_BLANK + termName;
            }
        }
    }

    /**
     * Update the sub-term prefix from "See also" to "See" if there is only one sub-term.
     */
    public void updateSubTerm(){
        if (subTerms.size()==1){
            // if there is only one subterm, it is necessary to update
            final IndexTerm term = subTerms.get(0); // get the only subterm
            if (term.getTermPrefix()!= null &&
                    IndexTerm_Prefix_See.equalsIgnoreCase(term.getTermPrefix().trim())){ 
                //if the only subterm is index-see update it to index-see-also
                term.setTermPrefix(IndexTerm_Prefix_See_Also); 
            }
        }
    }

    /**
     * check whether this term is leaf term
     * leaf means the current indexterm element doesn't contains any subterms
     * or only has "index-see" or "index-see-also" subterms.
     * @return boolean
     */
    public boolean isLeaf() {
        return leaf;
    }
}
