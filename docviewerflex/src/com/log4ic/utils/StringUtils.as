/**
 * @author: 张立鑫
 * @version: 1
 */
package com.log4ic.utils {
public class StringUtils {
    //忽略大小字母比较字符是否相等;
    public static function equalsIgnoreCase(char1:String, char2:String):Boolean {
        return char1.toLowerCase() == char2.toLowerCase();
    }

    //比较字符是否相等;
    public static function equals(char1:String, char2:String):Boolean {
        return char1 == char2;
    }

    //是否为Email地址;
    public static function isEmail(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char);
        var pattern:RegExp = /(\w|[_.\-])+@((\w|-)+\.)+\w{2,4}+/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //是否是数值字符串;
    public static function isNumber(char:String):Boolean {
        if (char == null) {
            return false;
        }
        return !(isNaN(Number(char)))
    }

    //是否为Double型数据;
    public static function isDouble(char:String):Boolean {
        char = trim(char);
        var pattern:RegExp = /^[-\+]?\d+(\.\d+)?$/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //Integer;
    public static function isInteger(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char);
        var pattern:RegExp = /^[-\+]?\d+$/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //English;
    public static function isEnglish(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char);
        var pattern:RegExp = /^[A-Za-z]+$/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //中文;
    public static function isChinese(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char);
        var pattern:RegExp = /^[\u0391-\uFFE5]+$/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //双字节
    public static function isDoubleChar(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char);
        var pattern:RegExp = /^[^\x00-\xff]+$/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //含有中文字符
    public static function hasChineseChar(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char);
        var pattern:RegExp = /[^\x00-\xff]/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //注册字符;
    public static function hasAccountChar(char:String, len:uint = 15):Boolean {
        if (char == null) {
            return false;
        }
        if (len < 10) {
            len = 15;
        }
        char = trim(char);
        var pattern:RegExp = new RegExp("^[a-zA-Z0-9][a-zA-Z0-9_-]{0," + len + "}$", "");
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    //URL地址;
    public static function isURL(char:String):Boolean {
        if (char == null) {
            return false;
        }
        char = trim(char).toLowerCase();
        var pattern:RegExp = /^http:\/\/[A-Za-z0-9]+\.[A-Za-z0-9]+[\/=\?%\-&_~`@[\]\':+!]*([^<>\"\"])*$/;
        var result:Object = pattern.exec(char);
        if (result == null) {
            return false;
        }
        return true;
    }

    // 是否为空白;
    public static function isWhitespace(char:String):Boolean {
        switch (char) {
            case " ":
            case "\t":
            case "\r":
            case "\n":
            case "\f":
                return true;
            default:
                return false;
        }
    }

    //去左右空格;
    public static function trim(char:String):String {
        if (char == null) {
            return null;
        }
        return rtrim(ltrim(char));
    }

    //去左空格;
    public static function ltrim(char:String):String {
        if (char == null) {
            return null;
        }
        var pattern:RegExp = /^\s*/;
        return char.replace(pattern, "");
    }

    //去右空格;
    public static function rtrim(char:String):String {
        if (char == null) {
            return null;
        }
        var pattern:RegExp = /\s*$/;
        return char.replace(pattern, "");
    }

    //是否为前缀字符串;
    public static function beginsWith(char:String, prefix:String):Boolean {
        return (prefix == char.substring(0, prefix.length));
    }

    //是否为后缀字符串;
    public static function endsWith(char:String, suffix:String):Boolean {
        return (suffix == char.substring(char.length - suffix.length));
    }

    //去除指定字符串;
    public static function remove(char:String, remove:String):String {
        return replace(char, remove, "");
    }

    //字符串替换;
    public static function replace(char:String, replace:String, replaceWith:String):String {
        return char.split(replace).join(replaceWith);
    }

    //utf16转utf8编码;
    public static function utf16to8(char:String):String {
        var out:Array = new Array();
        var len:uint = char.length;
        for (var i:uint = 0; i < len; i++) {
            var c:int = char.charCodeAt(i);
            if (c >= 0x0001 && c <= 0x007F) {
                out[i] = char.charAt(i);
            } else if (c > 0x07FF) {
                out[i] = String.fromCharCode(0xE0 | ((c >> 12) & 0x0F),
                        0x80 | ((c >> 6) & 0x3F),
                        0x80 | ((c >> 0) & 0x3F));
            } else {
                out[i] = String.fromCharCode(0xC0 | ((c >> 6) & 0x1F),
                        0x80 | ((c >> 0) & 0x3F));
            }
        }
        return out.join('');
    }

    //utf8转utf16编码;
    public static function utf8to16(char:String):String {
        var out:Array = new Array();
        var len:uint = char.length;
        var i:uint = 0;
        while (i < len) {
            var c:int = char.charCodeAt(i++);
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    // 0xxxxxxx
                    out[out.length] = char.charAt(i - 1);
                    break;
                case 12:
                case 13:
                    // 110x xxxx   10xx xxxx
                    var char2:int = char.charCodeAt(i++);
                    out[out.length] = String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    // 1110 xxxx  10xx xxxx  10xx xxxx
                    var char2:int = char.charCodeAt(i++);
                    var char3:int = char.charCodeAt(i++);
                    out[out.length] = String.fromCharCode(((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                    break;
            }
        }
        return out.join('');
    }


    function StringUtils() {
        throw new Error("StringUtil class is static container only");
    }

}
}
