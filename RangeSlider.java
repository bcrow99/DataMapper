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
    public int getValue() 
    {
        return super.getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value) 
    {
        int previous_value = getValue();
        if(previous_value == value) 
            return;
        
        int minimum         = getMinimum();
        if(value < minimum)
        {
        	value = minimum;
        	System.out.println("Resetting input less than minimum.");
        }
        int previous_extent = getExtent();
        int upper_value     = previous_value + previous_extent;
        if(value >= upper_value)
        {
        	value = upper_value - 1;
        	System.out.println("Resetting out of order input.");
        }
        int extent = upper_value - value;
        getModel().setRangeProperties(value, extent, getMinimum(), 
            getMaximum(), getValueIsAdjusting());
    }

    public int getUpperValue() 
    {
        return getValue() + getExtent();
    }

    public void setUpperValue(int value) 
    {
        int lower_value = getValue();
        if(value <= lower_value)
        {
        	value = lower_value + 1;
        	System.out.println("Resetting out of order input.");
        }
        int maximum         = getMaximum();
        if(value > maximum)
        {
        	value = maximum;
        	System.out.println("Resetting input more than maximum.");      	
        }
        int extent = value - lower_value;
        setExtent(extent);
    }
}

