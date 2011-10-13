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
	
	public class PageLoadingEvent extends Event
	{
		public static const PAGE_LOADING:String = "onPageLoading";
		
		public var pageNumber:Number;
		
		public function PageLoadingEvent(type:String,p:Number){
			super(type);
			pageNumber=p;
		}
		
		// Override the inherited clone() method.
		override public function clone():Event {
			return new PageLoadingEvent(type, pageNumber);
		}
		
	}
}