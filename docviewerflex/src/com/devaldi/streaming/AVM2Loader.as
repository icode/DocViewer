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

Inspired by the ForcibleLoader class by 
BeInteractive! (www.be-interactive.org)
*/

package com.devaldi.streaming
{
        import com.devaldi.events.SwfLoadedEvent;
        
        import flash.display.DisplayObject;
        import flash.display.Loader;
        import flash.errors.EOFError;
        import flash.events.ErrorEvent;
        import flash.events.Event;
        import flash.events.EventDispatcher;
        import flash.events.IEventDispatcher;
        import flash.events.IOErrorEvent;
        import flash.events.ProgressEvent;
        import flash.events.SecurityErrorEvent;
        import flash.net.URLRequest;
        import flash.net.URLStream;
        import flash.system.LoaderContext;
        import flash.utils.ByteArray;
        import flash.utils.Endian;
        
        /**
         * Usage:
         * <pre>
         * var loader:Loader = Loader(addChild(new Loader()));
         * var fLoader:ForcibleLoader = new ForcibleLoader(loader);
         * fLoader.load(new URLRequest('swf7.swf'));
         * </pre>
         */
		
		[Event(name="onDocumentLoadedError", type="flash.events.ErrorEvent")]
		[Event(name="onLoadersLoaded", type="flash.events.Event")]
		[Event(name="onSwfLoaded", type="com.devaldi.events.SwfLoadedEvent")]
		
        public class AVM2Loader implements IDocumentLoader
        {
				private var dispatcher:IEventDispatcher = new EventDispatcher();
				private var _loader:Loader = new Loader();
				private var _stream:URLStream;
				private var _inputBytes:ByteArray;
				private var _loaderCtx:LoaderContext;
				private var _resigned:Boolean = false;
				private var _bytesPending:uint = 0;
				private var _prevLength:uint = 0;
				private var _attempts:Number = 0;
				private var _doretry:Boolean = true;
				private var _request:URLRequest;
				public var version:uint = 0;
				private var numframes:int = -1;
				private var _progressive:Boolean = false;
				private var _loaderList:Array;
				private var _pagesSplit:Boolean = false;
				
                public function AVM2Loader(loaderCtx:LoaderContext, progressive:Boolean)
                {
                        _loaderCtx = loaderCtx;
                        _progressive = progressive;
						resetURLStream();
                }
				
				public function resetURLStream():void{
					
					if(_stream!=null){
						try{if(stream.connected){_stream.close();}
						}catch(e:Error){}
						
						if(!_progressive){
							_stream.removeEventListener(Event.COMPLETE, completeHandler);
							_stream.removeEventListener(ProgressEvent.PROGRESS , nonProgressiveProgress);
						}else{
							_stream.removeEventListener(ProgressEvent.PROGRESS , streamProgressHandler);
							_stream.removeEventListener(Event.COMPLETE, streamCompleteHandler);
						}
						
						_stream.removeEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
						
						_loader.unloadAndStop(true);
						_loader.contentLoaderInfo.removeEventListener(Event.COMPLETE, loaderComplete);
						_loader.contentLoaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
						_loader = new Loader();

					}
					
					_stream = new URLStream();
					
					if(!_progressive){
						_stream.addEventListener(Event.COMPLETE, completeHandler,false,0,true);
						_stream.addEventListener(ProgressEvent.PROGRESS , nonProgressiveProgress,false,0,true);
					}else{
						_stream.addEventListener(ProgressEvent.PROGRESS , streamProgressHandler,false,0,true);
						_stream.addEventListener(Event.COMPLETE, streamCompleteHandler,false,0,true);
					}
					
					_stream.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
					_stream.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
					_loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loaderComplete);
					_loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
				}
				
				private function loaderComplete(event:Event):void{
					dispatchEvent(new SwfLoadedEvent(SwfLoadedEvent.SWFLOADED,event.currentTarget));
				}
				
				public function get PagesSplit():Boolean{
					return _pagesSplit;
				}
				
				public function set PagesSplit(b:Boolean):void{
					_pagesSplit = b;
				}
				
				public function get LoaderList():Array{
					return _loaderList;
				}
				
				public function set LoaderList(v:Array):void{
					_loaderList = v;
				}				
                
                public function get Resigned():Boolean{
                	return _resigned;
                }

				public function get DocumentContainer():DisplayObject{
					return _loader;
				}
				
				public function get InputBytes():ByteArray {
					return _inputBytes;
				}
				
				public function set InputBytes(b:ByteArray):void {
					_inputBytes = b;
				}					
				
                public function get stream():URLStream
                {
                        return _stream;
                }
                
                public function get loader():Loader
                {
                        return _loader;
                }
                
                public function set loader(value:Loader):void
                {
                        _loader = value;
                }
                
                public function load(request:URLRequest, loaderCtx:LoaderContext):void
                {
					//resetURLStream();
					
					_attempts++;					
					_request = request;
                    _stream.load(request);
                    _inputBytes = new ByteArray();
					
					// wait with this one.. seems a bit dodgy
					//flash.utils.setTimeout(retry,7000);
                }
				
				private function retry():void{
					if(_attempts<4 && _doretry){
						_attempts++;
						_stream.load(_request);
						flash.utils.setTimeout(retry,7000);
					}
				}
                
                private function streamCompleteHandler(event:Event):void{
                	flash.utils.setTimeout(confirmBytesLoaded,500);
					_stream.close();
                }
                
                private function confirmBytesLoaded():void{
                	try{
						_stream.readBytes(_inputBytes,_inputBytes.length);
						_loader.unloadAndStop(true);
						_loader.loadBytes(_inputBytes,_loaderCtx);
					}
					catch(e:Error){ // on error try again, version 9 of flash player sometimes fails.
						_loader.unloadAndStop(true);
						_loader.loadBytes(_inputBytes,_loaderCtx);
					}
                }
                
				private function nonProgressiveProgress(event:ProgressEvent):void{
					_doretry = (_loader.contentLoaderInfo==null || _loader.contentLoaderInfo.bytesLoaded<2000);
				}
				
                private function streamProgressHandler(event:ProgressEvent):void{
                	//if there are no bytes do nothing
					_stream.readBytes(_inputBytes,_inputBytes.length);_bytesPending = _inputBytes.length - _prevLength;
					
					if(_inputBytes.length>4){
						version = uint(_inputBytes[3]);
						
						if (version <= 9) {
							updateVersion(9,_inputBytes);
						}					
					}
					
					if(_bytesPending > (event.bytesTotal / 10) || (_loader.contentLoaderInfo != null && event.bytesTotal == _bytesPending + _loader.contentLoaderInfo.bytesLoaded)) {
						_bytesPending = 0;
                		_prevLength = _inputBytes.length;
                		try{
							_loader.unloadAndStop(true);
							_loader.loadBytes(_inputBytes,_loaderCtx);}
						catch(e:Error){ // on error try again, version 9 of flash player sometimes fails.
							_loader.unloadAndStop(true);
							_loader.loadBytes(_inputBytes,_loaderCtx);
						}
                	}

					_doretry = (_loader.contentLoaderInfo==null || _loader.contentLoaderInfo.bytesLoaded<2000);					
                }
                
                private function completeHandler(event:Event):void
                {
                        _inputBytes = new ByteArray();
                        _stream.readBytes(_inputBytes);
                        _stream.close();
                        _inputBytes.endian = Endian.LITTLE_ENDIAN;
                        
                        if (isCompressed(_inputBytes)) {
                                uncompress(_inputBytes);
                        }
                        
                        version = uint(_inputBytes[3]);
						
                        if (version <= 9) {
                                if (version == 8 || version == 9) {
                                        flagSWF9Bit(_inputBytes);
                                }else if (version <= 7) {
                                        insertFileAttributesTag(_inputBytes);
                                }
                                updateVersion(9,_inputBytes);
                        }
                        
						loader.unloadAndStop(true);
                        loader.loadBytes(_inputBytes,_loaderCtx);
                }
                
                public function isCompressed(bytes:ByteArray):Boolean
                {
				    return bytes[0] == 0x43;
                }
                
                private function uncompress(bytes:ByteArray):void
                {
                        var cBytes:ByteArray = new ByteArray();
                        cBytes.writeBytes(bytes, 8);
                        bytes.length = 8;
                        bytes.position = 8;
                        cBytes.uncompress();
                        bytes.writeBytes(cBytes);
                        bytes[0] = 0x46;
                        cBytes.length = 0;
                }

				private function getBodyPosition(bytes:ByteArray):uint
                {
                        var result:uint = 0;
                        
                        result += 3; // FWS/CWS
                        result += 1; // version(byte)
                        result += 4; // length(32bit-uint)
                        
                        var rectNBits:uint = bytes[result] >>> 3;
                        result += (5 + rectNBits * 4) / 8; // stage(rect)
                        
                        result += 2;
                        
                        result += 1; // frameRate(byte)
                        result += 2; // totalFrames(16bit-uint)
                        
                        return result;
                }
                
                private function findFileAttributesPosition(offset:uint, bytes:ByteArray):int
                {
                        bytes.position = offset;
                        
                        try {
                                for (;;) {
                                        var byte:uint = bytes.readShort();
                                        var tag:uint = byte >>> 6;
                                        if (tag == 69) {
                                                return bytes.position - 2;
                                        }
                                        var length:uint = byte & 0x3f;
                                        if (length == 0x3f) {
                                                length = bytes.readInt();
                                        }
                                        bytes.position += length;
                                }
                        }
                        catch (e:EOFError) {
                        }
                        
                        return -1;
                }
				
				public function postProcessBytes(bytes:ByteArray):void{
					flagSWF9Bit(bytes);
				}
                
                public function flagSWF9Bit(bytes:ByteArray):void
                {
                        var pos:int = findFileAttributesPosition(getBodyPosition(bytes), bytes);
						
						if (pos != -1) {
							bytes[pos + 2] |= 0x08;
						}
						else {
							insertFileAttributesTag(bytes);
						}
                }
				
                public function signFileHeader(bytes:ByteArray, ldr:Loader=null):void{
                	_resigned=true;
	                insertFileAttributesTag(bytes);
					
					if(ldr!=null)
					{
						ldr.unloadAndStop(true);
						ldr.loadBytes(bytes,_loaderCtx);
					}
					else{
						_loader.unloadAndStop(true);
						_loader.loadBytes(bytes,_loaderCtx);
					}
                }
                
                private function insertFileAttributesTag(bytes:ByteArray):void
                {
                        var pos:uint = getBodyPosition(bytes);
                        var afterBytes:ByteArray = new ByteArray();
                        afterBytes.writeBytes(bytes, pos);
                        bytes.length = pos;
                        bytes.position = pos;
                        bytes.writeByte(0x44);
                        bytes.writeByte(0x11);
                        bytes.writeByte(0x08);
                        bytes.writeByte(0x00);
                        bytes.writeByte(0x00);
                        bytes.writeByte(0x00);
                        bytes.writeBytes(afterBytes);
                        afterBytes.length = 0;
                }
                
                public function updateVersion(version:uint, b:ByteArray):void
                {
                        b[3] = version;
                }
				
				public function dispatchEvent(event:Event):Boolean {
					return dispatcher.dispatchEvent(event);
				}
				
				public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
					dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
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
                
                private function ioErrorHandler(event:IOErrorEvent):void
                {
					var evt:ErrorEvent = new ErrorEvent("onDocumentLoadedError");
					evt.text = event.text;
                	dispatchEvent(evt);
                }
                
                private function securityErrorHandler(event:SecurityErrorEvent):void
                {
					var evt:ErrorEvent = new ErrorEvent("onDocumentLoadedError");
					evt.text = event.text;
					dispatchEvent(evt);
                }
        }
}
