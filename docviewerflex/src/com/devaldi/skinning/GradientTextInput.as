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
	
	import mx.controls.TextInput;
	import mx.events.*;
	
	public class GradientTextInput extends mx.controls.TextInput
	{
		public function GradientTextInput()
		{
			super();
		}
		
		override public function set enabled(value:Boolean):void{
			super.enabled = value;
			enableHandler(null);
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
		}

	}
}