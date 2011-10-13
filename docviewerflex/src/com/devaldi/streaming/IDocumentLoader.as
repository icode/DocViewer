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
	import flash.display.Loader;
	import flash.events.IEventDispatcher;
	import flash.net.URLRequest;
	import flash.net.URLStream;
	import flash.system.LoaderContext;
	import flash.utils.ByteArray;
	
	public interface IDocumentLoader extends IEventDispatcher
	{
		function get DocumentContainer():DisplayObject;
		function get LoaderList():Array;
		function set LoaderList(v:Array):void;
		function postProcessBytes(b:ByteArray):void;
		function load(request:URLRequest, loaderCtx:LoaderContext):void;
		function loadFromBytes(bytes:ByteArray):void;
		function resetURLStream():void;
		function signFileHeader(bytes:ByteArray, ldr:Loader=null):void;
		function get InputBytes():ByteArray;
		function set InputBytes(b:ByteArray):void;
		function get Resigned():Boolean;
		function get stream():URLStream;	
		function get IsSplit():Boolean;
		function set IsSplit(b:Boolean):void;
	}
}