/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jhserv.jacks.httpservice.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class implentes a ConcurrentDictionary based off of ConcurrentHashMap
 * instead of Hashtable. All methods just delegate to the underlying ConcurrentHashMap.
 * So all the CuncurrentHashMap rules apply to this class as well.
 *
 * @param <K>
 * @param <V>
 * @author rjackson
 */
public class ConcurrentDictionary<K, V> extends Dictionary<K, V>
        implements ConcurrentMap<K,V>, Map<K,V>, Serializable  {

    private final ConcurrentHashMap<K, V> backingMap;
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new, empty map with a default initial capacity (16), load
     * factor (0.75) and concurrencyLevel (16).
     */
    public ConcurrentDictionary() {
        backingMap = new ConcurrentHashMap<K, V>();
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with
     * default load factor (0.75) and concurrencyLevel (16).
     *
     * @param initialCapacity
     */
    public ConcurrentDictionary(int initialCapacity) {
       backingMap = new ConcurrentHashMap<K, V>(initialCapacity);
    }

    /**
     * Creates a new, empty map with the specified initial capacity and load
     * factor and with the default concurrencyLevel (16).
     *
     * @param initialCapacity
     * @param loadFactor
     */
    public ConcurrentDictionary(int initialCapacity, float loadFactor) {
        backingMap = new ConcurrentHashMap<K, V>(initialCapacity, loadFactor);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, load
     * factor and concurrency level.
     *
     * @param initialCapacity
     * @param loadFactor
     * @param concurrencyLevel
     */
    public ConcurrentDictionary(int initialCapacity, float loadFactor, int concurrencyLevel) {
        backingMap = new ConcurrentHashMap<K, V>(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Creates a new map with the same mappings as the given map.
     *
     * @param m
     */
    public ConcurrentDictionary(Map<? extends K, ? extends V> m) {
        backingMap = new ConcurrentHashMap<K, V>(m);
    }
    
    /**
     * Creates a new dictionary using the data supplied in the Properties object.
     * It should be noted that this method will fail if the Properties object 
     * contains anything other than String keys and String values. Also <K, V> 
     * must be <String, String> for this method to work correctly. If you use any 
     * other Key or Value type I'm pretty sure you will get all kinds of errors.
     * 
     * The underlying map is created with a capacity of 1.5 times the number of mappings in 
     * the given Properties or 16 (whichever is greater), and a default load factor 
     * (0.75) and concurrencyLevel (16). 
     * 
     * @param props
     */
    public ConcurrentDictionary(Properties props) {
        int propSize = props.size();
        if(16 > (propSize * 1.5)) {
            backingMap = new ConcurrentHashMap<K, V>(16);
        } else {
            backingMap = new ConcurrentHashMap<K, V>((int)(propSize * 1.5));
        }

        // I don't really like this
        @SuppressWarnings("unchecked")
        Set<K> keyset = (Set<K>)props.keySet();
        for(K key: keyset) {
            // Again I don't really like this.
            @SuppressWarnings("unchecked")
            V value = (V)props.get(key);
            backingMap.put(key, value);
        }

    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public Enumeration<K> keys() {
        return backingMap.keys();
    }

    @Override
    public Enumeration<V> elements() {
        return backingMap.elements();
    }

    @Override
    public V get(Object key) {
        return backingMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return backingMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return backingMap.remove(key);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return backingMap.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return backingMap.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return backingMap.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return backingMap.replace(key, value);
    }

    @Override
    public boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backingMap.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        backingMap.putAll(m);
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return backingMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return backingMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return backingMap.entrySet();
    }
}
