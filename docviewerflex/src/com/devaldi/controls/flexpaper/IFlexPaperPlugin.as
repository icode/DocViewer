package com.devaldi.controls.flexpaper
{
	import com.devaldi.streaming.DupImage;
	import flash.display.MovieClip;

	[Event(name="onPageNavigate", type="flash.events.Event")]
	
	public interface IFlexPaperPlugin
	{
		function drawSelf(pageIndex:int, drawingObject:Object,scale:Number):void;
		function init():void;
		function registerCallbackMethods():void;
		function clear():void;
		function bindPaperEventHandler(d:DupImage):void;
	}
}