/**
 * MIT License
 *
 * Copyright (c) 2018 John Salomon - John´s Project
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.johnsproject.jpge2.processors;

public class ColorProcessor {

	public static final int WHITE = convert(255, 255, 255);
	public static final int BLACK = convert(0, 0, 0);
	
	private static final byte GREENSHIFT = 8;
	private static final byte REDSHIFT = 16;
	private static final byte ALPHASHIFT = 24;
	private static final int HEX = 0xFF;

	public static int convert(int r, int g, int b, int a) {
		int color = 0;
		color |= MathProcessor.clamp(a, 0, 255) << ALPHASHIFT;
		color |= MathProcessor.clamp(r, 0, 255) << REDSHIFT;
		color |= MathProcessor.clamp(g, 0, 255) << GREENSHIFT;
		color |= MathProcessor.clamp(b, 0, 255);
		return color;
	}

	public static int convert(int r, int g, int b) {
		int color = 0;
		color |= (255) << ALPHASHIFT;
		color |= MathProcessor.clamp(r, 0, 255) << REDSHIFT;
		color |= MathProcessor.clamp(g, 0, 255) << GREENSHIFT;
		color |= MathProcessor.clamp(b, 0, 255);
		return color;
	}

	public static int getBlue(int color) {
		return (color) & HEX;
	}

	public static int getGreen(int color) {
		return (color >> GREENSHIFT) & HEX;
	}

	public static int getRed(int color) {
		return (color >> REDSHIFT) & HEX;
	}

	public static int getAlpha(int color) {
		return (color >> ALPHASHIFT) & HEX;
	}

	public static int multiply(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		factor += 255;
		r = (r * factor) >> 8;
		g = (g * factor) >> 8;
		b = (b * factor) >> 8;
		return convert(r, g, b, a);
	}
	
	public static int multiplyRGBA(int color, int factor) {
		int r = getRed(color), g = getGreen(color), b = getBlue(color), a = getAlpha(color);
		factor += 255;
		r = (r * factor) >> 8;
		g = (g * factor) >> 8;
		b = (b * factor) >> 8;
		a = (a * factor) >> 8;
		return convert(r, g, b, a);
	}
	
	public static int add(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 + r2);
		int g = (g1 + g2);
		int b = (b1 + b2);
		return convert(r, g, b, a1);
	}
	
	public static int addRGBA(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 + r2);
		int g = (g1 + g2);
		int b = (b1 + b2);
		int a = (a1 + a2);
		return convert(r, g, b, a);
	}
	
	public static int multiplyColor(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 * r2) >> 8;
		int g = (g1 * g2) >> 8;
		int b = (b1 * b2) >> 8;
		return convert(r, g, b, a1);
	}
	
	public static int multiplyColorRGBA(int color1, int color2) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 * r2) >> 8;
		int g = (g1 * g2) >> 8;
		int b = (b1 * b2) >> 8;
		int a = (a1 * a2) >> 8;
		return convert(r, g, b, a);
	}
	
	public static int lerp(int color1, int color2, int factor) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2);
		int r = (r1 + (((r2 - r1) * factor) >> 8));
		int g = (g1 + (((g2 - g1) * factor) >> 8));
		int b = (b1 + (((b2 - b1) * factor) >> 8));
		return convert(r, g, b, a1);
	}
	
	public static int lerpRGBA(int color1, int color2, int factor) {
		int r1 = getRed(color1), g1 = getGreen(color1), b1 = getBlue(color1), a1 = getAlpha(color1);
		int r2 = getRed(color2), g2 = getGreen(color2), b2 = getBlue(color2), a2 = getAlpha(color2);
		int r = (r1 + (((r2 - r1) * factor) >> 8));
		int g = (g1 + (((g2 - g1) * factor) >> 8));
		int b = (b1 + (((b2 - b1) * factor) >> 8));
		int a = (a1 + (((a2 - a1) * factor) >> 8));
		return convert(r, g, b, a);
	}
}
