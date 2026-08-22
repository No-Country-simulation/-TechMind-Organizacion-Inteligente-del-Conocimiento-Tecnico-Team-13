package com.application.Views.Library;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

public class NativeRangeInput extends Component {

    private static final String VALUE_PROPERTY = "value";

    public NativeRangeInput(int min, int max, int defaultValue) {
        super(new Element("input")); // Call super with the root element
        getElement().setAttribute("type", "range");
        getElement().setAttribute("min", String.valueOf(min));
        getElement().setAttribute("max", String.valueOf(max));
        setValue(defaultValue);
    }

    public void setValue(int value) {
        getElement().setProperty(VALUE_PROPERTY, value);
    }

    public int getValue() {
        return getElement().getProperty(VALUE_PROPERTY, 0);
    }

    public Registration addValueChangeListener(ValueChangeListener listener) {
        return getElement().addEventListener("change", event -> listener.valueChanged(this));
    }

    @FunctionalInterface
    public interface ValueChangeListener {
        void valueChanged(NativeRangeInput source);
    }
}
