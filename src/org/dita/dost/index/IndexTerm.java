/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.index;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dita.dost.util.Constants;

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

            // Add targets when same subTerms and same term name
            if (subTerm.getSubTerms().equals(term.getSubTerms())
                    && subTerm.getTermName().equals(term.getTermName())) {
                subTerm.addTargets(term.getTargetList());
                return;
            }
        }

        if (i == subTermNum) {
            subTerms.add(term);
        }
    }

    /**
     * IndexTerm will be equal if they have same name, target and subterms.
     * 
     * @param o
     */
    public boolean equals(Object o) {
        IndexTerm it = (IndexTerm) o;

        if (o == this) {
            return true;
        }

        if (!(o instanceof IndexTerm)) {
            return false;
        }

        return termName.equals(it.termName)
                && targetList.equals(it.getTargetList())
                && subTerms.equals(it.subTerms);
    }

    /**
     * Generate hash code for IndexTerm
     */
    public int hashCode() {
        int result = Constants.INT_17;

        result = Constants.INT_37 * result + termName.hashCode();
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
            Collections.sort(subTerms, Collator.getInstance(termLocale));
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
        return Collator.getInstance(termLocale).compare(termName,
                ((IndexTerm) obj).getTermName());
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
        int targetNum = targets.size();
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
}
