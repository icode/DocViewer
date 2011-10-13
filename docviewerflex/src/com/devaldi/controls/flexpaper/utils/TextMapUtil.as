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

package com.devaldi.controls.flexpaper.utils
{
	import com.devaldi.streaming.IGenericDocument;
	import com.devaldi.streaming.SwfDocument;
	
	import flash.display.MovieClip;
	
	public class TextMapUtil
	{
		public static var langMap:Array;
		public static var totalFragments:String = "";
		
		public static function alw(word:String,replacement:String,len:int=1):void{
			var idx:int = langMap.length;
			langMap[idx] = new Array(3); langMap[idx][0] = word;langMap[idx][1] = replacement;langMap[idx][2] = len;  
		}
		
		public static function initLangMap(mc:IGenericDocument):void{
			if(totalFragments.length == 0){
				for(var i:int=0;i<mc.totalFrames;i++){
					mc.gotoAndStop(i+1);
					totalFragments += mc.textSnapshot.getText(0,mc.textSnapshot.charCount,false);
				}				
				
				totalFragments = totalFragments.toLowerCase();
			}
			
			if(langMap==null){
				langMap = new Array();
				
				TextMapUtil.alw(" *or ", "f");
				TextMapUtil.alw(" *rom ", "fr",2);
				TextMapUtil.alw(" o* ", "f ",2);
				TextMapUtil.alw(" ab*e ", "le",2);
				TextMapUtil.alw(" a*d ", "n");
				TextMapUtil.alw(" *ree ", "fre",3);
				TextMapUtil.alw("*uture", "fut",3);
				TextMapUtil.alw("inc*u", "lu",2)
				TextMapUtil.alw(" usua* ", "l")
				TextMapUtil.alw("wor*d", "l");
				TextMapUtil.alw(" ear*y ", "l")
				TextMapUtil.alw(" cou*d ", "ld",2);
				TextMapUtil.alw(" shou*d ", "l");
				TextMapUtil.alw("Whi*e", "l");
				TextMapUtil.alw("per*ormance", "for",3);
				TextMapUtil.alw("*ater", "l");
				TextMapUtil.alw("common*y", "l");
				TextMapUtil.alw("ex*ort", "p");
				TextMapUtil.alw("ora*e", "g");
				TextMapUtil.alw("su*ar", "g");
				TextMapUtil.alw(" a*ong ", "long",3);
				TextMapUtil.alw("im*ort", "p");
				TextMapUtil.alw("dai*y", "ly",2);
				TextMapUtil.alw("e*change", "x");
				TextMapUtil.alw("*lobal", "g");
				TextMapUtil.alw("g*oba*", "l");
				TextMapUtil.alw("*arket", "m");
				TextMapUtil.alw("e*ports", "x");
				TextMapUtil.alw("o*tions", "p");
				TextMapUtil.alw("singa*ore", "p");
				TextMapUtil.alw("*roduct", "p");
				TextMapUtil.alw("**oor ", "fl",2);
				TextMapUtil.alw(" **ows ", "fl",2);
				TextMapUtil.alw("**exible", "fl");
				TextMapUtil.alw("**nancial", "fi");
				TextMapUtil.alw(" *rst", "fi");
				TextMapUtil.alw("commercia* ", "l");
				TextMapUtil.alw("g*oba*", "l");
				TextMapUtil.alw(" wor*d ", "l");
				TextMapUtil.alw(" an* ", "d");
				TextMapUtil.alw(" *now ", "k");
				TextMapUtil.alw("pac*age", "k")
				TextMapUtil.alw(" *nown ", "k");
				TextMapUtil.alw(" *ade ", "m");
				TextMapUtil.alw(" na*ed ", "m");
				TextMapUtil.alw(" *ore ", "m");
				TextMapUtil.alw(" na*e ", "m"); 
				TextMapUtil.alw(" *ade ", "m"); 
				TextMapUtil.alw(" beca*e ", "m");
				TextMapUtil.alw(" beco*e ", "m");
				TextMapUtil.alw(" be*ow ", "lo",2);
				TextMapUtil.alw(" whi*e ", "le",2);
				TextMapUtil.alw(" we** ", "l");
				TextMapUtil.alw(" wi** ", "l");
				TextMapUtil.alw("bra*il", "z");
				TextMapUtil.alw("e*clusi", "x");
				TextMapUtil.alw("inde*", "x");
				TextMapUtil.alw("e*change", "x");
				TextMapUtil.alw("ac*uisition", "q");
				TextMapUtil.alw("organi*at", "z");
				TextMapUtil.alw("co*ee", "ff");
				TextMapUtil.alw(" *n ", "n");
				TextMapUtil.alw(" reduci*g ", "n");
				TextMapUtil.alw(" e*ergy ", "n"); 
			}
		}
		
		public static function findLangMapMatch(s:String,pos:int):int{
			
			for(var li:int=0;li<langMap.length;li++){
				if(langMap[li][2]==1&&langMap[li].length>0 && totalFragments.indexOf(StringReplaceAll(langMap[li][0],"*",s.charAt(pos)))>0){
					return li;
				}else if(langMap[li][2]==2&&langMap[li].length>0 && totalFragments.indexOf(StringReplaceAll(langMap[li][0],"*",s.charAt(pos)+s.charAt(pos+1)))>0){
					return li;
				}else if(langMap[li][2]==3&&langMap[li].length>0 && totalFragments.indexOf(StringReplaceAll(langMap[li][0],"*",s.charAt(pos)+s.charAt(pos+1)+s.charAt(pos+2)))>0){
					return li;
				}
					
			}
			
			return -1;
		}
		
		public static function checkUnicodeIntegrity(s:String, search:String=null, mc:IGenericDocument=null):String{
			TextMapUtil.initLangMap(mc);

			var lmi:int = -1;
			var bContainsErr:Boolean=false;
				
			for(var ci:int=0;ci<s.length;ci++){
				if(s.charCodeAt(ci) > 10000){
					bContainsErr = true;
					lmi = TextMapUtil.findLangMapMatch(s,ci);
					if(lmi!=-1){
						if(langMap[lmi][2]==1)
							s = StringReplaceAll(s,s.charAt(ci),langMap[lmi][1]);
						else
							s = StringReplaceAll(s,s.charAt(ci)+langMap[lmi][1].substring(1),langMap[lmi][1]);
					}else{ // default to 'f'
						s = StringReplaceAll(s,s.charAt(ci),"f");
					}
				}
			}
			
			if(bContainsErr){
				s = ((search!=null&&search.indexOf("fl")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57394),"fl"):s;
				s = ((search!=null&&search.indexOf("fi")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57345),"fi"):s;
				s = ((search!=null&&search.indexOf("fi")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57393),"fi"):s;
				s = ((search!=null&&search.indexOf("fi")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57370),"fi"):s;
				s = ((search!=null&&search.indexOf("f")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57385),"f"):s;
				s = ((search!=null&&search.indexOf("f")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57374),"f"):s;
				s = ((search!=null&&search.indexOf("f")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57396),"f"):s;
				s = ((search!=null&&search.indexOf("f")>=0)||search==null)?StringReplaceAll(s,String.fromCharCode(57398),"f"):s;
				s = StringReplaceAll(s," ol "," of ");
				s = StringReplaceAll(s," lor "," for ");
				s = StringReplaceAll(s," lound "," found ");
				s = StringReplaceAll(s," lrom "," from ");
				s = StringReplaceAll(s," worfd "," world ");
				s = StringReplaceAll(s," whife "," while ");
				s = StringReplaceAll(s," Whife "," while ");
				s = StringReplaceAll(s," gfobaf "," global ");
				s = StringReplaceAll(s," ofers "," offers ");
				s = StringReplaceAll(s," ofer "," offer ");
				s = StringReplaceAll(s," commonfy "," commonly ");
				s = StringReplaceAll(s," afong "," along ");
				s = StringReplaceAll(s,"incfud","includ");
			}
			return s;	
		}
		
		public static function StringReplaceAll( source:String, find:String, replacement:String ) : String
		{
			return source.split( find ).join( replacement );
		}
		
	}
}
