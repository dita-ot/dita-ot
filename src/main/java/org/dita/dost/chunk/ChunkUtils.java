/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import static org.dita.dost.reader.ChunkMapReader.CHUNK_BY_TOPIC;
import static org.dita.dost.reader.ChunkMapReader.CHUNK_TO_CONTENT;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CHUNK;
import static org.dita.dost.util.Constants.MAP_TOPICREF;
import static org.dita.dost.util.XMLUtils.getChildElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dita.dost.util.RoseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ChunkUtils {

  public static final Pattern WHITESPACE = Pattern.compile("\\s+");

  /**
   * Check if DITA 1.x map chunk operations can be rewritten to DITA 2.x.
   *
   * @param doc DITA 1.x map
   * @return {@code true} if map can be rewritten to DITA 2.x
   */
  public static boolean isCompatible(Document doc) {
    var chunkTree = getChunkTree(doc);
    // Has chunks
    if (chunkTree.isEmpty()) {
      return false;
    }
    // Only combine without nested chunks
    if (
      chunkTree
        .stream()
        .allMatch(node -> {
          var value = node.getValue();
          return value.size() == 1 && value.contains(CHUNK_TO_CONTENT) && node.getChildren().isEmpty();
        })
    ) {
      return true;
    }
    // Only split
    var tokens = chunkTree.stream().flatMap(RoseTree::flatten).flatMap(Set::stream).collect(Collectors.toSet());
    if (tokens.size() == 1 && tokens.contains(CHUNK_BY_TOPIC)) {
      return true;
    }
    return false;
  }

  private static List<RoseTree<Set<String>>> getChunkTree(Document doc) {
    List<RoseTree<Set<String>>> res = new ArrayList<>();
    collectChunkTreeTokens(doc.getDocumentElement(), res);
    return res;
  }

  private static void collectChunkTreeTokens(Element elem, List<RoseTree<Set<String>>> dst) {
    var chunkAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CHUNK);
    if (chunkAttr != null) {
      var chunkTokens = Stream
        .of(WHITESPACE.split(chunkAttr.getValue()))
        .filter(token -> !token.isBlank())
        .collect(Collectors.toSet());
      List<RoseTree<Set<String>>> res = new ArrayList<>();
      for (Element child : getChildElements(elem, MAP_TOPICREF)) {
        collectChunkTreeTokens(child, res);
      }
      var chunkTree = new RoseTree<>(chunkTokens, res);
      dst.add(chunkTree);
    } else {
      for (Element child : getChildElements(elem, MAP_TOPICREF)) {
        collectChunkTreeTokens(child, dst);
      }
    }
  }
}
