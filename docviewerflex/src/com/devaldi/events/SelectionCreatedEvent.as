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

package com.devaldi.events
{
	import flash.events.Event;
	
	public class SelectionCreatedEvent extends Event
	{
		public static const SELECTION_CREATED:String = "onSelectionCreated";
		public var text:String;
		
		public function SelectionCreatedEvent(type:String,s:String){
			super(type);
			text=s;
		}
		
		// Override the inherited clone() method.
		override public function clone():Event {
			return new SelectionCreatedEvent(type, text);
		}
		
	}
}