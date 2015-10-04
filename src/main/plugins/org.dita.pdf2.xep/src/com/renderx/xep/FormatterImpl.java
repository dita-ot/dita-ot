// Dummy class to allow compiling against RenderX

package com.renderx.xep;

import com.renderx.xep.lib.ConfigurationException;
import com.renderx.xep.lib.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import java.io.IOException;

public class FormatterImpl implements Formatter {
    public FormatterImpl() throws ConfigurationException {
        throw new UnsupportedOperationException();
    }

    public void cleanup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void render(final Source source, final FOTarget foTarget) throws SAXException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void render(Source var1, FOTarget var2, Logger var3) throws SAXException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentHandler createContentHandler(final String s, final FOTarget foTarget) throws ConfigurationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentHandler createContentHandler(final String s, final FOTarget foTarget, final Logger logger) throws SAXException, IOException {
        throw new UnsupportedOperationException();
    }
}
