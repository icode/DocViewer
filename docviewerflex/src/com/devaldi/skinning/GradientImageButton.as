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

package com.devaldi.skinning
{
	import flash.events.Event;
	import flash.filters.*;
	import flash.display.GradientType;
	import mx.controls.Button;
	import mx.events.*;
	import flash.geom.Matrix;
	
	public class GradientImageButton extends Button
	{
		public function GradientImageButton()
		{
			super();
			
			addEventListener("enabledChanged",enableHandler);
		}
		
	     override protected function updateDisplayList(w:Number, h:Number):void {
			if(selected){
				graphics.clear();
				
				var gradientBoxMatrix:Matrix = new Matrix();
				gradientBoxMatrix.createGradientBox(w, h, 0, 0, 0);

				graphics.beginGradientFill(GradientType.LINEAR,[0x009dff, 0x0079db, 0x0055b7],[0.22, 0.22, 0.22],[0, 128, 255],gradientBoxMatrix);
				//graphics.beginGradientFill(GradientType.LINEAR,[0xdddddd, 0xcccccc, 0xdedede],[1, 1, 1],[100, 200, 255],gradientBoxMatrix);
				
				graphics.drawRect(0, 0, width, height);
			}else{
				graphics.clear();
			}
			
			super.updateDisplayList(w,h);
	     }
				
		private function enableHandler(event:Event):void
		{
			// define the color filter
			var matrix:Array = new Array();
			
			if (!enabled)
			{
				matrix = matrix.concat([0.31, 0.61, 0.08, 0, 0]); 	// red
				matrix = matrix.concat([0.31, 0.61, 0.08, 0, 0]); 	// green
				matrix = matrix.concat([0.31, 0.61, 0.08, 0, 0]); 	// blue
				matrix = matrix.concat([0, 0, 0, 0.3, 0]); 			// alpha
				selected = false;
			}
			else
			{
				matrix = matrix.concat([1, 0, 0, 0, 0]); 	// red
				matrix = matrix.concat([0, 1, 0, 0, 0]); 	// green
				matrix = matrix.concat([0, 0, 1, 0, 0]); 	// blue
				matrix = matrix.concat([0, 0, 0, 1, 0]); 	// alpha
			}
			
			var filter:BitmapFilter = new ColorMatrixFilter(matrix);
			
			// apply color filter
			filters = new Array(filter) ;
			
			// activate or disacivate the button mode
			buttonMode = enabled ;
		}
	}
}