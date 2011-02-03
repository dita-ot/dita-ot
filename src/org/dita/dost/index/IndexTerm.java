/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

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
 * This class represents an indexterm.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTerm implements Comparable {
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
    private static ArrayList<String> rtlLocaleList = null;
    
    /** 
     * The boolean to show whether current term is leaf term
     * leaf means the current indexterm element doesn't contains any subterms 
     * or only has "index-see" or "index-see-also" subterms.
     */
    private boolean leaf = true;
    
    //initialization for rtlLocaleList
    static{
    	rtlLocaleList = new ArrayList<String>(Constants.INT_2);
    	rtlLocaleList.add("ar_EG");
    	rtlLocaleList.add("he_IL");
    }
    
    /**
     * Constructor.
     */
    public IndexTerm() {
        subTerms = new ArrayList<IndexTerm>(Constants.INT_1);
        targetList = new ArrayList<IndexTermTarget>(Constants.INT_1);
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
    public static void setTermLocale(Locale locale) {
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
    public void setTermName(String name) {
        this.termName = name;
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
	public void setTermKey(String key) {
		this.termKey = key;
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
    public void setStartAttribute(String start){
    	this.start=start;
    }
    
    /**
     * Set the end attribute.
     * @param end attribute
     */
    
    public void setEndAttribute(String end){
    	this.end=end;
    }
    /**
     * Add a sub term into the sub term list.
     * 
     * @param term index term to be added
     */
    public void addSubTerm(IndexTerm term) {
        int i = 0;
        int subTermNum = subTerms.size();
        
        if (!Constants.IndexTerm_Prefix_See.equals(term.getTermPrefix()) && 
        		!Constants.IndexTerm_Prefix_See_Also.equals(term.getTermPrefix())){
        	//if the term is not "index-see" or "index-see-also"
        	leaf = false;
        }

        for (; i < subTermNum; i++) {
            IndexTerm subTerm = subTerms.get(i);

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
    public void addSubTerms(List<IndexTerm> terms) {
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
    public boolean equals(Object o) {
        if (!(o instanceof IndexTerm)) {
            return false;
        } else if (o == this) {
            return true;
        }
    	IndexTerm it = (IndexTerm) o;
        boolean eqTermName;
        boolean eqTermKey;
        boolean eqTargetList;
        boolean eqSubTerms;
        boolean eqTermPrefix;

        eqTermName =  termName == it.getTermName() || termName != null && termName.equals(it.getTermName());
        eqTermPrefix = termPrefix == it.getTermPrefix() || termPrefix != null && termPrefix.equals(it.getTermPrefix());
		eqTermKey =  termKey == it.getTermKey() || termKey != null && termKey.equals(it.getTermKey());
		eqTargetList = targetList == it.getTargetList() || targetList != null && targetList.equals(it.getTargetList());
		eqSubTerms =  subTerms == it.getSubTerms() || subTerms != null && subTerms.equals(it.getSubTerms());
		
		return eqTermName && eqTermKey && eqTargetList && eqSubTerms && eqTermPrefix;
    }

    /**
     * Generate hash code for IndexTerm.
     * @return hashcode
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
                IndexTerm subTerm = subTerms.get(i);
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
    public int compareTo(Object obj) {
        return DITAOTCollator.getInstance(termLocale).compare(termKey,
                ((IndexTerm) obj).getTermKey());
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
    public void addTarget(IndexTermTarget target) {
        if (!targetList.contains(target)) {
            targetList.add(target);
        }
    }

    /**
     * Add all the indexterm targets in the list.
     * 
     * @param targets list of targets
     */
    public void addTargets(List<IndexTermTarget> targets) {
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
	public String toString() {
		StringBuffer buffer = new StringBuffer(Constants.INT_128);
		
		buffer.append("{Term name: ").append(termName); //$NON-NLS-1$
		buffer.append(", Term key: ").append(termKey); //$NON-NLS-1$
		buffer.append(", Target list: "); //$NON-NLS-1$
		buffer.append(targetList.toString());		
		buffer.append(", Sub-terms: "); //$NON-NLS-1$
		buffer.append(subTerms.toString());
		buffer.append("}"); //$NON-NLS-1$
				
		return buffer.toString();
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
	public void setTermPrefix(String termPrefix) {
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
				return termPrefix + Constants.STRING_BLANK + termName;
			}else if (rtlLocaleList.contains(termLocale.toString())){
				return termName + Constants.STRING_BLANK
				    + Messages.getString("IndexTerm." + termPrefix.toLowerCase().trim().replace(' ', '-'),
				    		termLocale);
			}else {
				return Messages.getString("IndexTerm." + termPrefix.toLowerCase().trim().replace(' ', '-'),
						termLocale)
				    + Constants.STRING_BLANK + termName;
			}
		}
	}
	
    /**
     * Update the sub-term prefix from "See also" to "See" if there is only one sub-term.
     */
	public void updateSubTerm(){
		if (subTerms.size()==1){
			// if there is only one subterm, it is necessary to update
			IndexTerm term = subTerms.get(0); // get the only subterm
			if (term.getTermPrefix()!= null &&
					Constants.IndexTerm_Prefix_See.equalsIgnoreCase(term.getTermPrefix().trim())){ //$NON-NLS-1$
				//if the only subterm is index-see update it to index-see-also
				term.setTermPrefix(Constants.IndexTerm_Prefix_See_Also); //$NON-NLS-1$
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
