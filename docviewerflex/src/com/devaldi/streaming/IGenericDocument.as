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
package com.devaldi.streaming
{
	import flash.display.DisplayObject;
	import flash.display.LoaderInfo;
	import flash.display.MovieClip;
	import flash.text.TextSnapshot;

	public interface IGenericDocument
	{
		function gotoAndStop(pagNumber:int):void;
		function stop():void;
		function getDocument():DisplayObject;
		function get parent():DisplayObject;
		function get totalFrames():int;
		function get framesLoaded():int;
		function get textSnapshot():TextSnapshot;
		function set alpha(value:Number):void;
		function get currentFrame():int;
		function get height():Number;
		function get width():Number;
		function get loaderInfo():LoaderInfo;
		function set scaleX(n:Number):void;
		function set scaleY(n:Number):void;
	}
}