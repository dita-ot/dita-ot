/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Collection;

/**
 * @author Zhang, Yuan Peng
 */
public interface Content {
    public Collection getCollection();

    public Object getObject();

    public void setCollection(Collection collection);

    public void setObject(Object object);
}
