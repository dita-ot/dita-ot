package org.dita.dost.writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static javax.xml.XMLConstants.*;
import javax.xml.namespace.QName;
import java.util.ArrayDeque;
import java.util.Deque;
import static org.dita.dost.util.XMLUtils.*;
import static org.dita.dost.util.Constants.*;

public class NameGeneralizationFilter extends AbstractXMLFilter {

    final Deque<QName> nameStack = new ArrayDeque<>();

    @Override
    public void startElement(final String uri, final String localName, final String name,
                             final Attributes atts) throws SAXException {
        final QName newName = getGeneralizedName(uri, localName, name, atts);
        nameStack.push(newName);
        super.startElement(newName.getNamespaceURI(), newName.getLocalPart(), getName(newName), atts);
    }

    private QName getGeneralizedName(final String uri, final String localName, final String name,
                                     final Attributes atts) {
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (uri.isEmpty() && cls != null) {
            final String baseType = getBaseName(cls);
            final QName newName = new QName(uri, baseType, DEFAULT_NS_PREFIX);
            return newName;
        }
        return new QName(uri, localName, getPrefix(name));
    }

    private String getBaseName(final String cls) {
        final int sep = cls.indexOf('/');
        final int delim = cls.indexOf(' ', sep);
        return cls.substring(sep + 1, delim);
    }

    private String getName(final QName qname) {
        return qname.getPrefix().isEmpty() ? qname.getLocalPart() : (qname.getPrefix() + ":" + qname.getLocalPart());
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {
        final QName newName = nameStack.pop();
        super.endElement(newName.getNamespaceURI(), newName.getLocalPart(), getName(newName));
    }

}
