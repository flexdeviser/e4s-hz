package org.e4s.configuration.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import info.jerrinot.subzero.AbstractTypeSpecificUserSerializer;


public class CachePayload<T extends MeterPQ> extends AbstractTypeSpecificUserSerializer<List> implements Serializable {

    private List<T> body = new ArrayList<>();

    public CachePayload() {
        super(List.class);
    }

    public boolean add(T meterPQ){
        return body.add(meterPQ);
    }

    public List<T> getAll(){
        return body;
    }

    @Override
    public int getTypeId() {
        return 10088;
    }
}
