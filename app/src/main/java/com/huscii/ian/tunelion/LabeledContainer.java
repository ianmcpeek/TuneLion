package com.huscii.ian.tunelion;

import java.util.ArrayList;

/**
 * Created by Ian on 8/15/2015.
 */
public class LabeledContainer {
    private String label;
    private ArrayList values;

    LabeledContainer(String label) {
        this.label = label;
        values = new ArrayList();
    }

    public void addContainer(ArrayList container) {
        values.add(container);
    }

    public ArrayList getContainer() {
        return values;
    }

    public String getLabel() {
        return label;
    }

    public boolean contains(String otherLabel) {
        return label.equals(otherLabel);
    }
}
