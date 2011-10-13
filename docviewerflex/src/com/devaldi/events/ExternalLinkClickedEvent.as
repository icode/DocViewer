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
	
	public class ExternalLinkClickedEvent extends Event
	{
		public var link:String;
		public static const EXTERNALLINK_CLICKED:String = "onExternalLinkClicked";
		
		public function ExternalLinkClickedEvent(type:String,llink:String)
		{
			super(type);
			link=llink;
		}
		
		// Override the inherited clone() method.
		override public function clone():Event {
			return new ExternalLinkClickedEvent(type, link);
		}
	}
}