/**
 * Processes chunking in maps.
 *
 * <p>The chunking process is as follows:</p>
 *
 * <ol>
 *     <li>Read map into DOM document</li>
 *     <li>Collect all {@code to-content} chunks</li>
 *     <li>Collect all {@code by-topic} chunks</li>
 *     <li>Process all {@code to-content} chunks, merging into file name {@code *.tmp} so we don’t override original content</li>
 *     <li>Process all {@code by-topic} chunks, splitting into target file names</li>
 *     <li>Move all {@code *.tmp} files into final target files</li>
 *     <li>Delete all files that are part of {@code to-content} chunk and are not referenced by any other topicref</li>
 * </ol>
 *
 * @since 3.5
 */
package org.dita.dost.chunk;