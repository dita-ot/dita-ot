/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.util.Collection;
import org.apache.xerces.impl.Constants;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLFilterImpl;

public class CustomXMLReader extends XMLFilterImpl {

  private Collection<String> formats;
  private String processingMode;

  public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    switch (name) {
      case PROPERTY_FORMATS:
        formats = (Collection<String>) value;
        break;
      case PROPERTY_PROCESSING_MODE:
        processingMode =
          switch ((String) value) {
            case "skip" -> throw new SAXNotRecognizedException();
            case "strict" -> throw new SAXNotSupportedException();
            default -> (String) value;
          };
        break;
      case Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY:
        throw new SAXNotSupportedException();
      default:
        throw new IllegalArgumentException("%s=%s".formatted(name, value));
    }
  }

  public Collection<String> getFormats() {
    return formats;
  }

  public String getProcessingMode() {
    return processingMode;
  }
}
