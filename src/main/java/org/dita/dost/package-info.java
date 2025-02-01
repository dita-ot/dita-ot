/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

/**
 * <h2 id="map-first-pre-processing">Map-first pre-processing</h2>
 *
 * <p>Map-first pre-processing at a high level is split into two stages, in
 * order:</p>
 *
 * <ol>
 * <li>Map processing (<code>preprocess2.maps</code>)</li>
 * <li>Topic processing (<code>preprocess2.topics</code>)</li>
 * </ol>
 *
 * <h3 id="map-processing">Map processing</h3>
 *
 * <p>Map processing is split into following stages, in order:</p>
 *
 * <ol>
 * <li>Read input maps and handle initial processing (<code>map-reader</code>)
 * <p>{@link org.dita.dost.module.reader.MapReaderModule MapReaderModule}</p>
 * <p>SAX</p>
 * </li>
 *
 * <li>Resolve map references (<code>map-mapref</code>)
 * <p>{@link org.dita.dost.module.MaprefModule MaprefModule} using XSLT</p>
 * <p>s9api</p>
 * </li>
 *
 * <li>Filter and profile maps (<code>map-reader-profile</code>)
 * <p>{@link org.dita.dost.module.ProfileModule ProfileModule}</p>
 * <p>SAX</p>
 * </li>
 *
 * <li>Process branch filters in maps (<code>map-branch-filter</code>)
 * <p>{@link org.dita.dost.module.filter.MapBranchFilterModule MapBranchFilterModule}</p>
 * <p>s9api, DOM</p>
 * </li>
 *
 * <li>Resolve keys in maps (<code>map-keyref</code>)
 * <p>{@link org.dita.dost.module.KeyrefModule KeyrefModule}</p>
 * <p>s9api, SAX</p>
 * </li>
 *
 * <li>Resolve push-conrefs in maps (<code>map-conrefpush</code>)
 * <p>{@link org.dita.dost.module.ConrefPushModule ConrefPushModule}</p>
 * <p>DOM</p>
 * </li>
 *
 * <li>Resolve conrefs in maps (<code>map-conref</code>)
 * <p>{@link org.dita.dost.module.XsltModule XsltModule}</p>
 * <p>s9api</p>
 * </li>
 *
 * <li>Lazy map filtering (<code>map-profile</code>)
 * <p>{@link org.dita.dost.module.ProfileModule ProfileModule}</p>
 * <p>SAX</p>
 * </li>
 * </ol>
 *
 * <h3 id="topic-processing">Topic processing</h3>
 *
 * <p>Topic processing is split into following stages, in order:</p>
 *
 * <ol>
 * <li>Initial topic parse and processing (<code>topic-reader</code>)
 * <p>{@link org.dita.dost.module.reader.TopicReaderModule TopicReaderModule}</p>
 * <p>SAX</p>
 * </li>
 *
 * <li>Filter branches (<code>topic-branch-filter</code>)
 * <p>{@link org.dita.dost.module.filter.TopicBranchFilterModule TopicBranchFilterModule}</p>
 * <p>s9api, DOM</p>
 * </li>
 *
 * <li>Rerun conref on maps to pull content from topics into maps (<code>topic-map-conref</code>)
 * <p>{@link org.dita.dost.module.XsltModule XsltModule}</p>
 * <p>s9api</p>
 * </li>
 *
 * <li>Resolve keyrefs in topics (<code>topic-keyref</code>)
 * <p>{@link org.dita.dost.module.KeyrefModule KeyrefModule}</p>
 * <p>s9api, SAX</p>
 * </li>
 *
 * <li>Handle copy-to for topics (<code>topic-copy-to</code>)
 * <p>{@link org.dita.dost.module.CopyToModule CopyToModule}</p>
 * <p>SAX</p>
 * </li>
 *
 * <li>Resolve push-conrefs in topics (<code>topic-conrefpush</code>)
 * <p>{@link org.dita.dost.module.ConrefPushModule ConrefPushModule}</p>
 * <p>SAX</p>
 * </li>
 *
 * <li>Resolve conrefs in topics (<code>topic-conref</code>)
 * <p>{@link org.dita.dost.module.XsltModule XsltModule}</p>
 * <p>s9api</p>
 * </li>
 *
 * <li>Lazy topic filtering (<code>topic-profile</code>)
 * <p>{@link org.dita.dost.module.FilterModule FilterModule}</p>
 * <p>DOM, SAX</p>
 * </li>
 *
 * <li>Process topic fragments links in topics (<code>preprocess2.topic-fragment</code>)
 * <p>{@link org.dita.dost.module.XmlFilterModule XmlFilterModule}</p>
 * <p>SAX</p>
 * </li>
 *
 * <li>Topic chunk processing (<code>topic-chunk</code>)
 * <p>{@link org.dita.dost.module.ChunkModule ChunkModule}</p>
 * <p>DOM</p>
 * </li>
 *
 * <li>Cascades metadata from maps to topics and then from topics to maps (<code>topic-move-meta-entries</code>)
 * <p>{@link org.dita.dost.module.MoveMetaModule MoveMetaModule}</p>
 * <p>DOM</p>
 * </li>
 *
 * <li>Find and generate related link information (<code>topic-maplink</code>)
 * <p>{@link org.dita.dost.module.MoveLinksModule MoveLinksModule} using XSLT</p>
 * <p>s9api</p>
 * </li>
 *
 * <li>Pull metadata for link and xref element (<code>topic-topicpull</code>)
 * <p>{@link org.dita.dost.module.XsltModule XsltModule}</p>
 * <p>s9api</p>
 * </li>
 * </ol>
 */
package org.dita.dost;
