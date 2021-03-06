package com.johnsproject.jgameengine.util;

import static com.johnsproject.jgameengine.util.FixedPointUtils.*;
import static com.johnsproject.jgameengine.util.MatrixUtils.*;
import static com.johnsproject.jgameengine.util.VectorUtils.*;

import com.johnsproject.jgameengine.model.Frustum;

public final class TransformationUtils {
	
	private TransformationUtils() { }

	public static int[][] orthographicMatrix(int[][] matrix, Frustum frustum) {
		final int top = frustum.getRenderTargetTop();
		final int bottom = frustum.getRenderTargetBottom();
		final int near = frustum.getNear();
		final int far = frustum.getFar();		
		final int scaleFactor = FixedPointUtils.multiply(frustum.getFocalLength(), bottom - top + 1);
		final int[][] projectionMatrix = MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		projectionMatrix[0][0] = scaleFactor >> 5;
		projectionMatrix[1][1] = -scaleFactor >> 5;
		projectionMatrix[2][2] = -FixedPointUtils.divide(FP_ONE, far);
		projectionMatrix[3][2] = -FixedPointUtils.divide(near, far);
		projectionMatrix[3][3] = FP_ONE;
		return projectionMatrix;
	}

	public static int[][] perspectiveMatrix(int[][] matrix, Frustum frustum) {
		final int top = frustum.getRenderTargetTop();
		final int bottom = frustum.getRenderTargetBottom();
		final int near = frustum.getNear();
		final int far = frustum.getFar();	
		final int farNear = far - near;
		final int scaleFactor = FixedPointUtils.multiply(frustum.getFocalLength(), bottom - top + 1);
		final int[][] projectionMatrix = MatrixUtils.copy(matrix, MatrixUtils.MATRIX_IDENTITY);
		projectionMatrix[0][0] = scaleFactor;
		projectionMatrix[1][1] = -scaleFactor;
		projectionMatrix[2][2] = -FixedPointUtils.divide(far, farNear);
		projectionMatrix[3][2] = -FixedPointUtils.divide(FixedPointUtils.multiply(near, far), farNear);
		projectionMatrix[2][3] = -FP_ONE;
		projectionMatrix[3][3] = 0;
		return projectionMatrix;
	}

	public static int[] screenportVector(int[] location, Frustum frustum) {
		final int left = frustum.getRenderTargetLeft();
		final int right = frustum.getRenderTargetRight();
		final int top = frustum.getRenderTargetTop();
		final int bottom = frustum.getRenderTargetBottom();
		final int halfWidth = left + ((right - left) >> 1);
		final int halfHeight = top + ((bottom - top) >> 1);
		location[VECTOR_X] += halfWidth;
		location[VECTOR_Y] += halfHeight;
		return location;
	}
	
	public static int[] translate(int[] vector, int[] direction) {
		return translate(vector, direction[VECTOR_X], direction[VECTOR_Y], direction[VECTOR_Z]);
	}
	
	public static int[] translate(int[] vector, int x, int y, int z) {
		vector[VECTOR_X] = vector[VECTOR_X] + x;
		vector[VECTOR_Y] = vector[VECTOR_Y] + y;
		vector[VECTOR_Z] = vector[VECTOR_Z] + z;
		return vector;
	}
	
	public static int[] scale(int[] vector, int factor) {
		vector[VECTOR_X] = FixedPointUtils.multiply(vector[VECTOR_X], factor);
		vector[VECTOR_Y] = FixedPointUtils.multiply(vector[VECTOR_Y], factor);
		vector[VECTOR_Z] = FixedPointUtils.multiply(vector[VECTOR_Z], factor);
		return vector;
	}
	
	/**
	 * Sets result equals the vector reflected across reflectionVector.
	 * 
	 * @param vector
	 * @param reflectionVector
	 * @param result
	 */
	public static int[] reflect(int[] vector, int[] reflectionVector) {
		final int x = reflectionVector[VECTOR_X];
		final int y = reflectionVector[VECTOR_Y];
		final int z = reflectionVector[VECTOR_Z];
		final int dot = (int)(2 * VectorUtils.dotProduct(vector, reflectionVector));
		VectorUtils.multiply(reflectionVector, dot);
		VectorUtils.subtract(vector, reflectionVector);
		reflectionVector[VECTOR_X] = x;
		reflectionVector[VECTOR_Y] = y;
		reflectionVector[VECTOR_Z] = z;
		return vector;
	}

	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at x axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public static int[] rotateX(int[] vector, int angle) {
		final int sin = FixedPointUtils.sin(angle);
		final int cos = FixedPointUtils.cos(angle);
		final int x = vector[VECTOR_X];
		final int y = vector[VECTOR_Y];
		final int z = vector[VECTOR_Z];
		vector[VECTOR_X] = x;
		vector[VECTOR_Y] = FixedPointUtils.multiply(y, cos);
		vector[VECTOR_Y] -= FixedPointUtils.multiply(z, sin);
		vector[VECTOR_Z] = FixedPointUtils.multiply(z, cos);
		vector[VECTOR_Z] += FixedPointUtils.multiply(y, sin);
		return vector;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at y axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public static int[] rotateY(int[] vector, int angle) {
		final int sin = FixedPointUtils.sin(angle);
		final int cos = FixedPointUtils.cos(angle);
		final int x = vector[VECTOR_X];
		final int y = vector[VECTOR_Y];
		final int z = vector[VECTOR_Z];
		vector[VECTOR_X] = FixedPointUtils.multiply(x, cos);
		vector[VECTOR_X] += FixedPointUtils.multiply(z, sin);
		vector[VECTOR_Y] = y;
		vector[VECTOR_Z] = FixedPointUtils.multiply(z, cos);
		vector[VECTOR_Z] -= FixedPointUtils.multiply(x, sin);
		return vector;
	}
	
	/**
	 * Sets result equals the vector rotated around (0, 0, 0) at z axis by the given angle.
	 * 
	 * @param vector
	 * @param angle
	 * @param result
	 * @return
	 */
	public static int[] rotateZ(int[] vector, int angle) {
		final int sin = FixedPointUtils.sin(angle);
		final int cos = FixedPointUtils.cos(angle);
		final int x = vector[VECTOR_X];
		final int y = vector[VECTOR_Y];
		final int z = vector[VECTOR_Z];
		vector[VECTOR_X] = FixedPointUtils.multiply(x, cos);
		vector[VECTOR_X] += FixedPointUtils.multiply(y, sin);
		vector[VECTOR_Y] = FixedPointUtils.multiply(y, cos);
		vector[VECTOR_Y] -= FixedPointUtils.multiply(x, sin);
		vector[VECTOR_Z] = z;
		return vector;
	}
	
	/**
	 * Sets result equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] translationMatrix(int[][] matrix, int[] vector) {
		return translationMatrix(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	/**
	 * Sets result equals the translated matrix.
	 *
	 * @param matrix
	 * @param x
	 * @param y
	 * @param z
	 * @param result
	 */
	public static int[][] translationMatrix(int[][] matrix, int x, int y , int z) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		matrix[3][0] = x;
		matrix[3][1] = y;
		matrix[3][2] = z;
		return matrix;
	}
	
	public static int[][] scaleMatrix(int[][] matrix, int[] vector) {
		return scaleMatrix(matrix, vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}
	
	public static int[][] scaleMatrix(int[][] matrix, int x, int y, int z) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		matrix[0][0] = x;
		matrix[1][1] = y;
		matrix[2][2] = z;
		return matrix;
	}
	
	public static int[][] xRotationMatrix(int[][] matrix, int angle) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		final int cos = FixedPointUtils.cos(angle);
		final int sin = FixedPointUtils.sin(angle);
		matrix[1][1] = cos;
		matrix[1][2] = sin;
		matrix[2][1] = -sin;
		matrix[2][2] = cos;
		return matrix;
	}
	
	public static int[][] yRotationMatrix(int[][] matrix, int angle) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		final int cos = FixedPointUtils.cos(angle);
		final int sin = FixedPointUtils.sin(angle);
		matrix[0][0] = cos;
		matrix[0][2] = -sin;
		matrix[2][0] = sin;
		matrix[2][2] = cos;
		return matrix;
	}
	
	public static int[][] zRotationMatrix(int[][] matrix, int angle) {
		MatrixUtils.copy(matrix, MATRIX_IDENTITY);
		final int cos = FixedPointUtils.cos(angle);
		final int sin = FixedPointUtils.sin(angle);
		matrix[0][0] = cos;
		matrix[0][1] = sin;
		matrix[1][0] = -sin;
		matrix[1][1] = cos;
		return matrix;
	}
}