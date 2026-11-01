import javax.swing.JSlider;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>Note that RangeSlider makes use of the default BoundedRangeModel, which 
 * supports an inner range defined by a value and an extent.  The upper value
 * returned by RangeSlider is simply the lower value plus the extent.</p>
 */
public class RangeSlider extends JSlider {

    /**
     * Constructs a RangeSlider with default minimum and maximum values of 0
     * and 100.
     */
    public RangeSlider() {
        initSlider();
    }

    /**
     * Constructs a RangeSlider with the specified default minimum and maximum 
     * values.
     */
    public RangeSlider(int min, int max) {
        super(min, max);
        initSlider();
    }

    /**
     * Initializes the slider by setting default properties.
     */
    private void initSlider() {
        setOrientation(HORIZONTAL);
    }

    /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        setUI(new RangeSliderUI(this));
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }

    /**
     * Returns the lower value in the range.
     */
    @Override
    public int getValue() {
        return super.getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value) {
        int oldValue = getValue();
        if (oldValue == value) {
            return;
        }

        // Compute new value and extent to maintain upper value.
        int oldExtent = getExtent();
        int minimum   = getMinimum();
        if(value < minimum)
        	value = minimum;
        int upper_value = oldValue + oldExtent;
        if(value >  upper_value)
        	value = upper_value;
        
        //int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
        int newExtent = upper_value - value;

        // Set new value and extent, and fire a single change event.
        getModel().setRangeProperties(value, newExtent, getMinimum(), 
            getMaximum(), getValueIsAdjusting());
    }

    /**
     * Sets the lower and upper values together, firing a single change event.
     * Unlike calling setValue() and setUpperValue() one after the other, the
     * result doesn't depend on the current values or on the order of the
     * calls.  If lower is greater than upper the two are swapped, and both
     * are clamped to the slider's minimum and maximum.
     */
    public void setRange(int lower, int upper) {
        if (lower > upper) {
            int temp = lower;
            lower = upper;
            upper = temp;
        }
        int minimum = getMinimum();
        int maximum = getMaximum();
        lower = Math.min(Math.max(lower, minimum), maximum);
        upper = Math.min(Math.max(upper, minimum), maximum);

        getModel().setRangeProperties(lower, upper - lower, minimum, maximum,
            getValueIsAdjusting());
    }

    /**
     * Sets the minimum value of the slider.  Unlike the default model, which
     * keeps the extent and so can move the upper value, this keeps both
     * values where they are and only clamps them to the new bounds.
     */
    @Override
    public void setMinimum(int minimum) {
        int maximum = Math.max(minimum, getMaximum());
        int lower   = Math.min(Math.max(getValue(), minimum), maximum);
        int upper   = Math.min(Math.max(getUpperValue(), lower), maximum);

        // Store the clamped values first, within bounds that include both the
        // old and new limits, so the model's own update leaves them alone.
        getModel().setRangeProperties(lower, upper - lower, Math.min(minimum, getMinimum()),
            Math.max(maximum, getMaximum()), getValueIsAdjusting());
        super.setMinimum(minimum);
    }

    /**
     * Sets the maximum value of the slider.  Unlike the default model, which
     * keeps the extent and so can move the lower value, this keeps both
     * values where they are and only clamps them to the new bounds.
     */
    @Override
    public void setMaximum(int maximum) {
        int minimum = Math.min(getMinimum(), maximum);
        int upper   = Math.min(Math.max(getUpperValue(), minimum), maximum);
        int lower   = Math.min(Math.max(getValue(), minimum), upper);

        getModel().setRangeProperties(lower, upper - lower, Math.min(minimum, getMinimum()),
            Math.max(maximum, getMaximum()), getValueIsAdjusting());
        super.setMaximum(maximum);
    }

    /**
     * Returns the upper value in the range.
     */
    public int getUpperValue() 
    {
        return getValue() + getExtent();
    }

    /**
     * Sets the upper value in the range.
     */
    public void setUpperValue(int value) 
    {
        // Compute new extent.
        int lower_value = getValue();
        if(value < lower_value)
        {
            setExtent(0);	
        }
        else
        {
            int maximum = getMaximum();
            if(value > maximum)
            	value = maximum;
            int extent = value - lower_value;
            setExtent(extent);
        }
        //int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);
    }
}

