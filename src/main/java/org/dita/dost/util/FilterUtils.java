/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;

import org.xml.sax.Attributes;

/**
 * Utility class used for flagging and filtering.
 * 
 * @author Wu, Zhi Qiang
 */
public final class FilterUtils {

    public enum Action {
        INCLUDE, EXCLUDE, PASSTHROUGH, FLAG
    }

    private static final String[] PROFILE_ATTRIBUTES = {
        ATTRIBUTE_NAME_AUDIENCE,
        ATTRIBUTE_NAME_PLATFORM,
        ATTRIBUTE_NAME_PRODUCT,
        ATTRIBUTE_NAME_OTHERPROPS,
        ATTRIBUTE_NAME_PROPS,
        ATTRIBUTE_NAME_PRINT
    };
    
    public static final FilterKey DEFAULT = new FilterKey(DEFAULT_ACTION, null);

    private DITAOTLogger logger;
    /** Immutable default filter map. */
    private final Map<FilterKey, Action> defaultFilterMap;
    private Map<FilterKey, Action> filterMap = null;
    private final Set<FilterKey> notMappingRules = new HashSet<FilterKey>();
    
    @Deprecated
    public FilterUtils() {
        defaultFilterMap = Collections.emptyMap();
        filterMap = defaultFilterMap;
    }
    
    /**
     * Construct filter utility.
     * 
     * @param isPrintType transformation output is print-oriented
     */
    public FilterUtils(final boolean isPrintType) {
        final Map<FilterKey, Action> dfm = new HashMap<FilterKey, Action>();
        dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_YES), Action.INCLUDE);
        if (isPrintType) {
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_PRINT_ONLY), Action.INCLUDE);
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_NO), Action.EXCLUDE);
        } else {
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_PRINT_ONLY), Action.EXCLUDE);
            dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, ATTR_PRINT_VALUE_NO), Action.INCLUDE);            
        }
        dfm.put(new FilterKey(ATTRIBUTE_NAME_PRINT, null), Action.INCLUDE);
        defaultFilterMap = Collections.unmodifiableMap(dfm);
        filterMap = defaultFilterMap;
    }
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Set the filter map.
     * 
     * @param filtermap The filterMap to set.
     */
    public void setFilterMap(final Map<FilterKey, Action> filtermap) {
        if (!filtermap.isEmpty()) {
            final Map<FilterKey, Action> fm = new HashMap<FilterKey, Action>(defaultFilterMap);
            fm.putAll(filtermap);
            filterMap = Collections.unmodifiableMap(fm);
        } else {
            filterMap = defaultFilterMap;
        }
    }

    /**
     * Getter for filter map.
     * 
     * @return filter map
     */
    public Map<FilterKey, Action> getFilterMap() {
        return filterMap;
    }

    /**
     * Check if the given Attributes need to be excluded.
     * 
     * @param atts attributes
     * @param extProps {@code props} attribute specializations
     * @return true if any one of attributes 'audience', 'platform', 'product',
     *         'otherprops', 'props', or 'print' was excluded.
     */
    public boolean needExclude(final Attributes atts, final String[][] extProps) {
        if (filterMap == null || filterMap.isEmpty()) {
            return false;
        }

        for (final String attr: PROFILE_ATTRIBUTES) {
            final String value = atts.getValue(attr);
            if (value != null) {
                final Map<String, List<String>> groups = getGroups(value);
                for (Map.Entry<String, List<String>> group: groups.entrySet()) {
                    if (group.getKey() != null) {
                        if (extCheckExclude(new String[] { attr, group.getKey() }, group.getValue())) {
                            return true;
                        }
                    } else {
                        if (extCheckExclude(new String[] { attr }, group.getValue())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        if (extProps != null && extProps.length != 0) {
            for (final String[] propList: extProps) {
                int propListIndex = propList.length - 1;
                final String propName = propList[propListIndex];
                String propValue = atts.getValue(propName);
    
                while (propValue == null && propListIndex > 0) {
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

    private final Pattern groupPattern = Pattern.compile("(\\w+)\\((.+?)\\)");
    
    public Map<String, List<String>> getGroups(final String value) {
        final Map<String, List<String>> res = new HashMap<String, List<String>>();
        
        final StringBuilder buf = new StringBuilder();
        int previousEnd = 0;
        final Matcher m = groupPattern.matcher(value);
        if (m != null) {
            while(m.find()) {
                buf.append(value.subSequence(previousEnd, m.start()));
                final String v = m.group(2); 
                if (!v.trim().isEmpty()) {
                    final String k = m.group(1);
                    if (res.containsKey(k)) {
                        final List<String> l = new ArrayList<String>(res.get(k));
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
        } else {
            res.put(null, Arrays.asList(value.trim().split("\\s+")));
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
     * @param propList
     * @param attValue
     * @return {@code true} if should be excluded, otherwise {@code false}
     */
    private boolean extCheckExclude(final String[] propList, final List<String> attValue) {
        if (attValue == null || attValue.isEmpty() || propList.length == 0 || attValue.contains("(")) {
            return false;
        }
        for (int propListIndex = propList.length - 1; propListIndex >= 0; propListIndex--) {
            boolean hasNullAction = false;
            boolean hasExcludeAction = false;
            final String attName = propList[propListIndex];
            checkRuleMapping(attName, attValue);
            for (final String attSubValue: attValue) {
                final FilterKey filterKey = new FilterKey(attName, attSubValue);
                final Action filterAction = filterMap.get(filterKey);
                // no action will be considered as 'not exclude'
                if (filterAction == null) {
                    // check Specified DefaultAction mapping this attribute's name
                    final Action defaultAction = filterMap.get(new FilterKey(attName, null));
                    if (defaultAction != null) {
                        if (Action.EXCLUDE != defaultAction) {
                            return false;
                        } else {
                            hasExcludeAction = true;
                            if (hasNullAction) {
                                if (checkExcludeOfGlobalDefaultAction()) {
                                    hasNullAction = false;
                                } else {
                                    return false;
                                }
                            }
                        }
                    } else {
                        if (hasExcludeAction) {
                            if (!checkExcludeOfGlobalDefaultAction()) {
                                return false;
                            }
                        } else {
                            hasNullAction = true;
                        }
                    }
                } else if (Action.EXCLUDE == filterAction) {
                    hasExcludeAction = true;
                    if (hasNullAction) {
                        if (checkExcludeOfGlobalDefaultAction()) {
                            hasNullAction = false;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            if (hasNullAction) {
                // if there is exclude action but not all value should be excluded
                // under the condition of default action also not exist or not excluded
                if (0 == propListIndex) {
                    // the ancient parent on the top level
                    return checkExcludeOfGlobalDefaultAction();
                }
            } else if (hasExcludeAction) {
                // if all of the value should be excluded
                return true;
            }
            // If no action for this extended prop has been found, we need to check the parent prop action
        }

        return false;
    }

    private boolean checkExcludeOfGlobalDefaultAction() {
        final Action defaultAction = filterMap.get(DEFAULT);
        if (defaultAction == null) {
            return false;
        } else {
            return Action.EXCLUDE == defaultAction;
        }
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
            if (filterAction == null) {
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
            result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
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
            if (attribute == null) {
                if (other.attribute != null) {
                    return false;
                }
            } else if (!attribute.equals(other.attribute)) {
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

}
