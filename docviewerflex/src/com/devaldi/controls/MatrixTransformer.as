package com.devaldi.controls
{
	import flash.geom.Matrix;
	
	/**
	 * Supports simple matrix transformations: scaling, translation, rotation, and skewing. 
	 */
	
	public class MatrixTransformer
	{
		/**
		 * Calls the appropriate method of the MatrixTransformer class, based on the radio buttons
		 * selected. Then updates the transformation matrix based on the results of the method.
		 *
		 * Runs the same transformation matrix through successive transformations and then applies
		 * the matrix to the image at the very end, which is more efficient than applying after
		 * each individual transformation.
		 */
		public static function transform(sourceMatrix:Matrix, 
										 xScale:Number=100, 
										 yScale:Number=100, 
										 dx:Number=0, 
										 dy:Number=0,
										 rotation:Number=0, 
										 skew:Number=0, 
										 skewType:String="right"):Matrix 
		{
			// skew:
			sourceMatrix = MatrixTransformer.skew(sourceMatrix, skew, skewType);
			
			// scale:
			sourceMatrix = MatrixTransformer.scale(sourceMatrix, xScale, yScale);
			
			// translate:
			sourceMatrix = MatrixTransformer.translate(sourceMatrix, dx, dy);
			
			// rotate:
			sourceMatrix = MatrixTransformer.rotate(sourceMatrix, rotation, "degrees");
			
			return sourceMatrix;
		}
		
		/**
		 * Scales a matrix and returns the result. The percent parameter lets the user specify scale 
		 * factors (xScale and yScale) as percentages (such as 33) instead of absolute values (such as 0.33). 
		 */
		public static function scale(sourceMatrix:Matrix, xScale:Number, yScale:Number, percent:Boolean = true):Matrix 
		{
			if (percent) 
			{
				xScale = xScale / 100;
				yScale = yScale / 100;
			}
			sourceMatrix.scale(xScale, yScale)
			return sourceMatrix;
		}
		
		/**
		 * Translates a matrix and returns the result. 
		 */
		public static function translate(sourceMatrix:Matrix, dx:Number, dy:Number):Matrix {
			sourceMatrix.translate(dx, dy);
			return sourceMatrix;
		}
		
		/**
		 * Rotates a matrix and returns the result. The unit parameter lets the user specify "degrees", 
		 * "gradients", or "radians". 
		 */
		public static function rotate(sourceMatrix:Matrix, angle:Number, unit:String = "radians"):Matrix {
			if (unit == "degrees") 
			{
				angle = Math.PI * 2 * angle / 360;
			}
			if (unit == "gradients")
			{
				angle = Math.PI * 2 * angle / 100;
			}
			sourceMatrix.rotate(angle)
			return sourceMatrix;
		}
		
		/**
		 * Scales a matrix and returns the result. The skewSide parameter lets the user 
		 * determine which side to skew (right or bottom).The unit parameter lets the user 
		 * specify "degrees", "gradients", or "radians". 
		 */
		public static function skew(sourceMatrix:Matrix, angle:Number, skewSide:String = "right", unit:String = "degrees"):Matrix {
			if (unit == "degrees") 
			{
				angle = Math.PI * 2 * angle / 360;
			}
			if (unit == "gradients")
			{
				angle = Math.PI * 2 * angle / 100;
			}
			var skewMatrix:Matrix = new Matrix();                   
			if (skewSide == "right") 
			{
				skewMatrix.b = Math.tan(angle);
			} else  { // skewSide == "bottom"
				skewMatrix.c = Math.tan(angle);
			}
			sourceMatrix.concat(skewMatrix)
			return sourceMatrix;
		}
		
	}
}