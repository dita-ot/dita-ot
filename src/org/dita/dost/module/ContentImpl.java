/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Collection;

/**
 * ContentImpl is the implementation of content container. It contains Collection and
 * Object and can be used to exchange different types of data between module, reader 
 * and writer instances.
 * 
 * @author Zhang, Yuan Peng
 */
public class ContentImpl implements Content {

    /**
     * Automatically generated constructor: ContentImpl
     */
    public ContentImpl() {
    	collection = null;
    	object = null;
    }

    private Collection collection;
    private Object object;


    /**
     * @see org.dita.dost.module.Content#getCollection()
     * 
     */
    public Collection getCollection() {
        return collection;
    }


    /**
     * @see org.dita.dost.module.Content#getValue()
     * 
     */
    public Object getValue() { 
        return object;
    }


    /**
     * 
     * 
     */
    public void setCollection(Collection collection) {
        this.collection = collection;
    }


    /**
     * 
     * 
     */
    public void setValue(Object object) {
        this.object = object;
    }
}
