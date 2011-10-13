package com.devaldi.controls.flexpaper.utils
{
	public class VersionHandler
	{
		private static var v:Array = new Array();
		
		public static function version():Array{
			v[0] = 1;
			v[1] = 4;
			v[2] = 5;
			return v;
		}
		
		public static function versionAsText():String{
			return version()[0] + "." + version()[1] + "." + version()[2];
		}
	}
}