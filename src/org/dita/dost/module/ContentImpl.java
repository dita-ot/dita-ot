/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Collection;

/**
 * @author Zhang, Yuan Peng
 */
public class ContentImpl implements Content {

    Collection collection;
    Object object;

    /**
     * 
     */
    public Collection getCollection() {
        return collection;
    }

    /**
     * 
     */
    public Object getObject() { 
        return object;
    }

    /**
     * 
     */
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    /**
     * 
     */
    public void setObject(Object object) {
        this.object = object;
    }
}
