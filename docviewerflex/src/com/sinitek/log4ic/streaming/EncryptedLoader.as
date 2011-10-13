/**
 * @author: ÕÅÁ¢öÎ
 * @version: 1
 * @date: 11-8-26 ÏÂÎç3:36
 */
package com.sinitek.log4ic.streaming {
import com.sinitek.log4ic.utils.security.Security;
import com.sinitek.log4ic.utils.security.XXTEA;

import flash.events.Event;

import flash.net.URLLoader;
import flash.net.URLLoaderDataFormat;
import flash.net.URLRequest;
import flash.utils.ByteArray;

import mx.controls.Alert;

public class EncryptedLoader extends URLLoader {
    public function EncryptedLoader(key:ByteArray = null) {
        this._key = key;
        this.addEventListener(Event.COMPLETE, function(e:Event):void {
            _loaded = true;
            if(_key){
                _decryptedData = XXTEA.decrypt(data, _key);
            }
        });
    }

    private var _loaded:Boolean = false;
    private var _key:ByteArray = null;
    private var _decryptedData;

    public function get loaded():Boolean {
        return _loaded;
    }

    public override function load(request:flash.net.URLRequest):void {
        this.dataFormat = URLLoaderDataFormat.BINARY;

        this._loaded = false;

        super.load(request);
    }


    public function get decryptedData():* {
        return _decryptedData;
    }

    public function get key():ByteArray {
        return _key;
    }

    public function set key(value:ByteArray):void {
        _key = value;
    }
}
}
