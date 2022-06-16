package com.devaldi.controls
{
	import flash.display.DisplayObject;
	import flash.display.DisplayObjectContainer;
	
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.core.Container;
	
	public class FlowTwoPage extends VBox
	{
		public function FlowTwoPage()
		{
		}
		
		public override function addChild(child:DisplayObject):DisplayObject
		{
			var hb:HBox;
			if(numChildren == 0 || (numChildren > 0 && (getChildAt(numChildren-1) as HBox).numChildren == 2)){
				hb = new HBox();
				hb.setStyle("horizontalGap",1);
				super.addChild(hb);
			}else{
				hb = (getChildAt(numChildren-1) as HBox);
			}
			
			hb.addChild(child);
			return child;
		}
	}
}