/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;

import org.dita.dost.module.filter.SubjectScheme;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

/**
 * Utility class used for flagging and filtering.
 *
 * @author Wu, Zhi Qiang
 */
public final class FilterUtils {

    /** Subject scheme file extension */
    public static final String SUBJECT_SCHEME_EXTENSION = ".subm";
    public static final FilterKey DEFAULT = new FilterKey(QName.valueOf(DEFAULT_ACTION), null);

    private static final String FLAG_STYLE_PREFIX = "flag__style--";

    private DITAOTLogger logger;
    /** Actions for filter keys. */
    private final Map<FilterKey, Action> filterMap;
    /** Set of filter keys for which an error has already been thrown. */
    private final Set<FilterKey> notMappingRules = new HashSet<>();
    private boolean logMissingAction;
    private final String foregroundConflictColor;
    private final String backgroundConflictColor;
    private Set<QName> filterAttributes;
    private Set<QName> flagAttributes;

    public FilterUtils(final Map<FilterKey, Action> filterMap, String foregroundConflictColor,
                       String backgroundConflictColor) {
        this.logMissingAction = !filterMap.isEmpty();
        this.filterMap = new HashMap<>(filterMap);
        this.foregroundConflictColor = foregroundConflictColor;
        this.backgroundConflictColor = backgroundConflictColor;
        filterAttributes = getProfileAttributes(Configuration.configuration.get("filter-attributes"));
        flagAttributes = getFlaggingAttributes(Configuration.configuration.get("flag-attributes"));
    }

    /**
     * Conveninence constructur that only handles print filtering.
     *
     * @param isPrintType transformation output is print-oriented
     */
    public FilterUtils(final boolean isPrintType) {
        this(isPrintType, emptyMap(), null, null);
    }

    /**
     * Construct filter utility.
     *
     * @param isPrintType transformation output is print-oriented
     */
    public FilterUtils(final boolean isPrintType, final Map<FilterKey, Action> filterMap,
                       String foregroundConflictColor, String backgroundConflictColor) {
        final Map<FilterKey, Action> dfm = new HashMap<>();
        dfm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_PRINT), ATTR_PRINT_VALUE_YES), Action.INCLUDE);
        if (isPrintType) {
            dfm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_PRINT), ATTR_PRINT_VALUE_PRINT_ONLY), Action.INCLUDE);
            dfm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_PRINT), ATTR_PRINT_VALUE_NO), Action.EXCLUDE);
        } else {
            dfm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_PRINT), ATTR_PRINT_VALUE_PRINT_ONLY), Action.EXCLUDE);
            dfm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_PRINT), ATTR_PRINT_VALUE_NO), Action.INCLUDE);
        }
        dfm.put(new FilterKey(QName.valueOf(ATTRIBUTE_NAME_PRINT), null), Action.INCLUDE);
        dfm.putAll(filterMap);
        this.logMissingAction = !filterMap.isEmpty();
        this.filterMap = dfm;
        this.foregroundConflictColor = foregroundConflictColor;
        this.backgroundConflictColor = backgroundConflictColor;
        filterAttributes = getProfileAttributes(Configuration.configuration.get("filter-attributes"));
        flagAttributes = getFlaggingAttributes(Configuration.configuration.get("flag-attributes"));
    }

    @VisibleForTesting
    public FilterUtils(boolean isPrintType, Map<FilterKey, Action> filterMap,
                       String foregroundConflictColor, String backgroundConflictColor,
                       Set<QName> filterAttributes, Set<QName> flagAttributes) {
        this(isPrintType, filterMap, foregroundConflictColor, backgroundConflictColor);
        this.filterAttributes = Sets.union(this.filterAttributes, filterAttributes);
        this.flagAttributes = Sets.union(this.flagAttributes, flagAttributes);
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public String toString() {
        return filterMap.toString();
    }

    private static Set<QName> getProfileAttributes(final String conf) {
        final ImmutableSet.Builder<QName> res = ImmutableSet.<QName>builder()
                .add(QName.valueOf(ATTRIBUTE_NAME_AUDIENCE),
                        QName.valueOf(ATTRIBUTE_NAME_PLATFORM),
                        QName.valueOf(ATTRIBUTE_NAME_PRODUCT),
                        QName.valueOf(ATTRIBUTE_NAME_OTHERPROPS),
                        QName.valueOf(ATTRIBUTE_NAME_PROPS),
                        QName.valueOf(ATTRIBUTE_NAME_PRINT),
                        QName.valueOf(ATTRIBUTE_NAME_DELIVERYTARGET));
        if (conf != null) {
            Stream.of(conf.trim().split("\\s*,\\s*"))
                    .map(QName::valueOf)
                    .forEach(res::add);
        }
        return res.build();
    }

    private static Set<QName> getFlaggingAttributes(final String conf) {
        final ImmutableSet.Builder<QName> res = ImmutableSet.<QName>builder()
                .add(QName.valueOf(ATTRIBUTE_NAME_AUDIENCE),
                        QName.valueOf(ATTRIBUTE_NAME_PLATFORM),
                        QName.valueOf(ATTRIBUTE_NAME_PRODUCT),
                        QName.valueOf(ATTRIBUTE_NAME_OTHERPROPS),
                        QName.valueOf(ATTRIBUTE_NAME_PROPS),
                        QName.valueOf(ATTRIBUTE_NAME_PRINT),
                        QName.valueOf(ATTRIBUTE_NAME_DELIVERYTARGET),
                        QName.valueOf(ATTRIBUTE_NAME_REV)
        );
        if (conf != null) {
            Stream.of(conf.trim().split("\\s*,\\s*"))
                    .map(QName::valueOf)
                    .forEach(res::add);
        }
        return res.build();
    }

    public Set<Flag> getFlags(final Attributes atts, final QName[][] extProps) {
        if (filterMap.isEmpty()) {
            return emptySet();
        }

        final Set<Flag> res = new HashSet<>();
        for (final QName attr: flagAttributes) {
            final String value = atts.getValue(attr.getNamespaceURI(), attr.getLocalPart());
            if (value != null) {
                final Map<QName, List<String>> groups = getGroups(value);
                for (Map.Entry<QName, List<String>> group: groups.entrySet()) {
                    final QName[] propList =
                            group.getKey() != null
                                    ? new QName[]{attr, group.getKey()}
                                    : new QName[]{attr};
                    res.addAll(extCheckFlag(propList, group.getValue()));
                }
            }
        }
        if (res.isEmpty()) {
            if (extProps != null && extProps.length != 0) {
                for (final QName[] propList : extProps) {
                    int propListIndex = propList.length - 1;
                    final QName propName = propList[propListIndex];
                    String propValue = atts.getValue(propName.getNamespaceURI(), propName.getLocalPart());

                    while ((propValue == null || propValue.trim().isEmpty()) && propListIndex > 0) {
                        propListIndex--;
                        final QName current = propList[propListIndex];
                        propValue = getLabelValue(propName, atts.getValue(current.getNamespaceURI(), current.getLocalPart()));
                    }
                    if (propValue != null) {
                        res.addAll(extCheckFlag(propList, Arrays.asList(propValue.split("\\s+"))));
                    }
                }
            }
        }

        return checkConflict(res);
    }

    private Set<Flag> checkConflict(Set<Flag> res) {
        if (foregroundConflictColor == null && backgroundConflictColor == null) {
            return res;
        }
        final Set<String> color = new HashSet<>();
        final Set<String> backcolor = new HashSet<>();
        for (Flag f : res) {
            if (f.color != null) {
                color.add(f.color);
            }
            if (f.backcolor != null) {
                backcolor.add(f.backcolor);
            }
        }
        final boolean conflictColor = color.size() > 1;
        final boolean conflictBackcolor = backcolor.size() > 1;
        if ((conflictColor && foregroundConflictColor != null)
                || (conflictBackcolor && backgroundConflictColor != null)) {
            return res.stream()
                    .map(f -> new Flag(ELEMENT_NAME_PROP, conflictColor ? foregroundConflictColor : f.color,
                            conflictBackcolor ? backgroundConflictColor : f.backcolor,
                            f.style, f.changebar, f.startflag, f.endflag, f.outputClass))
                    .collect(Collectors.toSet());
        } else {
            return res;
        }
    }

    public Set<Flag> getFlags(final Element element, final QName[][] props) {
        final XMLUtils.AttributesBuilder buf = new XMLUtils.AttributesBuilder();
        final NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength() ; i++) {
            final Node attr = attrs.item(i);
            if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
                buf.add((Attr) attr);
            }
        }
        return getFlags(buf.build(), props);
    }

    /**
     * @param propList attribute group names, from most common to most specific
     * @param attValue attribute group values
     */
    @VisibleForTesting
    List<Flag> extCheckFlag(final QName[] propList, final List<String> attValue) {
        final List<Flag> res = new ArrayList<>();
        for (final QName attName : propList) {
            for (final String attSubValue : attValue) {
                Action filterAction = filterMap.get(new FilterKey(attName, attSubValue));
                if (filterAction == null) {
                    filterAction = filterMap.get(new FilterKey(attName, null));
                }
                if (filterAction instanceof Flag) {
                    res.add((Flag) filterAction);
                }
            }
        }

        return res;
    }

    /**
     * Test if element should be excluded based on filter.
     */
    public boolean needExclude(final Element element, final QName[][] props) {
        final XMLUtils.AttributesBuilder buf = new XMLUtils.AttributesBuilder();
        final NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength() ; i++) {
            final Node attr = attrs.item(i);
            if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
                buf.add((Attr) attr);
            }
        }
        return needExclude(buf.build(), props);
    }

    /**
     * Check if the given Attributes need to be excluded.
     *
     * @param atts attributes
     * @param extProps {@code props} attribute specializations
     * @return {@code true} if any profiling attribute was excluded, otherwise {@code false}
     */
    public boolean needExclude(final Attributes atts, final QName[][] extProps) {
        if (filterMap.isEmpty()) {
            return false;
        }

        for (final QName attr: filterAttributes) {
            final String value = atts.getValue(attr.getNamespaceURI(), attr.getLocalPart());
            if (value != null) {
                final Map<QName, List<String>> groups = getGroups(value);
                for (Map.Entry<QName, List<String>> group: groups.entrySet()) {
                    final QName[] propList =
                            group.getKey() != null
                                    ? new QName[]{attr, group.getKey()}
                                    : new QName[]{attr};
                    if (extCheckExclude(propList, group.getValue())) {
                        return true;
                    }
                }
            }
        }

        if (extProps != null && extProps.length != 0) {
            for (final QName[] propList: extProps) {
                int propListIndex = propList.length - 1;
                final QName propName = propList[propListIndex];
                String propValue = atts.getValue(propName.getNamespaceURI(), propName.getLocalPart());

                while ((propValue == null || propValue.trim().isEmpty()) && propListIndex > 0) {
                    propListIndex--;
                    final QName current = propList[propListIndex];
                    propValue = getLabelValue(propName, atts.getValue(current.getNamespaceURI(), current.getLocalPart()));
                }
                if (propValue != null && extCheckExclude(propList, Arrays.asList(propValue.split("\\s+")))) {
                    return true;
                }
            }
        }
        return false;
    }

    private final Pattern groupPattern = Pattern.compile("(\\w+)\\((.*?)\\)");

    /**
     * Parse groups
     *
     * @param value profiling attribute value
     * @return map of groups names to group values, ungrouped values have {@code null} name
     */
    @VisibleForTesting
    Map<QName, List<String>> getGroups(final String value) {
        final Map<QName, List<String>> res = new HashMap<>();

        final StringBuilder buf = new StringBuilder();
        int previousEnd = 0;
        final Matcher m = groupPattern.matcher(value);
        while(m.find()) {
            buf.append(value.subSequence(previousEnd, m.start()));
            final String v = m.group(2);
            if (!v.trim().isEmpty()) {
                final QName k = QName.valueOf(m.group(1));
                if (res.containsKey(k)) {
                    final List<String> l = new ArrayList<>(res.get(k));
                    l.addAll(Arrays.asList(v.trim().split("\\s+")));
                    res.put(k, l);
                } else {
                    res.put(k, Arrays.asList(v.trim().split("\\s+")));
                }
            }
            previousEnd = m.end();
        }
        buf.append(value.substring(previousEnd));
        if (!buf.toString().trim().isEmpty()) {
            res.put(null, Arrays.asList(buf.toString().trim().split("\\s+")));
        }
        return res;
    }

    /**
     * Get labelled props value.
     *
     * @param propName attribute name
     * @param attrPropsValue attribute value
     * @return props value, {@code null} if not available
     */
    private String getLabelValue(final QName propName, final String attrPropsValue) {
        if (attrPropsValue != null) {
            int propStart = -1;
            if (attrPropsValue.startsWith(propName + "(") || attrPropsValue.contains(" " + propName + "(")) {
                propStart = attrPropsValue.indexOf(propName + "(");
            }
            if (propStart != -1) {
                propStart = propStart + propName.toString().length() + 1;
            }
            final int propEnd = attrPropsValue.indexOf(")", propStart);
            if (propStart != -1 && propEnd != -1) {
                return attrPropsValue.substring(propStart, propEnd).trim();
            }
        }
        return null;
    }

    /**
     * Check the given extended attribute in propList to see if it was excluded.
     *
     * @param propList attribute group names, from most common to most specific
     * @param attValue attribute group values
     * @return {@code true} if should be excluded, otherwise {@code false}
     */
    @VisibleForTesting
    boolean extCheckExclude(final QName[] propList, final List<String> attValue) {
        for (int propListIndex = propList.length - 1; propListIndex >= 0; propListIndex--) {
            final QName attName = propList[propListIndex];
            checkRuleMapping(attName, attValue);
            boolean hasNonExcludeAction = false;
            boolean hasExcludeAction = false;
            for (final String attSubValue: attValue) {
                final FilterKey filterKey = new FilterKey(attName, attSubValue);
                final Action filterAction = filterMap.get(filterKey);
                // no action will be considered as 'not exclude'
                if (filterAction == null) {
                    // check Specified DefaultAction mapping this attribute's name
                    final Action defaultAction = filterMap.get(new FilterKey(attName, null));
                    if (defaultAction != null) {
                        if (defaultAction instanceof Exclude) {
                            hasExcludeAction = true;
                        } else {
                            return false;
                        }
                    } else {
                        if (hasExcludeAction) {
                            if (!isDefaultExclude()) {
                                return false;
                            }
                        } else {
                            hasNonExcludeAction = true;
                        }
                    }
                } else if (filterAction instanceof Exclude) {
                    hasExcludeAction = true;
                    if (hasNonExcludeAction) {
                        if (isDefaultExclude()) {
                            hasNonExcludeAction = false;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            // if there is exclude action but not all value should be excluded
            if (hasNonExcludeAction) {
                // under the condition of default action also not exist or not excluded
                if (0 == propListIndex) {
                    // the ancient parent on the top level
                    return isDefaultExclude();
                }
            // if all of the value should be excluded
            } else if (hasExcludeAction) {
                return true;
            }
            // If no action for this extended prop has been found, we need to check the parent prop action
        }

        return false;
    }

    private boolean isDefaultExclude() {
        final Action defaultAction = filterMap.get(DEFAULT);
        return defaultAction != null && defaultAction instanceof Exclude;
    }

    /**
     * Check if attribute value has mapping in filter configuration and throw messages.
     * @param attName attribute name
     * @param attValue attribute value
     */
    private void checkRuleMapping(final QName attName, final List<String> attValue) {
        if (attValue == null || attValue.isEmpty()) {
            return;
        }
        for (final String attSubValue: attValue) {
            final FilterKey filterKey = new FilterKey(attName, attSubValue);
            final Action filterAction = filterMap.get(filterKey);
            if (filterAction == null && logMissingAction) {
                if (!alreadyShowed(filterKey)) {
                    logger.info(MessageUtils.getMessage("DOTJ031I", filterKey.toString()).toString());
                }
            }
        }
    }

    private boolean alreadyShowed(final FilterKey notMappingKey) {
        if (!notMappingRules.contains(notMappingKey)) {
            notMappingRules.add(notMappingKey);
            return false;
        }
        return true;
    }

    /**
     * Filter key object.
     *
     * @since 1.6
     */
    public static class FilterKey {
        /** Attribute name */
        public final QName attribute;
        /** Attribute value, may be {@code null} */
        public final String value;

        public FilterKey(final QName attribute, final String value) {
            if (attribute == null) {
                throw new IllegalArgumentException("Attribute may not be null");
            }
            this.attribute = attribute;
            this.value = value;
        }

        @Override
        public String toString() {
            return value != null ? attribute.toString() + EQUAL + value : attribute.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (attribute.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof FilterKey)) {
                final Throwable t = new RuntimeException("Not comparing FilterKey");
                t.printStackTrace();
                return false;
            }
            final FilterKey other = (FilterKey) obj;
            if (!attribute.equals(other.attribute)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }
    }

    // Subject scheme support

    /**
     * Refine filter with subject scheme.
     *
     * @param bindingMap subject scheme bindings
     * @return new filter with subject scheme information
     */
    public FilterUtils refine(final SubjectScheme bindingMap) {
        return refine(bindingMap.subjectSchemeMap);
    }

    /**
     * Refine filter with subject scheme.
     *
     * @param bindingMap subject scheme bindings, {@code Map<AttName, Map<ElemName, Set<Element>>>}
     * @return new filter with subject scheme information
     */
    private FilterUtils refine(final Map<QName, Map<String, Set<Element>>> bindingMap) {
        if (bindingMap != null && !bindingMap.isEmpty()) {
            final Map<FilterKey, Action> buf = new HashMap<>(filterMap);
            for (final Map.Entry<FilterKey, Action> e: filterMap.entrySet()) {
                refineAction(e.getValue(), e.getKey(), bindingMap, buf);
            }
            final FilterUtils filterUtils = new FilterUtils(buf, foregroundConflictColor, backgroundConflictColor);
            filterUtils.setLogger(logger);
            filterUtils.logMissingAction = logMissingAction;
            return filterUtils;
        } else {
            return this;
        }
    }
    /**
     * Refine action key with information from subject schemes.
     */
    private void refineAction(final Action action, final FilterKey key, final Map<QName, Map<String, Set<Element>>> bindingMap,
                              final Map<FilterKey, Action> destFilterMap) {
        if (key.value != null) {
            final Map<String, Set<Element>> schemeMap = bindingMap.get(key.attribute);
            if (schemeMap != null && !schemeMap.isEmpty()) {
                for (final Set<Element> submap: schemeMap.values()) {
                    for (final Element e: submap) {
                        final Element subRoot = searchForKey(e, key.value);
                        if (subRoot != null) {
                            insertAction(subRoot, key.attribute, action, destFilterMap);
                        }
                    }
                }
            }
        }
    }
    /**
     * Search subject scheme elements for a given key
     * @param root subject scheme element tree to search through
     * @param keyValue key to locate
     * @return element that matches the key, otherwise {@code null}
     */
    private Element searchForKey(final Element root, final String keyValue) {
        if (root == null || keyValue == null) {
            return null;
        }
        final LinkedList<Element> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            final Element node = queue.removeFirst();
            final NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    queue.add((Element)children.item(i));
                }
            }
            if (SUBJECTSCHEME_SUBJECTDEF.matches(node)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (keyValue.equals(key)) {
                    return node;
                }
            }
        }
        return null;
    }
    /**
     * Insert subject scheme based action into filetermap if key not present in the map
     *
     * @param subTree subject scheme definition element
     * @param attName attribute name
     * @param action action to insert
     */
    private void insertAction(final Element subTree, final QName attName, final Action action, final Map<FilterKey, Action> destFilterMap) {
        if (subTree == null || action == null) {
            return;
        }

        final LinkedList<Element> queue = new LinkedList<>();

        // Skip the sub-tree root because it has been added already.
        NodeList children = subTree.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                queue.offer((Element)children.item(i));
            }
        }

        while (!queue.isEmpty()) {
            final Element node = queue.poll();
            children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element)children.item(i));
                }
            }
            if (SUBJECTSCHEME_SUBJECTDEF.matches(node)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (key != null && !key.trim().isEmpty()) {
                    final FilterKey k = new FilterKey(attName, key);
                    if (!destFilterMap.containsKey(k)) {
                        destFilterMap.put(k, action);
                    }
                }
            }
        }
    }


    public interface Action {
        Action INCLUDE = new Include();
        Action EXCLUDE = new Exclude();
        Action PASSTHROUGH = new Passthrough();
    }

    public static class Include implements Action {
        @Override
        public String toString() {
            return "include";
        }
    }

    public static class Exclude implements Action {
        @Override
        public String toString() {
            return "exclude";
        }
    }

    public static class Passthrough implements Action {
        @Override
        public String toString() {
            return "passthrough";
        }
    }

    public static class Flag implements Action {

        public final String proptype;
        public final String color;
        public final String backcolor;
        public final String[] style;
        public final String changebar;
        public final FlagImage startflag;
        public final FlagImage endflag;
        public final String outputClass;

        public Flag(String proptype, String color, String backcolor, String[] style, String changebar,
                FlagImage startflag, FlagImage endflag, String outputClass) {
            this.proptype = proptype;
            this.color = color;
            this.backcolor = backcolor;
            this.style = style;
            this.changebar = changebar;
            this.startflag = startflag;
            this.endflag = endflag;
            this.outputClass = outputClass;
        }

        public Flag adjustPath(final URI currentFile, final Job job) {
            return new Flag(proptype, color, backcolor, style, changebar,
                    adjustPath(startflag, currentFile, job),
                    adjustPath(endflag, currentFile, job),
                    outputClass);
        }

        private FlagImage adjustPath(final FlagImage img, final URI currentFile, final Job job) {
            if (img == null) {
                return img;
            }
            final URI rel;
            final Job.FileInfo flagFi = job.getFileInfo(img.href);
            if (flagFi != null) {
                final Job.FileInfo current = job.getFileInfo(currentFile);
                final URI flag = job.tempDirURI.resolve(flagFi.uri);
                final URI curr = job.tempDirURI.resolve(current.uri);
                rel = URLUtils.getRelativePath(curr, flag);
            } else {
                rel = img.href;
            }
            return new FlagImage(rel, img.alt);
        }

        public void writeStartFlag(final ContentHandler contentHandler) throws SAXException {
            final StringJoiner outputClassAttr = new StringJoiner(" ");
            final StringBuilder styleAttr = new StringBuilder();
            if (color != null) {
                styleAttr.append("color:").append(color).append(";");
            }
            if (backcolor != null) {
                styleAttr.append("background-color:").append(backcolor).append(";");
            }
            if (outputClass != null) {
                outputClassAttr.add(outputClass);
            }
            if (style != null) {
                for (final String style : style) {
                    outputClassAttr.add(FLAG_STYLE_PREFIX + style);
                }
            }

            final XMLUtils.AttributesBuilder atts = new XMLUtils.AttributesBuilder()
                    .add(ATTRIBUTE_NAME_CLASS, DITA_OT_D_DITAVAL_STARTPROP.toString());
            if (outputClassAttr.length() != 0) {
                atts.add(ATTRIBUTE_NAME_OUTPUTCLASS, outputClassAttr.toString());
            }
            if (styleAttr.length() != 0) {
                atts.add(ATTRIBUTE_NAME_STYLE, styleAttr.toString());
            }
            contentHandler.startElement(NULL_NS_URI, DITA_OT_D_DITAVAL_STARTPROP.localName, DITA_OT_D_DITAVAL_STARTPROP.localName,
                    atts.build());
            writeProp(contentHandler, true);
            contentHandler.endElement(NULL_NS_URI, DITA_OT_D_DITAVAL_STARTPROP.localName, DITA_OT_D_DITAVAL_STARTPROP.localName);
        }

        public void writeEndFlag(final ContentHandler contentHandler) throws SAXException {
            contentHandler.startElement(NULL_NS_URI, DITA_OT_D_DITAVAL_ENDPROP.localName, DITA_OT_D_DITAVAL_ENDPROP.localName,
                    new XMLUtils.AttributesBuilder()
                            .add(ATTRIBUTE_NAME_CLASS, DITA_OT_D_DITAVAL_ENDPROP.toString())
                            .build());
            writeProp(contentHandler, false);
            contentHandler.endElement(NULL_NS_URI, DITA_OT_D_DITAVAL_ENDPROP.localName, DITA_OT_D_DITAVAL_ENDPROP.localName);
        }

        private void writeProp(final ContentHandler contentHandler, final boolean isStart) throws SAXException {
            final XMLUtils.AttributesBuilder propAtts = new XMLUtils.AttributesBuilder().add("action", "flag");
            if (color != null) {
                propAtts.add("color", color);
            }
            if (backcolor != null) {
                propAtts.add("backcolor", backcolor);
            }
            if (style != null) {
                propAtts.add("style", Stream.of(style).collect(Collectors.joining(" ")));
            }
            if (outputClass != null) {
                propAtts.add("outputclass", outputClass);
            }
            if (changebar != null) {
                propAtts.add("changebar", changebar);
            }
            contentHandler.startElement(NULL_NS_URI, proptype, proptype, propAtts.build());
            if (isStart && startflag != null) {
                startflag.writeFlag(contentHandler, "startflag");
            }
            if (!isStart && endflag != null) {
                endflag.writeFlag(contentHandler, "endflag");
            }
            contentHandler.endElement(NULL_NS_URI, proptype, proptype);
        }

        public Element getStartFlag() {
            return writeToElement((ContentHandler contentHandler) -> {
                try {
                    writeStartFlag(contentHandler);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public Element getEndFlag() {
            return writeToElement((ContentHandler contentHandler) -> {
                try {
                    writeEndFlag(contentHandler);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private static Element writeToElement(final Consumer<ContentHandler> writer) {
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer;
            try {
                transformer = factory.newTransformer();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }
            final SAXSource xmlSource = new SAXSource(new XMLReader() {
                @Override
                public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
                    return false;
                }
                @Override
                public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
                }
                @Override
                public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
                    return null;
                }
                @Override
                public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
                }
                @Override
                public void setEntityResolver(EntityResolver resolver) {
                }
                @Override
                public EntityResolver getEntityResolver() {
                    return null;
                }
                @Override
                public void setDTDHandler(DTDHandler handler) {
                }
                @Override
                public DTDHandler getDTDHandler() {
                    return null;
                }
                private ContentHandler contentHandler;
                @Override
                public void setContentHandler(ContentHandler handler) {
                    this.contentHandler = handler;
                }
                @Override
                public ContentHandler getContentHandler() {
                    return contentHandler;
                }
                @Override
                public void setErrorHandler(ErrorHandler handler) {
                }
                @Override
                public ErrorHandler getErrorHandler() {
                    return null;
                }
                @Override
                public void parse(InputSource input) throws IOException, SAXException {
                    parse((String) null);
                }
                @Override
                public void parse(String input) throws IOException, SAXException {
                    getContentHandler().startDocument();
                    writer.accept(getContentHandler());
                    getContentHandler().endDocument();
                }
            }, null);
            final DOMResult outputTarget = new DOMResult();
            try {
                transformer.transform(xmlSource, outputTarget);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
            return ((Document) outputTarget.getNode()).getDocumentElement();
        }

        @Override
        public String toString() {
            return "Flag{" +
                    "color='" + color + '\'' +
                    ", backcolor='" + backcolor + '\'' +
                    ", style=" + Arrays.toString(style) +
                    ", outputclass=" + outputClass +
                    ", changebar='" + changebar + '\'' +
                    ", startflag=" + startflag +
                    ", endflag=" + endflag +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Flag flag = (Flag) o;

            if (!Objects.equals(color, flag.color)) return false;
            if (!Objects.equals(backcolor, flag.backcolor)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(style, flag.style)) return false;
            if (!Objects.equals(outputClass, flag.outputClass)) return false;
            if (!Objects.equals(changebar, flag.changebar)) return false;
            if (!Objects.equals(startflag, flag.startflag)) return false;
            return Objects.equals(endflag, flag.endflag);
        }

        @Override
        public int hashCode() {
            int result = color != null ? color.hashCode() : 0;
            result = 31 * result + (backcolor != null ? backcolor.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(style);
            result = 31 * result + (changebar != null ? changebar.hashCode() : 0);
            result = 31 * result + (outputClass != null ? outputClass.hashCode() : 0);
            result = 31 * result + (startflag != null ? startflag.hashCode() : 0);
            result = 31 * result + (endflag != null ? endflag.hashCode() : 0);
            return result;
        }

        public static class FlagImage {
            public final URI href;
            public final String alt;

            public FlagImage(URI href, String alt) {
                this.href = href;
                this.alt = alt;
            }

            private void writeFlag(final ContentHandler contentHandler, final String tag) throws SAXException {
                final XMLUtils.AttributesBuilder propAtts = new XMLUtils.AttributesBuilder().add("action", "flag");
                final URI abs = href;
                if (abs != null) {
                    propAtts.add(DITA_OT_NS, ATTRIBUTE_NAME_IMAGEREF_URI, "dita-ot:" + ATTRIBUTE_NAME_IMAGEREF_URI, "CDATA", abs.toString());
                    final URI rel = abs;
                    propAtts.add(DITA_OT_NS, "original-" + ATTRIBUTE_NAME_IMAGEREF, "dita-ot:original-" + ATTRIBUTE_NAME_IMAGEREF, "CDATA", rel.toString());
                    propAtts.add(ATTRIBUTE_NAME_IMAGEREF, rel.toString());
                }
                contentHandler.startElement(NULL_NS_URI, tag, tag, propAtts.build());
                if (alt != null) {
                    contentHandler.startElement(NULL_NS_URI, "alt-text", "alt-text", XMLUtils.EMPTY_ATTRIBUTES);
                    final char[] chars = alt.toCharArray();
                    contentHandler.characters(chars, 0, chars.length);
                    contentHandler.endElement(NULL_NS_URI, "alt-text", "alt-text");
                }
                contentHandler.endElement(NULL_NS_URI, tag, tag);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                FlagImage flagImage = (FlagImage) o;

                if (!Objects.equals(href, flagImage.href)) return false;
                return Objects.equals(alt, flagImage.alt);
            }

            @Override
            public int hashCode() {
                int result = href != null ? href.hashCode() : 0;
                result = 31 * result + (alt != null ? alt.hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "FlagImage{" +
                        "href=" + href +
                        ", alt='" + alt + '\'' +
                        '}';
            }
        }
    }

}
