/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;

import java.util.HashMap;
import java.util.function.Function;


public class TreeWalker {
//    private Map<QName, Function<XdmNode, XdmNode>> handlers = new HashMap<>();
//
//    public void register(QName name, ElementHandler handler) {
//        if (handlers.containsKey(name)) {
//            throw new UnsupportedOperationException("Cannot have multiple handlers for the same element type");
//        }
//        handlers.put(name, handler);
//    }
//
//    public XdmNode walk(XdmNode node) throws XPathException {
//        final XdmDestination destination = new XdmDestination();
//        final PipelineConfiguration pipe = node.getUnderlyingNode().getConfiguration().makePipelineConfiguration();
//        final Receiver receiver = destination.getReceiver(pipe, new SerializationProperties());
//
//        receiver.open();
//        receiver.startDocument(0);
//        traverse(receiver, node);
//        receiver.endDocument();
//        receiver.close();
//
//        return destination.getXdmNode();
//    }
//
//    public void traverse(Receiver receiver, XdmNode node) throws XPathException {
//        XdmSequenceIterator<XdmNode> iter = null;
//
//        if (node.getNodeKind() == XdmNodeKind.DOCUMENT) {
//            iter = node.axisIterator(Axis.CHILD);
//            while (iter.hasNext()) {
//                final XdmNode item = iter.next();
//                if (item.getNodeKind() == XdmNodeKind.ELEMENT) {
//                    traverse(receiver, item);
//                } else {
//                    receiver.append(item.getUnderlyingNode());
//                }
//            }
//        } else if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
//            if (handlers.containsKey(node.getNodeName())) {
//                receiver.append(handlers.get(node.getNodeName()).process(node).getUnderlyingNode());
//            } else {
//                final NodeInfo inode = node.getUnderlyingNode();
//                final FingerprintedQName name = new FingerprintedQName(inode.getPrefix(), inode.getURI(), inode.getLocalPart());
//                receiver.startElement(name, inode.getSchemaType(), inode.attributes(), inode.getAllNamespaces(), inode.saveLocation(), 0);
//                iter = node.axisIterator(Axis.CHILD);
//                while (iter.hasNext()) {
//                    XdmNode item = iter.next();
//                    if (item.getNodeKind() == XdmNodeKind.ELEMENT) {
//                        traverse(receiver, item);
//                    } else {
//                        receiver.append(item.getUnderlyingNode());
//                    }
//                }
//                receiver.endElement();
//            }
//        } else {
//            receiver.append(node.getUnderlyingNode());
//        }
//    }
}
