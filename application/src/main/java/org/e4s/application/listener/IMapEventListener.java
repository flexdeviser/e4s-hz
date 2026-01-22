package org.e4s.application.listener;

import java.io.Serializable;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryRemovedListener;

public class IMapEventListener implements EntryRemovedListener<String, String>, EntryEvictedListener<String, String>,
    EntryExpiredListener<String, String>, Serializable {

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        System.out.println("~");
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        System.out.println("~");
    }


    @Override
    public void entryExpired(EntryEvent<String, String> event) {

    }
}
