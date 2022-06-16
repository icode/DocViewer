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
	import flash.display.Bitmap;
	import flash.display.DisplayObject;
	import flash.display.LoaderInfo;
	import flash.display.MovieClip;
	import flash.text.TextSnapshot;
	
	public class SwfDocument implements IGenericDocument{
		private var _mc:MovieClip;
		
		public function  SwfDocument(mc:DisplayObject){
			if(mc is MovieClip)
				_mc = mc as MovieClip;
			else if(mc is Bitmap){
				_mc = new MovieClip();
				_mc.addChild(mc);
			}
		}
		
		public function gotoAndStop(pagNumber:int):void{
			_mc.gotoAndStop(pagNumber);
		}
		
		public function stop():void{
			_mc.stop();
		}
		
		public function getDocument():DisplayObject{
			return _mc;
		}
		
		public function get parent():DisplayObject{
			return _mc.parent;
		}
		
		public function get totalFrames():int{
			return _mc.totalFrames;
		}
		
		public function get framesLoaded():int{
			return _mc.framesLoaded;
		}
		
		public function get textSnapshot():TextSnapshot{
			return _mc.textSnapshot;
		}
		
		public function set alpha(value:Number):void{
			_mc.alpha = value;
		}
		
		public function get currentFrame():int{
			return _mc.currentFrame;
		}
		
		public function get height():Number{
			return _mc.height;
		}
		
		public function get width():Number{
			return _mc.width;
		}
		
		public function get loaderInfo():LoaderInfo{
			return _mc.loaderInfo;
		}
		
		public function set scaleX(n:Number):void{
			_mc.scaleX = n;
		}
		
		public function set scaleY(n:Number):void{
			_mc.scaleY = n;
		}
	}
}