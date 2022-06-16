/**
 * @author: 张立鑫
 * @version: 1
 */
package com.log4ic.utils.security {

import flash.utils.ByteArray;
import flash.utils.Endian;

public class XXTEA {
    private static const delta:uint = uint(0x9E3779B9);

    private static function LongArrayToByteArray(data:Array, includeLength:Boolean):ByteArray {
        var length:uint = data.length;
        var n:uint = (length - 1) << 2;
        if (includeLength) {
            var m:uint = data[length - 1];
            if ((m < n - 3) || (m > n)) {
                return null;
            }
            n = m;
        }
        var result:ByteArray = new ByteArray();
        result.endian = Endian.LITTLE_ENDIAN;
        for (var i:uint = 0; i < length; i++) {
            result.writeUnsignedInt(data[i]);
        }
        if (includeLength) {
            result.length = n;
            return result;
        }
        else {
            return result;
        }
    }

    private static function ByteArrayToLongArray(data:ByteArray, includeLength:Boolean):Array {
        var length:uint = data.length;
        var n:uint = length >> 2;
        if (length % 4 > 0) {
            n++;
            data.length += (4 - (length % 4));
        }
        data.endian = Endian.LITTLE_ENDIAN;
        data.position = 0;
        var result:Array = [];
        for (var i:uint = 0; i < n; i++) {
            result[i] = data.readUnsignedInt();
        }
        if (includeLength) {
            result[n] = length;
        }
        data.length = length;
        return result;
    }

    public static function encrypt(data:ByteArray, key:ByteArray):ByteArray {
        if (data.length == 0) {
            return new ByteArray();
        }
        var v:Array = ByteArrayToLongArray(data, true);
        var k:Array = ByteArrayToLongArray(key, false);
        if (k.length < 4) {
            k.length = 4;
        }
        var n:uint = v.length - 1;
        var z:uint = v[n];
        var y:uint = v[0];
        var mx:uint;
        var e:uint;
        var p:uint;
        var q:uint = uint(6 + 52 / (n + 1));
        var sum:uint = 0;
        while (0 < q--) {
            sum = sum + delta;
            e = sum >>> 2 & 3;
            for (p = 0; p < n; p++) {
                y = v[p + 1];
                mx = (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
                z = v[p] = v[p] + mx;
            }
            y = v[0];
            mx = (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            z = v[n] = v[n] + mx;
        }
        return LongArrayToByteArray(v, false);
    }

    public static function decrypt(data:ByteArray, key:ByteArray):ByteArray {
        if (data.length == 0) {
            return new ByteArray();
        }
        var v:Array = ByteArrayToLongArray(data, false);
        var k:Array = ByteArrayToLongArray(key, false);
        if (k.length < 4) {
            k.length = 4;
        }
        var n:uint = v.length - 1;
        var z:uint = v[n - 1];
        var y:uint = v[0];
        var mx:uint;
        var e:uint;
        var p:uint;
        var q:uint = uint(6 + 52 / (n + 1));
        var sum:uint = q * delta;
        while (sum != 0) {
            e = sum >>> 2 & 3;
            for (p = n; p > 0; p--) {
                z = v[p - 1];
                mx = (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
                y = v[p] = v[p] - mx;
            }
            z = v[n];
            mx = (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            y = v[0] = v[0] - mx;
            sum = sum - delta;
        }
        return LongArrayToByteArray(v, true);
    }
}
}
