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

package com.devaldi.skinning
{
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.IBitmapDrawable;
import flash.display.Sprite;
import flash.geom.Matrix;
import flash.geom.Point;
import flash.geom.Rectangle;

import mx.skins.ProgrammaticSkin;

 public class GradientBackground extends ProgrammaticSkin
 	{

	 	public function GradientBackground()
		{
		}
	 
	 	override public function get measuredWidth():Number
        {
            return 20;
        }
        
        override public function get measuredHeight():Number
        {
            return 20;
        }
        
        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
        {
			
            var fillColors:Array = getStyle("fillColors");
            var fillAlphas:Array = getStyle("fillAlphas");
            var cornerRadius:int = getStyle("cornerRadius");
            var gradientType:String = getStyle("gradientType");
            var angle:Number = getStyle("angle");
            var focalPointRatio:Number = getStyle("focalPointRatio");
            
            // Default values, if styles aren't defined
            if (fillColors == null)
                fillColors = [0xEEEEEE, 0x999999];
            
            if (fillAlphas == null)
                fillAlphas = [1, 1];
            
            if (gradientType == "" || gradientType == null)
                gradientType = "linear";
            
            if (isNaN(angle))
                angle = 90;
            
            if (isNaN(focalPointRatio))
                focalPointRatio = 0.5;
            
            var matrix:Matrix = new Matrix();
            matrix.createGradientBox(unscaledWidth, unscaledHeight, angle * Math.PI / 180);
            
            graphics.beginGradientFill(gradientType, fillColors, fillAlphas, [0, 255] , matrix, "pad", "rgb", focalPointRatio);
            //graphics.drawRect(0, 0, unscaledWidth, unscaledHeight);
            graphics.drawRoundRect(0, 0, unscaledWidth, unscaledHeight, cornerRadius*.5, cornerRadius*.5); 
			graphics.endFill();
        }
    }
}