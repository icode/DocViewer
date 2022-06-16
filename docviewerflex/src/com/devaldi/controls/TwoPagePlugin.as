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

package com.devaldi.controls
{
	import com.devaldi.controls.flexpaper.FitModeEnum;
	import com.devaldi.controls.flexpaper.IFlexPaperViewModePlugin;
	import com.devaldi.controls.flexpaper.ShapeMarker;
	import com.devaldi.controls.flexpaper.Viewer;
	import com.devaldi.controls.flexpaper.utils.StreamUtil;
	import com.devaldi.events.CurrentPageChangedEvent;
	import com.devaldi.streaming.DupLoader;
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.events.MouseEvent;
	import flash.utils.setTimeout;
	
	import mx.containers.HBox;
	import mx.core.Container;
	import mx.core.UIComponent;

	public class TwoPagePlugin implements IFlexPaperViewModePlugin, IEventDispatcher{
		private var dispatcher:IEventDispatcher = new EventDispatcher();
		private var viewer:Viewer;
		private var _saveScale:Number = 1;
		private var _hasinitialized:Boolean = false;
		
		public function TwoPagePlugin(){
			
		}	
		
		public function get Name():String{
			return "TwoPage";
		}
		
		public function get SaveScale():Number{
			return _saveScale;
		}
		
		public function set SaveScale(n:Number):void{
			_saveScale = n;
		}
		
		public function setViewMode(s:String):void{
		}
		
		public function addChild(i:int,o:DisplayObject):void{
			if(i==0 || i==1){
				viewer.DisplayContainer.addChild(o);
			}
		}
		
		public function renderPage(i:Number):void{
			var rp:int = 0;
			rp = (i % 2 == 0)?0:1;
			var uloaderidx:int = (i % 2 == 0)?0:1;
			
			if(!viewer.BusyLoading && viewer.DocLoader.LoaderList!=null && viewer.DocLoader.LoaderList.length>0){
				if(viewer.libMC!=null&&viewer.numPagesLoaded>=viewer.PageList[i].dupIndex && 
					viewer.DocLoader.LoaderList[uloaderidx] != null && 
					viewer.DocLoader.LoaderList[uloaderidx].content==null||(viewer.DocLoader.LoaderList[uloaderidx].content!=null&&viewer.DocLoader.LoaderList[uloaderidx].content.framesLoaded<viewer.PageList[rp].dupIndex)){
					
					viewer.BusyLoading = true;
					viewer.DocLoader.LoaderList[uloaderidx].loadBytes(viewer.DocLoader.InputBytes,StreamUtil.getExecutionContext());
					flash.utils.setTimeout(viewer.repositionPapers,200);
				}
			}
			
			if(viewer.PageList[rp]!=null && viewer.DocLoader.LoaderList[uloaderidx]!=null && viewer.DocLoader.LoaderList[uloaderidx].content!=null){
				viewer.DocLoader.LoaderList[uloaderidx].content.gotoAndStop(i+1);
				viewer.PageList[rp].addChild(viewer.DocLoader.LoaderList[uloaderidx]);
				viewer.PageList[rp].loadedIndex = i+1;
				viewer.PageList[rp].dupIndex = i+1; 
			}
		}
		
		public function renderSelection(i:int,marker:ShapeMarker):void{
			var rp:int = (i % 2 == 0)?0:1;
			if(marker!=null && marker.parent != null && viewer.PageList[rp] !=null){
				if(i+1 == viewer.SearchPageIndex && marker.parent != viewer.PageList[rp]){
					viewer.PageList[rp].addChildAt(marker,viewer.PageList[rp].numChildren);
				}else if(i+1 == viewer.SearchPageIndex && marker.parent == viewer.PageList[rp]){
					viewer.PageList[rp].setChildIndex(marker,viewer.PageList[rp].numChildren -1);
				}
			}
		}
		
		public function renderMark(sm:UIComponent,pageIndex:int):void{
			var rp:int = (pageIndex % 2 == 0)?0:1;
			
			viewer.PageList[rp].addChildAt(sm,viewer.PageList[rp].numChildren);
		}
		
		public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
			dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
		}
		
		public function checkIsVisible(pageIndex:int):Boolean{
			return (pageIndex==viewer.currPage-1) || (pageIndex==viewer.currPage);
		}
		
		public function dispatchEvent(event:Event):Boolean {
			return true;	
		}
		
		public function get currentPage():int{
			if(viewer.currPage==0)
				viewer.currPage = 1;
			
			return viewer.currPage;
		}
		
		public function get doZoom():Boolean{
			return true;
		}
		
		public function get doFitHeight():Boolean{
			return true;
		}
		
		public function get doFitWidth():Boolean{
			return false;
		}
		
		public function get loaderListLength():int{
			return 2;
		}
		
		public function get supportsTextSelect():Boolean{
			return true;
		}
		
		public function gotoPage(page:Number,adjGotoPage:int=0):void{
			if(viewer.currPage == page || page % 2 == 0){return;}
			
			if(page % 2 == 0 && page > viewer.currPage){
				page = page + 1;
			}else if(page % 2 == 0 && page < viewer.currPage){
				page = page - 1;
			}
			
			viewer.currPage = page;
			viewer.dispatchEvent(new CurrentPageChangedEvent(CurrentPageChangedEvent.PAGE_CHANGED,page));
		}
		
		public function mvPrev():void{
			if(viewer.currPage>1){viewer.gotoPage(viewer.currPage-2,0);}
		}
		
		public function mvNext():void{
			if(viewer.currPage+2<=viewer.numPages){viewer.gotoPage(viewer.currPage+2,0);}
		}
		
		public function handleDoubleClick(event:MouseEvent):void{
				viewer.FitMode = FitModeEnum.FITHEIGHT;
		}
		
		public function handleMouseDown(event:MouseEvent):void{
			if(Number(viewer.Scale) <= viewer.getFitHeightFactor()){
				viewer.Zoom(viewer.MaxZoomSize/3);
			}else{
				viewer.FitMode = FitModeEnum.FITHEIGHT;
			}
		}
		
		public function hasEventListener(type:String):Boolean {
			return dispatcher.hasEventListener(type);
		}
		
		public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
			dispatcher.removeEventListener(type, listener, useCapture);
		}
		
		public function willTrigger(type:String):Boolean {
			return dispatcher.willTrigger(type);
		}
		
		public function initComponent(v:Viewer):Boolean{
			viewer = v;
			viewer.DisplayContainer = new HBox();
			viewer.DisplayContainer.setStyle("horizontalAlign", "center");
			viewer.DisplayContainer.setStyle("horizontalGap",1);
			viewer.PaperContainer.childrenDoDrag = true;
			viewer.PaperContainer.addChild(viewer.DisplayContainer);
			viewer.PaperContainer.addEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler, true,0,true);
			viewer.FitPageOnLoad = true;
			viewer.DisplayContainer.visible = false;
			viewer.DisplayContainer.addEventListener(Event.RENDER,doRender,false,0,true);
			
			_hasinitialized = false;
			return true;
		}
		
		private function mouseMoveHandler(evt:MouseEvent):void{
			if(evt.target is DupLoader && viewer.FitMode != FitModeEnum.FITHEIGHT){
				viewer.PaperContainer.verticalScrollPosition = viewer.PaperContainer.maxVerticalScrollPosition * (viewer.PaperContainer.mouseY/viewer.PaperContainer.height);
				viewer.PaperContainer.horizontalScrollPosition = viewer.PaperContainer.maxHorizontalScrollPosition * (viewer.PaperContainer.mouseX/viewer.PaperContainer.width);
			}
		}
		
		private function doRender(e:Event):void{
			if(!_hasinitialized){
				viewer.FitMode = FitModeEnum.FITHEIGHT;
				_hasinitialized = true;
			}
		}		
		
		public function initOnLoading():void{
			viewer.BusyLoading = true; 
			viewer.DocLoader.LoaderList[0].loadBytes(viewer.libMC.loaderInfo.bytes,StreamUtil.getExecutionContext());
		}
	}
}