/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.reader;

import java.util.Collection;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLFilterImpl;

public class CustomXMLReader extends XMLFilterImpl {

  private Collection<String> formats;
  private String processingMode;

  public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    switch (name) {
      case "https://dita-ot.org/property/formats":
        formats = (Collection<String>) value;
        break;
      case "https://dita-ot.org/property/processing-mode":
        processingMode =
          switch ((String) value) {
            case "skip" -> throw new SAXNotRecognizedException();
            case "strict" -> throw new SAXNotSupportedException();
            default -> (String) value;
          };
        break;
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
