/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.index;

/**
 * This class represent the topicref element in ditamap.
 *
 * @param href The href attribute of the topicref element.
 * @param format The format attribute of the topicref element.
 * @param navtitle the navtitle attribute of topicref element.
 * @since 1.0
 */
public record TopicrefElement(String href, String format, String navtitle) {}
