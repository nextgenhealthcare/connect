/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.Cacheable;
import com.mirth.connect.server.util.SqlConfig;

/**
 * This class is used by the server to keep a consistent cache of objects placed in the database
 * with an ID, name, revision, and serialized XML blob. Every public getter method first calls the
 * synchronized refreshCache() method to update any modified or removed objects in the cache.
 */
public class Cache<V extends Cacheable<V>> {
    private Logger logger = Logger.getLogger(getClass());
    private String cacheName;
    private String selectRevisionsQueryId;
    private String selectQueryId;
    private boolean nameUnique;

    protected Map<String, V> cacheById = new ConcurrentHashMap<String, V>();
    protected Map<String, V> cacheByName;

    public Cache(String cacheName, String selectRevisionsQueryId, String selectQueryId) {
        this(cacheName, selectRevisionsQueryId, selectQueryId, true);
    }

    public Cache(String cacheName, String selectRevisionsQueryId, String selectQueryId, boolean nameUnique) {
        this.cacheName = cacheName;
        this.selectRevisionsQueryId = selectRevisionsQueryId;
        this.selectQueryId = selectQueryId;
        this.nameUnique = nameUnique;

        if (nameUnique) {
            cacheByName = new ConcurrentHashMap<String, V>();
        }
    }

    public Map<String, V> getAllItems() {
        refreshCache();

        Map<String, V> map = new LinkedHashMap<String, V>();

        if (nameUnique) {
            for (V item : new TreeMap<String, V>(cacheByName).values()) {
                map.put(item.getId(), item.cloneIfNeeded());
            }
        } else {
            List<V> list = new ArrayList<V>(cacheById.values());
            Collections.sort(list, new Comparator<V>() {
                @Override
                public int compare(V o1, V o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });

            for (V item : list) {
                map.put(item.getId(), item);
            }
        }

        return map;
    }

    public V getCachedItemById(String id) {
        refreshCache();

        return cloneIfNeeded(cacheById.get(id));
    }

    public V getCachedItemByName(String name) {
        if (nameUnique) {
            refreshCache();
            return cloneIfNeeded(cacheByName.get(name));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Set<String> getCachedIds() {
        refreshCache();

        return new LinkedHashSet<String>(cacheById.keySet());
    }

    public Set<String> getCachedNames() {
        if (nameUnique) {
            refreshCache();
            return new LinkedHashSet<String>(cacheByName.keySet());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected synchronized void refreshCache() {
        try {
            // Get the current revisions in the database
            Map<String, Integer> databaseRevisions = getRevisions();

            // Remove any from the cache that no longer exist in the database
            for (String id : cacheById.keySet()) {
                if (!databaseRevisions.containsKey(id)) {
                    // Remove from cache
                    V item = cacheById.remove(id);
                    if (nameUnique) {
                        cacheByName.remove(item.getName());
                    }
                }
            }

            // Put any new or updated items in the database in the cache
            for (Entry<String, Integer> revisionEntry : databaseRevisions.entrySet()) {
                String id = revisionEntry.getKey();

                if (!cacheById.containsKey(id) || revisionEntry.getValue() > cacheById.get(id).getRevision()) {
                    V item = getItem(id);

                    if (item != null) {
                        String name = item.getName();
                        V oldItem = cacheById.get(id);

                        cacheById.put(id, item);

                        if (nameUnique) {
                            cacheByName.put(name, item);

                            // If the name changed, remove the old name from the cache
                            if (oldItem != null) {
                                String oldName = oldItem.getName();
                                if (!oldName.equals(name)) {
                                    cacheByName.remove(oldName);
                                }
                            }
                        }
                    } else {
                        /*
                         * The item was either removed from the database after the initial revision
                         * query or an error occurred while attempting to retrieve it, remove it
                         * from the cache if it already existed.
                         */
                        if (cacheById.containsKey(id)) {
                            V oldItem = cacheById.remove(id);
                            if (nameUnique) {
                                cacheByName.remove(oldItem.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error refreshing " + cacheName + " cache", e);
        }
    }

    private V cloneIfNeeded(V item) {
        return item != null ? item.cloneIfNeeded() : item;
    }

    private V getItem(String id) {
        try {
            return SqlConfig.getSqlSessionManager().selectOne(selectQueryId, id);
        } catch (Exception e) {
            logger.error(cacheName + " cache: Failed to load item " + id + " from the database", e);
            return null;
        }
    }

    private Map<String, Integer> getRevisions() throws ControllerException {
        try {
            List<Map<String, Object>> results = SqlConfig.getSqlSessionManager().selectList(selectRevisionsQueryId);

            Map<String, Integer> revisionMap = new HashMap<String, Integer>();
            for (Map<String, Object> result : results) {
                revisionMap.put((String) result.get("id"), (Integer) result.get("revision"));
            }

            return revisionMap;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }
}