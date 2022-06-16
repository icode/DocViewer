/* 
Copyright 2009 Erik Engström

This file is part of FlexPaper.

FlexPaper is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

FlexPaper is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FlexPaper.  If not, see <http://www.gnu.org/licenses/>.	
*/

package com.devaldi.controls
{
 import com.devaldi.streaming.DupImage;
 
 import flash.display.Bitmap;
 import flash.display.DisplayObject;
 import flash.events.Event;
 
 import mx.collections.ArrayCollection;
 import mx.containers.Box;
 import mx.containers.BoxDirection;
 import mx.controls.Image;
 import mx.events.ResizeEvent;
 

    [DefaultProperty("children")]
    public class FlowBox extends Box
    {
        
        private var _children:ArrayCollection = new ArrayCollection();
        private var _childrenChanged:Boolean = false;
        
        public function FlowBox()
        {
            this.addEventListener(ResizeEvent.RESIZE,resizeHanlder);
        }
        
        
        /**
         * If this component is resized, the child
         * components must be laid out again
         */
        private function resizeHanlder(e:Event):void
        {
            relayoutChildren();
        }
        
        
        /**
         * Layout all child components (if we need to) during 
         * the commit properties phase of execution
         */
        protected override function commitProperties():void
        {
            super.commitProperties();
            
            if( _childrenChanged )
            {
                _childrenChanged = false;

                layoutChildren();
            }    
        }
        
        
        /**
         * Detect if any styles have been set which would 
         * require the child components to be laid out again
         */
        override public function setStyle(styleProp:String, newValue:*):void
        {
            super.setStyle( styleProp, newValue );
            
             if( this.initialized &&
                 ( styleProp == "horizontalAlign"
                || styleProp == "verticalAlign"
                || styleProp == "horizontalGap"
                || styleProp == "verticalGap"
                || styleProp == "paddingLeft"
                || styleProp == "paddingTop"
                || styleProp == "paddingRight"
                || styleProp == "paddingBottom" ) )
                relayoutChildren();
        }
        
        
        /**
         * Add all child components to ourself, creating
         * sub containers as required for the layout
         */
        private function layoutChildren():void
        {
            clearContentsForRelayout();
            
            var currentSubContainer:Box = createSubContainer();
            super.addChildAt( currentSubContainer, super.numChildren );
            
            for each( var child:DisplayObject in _children )
            {
                if( !canFit( child, currentSubContainer ) )
                {
                    currentSubContainer = createSubContainer();
                    super.addChildAt( currentSubContainer, super.numChildren );    
                }
                
                currentSubContainer.addChild( child );
            }
        }
        
        
        /**
         * Create a sub container (row or column) for 
         * this component will the required configuration
         */
        private function createSubContainer():Box
        {
            var subContainer:Box = new Box();
            
            subContainer.direction = this.direction;
            
            if( this.direction == BoxDirection.HORIZONTAL )
                subContainer.width = super.width - super.getStyle("paddingLeft") -  super.getStyle("paddingRight");
            else
                subContainer.height = super.height - super.getStyle("paddingTop") -  super.getStyle("paddingBottom");
            
            subContainer.setStyle( "paddingLeft", 0 );
            subContainer.setStyle( "paddingTop", 0 );
            subContainer.setStyle( "paddingBottom", 0 );
            subContainer.setStyle( "paddingRight", 0 );
            
            subContainer.setStyle( "horizontalAlign", this.getStyle( "horizontalAlign" ) );
            subContainer.setStyle( "verticalAlign", this.getStyle( "verticalAlign" ) );            
            subContainer.setStyle( "horizontalGap", this.getStyle( "horizontalGap" ) );
            subContainer.setStyle( "verticalGap", this.getStyle( "verticalGap" ) );
            
            subContainer.setStyle( "backgroundAlpha", 0 );
            
            return subContainer;
        }
        
        
        /**
         * Removes all internal layout containers from this 
         * container so that the children can be re-laid out
         */
        private function clearContentsForRelayout():void
        {
            for each( var child:DisplayObject in super.getChildren() )
            {
                super.removeChild( child );
            }
        }
        
        
        /**
         * Tests whether the specified component could fit within
         * the specfied container without any clipping or scrollbars
         */
        private function canFit( child:DisplayObject, parent:Box ):Boolean
        {        
            var gap:Number;
            var padding:Number;
            var criticalDimension:String;
            
            if( parent.direction == BoxDirection.HORIZONTAL )
            {
                gap = parent.getStyle( "horizontalGap" );
                padding = parent.getStyle( "paddingLeft" ) + parent.getStyle( "paddingRight" );
                criticalDimension = "width";
            }
            else
            {
                gap = parent.getStyle( "verticalGap" );
                padding = parent.getStyle( "paddingTop" ) + parent.getStyle( "paddingBottom" );
                criticalDimension = "height";
            }
            
            var usedSpace:Number = padding;
            var seperator:Number = 0;
            for each( var existingChild:DisplayObject in parent.getChildren() )
            {
                usedSpace += seperator + existingChild[criticalDimension];
                seperator = gap;
            }
            
            var requiredSpace:Number = usedSpace + gap + child[criticalDimension];            
            return ( requiredSpace < parent[criticalDimension] );
        }
        
        
        /**
         * Flag that the children of this control have changed,
         * and should be redrawn at the next convenient time
         */
        public function relayoutChildren():void
        {
            _childrenChanged = true;
            invalidateProperties();    
        }
    
        
        /**
         * Need to invert the direction property of this control
         * so that the behaviour is logical
         */
        override public function set direction(value:String):void
        {
            if( value == BoxDirection.HORIZONTAL )
                super.direction = BoxDirection.VERTICAL;
            else
                super.direction = BoxDirection.HORIZONTAL;
            
            relayoutChildren();
        }
        override public function get direction():String
        {
            if( super.direction == BoxDirection.HORIZONTAL )
                return BoxDirection.VERTICAL;
            else
                return BoxDirection.HORIZONTAL;
        }
        
        
        /**
         * Override all of the child manipulation functions to mask
         * the internal child layout functions of this container
         */
        public override function addChild(child:DisplayObject):DisplayObject
        {
            _children.addItem( child );
            relayoutChildren();
            return child;
        }        
        override public function addChildAt(child:DisplayObject, index:int):DisplayObject
        {
            _children.addItemAt( child, index );
            relayoutChildren();
            return child;
        }        
        override public function removeChild(child:DisplayObject):DisplayObject
        {
            var tmp:DisplayObject = _children.removeItemAt( _children.getItemIndex( child ) ) as DisplayObject;
            relayoutChildren();
            return tmp;
        }        
        override public function removeChildAt(index:int):DisplayObject
        {
            var tmp:DisplayObject = _children.removeItemAt( index ) as DisplayObject;
            relayoutChildren();
            return tmp;
        }        
        override public function removeAllChildren():void
        {
			var du:Object;
			
			for(var c:int=0;c<_children.length;c++){
				for(var i:int=0;i<_children[c].numChildren;i++){
					du = _children[c].getChildAt(i);
					
					if( du is Bitmap && (du as Bitmap).bitmapData != null)
						du.bitmapData.dispose();
				}				
			}
			
			
            _children.removeAll();
            relayoutChildren();
        }        
        override public function getChildren():Array
        {
            return _children.toArray();    
        }        
        override public function getChildIndex(child:DisplayObject):int
        {
            return _children.getItemIndex( child );
        }
		
		public function getItemAt(i:int):DisplayObject{
			return _children.getItemAt(i) as DisplayObject;
		}    
        
        /**
         * This property recieves the child components
         * of this container when they are set in MXML
         * 
         * This is set as the default property of this component
         */
        public function set children( value:* ):void
        {
            if( value is DisplayObject )
            {
                _children = new ArrayCollection([value]);
            }
            else if( value is Array )
            {
                var tmp:Array = value as Array;
                _children = new ArrayCollection();
                
                for each( var child:DisplayObject in tmp )
                {
                    _children.addItem( child );
                }
            }            
            relayoutChildren();
        }
    }
}