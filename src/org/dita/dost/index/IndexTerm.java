/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dita.dost.util.Constants;
import org.dita.dost.util.DITAOTCollator;

/**
 * This class represent indexterm.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTerm implements Comparable {
    /** The locale of indexterm, used for sorting */
    private static Locale termLocale = null;

    /** The name of the indexterm */
    private String termName = null;

    /** The target list of the indexterm */
    private List targetList = null;

    /** The sorting termKey of the indexterm, default will be the term name */
    private String termKey = null;
    
    /** The sub indexterms contained by this indexterm */
    private List subTerms = null;

    /**
     * Constructor
     */
    public IndexTerm() {
        subTerms = new ArrayList(Constants.INT_1);
        targetList = new ArrayList(Constants.INT_1);
    }

    /**
     * Get the global locale of indexterm.
     * 
     * @return
     */
    public static Locale getTermLocale() {
        return termLocale;
    }

    /**
     * Set the global local of indexterm.
     * 
     * @param locale
     */
    public static void setTermLocale(Locale locale) {
        termLocale = locale;
    }

    /**
     * Get term name.
     * 
     * @return
     */
    public String getTermName() {
        return termName;
    }

    /**
     * Set term name.
     * 
     * @param name
     */
    public void setTermName(String name) {
        this.termName = name;
    }

    /**
     * Getter of termkey
	 * @return Returns the termKey.
	 */
	public String getTermKey() {
		return termKey;
	}

	/**
	 * Setter of termKey
	 * @param key The termKey to set.
	 */
	public void setTermKey(String key) {
		this.termKey = key;
	}

	/**
     * Get the sub term list.
     * 
     * @return
     */
    public List getSubTerms() {
        return subTerms;
    }

    /**
     * Add a sub term into the sub term list.
     * 
     * @param term
     */
    public void addSubTerm(IndexTerm term) {
        int i = 0;
        int subTermNum = subTerms.size();

        for (; i < subTermNum; i++) {
            IndexTerm subTerm = (IndexTerm) subTerms.get(i);

            if (subTerm.equals(term)) {
                return;
            }

            // Add targets when same term name and same term key
            if (subTerm.getTermName().equals(term.getTermName())
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
     * @param terms
     */
    public void addSubTerms(List terms) {
    	int subTermsNum = 0;
    	if (terms == null) {
    		return;
    	}
    	
    	subTermsNum = terms.size();
    	for (int i = 0; i < subTermsNum; i++) {
    		addSubTerm((IndexTerm) terms.get(i));
    	}
    }

    /**
     * IndexTerm will be equal if they have same name, target and subterms.
     * 
     * @param o
     */
    public boolean equals(Object o) {
        IndexTerm it = (IndexTerm) o;
        boolean eqTermName;
        boolean eqTermKey;
        boolean eqTargetList;
        boolean eqSubTerms;

        if (o == this) {
            return true;
        }

        if (!(o instanceof IndexTerm)) {
            return false;
        }
        

        eqTermName =  termName == it.getTermName() || termName != null && termName.equals(it.getTermName());
		eqTermKey =  termKey == it.getTermKey() || termKey != null && termKey.equals(it.getTermKey());
		eqTargetList = targetList == it.getTargetList() || targetList != null && targetList.equals(it.getTargetList());
		eqSubTerms =  subTerms == it.getSubTerms() || subTerms != null && subTerms.equals(it.getSubTerms());
		
		return eqTermName && eqTermKey && eqTargetList && eqSubTerms;
    }

    /**
     * Generate hash code for IndexTerm
     */
    public int hashCode() {
        int result = Constants.INT_17;

        result = Constants.INT_37 * result + termName.hashCode();
        result = Constants.INT_37 * result + termKey.hashCode();
        result = Constants.INT_37 * result + targetList.hashCode();
        result = Constants.INT_37 * result + subTerms.hashCode();

        return result;
    }

    /**
     * Sort all the subterms iteratively.
     */
    public void sortSubTerms() {
        int subTermNum = subTerms.size();

        if (subTerms != null && subTermNum > 0) {
            Collections.sort(subTerms);
            for (int i = 0; i < subTermNum; i++) {
                IndexTerm subTerm = (IndexTerm) subTerms.get(i);
                subTerm.sortSubTerms();
            }
        }
    }

    /**
     * Compare the given indexterm with current term.
     * 
     * @param obj
     */
    public int compareTo(Object obj) {
        return DITAOTCollator.getInstance(termLocale).compare(termKey,
                ((IndexTerm) obj).getTermKey());
    }

    /**
     * Get the target list of current indexterm.
     * 
     * @return Returns the targetList.
     */
    public List getTargetList() {
        return targetList;
    }

    /**
     * All a new target.
     * 
     * @param target
     */
    public void addTarget(IndexTermTarget target) {
        if (!targetList.contains(target)) {
            targetList.add(target);
        }
    }

    /**
     * All all the targets in the list.
     * 
     * @param targets
     */
    public void addTargets(List targets) {
    	int targetNum = 0;
    	
    	if (targets == null) {
    		return;
    	}
    	
        targetNum = targets.size();
        for (int i = 0; i < targetNum; i++) {
            addTarget((IndexTermTarget) targets.get(i));
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
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(Constants.INT_128);
		
		buffer.append("{Term name: ").append(termName);
		buffer.append(", Term key: ").append(termKey);
		buffer.append(", Target list: ");
		buffer.append(targetList.toString());		
		buffer.append(", Sub-terms: ");
		buffer.append(subTerms.toString());
		buffer.append("}");
				
		return buffer.toString();
	}
}
