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
 import flash.display.GradientType;
	import flash.display.Graphics;
	import flash.events.Event;
	import flash.events.TimerEvent;
	import flash.geom.Matrix;
	import flash.utils.Timer;
	
	import mx.core.UIComponent;
	import mx.styles.CSSStyleDeclaration;
	import mx.styles.StyleManager;

	/**
	 *  The background alpha value (0-1).
	 *  @default 1
	 */
	[Style(name="backgroundAlpha", type="Number", format="Length", inherit="no")]

	/**
	 *  The background color.
	 *  @default NaN (no background)
	 */
	[Style(name="backgroundColor", type="uint", format="Color", inherit="no")]

	/**
	 *  The spinner alpha value (0-1).
	 *  @default 1
	 */
	[Style(name="spinnerAlpha", type="Number", format="Length", inherit="no")]

	/**
	 *  The normal spinner color.
	 *  @default #829AD1
	 */
	[Style(name="spinnerColor", type="uint", format="Color", inherit="no")]

	/**
	 *  The normal spinner color.
	 *  @default #829AD1
	 */
	[Style(name="spinnerHighlightColor", type="uint", format="Color", inherit="no")]

	/**
	 *  The thickness of the spinner when the default type is used.
	 *  @default 3
	 */
	[Style(name="spinnerThickness", type="Number", format="Length", inherit="no")]
	
	/**
	 *  The thickness of the spinner when the lines type is used.
	 *  @default 2
	 */
	[Style(name="spinnerLineThickness", type="Number", format="Length", inherit="no")]

	/**
	 *  The type of spinner - "circles", "lines", "gradientlines", or "gradientcircle".
	 *  @default "gradientcircle"
	 */
	[Style(name="spinnerType", type="String", enumeration="gradientcircle,circles,lines,gradientlines", inherit="no")]


	/**
	 * Draws an animated spinner in the shape of a circle.
	 * A timer is used to animate the spinner.
	 * If the startImmediately property is set to true, then the timer is started 
	 * when the spinner is added to its parent
	 * It is always stopped when it is removed from its parent.
	 * Otherwise you can call the stop() function or set running = false.
	 * 
	 *  <pre>
	 *  &lt;ui:Spinner
	 *   <strong>Properties</strong>
	 *   delay="100"
	 * 	 startImmediately="false"
	 *   <strong>Styles</strong>
	 *   backgroundAlpha="1"
	 *   backgroundColor="NaN"
 	 *   spinnerColor="#829AD1"
 	 *   spinnerHighlightColor="#001A8E"
 	 *   spinnerThickness="3"
 	 *   spinnerLineThickness="2"
 	 *   spinnerType="gradientcircle"
 	 * &gt;
 	 *      ...
 	 *      <i>child tags</i>
 	 *      ...
 	 *  &lt;/ui:Spinner&gt;
 	 *  </pre>
	 * 
	 * @author Chris Callendar
	 * @date April 16th, 2009
	 */
	public class Spinner extends UIComponent
	{

		// setup the default styles
		private static var classConstructed:Boolean = classConstruct(); 
		private static function classConstruct():Boolean {
			var style:CSSStyleDeclaration = StyleManager.getStyleDeclaration("Spinner");
            if (!style) {
                style = new CSSStyleDeclaration();
            }
            style.defaultFactory = function():void {
        	    this.backgroundAlpha = 1;
        	    this.backgroundColor = NaN;		// no default
        	    this.spinnerAlpha = 1;
           	    this.spinnerColor = 0x829AD1;
           	    this.spinnerHighlightColor = 0x001A8E;
        	    this.spinnerThickness = 3;
        	    this.spinnerLineThickness = 2;
        	    this.spinnerType = GRADIENT_CIRCLE;
        	};
			StyleManager.setStyleDeclaration("Spinner", style, true);      	
            return true;
        };
		
		public static const GRADIENT_CIRCLE:String = "gradientcircle";
		public static const CIRCLES:String = "circles";
		public static const LINES:String = "lines";
		public static const GRADIENT_LINES:String = "gradientlines";
		
		private static const MAX_ANGLE:Number = 2 * Math.PI;	// 360 degrees
		private static const POSITIONS:int = 8;
		private static const ANGLE_INCR:Number = MAX_ANGLE / POSITIONS;	// 45 degrees

		private var currentPosition:int;
		private var timer:Timer;
		private var matrix:Matrix;
		
		private var _startImmediately:Boolean;
		
		public function Spinner(spinnerWidth:Number = 16, spinnerHeight:Number = 16, 
								spinnerDelay:uint = 100) {
			super();
			this.width = spinnerWidth;
			this.height = spinnerHeight;
			this._startImmediately = false;
			currentPosition = 0;
			timer = new Timer(spinnerDelay);
			timer.addEventListener(TimerEvent.TIMER, timerHandler);

			// these listeners start and stop the timer
			addEventListener(Event.ADDED, addedToParent);
			addEventListener(Event.REMOVED, removedFromParent);
		}
		
		[Bindable(event="delayChanged")]
		[Inspectable(category="General", defaultValue="100")]
		public function get delay():Number {
			return timer.delay;
		}
		
		public function set delay(ms:Number):void {
			if ((ms >= 0) && (ms != timer.delay)) {
				timer.delay = ms;
				dispatchEvent(new Event("delayChanged"));
			}
		} 
		
		[Inspectable(category="General", defaultValue="false")]
		public function get startImmediately():Boolean {
			return _startImmediately;
		}
		
		public function set startImmediately(now:Boolean):void {
			_startImmediately = now;
		}
		
		private function addedToParent(event:Event):void {
			if (startImmediately) {
				start();
			}
		}
		
		private function removedFromParent(event:Event):void {
			stop();
		}
		
		private function timerHandler(event:TimerEvent):void {
			currentPosition = (currentPosition + 1) % POSITIONS;
			updateDisplayList(unscaledWidth, unscaledHeight);
		}
		
		public function get running():Boolean {
			return timer.running;
		}
		
		public function set running(run:Boolean):void {
			if (run != timer.running) {
				if (run) {
					timer.start();
				} else {
					timer.stop();
				}
			}
		}
		
		public function stop():void {
			if (timer.running) {
				timer.stop();
			}
		}
		
		public function start():void {
			if (!timer.running) {
				timer.start();
			}
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void {
			super.updateDisplayList(w, h);

			var g:Graphics = graphics;
			g.clear();
			fillBackground(g, w, h);
			drawSpinner(g, w, h);
		}
		
		/**
		 * Draws the spinner depending on the spinner styles 
		 * and the spinner type.
		 */
		protected function drawSpinner(g:Graphics, w:Number, h:Number):void {
			var spinnerAlpha:Number = getNumberStyle("spinnerAlpha", 1);
			if (spinnerAlpha == 0) {
				return;
			}
			var normal:uint = uint(getStyle("spinnerColor"));
			var highlight:uint = uint(getStyle("spinnerHighlightColor"));
			var thickness:Number = getNumberStyle("spinnerThickness", 3);
			var lineThickness:Number = getNumberStyle("spinnerLineThickness", 2);

			var midX:Number = Math.floor(w / 2);
			var midY:Number = Math.floor(h / 2);
			var diameter:Number = Math.min(w, h);
			var radius:int = Math.floor(diameter / 2);
			
			var spinnerType:String = getStyle("spinnerType");
			if (spinnerType == null) {
				spinnerType = GRADIENT_CIRCLE;
			}
			var drawLines:Boolean = (spinnerType == LINES);
			var drawCircles:Boolean = (spinnerType == CIRCLES);
			if (drawLines || drawCircles) {
				// draw 8 small spinning circles or lines
				var smallRadius:int = int(Math.min(w, h) / 8);
				var angle:Number = 0;
				var count:int = 0;
				while (angle < MAX_ANGLE) {
					// figure out the position around the outer circle
					// also adjust for the small radius to keep the small circles/lines inside the bounds
					var x1:Number = midX + (radius * Math.sin(angle)) - (smallRadius * Math.sin(angle));
					var y1:Number = midY - (radius * Math.cos(angle)) + (smallRadius * Math.cos(angle));
					var color:uint = (count == currentPosition ? highlight : normal);
					
					if (drawLines) {
						var x2:Number = midX;
						var y2:Number = midY;
						// make a hole in the center?
						x2 = x2 + (3 * Math.sin(angle));
						y2 = y2 - (3 * Math.cos(angle));
						drawLine(g, color, x1, y1, x2, y2, lineThickness, spinnerAlpha);
					} else if (drawCircles) {
						drawCircle(g, color, x1, y1, smallRadius, spinnerAlpha);
					}
					
					angle += ANGLE_INCR;
					count++;
				}	
			} else if (spinnerType == GRADIENT_LINES) {
				drawGradientLines(g, normal, highlight, midX, midY, radius, lineThickness, spinnerAlpha);
			} else {
				// draw a large solid spinning circle
				radius = Math.floor(diameter - thickness - 1) / 2; // keep the circle inside the bounds
				drawGradientCircle(g, normal, highlight, midX, midY, radius, thickness, spinnerAlpha);
			}
		}
		
		/**
		 * Draws a small circle at the given position.
		 */
		protected function drawCircle(g:Graphics, color:uint, midX:Number, midY:Number, radius:Number, 
				circleAlpha:Number = 1):void {
			if ((radius > 0) && (circleAlpha > 0)) {
				g.lineStyle(0, 0, 0);	// no border
				g.beginFill(color, circleAlpha);
				g.drawCircle(midX, midY, radius);
				g.endFill();
			}
		}
		
		/**
		 * Draws a small line from the given points.
		 */
		protected function drawLine(g:Graphics, color:uint, x1:Number, y1:Number, x2:Number, y2:Number, 
									thickness:int = 2, lineAlpha:Number = 1):void {
			if ((thickness > 0) && (lineAlpha > 0)) {
				// draw the line
				g.lineStyle(thickness, color, lineAlpha, true);
				g.moveTo(x1, y1);
				g.lineTo(x2, y2);
			}
		}
				
		/**
		 * Draws a circle of lines with a gradient style.
		 */
		protected function drawGradientLines(g:Graphics, color:uint, highlight:uint, 
				midX:Number, midY:Number, radius:Number, thickness:Number = 2, lineAlpha:Number = 1):void {
			if ((radius > 0) && (thickness > 0) && (lineAlpha > 0)) {
				// draw the gradient lines - the matrix rotation value changes to give the spinning appearance
				g.lineStyle(thickness);
				if (matrix == null) {
					matrix = new Matrix();
				}
				var gradientAngle:Number = currentPosition * ANGLE_INCR;
	            matrix.createGradientBox(2 * radius, 2 * radius, gradientAngle, midX - radius, midY - radius);
	            var avg:uint = average(highlight, color);  
	            g.lineGradientStyle(GradientType.LINEAR, [color, avg, highlight], 
	            					[lineAlpha, lineAlpha, lineAlpha], [0, 128, 255], matrix);
				
				var angle:Number = 0;
				var incr:Number = MAX_ANGLE / 12;
				while (angle < MAX_ANGLE) {
					// figure out the position around the outer circle
					// also adjust keep the small lines inside the bounds
					var x1:Number = midX + (radius * Math.sin(angle)) - (1 * Math.sin(angle));
					var y1:Number = midY - (radius * Math.cos(angle)) + (1 * Math.cos(angle));
					// make a hole in the center?
					var x2:Number = midX + (4 * Math.sin(angle));
					var y2:Number = midY - (4 * Math.cos(angle));
					g.moveTo(x1, y1);
					g.lineTo(x2, y2);
					
					angle += incr;
				}	
				
			}
		}
		
		/**
		 * Draws a gradient circle.
		 */
		protected function drawGradientCircle(g:Graphics, color:uint, highlight:uint, 
				midX:Number, midY:Number, radius:Number, thickness:Number = 3, circleAlpha:Number = 1):void {
			if ((radius > 0) && (thickness > 0) && (circleAlpha > 0)) {
				// draw the circle - the matrix rotation value changes to give the 
				// impression that the circle is spinning
				g.lineStyle(thickness);
				if (matrix == null) {
					matrix = new Matrix();
				}
				var angle:Number = currentPosition * ANGLE_INCR;
	            matrix.createGradientBox(2 * radius, 2 * radius, angle, midX - radius, midY - radius);  
	            g.lineGradientStyle(GradientType.LINEAR, [highlight, color], [circleAlpha, circleAlpha], [0, 96], matrix);
				g.drawCircle(midX, midY, radius);
			}
		}
		
		/**
		 * Fills the background using the backgroundAlpha and backgroundColor styles.
		 */
		protected function fillBackground(g:Graphics, w:Number, h:Number):void {
			var bgAlpha:Number = getNumberStyle("backgroundAlpha", 1);
			var color:Number = Number(getStyle("backgroundColor"));
			if (!isNaN(color) && (bgAlpha > 0) && (bgAlpha <= 1)) {
				g.lineStyle(0, 0, 0);
				g.beginFill(uint(color), bgAlpha);
				g.drawRect(0, 0, w, h);
				g.endFill();
			}
		}
			
		protected function getNumberStyle(styleName:String, defaultValue:Number):Number {
			var num:Number = getStyle(styleName);
			if (isNaN(num)) {
				num = defaultValue;
			}
			return num;
		}
		
		/**
		 * Combines the red, green, and blue color components into one 24 bit uint.
		 */
		public static function combine(r:uint, g:uint, b:uint):uint {
			return (Math.min(Math.max(0, r), 255) << 16) | 
				   (Math.min(Math.max(0, g), 255) << 8) | 
					Math.min(Math.max(0, b), 255);
		} 

		/**
		 * Returns the average of the two colors.  Doesn't look at alpha values. */
		public static function average(c1:uint, c2:uint):uint {
			var r:uint = Math.round((getRed(c1) + getRed(c2)) / 2);
			var g:uint = Math.round((getGreen(c1) + getGreen(c2)) / 2);
			var b:uint = Math.round((getBlue(c1) + getBlue(c2)) / 2);
			return combine(r, g, b);
		}
		
		public static function getRed(rgb:uint):uint {
			return ((rgb >> 16) & 0xFF);
		}
		
		public static function getGreen(rgb:uint):uint {
			return ((rgb >> 8) & 0xFF);
		}
				
		public static function getBlue(rgb:uint):uint {
			return (rgb & 0xFF);
		}
		
	}

}