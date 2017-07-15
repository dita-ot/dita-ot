/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import static java.util.Collections.emptyList;
import static org.dita.dost.util.Constants.*;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;

import org.dita.dost.module.filter.SubjectScheme;
import org.w3c.dom.*;
import org.xml.sax.Attributes;

/**
 * Utility class used for flagging and filtering.
 * 
 * @author Wu, Zhi Qiang
 */
public final class FilterUtils {

    /** Subject scheme file extension */
    public static final String SUBJECT_SCHEME_EXTENSION = ".subm";

    private static final String[] PROFILE_ATTRIBUTES = {
        ATTRIBUTE_NAME_AUDIENCE,
        ATTRIBUTE_NAME_PLATFORM,
        ATTRIBUTE_NAME_PRODUCT,
        ATTRIBUTE_NAME_OTHERPROPS,
        ATTRIBUTE_NAME_PROPS,
        ATTRIBUTE_NAME_PRINT,
        ATTRIBUTE_NAME_DELIVERYTARGET
    };
    
    public static final FilterKey DEFAULT = new FilterKey(DEFAULT_ACTION, null);

    private DITAOTLogger logger;
    /** Actions for filter keys. */
    private final Map<FilterKey, Action> filterMap;
    /** Set of filter keys for which an error has already been thrown. */
    private final Set<FilterKey> notMappingRules = new HashSet<>();
    private boolean logMissingAction;

    public FilterUtils(final Map<FilterKey, Action> filterMap) {
        this.logMissingAction = !filterMap.isEmpty();
        this.filterMap = new HashMap<>(filterMap);
    }

    /**
     * Construct filter utility.
     * 
     * @param isPrintType transformation output is print-oriented
     */
    public FilterUtils(final boolean isPrintType, final Map<FilterKey, Action> filterMap) {
        final Map<FilterKey, Action> dfm = new HashMap<>();
        dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_YES), Action.INCLUDE);
        if (isPrintType) {
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_PRINT_ONLY), Action.INCLUDE);
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_NO), Action.EXCLUDE);
        } else {
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_PRINT_ONLY), Action.EXCLUDE);
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_NO), Action.INCLUDE);            
        }
        dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, null), Action.INCLUDE);
        dfm.putAll(filterMap);
        this.logMissingAction = !filterMap.isEmpty();
        this.filterMap = dfm;
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public String toString() {
        return filterMap.toString();
    }

    public List<Flag> getFlags(final Attributes atts, final String[][] extProps) {
        if (filterMap.isEmpty()) {
            return emptyList();
        }

        final List<Flag> res = new ArrayList<>();
        for (final String attr: PROFILE_ATTRIBUTES) {
            final String value = atts.getValue(attr);
            if (value != null) {
                final Map<String, List<String>> groups = getGroups(value);
                for (Map.Entry<String, List<String>> group: groups.entrySet()) {
                    final String[] propList =
                            group.getKey() != null
                                    ? new String[]{attr, group.getKey()}
                                    : new String[]{attr};
                    res.addAll(extCheckFlag(propList, group.getValue()));
                }
            }
        }

//        if (extProps != null && extProps.length != 0) {
//            for (final String[] propList: extProps) {
//                int propListIndex = propList.length - 1;
//                final String propName = propList[propListIndex];
//                String propValue = atts.getValue(propName);
//
//                while ((propValue == null || propValue.trim().isEmpty()) && propListIndex > 0) {
//                    propListIndex--;
//                    propValue = getLabelValue(propName, atts.getValue(propList[propListIndex]));
//                }
//                if (propValue != null && extCheckExclude(propList, Arrays.asList(propValue.split("\\s+")))) {
////                    return true;
//                }
//            }
//        }
        return res;
    }

    /**
     * @param propList attribute group names, from most common to most specific
     * @param attValue attribute group values
     */
    @VisibleForTesting
    List<Flag> extCheckFlag(final String[] propList, final List<String> attValue) {
        final List<Flag> res = new ArrayList<>();
        for (final String attName : Arrays.asList(propList)) {
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
    public boolean needExclude(final Element element, final String[][] props) {
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
    public boolean needExclude(final Attributes atts, final String[][] extProps) {
        if (filterMap.isEmpty()) {
            return false;
        }

        for (final String attr: PROFILE_ATTRIBUTES) {
            final String value = atts.getValue(attr);
            if (value != null) {
                final Map<String, List<String>> groups = getGroups(value);
                for (Map.Entry<String, List<String>> group: groups.entrySet()) {
                    final String[] propList =
                            group.getKey() != null
                                    ? new String[]{attr, group.getKey()}
                                    : new String[]{attr};
                    if (extCheckExclude(propList, group.getValue())) {
                        return true;
                    }
                }
            }
        }

        if (extProps != null && extProps.length != 0) {
            for (final String[] propList: extProps) {
                int propListIndex = propList.length - 1;
                final String propName = propList[propListIndex];
                String propValue = atts.getValue(propName);

                while ((propValue == null || propValue.trim().isEmpty()) && propListIndex > 0) {
                    propListIndex--;
                    propValue = getLabelValue(propName, atts.getValue(propList[propListIndex]));
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
    Map<String, List<String>> getGroups(final String value) {
        final Map<String, List<String>> res = new HashMap<>();
        
        final StringBuilder buf = new StringBuilder();
        int previousEnd = 0;
        final Matcher m = groupPattern.matcher(value);
        while(m.find()) {
            buf.append(value.subSequence(previousEnd, m.start()));
            final String v = m.group(2);
            if (!v.trim().isEmpty()) {
                final String k = m.group(1);
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
    private String getLabelValue(final String propName, final String attrPropsValue) {
        if (attrPropsValue != null) {
            int propStart = -1;
            if (attrPropsValue.startsWith(propName + "(") || attrPropsValue.indexOf(" " + propName + "(", 0) != -1) {
                propStart = attrPropsValue.indexOf(propName + "(");
            }
            if (propStart != -1) {
                propStart = propStart + propName.length() + 1;
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
    boolean extCheckExclude(final String[] propList, final List<String> attValue) {
        for (int propListIndex = propList.length - 1; propListIndex >= 0; propListIndex--) {
            final String attName = propList[propListIndex];
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
    private void checkRuleMapping(final String attName, final List<String> attValue) {
        if (attValue == null || attValue.isEmpty()) {
            return;
        }
        for (final String attSubValue: attValue) {
            final FilterKey filterKey = new FilterKey(attName, attSubValue);
            final Action filterAction = filterMap.get(filterKey);
            if (filterAction == null && logMissingAction) {
                if (!alreadyShowed(filterKey)) {
                    logger.info(MessageUtils.getInstance().getMessage("DOTJ031I", filterKey.toString()).toString());
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
        public final String attribute;
        /** Attribute value, may be {@code null} */
        public final String value;

        public FilterKey(final String attribute, final String value) {
            if (attribute == null) {
                throw new IllegalArgumentException("Attribute may not be null");
            }
            this.attribute = attribute;
            this.value = value;
        }

        @Override
        public String toString() {
            return value != null ? attribute + EQUAL + value : attribute;
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
    private FilterUtils refine(final Map<String, Map<String, Set<Element>>> bindingMap) {
        if (bindingMap != null && !bindingMap.isEmpty()) {
            final Map<FilterKey, Action> buf = new HashMap<>(filterMap);
            for (final Map.Entry<FilterKey, Action> e: filterMap.entrySet()) {
                refineAction(e.getValue(), e.getKey(), bindingMap, buf);
            }
            final FilterUtils filterUtils = new FilterUtils(buf);
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
    private void refineAction(final Action action, final FilterKey key, final Map<String, Map<String, Set<Element>>> bindingMap,
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
    private void insertAction(final Element subTree, final String attName, final Action action, final Map<FilterKey, Action> destFilterMap) {
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

        public final String color;
        public final String backcolor;
        public final String[] style;
        public final String changebar;
        public final FlagImage startflag;
        public final FlagImage endflag;

        public Flag(String color, String backcolor, String style, String changebar,
                    FlagImage startflag, FlagImage endflag) {
            this.color = color;
            this.backcolor = backcolor;
            this.style = style != null ? style.split("\\s+") : null;
            this.changebar = changebar;
            this.startflag = startflag;
            this.endflag = endflag;
        }

        @Override
        public String toString() {
            return "Flag{" +
                    "color='" + color + '\'' +
                    ", backcolor='" + backcolor + '\'' +
                    ", style=" + Arrays.toString(style) +
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

            if (color != null ? !color.equals(flag.color) : flag.color != null) return false;
            if (backcolor != null ? !backcolor.equals(flag.backcolor) : flag.backcolor != null) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(style, flag.style)) return false;
            if (changebar != null ? !changebar.equals(flag.changebar) : flag.changebar != null) return false;
            if (startflag != null ? !startflag.equals(flag.startflag) : flag.startflag != null) return false;
            return endflag != null ? endflag.equals(flag.endflag) : flag.endflag == null;
        }

        @Override
        public int hashCode() {
            int result = color != null ? color.hashCode() : 0;
            result = 31 * result + (backcolor != null ? backcolor.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(style);
            result = 31 * result + (changebar != null ? changebar.hashCode() : 0);
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

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                FlagImage flagImage = (FlagImage) o;

                if (href != null ? !href.equals(flagImage.href) : flagImage.href != null) return false;
                return alt != null ? alt.equals(flagImage.alt) : flagImage.alt == null;
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
