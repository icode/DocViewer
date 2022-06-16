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
	import com.devaldi.controls.Spinner;
	import com.log4ic.resources.MenuIcons;
	
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObject;
	import flash.display.MovieClip;
	import flash.events.MouseEvent;
	import flash.filters.DropShadowFilter;
	import flash.filters.GlowFilter;
	import flash.text.TextSnapshot;
	import flash.utils.setTimeout;
	
	import mx.controls.Image;
	
	public class DupImage extends Image
	{
		public var dupIndex:int = 0;
		public var dupScale:Number = 0;
		public var scaleWidth:int;
		public var scaleHeight:int;
		public var loadedIndex:int = -1;
		public var _paperRotation:int = 0;
		public var doAddDropShadow:Boolean = true;
		public var doAddGlowFilter:Boolean = false;
		public var glowFilterColor:uint = 0x000000;
		public static var paperSource:DisplayObject; 
		private var _skinImgl:Bitmap = new MenuIcons.LOGO_SMALL();
		private var loadImg:Bitmap;
		private var loadSpinner:Spinner;
		
		public function DupImage(){}
		
		public function init():void{
			loadImg = new Bitmap();
			loadImg.bitmapData = _skinImgl.bitmapData;
			loadImg.smoothing = true;
			
			loadSpinner = new Spinner(50,50);
			loadSpinner.setStyle("spinnerType","gradientcircle");
			loadSpinner.setStyle("spinnerThickness","7");
			loadSpinner.styleName = "gradientlines";
		}
		
		public function get paperRotation():Number {
			return _paperRotation;
		}	
		
		public function set paperRotation(n:Number):void {
			if(n==90){
				var w:int = width;
				width = height;	
				height = w;
			}
			
			_paperRotation = n;
		}

		override public function get textSnapshot():TextSnapshot{
			if(getChildAt(0) is DupLoader && (getChildAt(0) as DupLoader).content!=null && (getChildAt(0) as DupLoader).content is MovieClip){
				return ((getChildAt(0) as DupLoader).content as MovieClip).textSnapshot;
			}else
				return super.textSnapshot;
		}
		
		override public function set source(value:Object):void{
			if(this.source != null && this.source is Bitmap && this.source.bitmapData != null){
				this.source.bitmapData.dispose();
			}
			
			super.source = value;
			
			if(value!=null && doAddDropShadow){
				if(this.filters.length==0){addDropShadow();}
			}
			
			if(value!=null && doAddGlowFilter){
				if(this.filters.length==0){addGlowFilter();}
			}
			
			if(value == null && hasEventListener(MouseEvent.ROLL_OVER)){
				removeEventListener(MouseEvent.ROLL_OVER,dupImageMoverHandler);
			}else{
				if(!hasEventListener(MouseEvent.ROLL_OVER))
					addEventListener(MouseEvent.ROLL_OVER,dupImageMoverHandler,false,0,true);
			}
		}
		
		override protected function createChildren():void {
			super.createChildren();
		}
		
		private function dupImageMoverHandler(event:MouseEvent):void{ // depricated.. only used when using bitmaps to render 
		    /* if(!contains(paperSource)){
				paperSource.gotoAndStop(dupIndex);
				paperSource.alpha = 0;
				addChild(paperSource);
			} */
		}
		
		public function addGlowFilter():void{
			if(this.filters.length==0){
				this.filters = null;
				var filter : GlowFilter = new GlowFilter(glowFilterColor, 1, 3, 3, 5, 1, true, false);
				this.filters = [ filter ];
			}
		}
				
		public function addDropShadow():void
		{
			if(this.filters.length==0){
			this.filters = null;
			 var filter : DropShadowFilter = new DropShadowFilter();
			 filter.blurX = 4;
			 filter.blurY = 4;
			 filter.quality = 2;
			 filter.alpha = 0.5;
			 filter.angle = 45;
			 filter.color = 0x202020;
			 filter.distance = 4;
			 filter.inner = false;
			 this.filters = [ filter ];
			}
		}			
		
		public function removeAllChildren():void{
			while(numChildren > 0)
				delete(removeChildAt(0));
			
			this.filters = null;
		}
		
		override public function addChild(child:DisplayObject):DisplayObject{
			//flash.utils.setTimeout(addDropShadow,200);
			super.addChildAt(child,0);
			
			while(numChildren >= 2)
				delete(removeChildAt(1));
			
			return super.getChildAt(0);
		}
		
		override public function addChildAt(child:DisplayObject,index:int):DisplayObject{
			return super.addChildAt(child,index);
		}
		
		public function addBlankChildAt(child:DisplayObject,index:int):DisplayObject{
			return super.addChildAt(child,index);
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void {
			if(w>0&&h>0){
			try{

			if(_paperRotation!=90||_paperRotation==180){
				graphics.beginFill(0xffffff,1);
				graphics.drawRect(0,0,w,h);
			}else{
				graphics.clear();
			}
			
			super.updateDisplayList(w,h);}catch (e:*) {}}
			
		}	 
		
		public function addGlowShadow():void
		{
			
		}
		
		public function getScaledHeight():Number
		{
			return scaleX>0? unscaledHeight * this.scaleX:height;
		}		
		
		public function resetPage(width:Number,height:Number,scale:Number,showSpinner:Boolean=false):void{
			
			if(loadImg.parent != this)
			{
				removeAllChildren();
				
				loadImg.x = width/2 - 80;
				loadImg.y = height/2 - 50;
				scaleX = scaleY = scale;
				addBlankChildAt(loadImg,numChildren);
			}
			
			if(loadSpinner.parent != this && showSpinner){
				loadSpinner.x = width/2-25;
				loadSpinner.y = height/2-125;
				loadSpinner.start();
				scaleX = scaleY = scale;
				addBlankChildAt(loadSpinner,numChildren);
			}	
			
		}
	}
}