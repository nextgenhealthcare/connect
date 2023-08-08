package com.mirth.connect.server.userutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

public class DestinationSetTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Generate a new test object. Provides default metaDataIds as [1,2,3].
     * @return a new test object
     */
    private DestinationSet getNewTestDestinationSet() {
        return getNewTestDestinationSetUsing(1,2,3);
    }

    /**
     * Generate a new test object.
     * @param metaDataIds
     * @return a new test object
     */
    private DestinationSet getNewTestDestinationSetUsing(Integer...metaDataIds) {
        Set<Integer> metaDataIdsSet = new HashSet<>(Arrays.asList(metaDataIds));

        ConnectorMessage connMsg = new ConnectorMessage();
        
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put(Constants.DESTINATION_SET_KEY, metaDataIdsSet);
        connMsg.setSourceMap(sourceMap);

        Map<String, Integer> destinationIdMap = new HashMap<>();
        destinationIdMap.put("Map to 2", 2);

        ImmutableConnectorMessage icm = new ImmutableConnectorMessage(connMsg, false, destinationIdMap);
        return new DestinationSet(icm);
    }

    // @Test
    // public void validateAssumptionsEmptySet() throws Exception {
    //     HashSet<Integer> set = new HashSet<>();

    //     assertFalse(set.addAll(Collections.emptyList()));

    //     assertFalse(set.contains(null));

    //     assertTrue(set.containsAll(Collections.emptyList()));
    //     assertFalse(set.containsAll(Arrays.asList("Invalid")));
    //     assertFalse(set.containsAll(Collections.singleton(null)));
    // }

    // @Test
    // public void validateAssumptionsPopulatedSet() throws Exception {
    //     HashSet<Integer> set = new HashSet<>(Arrays.asList(1,2,3,4,5));

    //     assertFalse(set.addAll(Collections.emptyList()));

    //     assertFalse(set.contains(null));

    //     assertTrue(set.containsAll(Collections.emptyList()));
    //     assertFalse(set.containsAll(Arrays.asList("Invalid")));
    //     assertFalse(set.containsAll(Collections.singleton(null)));
    // }

    @Test
    public void testIsEmptyWithoutSourceMapKey() throws Exception {
        DestinationSet ds = getNewTestDestinationSetUsing(new Integer[0]);

        assertTrue(ds.isEmpty());
        assertEquals(0, ds.size());
    }

    @Test
    public void testIsEmptyWithSourceMapKey() throws Exception {
        ConnectorMessage cm = new ConnectorMessage();
        Set<Integer> metaDataIds = new HashSet<>();
        Map<String, Object> sm = new HashMap<>();
        sm.put(Constants.DESTINATION_SET_KEY, metaDataIds);
        cm.setSourceMap(sm);
        ImmutableConnectorMessage icm = new ImmutableConnectorMessage(cm);
        DestinationSet ds = new DestinationSet(icm);

        assertTrue(ds.isEmpty());
        assertEquals(0, ds.size());
    }

    @Test
    public void testPopulatedSet() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertFalse(ds.isEmpty());
        assertEquals(3, ds.size());
    }

    @Test
    public void testAdd() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertTrue(ds.add(4));
        assertEquals(4, ds.size());

        //already in set
        assertFalse(ds.add(1));
        assertEquals(4, ds.size());

        assertFalse(ds.add(null));
        assertEquals(4, ds.size());
    }

    @Test
    public void testAddAll() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();
        
        assertFalse(ds.addAll(Arrays.asList(1)));
        assertEquals(3, ds.size());
        assertFalse(ds.addAll(Arrays.asList(2, 3)));
        assertEquals(3, ds.size());
        assertTrue(ds.addAll(Arrays.asList(3, 4)));
        assertEquals(4, ds.size());
        assertTrue(ds.addAll(Arrays.asList(5, 6)));
        assertEquals(6, ds.size());
        //TODO is this what we want?
        assertFalse(ds.addAll(null));
        assertEquals(6, ds.size());
    }

    @Test
    public void testClear() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        ds.clear();

        assertTrue(ds.isEmpty());
        assertEquals(0, ds.size());
    }

    @Test
    public void testContains() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertFalse(ds.contains(0));
        assertTrue(ds.contains(1));
        assertTrue(ds.contains("Map to 2"));
        assertTrue(ds.contains(3));
        assertFalse(ds.contains(4));
        assertFalse(ds.contains("Invalid"));
        assertFalse(ds.contains(null));
    }

    @Test
    public void testContainsAll() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertTrue(ds.containsAll(Collections.emptyList()));
        assertTrue(ds.containsAll(Arrays.asList(1)));
        assertTrue(ds.containsAll(Arrays.asList(2, 3)));
        assertTrue(ds.containsAll(Arrays.asList("Map to 2")));
        assertFalse(ds.containsAll(Arrays.asList(3, 4)));
        assertFalse(ds.containsAll(Arrays.asList(4)));
        assertFalse(ds.containsAll(Arrays.asList(4, 5)));
        assertFalse(ds.containsAll(Arrays.asList("Invalid")));
        assertFalse(ds.containsAll(Collections.singleton(null)));
        //TODO is this what we want?
        assertFalse(ds.containsAll(null));
    }

    @Test
    public void testIterator() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        Iterator<Integer> iter = ds.iterator();
        
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(1), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(2), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Integer.valueOf(3), iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testIteratorEmpty() throws Exception {
        DestinationSet ds = getNewTestDestinationSetUsing(new Integer[0]);

        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveCollection() throws Exception {
        DestinationSet ds = getNewTestDestinationSetUsing(1,2,3,4);

        assertTrue(ds.remove(Arrays.asList(3)));
        assertEquals(3, ds.size());
        assertTrue(ds.remove(Arrays.asList(0,1)));
        assertEquals(2, ds.size());
        assertFalse(ds.remove(Arrays.asList(5)));
        assertEquals(2, ds.size());
        assertFalse(ds.remove(Arrays.asList(0,5)));
        assertEquals(2, ds.size());
        assertTrue(ds.containsAll(Arrays.asList(4,2)));
        assertTrue(ds.remove(Arrays.asList(2,4)));
        assertEquals(0, ds.size());
        assertFalse(ds.remove(Arrays.asList(6)));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
        //TODO is this what we want?
        assertFalse(ds.remove((Collection<Object>)null));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveObject() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        Integer toRemove = 2;
        assertTrue(ds.remove(toRemove));
        assertEquals(2, ds.size());
        assertFalse(ds.contains(toRemove));

        toRemove = null;
        assertFalse(ds.remove(toRemove));
        assertEquals(2, ds.size());
        assertFalse(ds.contains(toRemove));
    }

    @Test
    public void testRemoveAll() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertTrue(ds.removeAll());
        assertEquals(0, ds.size());
        assertTrue(ds.isEmpty());
        assertFalse(ds.removeAll());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveAllCollection() throws Exception {
        DestinationSet ds = getNewTestDestinationSetUsing(1,2,3,4);

        assertFalse(ds.removeAll(Arrays.asList(0)));
        assertEquals(4, ds.size());
        assertTrue(ds.removeAll(Arrays.asList(0,1)));
        assertEquals(3, ds.size());
        assertTrue(ds.removeAll(Arrays.asList(4,5)));
        assertEquals(2, ds.size());
        assertFalse(ds.removeAll(Arrays.asList(6,7,8)));
        assertEquals(2, ds.size());
        assertTrue(ds.iterator().hasNext());
        //TODO is this what we want?
        assertFalse(ds.removeAll((Collection<Integer>)null));
        assertEquals(2, ds.size());
        assertTrue(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveAllExceptCollection() throws Exception {
        DestinationSet ds = getNewTestDestinationSetUsing(1,2,3,4,5);

        assertTrue(ds.removeAllExcept(Arrays.asList(1,3,5,6)));
        assertEquals(3, ds.size());
        assertTrue(ds.iterator().hasNext());
        assertTrue(ds.containsAll(Arrays.asList(1,3,5)));
        assertFalse(ds.contains(2));
        assertFalse(ds.contains(4));
        assertTrue(ds.removeAllExcept(Arrays.asList(3)));
        assertEquals(1, ds.size());
        assertTrue(ds.iterator().hasNext());
        assertTrue(ds.removeAllExcept(Arrays.asList(0)));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveAllExceptCollectionWithNullEntry() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertTrue(ds.removeAllExcept(Collections.singleton(null)));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveAllExceptCollectionWithNullCollection() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        //TODO is this what we want?
        assertFalse(ds.removeAllExcept((Collection<Object>)null));
        assertEquals(3, ds.size());
        assertTrue(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveAllExceptObject() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertTrue(ds.removeAllExcept(2));
        assertEquals(1, ds.size());
        assertTrue(ds.iterator().hasNext());
        assertFalse(ds.contains(1));
        assertTrue(ds.contains(2));
        assertFalse(ds.contains(3));
        assertTrue(ds.iterator().hasNext());
        assertTrue(ds.removeAllExcept(0));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRemoveAllExceptObjectWithNull() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        //TODO is this what we want?
        assertTrue(ds.removeAllExcept((Object)null));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRetainAll() throws Exception {
        DestinationSet ds = getNewTestDestinationSetUsing(1,2,3,4,5);

        assertTrue(ds.retainAll(Arrays.asList(1,3,5,6)));
        assertEquals(3, ds.size());
        assertTrue(ds.iterator().hasNext());
        assertTrue(ds.containsAll(Arrays.asList(1,3,5)));
        assertFalse(ds.contains(2));
        assertFalse(ds.contains(4));
        assertTrue(ds.iterator().hasNext());
        assertTrue(ds.retainAll(Arrays.asList(0)));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRetainAllWithNullEntry() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        assertTrue(ds.retainAll(Collections.singleton(null)));
        assertEquals(0, ds.size());
        assertFalse(ds.iterator().hasNext());
    }

    @Test
    public void testRetainAllWithNullCollection() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        //TODO is this what we want?
        assertFalse(ds.retainAll((Collection<Object>)null));
        assertEquals(3, ds.size());
        assertTrue(ds.iterator().hasNext());
    }

    @Test
    public void testToArray() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        Object[] arr = ds.toArray();
        assertEquals(3, arr.length);
        assertEquals(Integer.valueOf(1), arr[0]);
        assertEquals(Integer.valueOf(2), arr[1]);
        assertEquals(Integer.valueOf(3), arr[2]);
    }

    @Test
    public void testToArrayGenericProperlySized() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        Integer[] arr = new Integer[ds.size()];
        //the input and response should be the same array
        Integer[] resp = ds.toArray(arr);
        assertEquals(3, resp.length);
        assertTrue(Arrays.equals(arr, resp));
        assertEquals(Integer.valueOf(1), arr[0]);
        assertEquals(Integer.valueOf(2), arr[1]);
        assertEquals(Integer.valueOf(3), arr[2]);
    }

    @Test
    public void testToArrayGenericImproperlySized() throws Exception {
        DestinationSet ds = getNewTestDestinationSet();

        Integer[] resp = ds.toArray(new Integer[0]);
        assertEquals(3, resp.length);
        assertEquals(Integer.valueOf(1), resp[0]);
        assertEquals(Integer.valueOf(2), resp[1]);
        assertEquals(Integer.valueOf(3), resp[2]);
    }
}