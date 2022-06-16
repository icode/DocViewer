/**
 * @author: 张立鑫
 * @version: 1
 */
package com.log4ic.streaming {
import com.devaldi.streaming.DupLoader;
import com.log4ic.utils.security.XXTEA;

import flash.system.LoaderContext;

import flash.utils.ByteArray;

import mx.controls.Alert;

[Event(name="onDocumentLoadedError", type="flash.events.ErrorEvent")]
[Event(name="onLoadersLoaded", type="flash.events.Event")]
[Event(name="onSwfLoaded", type="com.devaldi.events.SwfLoadedEvent")]

public class EncryptedLoader extends DupLoader {
    private static var secretKey:ByteArray = null;
    private static var _secretKey:String = null;

    public static function set SecretKey(key:String):void{
        if (key) {
            _secretKey = key;
            secretKey = new ByteArray();
            secretKey.writeUTFBytes(key);
        }
    }

    public static function get SecretKey():String{
        return _secretKey;
    }



    override public function loadBytes(bytes:ByteArray, context:LoaderContext = null):void {
        if (secretKey) {
            bytes = XXTEA.decrypt(bytes, secretKey);
            Alert.show(secretKey.toString());
        }
        super.loadBytes(bytes, context);
    }
}
}
