package com.devaldi.controls
{
import mx.core.ILayoutElement;

import spark.components.supportClasses.GroupBase;
import spark.layouts.supportClasses.LayoutBase;

public class FlowLayout extends LayoutBase
{
    override public function updateDisplayList(containerWidth:Number,
                                         containerHeight:Number):void
    {
        // The position for the current element
        var x:Number = 0;
        var y:Number = 0;

        // loop through the elements
        var layoutTarget:GroupBase = target;
        var count:int = layoutTarget.numElements;
        for (var i:int = 0; i < count; i++)
        {
            // get the current element, we're going to work with the
            // ILayoutElement interface
            var element:ILayoutElement = layoutTarget.getElementAt(i);

            // Resize the element to its preferred size by passing
            // NaN for the width and height constraints
            element.setLayoutBoundsSize(NaN, NaN);
            
            // Find out the element's dimensions sizes.
            // We do this after the element has been already resized
            // to its preferred size.
            var elementWidth:Number = element.getLayoutBoundsWidth();
            var elementHeight:Number = element.getLayoutBoundsHeight();
            
            // Would the element fit on this line, or should we move
            // to the next line?
            if (x + elementWidth > containerWidth)
            {
                // Start from the left side
                x = 0;

                // Move down by elementHeight, we're assuming all 
                // elements are of equal height
                y += elementHeight;
            }

            // Position the element
            element.setLayoutBoundsPosition(x, y);

            // Update the current position, add a gap of 10
            x += elementWidth + 10;
        }
    }
}
}