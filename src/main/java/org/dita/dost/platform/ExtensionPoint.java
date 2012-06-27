/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */package org.dita.dost.platform;

/**
 * Extension point.
 * 
 * @since 1.5.3
 * @author Jarno Elovirta
 */
final class ExtensionPoint {

    /** Extension point ID. */
    final String id;
    /** Extension point name. */
    final String name;
    /** Plug-in defining the extension point. */
    final String plugin;

    /**
     * Extension point constructor.
     * 
     * @param id extension point ID
     * @param name extension point name, may be {@code null}
     * @param plugin ID of the plugin that defines the extension point
     * @throws NullPointerException if {@code id} or {@code plugin} is {@code null}
     */
    ExtensionPoint(final String id, final String name, final String plugin) {
        if (id == null) {
            throw new NullPointerException("id argument is null");
        }
        if (plugin == null) {
            throw new NullPointerException("plugin argument is null");
        }
        this.id = id;
        this.name = name;
        this.plugin = plugin;
    }

}
