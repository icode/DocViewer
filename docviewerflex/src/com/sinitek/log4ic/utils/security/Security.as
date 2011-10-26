/**
 * @author: 张立鑫
 * @version: 1
 */
package com.sinitek.log4ic.utils.security {
import com.sinitek.log4ic.streaming.EncryptedLoader;

import flash.events.Event;
import flash.events.ProgressEvent;
import flash.events.TimerEvent;
import flash.net.URLLoader;
import flash.net.URLLoaderDataFormat;
import flash.net.URLRequest;
import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.Timer;
import flash.utils.clearTimeout;
import flash.utils.setTimeout;

import mx.controls.Alert;

public class Security {

    private static var loaderMap:Dictionary = new Dictionary();


    public static function loadEncryptedFile(url:URLRequest, key:String, onComplete:Function, onProgress:Function):void {

        var loader:EncryptedLoader = loaderMap[url.url];

        if (loader) {
            if(loader.hasEventListener(Event.COMPLETE)){
                loader.removeEventListener(Event.COMPLETE, onComplete);
            }
            loader.addEventListener(Event.COMPLETE, onComplete);
            if (loader.loaded) {
                loader.dispatchEvent(new Event(ProgressEvent.PROGRESS));
                loader.dispatchEvent(new Event(Event.COMPLETE));
            }
            return;
        }
        var keyBytes:ByteArray = null;
        if (key) {
            keyBytes = new ByteArray();
            keyBytes.writeUTFBytes(key);
        }

        var urlLoader:EncryptedLoader = new EncryptedLoader(keyBytes);

        urlLoader.addEventListener(Event.COMPLETE, onComplete);
        if (onProgress) {
            urlLoader.addEventListener(ProgressEvent.PROGRESS, onProgress);
        }

        loaderMap[url.url] = urlLoader;

        urlLoader.load(url);
    }

    public static function isLoaded(url:URLRequest) {
        return !!loaderMap[url];
    }
}
}
