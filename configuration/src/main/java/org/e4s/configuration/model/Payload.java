package org.e4s.configuration.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class Payload {


    private Properties properties = new Properties();

    private List<String> data;

    private String key;

    public List<String> getData() {
        return data;
    }

    public void add(String item) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(item);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public void setAttribute(final String name, final String value) {
        properties.setProperty(name, value);
    }

    public String getAttribute(final String name) {
        return properties.getProperty(name, "");
    }

    public Enumeration<?> getKeys() {
        return properties.propertyNames();
    }
}
