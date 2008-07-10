package org.mule.util.queue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.io.FileUtils;

public class CachingPersistenceStrategy implements QueuePersistenceStrategy
{

    private QueuePersistenceStrategy ps;
    private Map objects;

    public CachingPersistenceStrategy(QueuePersistenceStrategy ps)
    {
        this.ps = ps;
        this.objects = Collections.synchronizedMap(new ReferenceMap());
    }

    public void open() throws IOException
    {
        ps.open();
    }

    public void close() throws IOException
    {
        objects.clear();
        ps.close();
    }

    public Object load(String queue, Object id) throws IOException
    {
        Object obj = objects.get(id);
        return ps.load(queue, id);
    }

    public void remove(String queue, Object id) throws IOException
    {
        objects.remove(id);
        ps.remove(queue, id);
    }

	public void removeQueue(String queue) throws IOException {
		objects.clear();
		ps.removeQueue(queue);
	}
    
    public List restore() throws IOException
    {
        return ps.restore();
    }

    public Object store(String queue, Object obj) throws IOException
    {
        Object id = ps.store(queue, obj);
        objects.put(id, obj);
        return id;
    }

}
