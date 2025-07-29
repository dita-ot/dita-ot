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

import java.util.*;
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
    var tokens = chunkTree.stream().flatMap(RoseTree::flatten).flatMap(Set::stream).collect(Collectors.toSet());
    // Only combine
    if (tokens.size() == 1 && tokens.contains(CHUNK_TO_CONTENT)) {
      return true;
    }
    // Only split
    if (tokens.size() == 1 && tokens.contains(CHUNK_BY_TOPIC)) {
      return true;
    }
    if (tokens.size() == 2 && tokens.contains(CHUNK_TO_CONTENT) && tokens.contains(CHUNK_BY_TOPIC)) {
      // No split inside combine
      for (RoseTree<Set<String>> tree : chunkTree) {
        if (findCombineWithSplitDescendant(tree)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public static boolean findCombineWithSplitDescendant(RoseTree<Set<String>> root) {
    final Deque<RoseTree<Set<String>>> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      final RoseTree<Set<String>> node = stack.pop();
      if (node.getValue().contains(CHUNK_TO_CONTENT)) {
        final Stack<RoseTree<Set<String>>> descendantStack = new Stack<>();
        descendantStack.push(node);
        while (!descendantStack.isEmpty()) {
          final RoseTree<Set<String>> descendant = descendantStack.pop();
          if (descendant.getValue().contains(CHUNK_BY_TOPIC)) {
            return true;
          }
          for (RoseTree<Set<String>> child : descendant.getChildren()) {
            descendantStack.push(child);
          }
        }
      }
      for (RoseTree<Set<String>> child : node.getChildren()) {
        stack.push(child);
      }
    }
    return false;
  }

  //  public static boolean hasSplitDescendant(RoseTreeNode node) {
  //    if (node.value == NodeType.SPLIT) {
  //      return true;
  //    }
  //    for (RoseTreeNode child : node.children) {
  //      if (hasSplitDescendant(child)) {
  //        return true;
  //      }
  //    }
  //    return false;
  //  }
  //
  //  public static boolean findCombineWithSplitDescendant(RoseTree<Set<String>> node) {
  //    if (node.getValue().equals(CHUNK_TO_CONTENT) && hasSplitDescendant(node)) {
  //      return true;
  //    }
  //    for (RoseTree<Set<String>> child : node.getChildren()) {
  //      if (findCombineWithSplitDescendant(child)) {
  //        return true;
  //      }
  //    }
  //    return false;
  //  }

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
