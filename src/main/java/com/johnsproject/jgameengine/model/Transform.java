package com.johnsproject.jgameengine.model;

import static com.johnsproject.jgameengine.math.VectorMath.*;

import com.johnsproject.jgameengine.math.MatrixMath;
import com.johnsproject.jgameengine.math.TransformationMath;
import com.johnsproject.jgameengine.math.VectorMath;

public class Transform {
	
	private final int[] location;
	private final int[] rotation;
	private final int[] scale;
	
	private final int[][] matrixCache1;
	private final int[][] matrixCache2;
	private final int[][] spaceEnterMatrix;
	private final int[][] spaceEnterNormalMatrix;
	private final int[][] spaceExitMatrix;
	private final int[][] spaceExitNormalMatrix;
	
	public Transform() {
		this(VectorMath.emptyVector(), VectorMath.emptyVector(), VectorMath.VECTOR_ONE.clone());
	}
	
	public Transform(int[] location, int[] rotation, int[] scale) {
		this.location = location;
		this.rotation = rotation;
		this.scale = scale;
		this.matrixCache1 = MatrixMath.indentityMatrix();
		this.matrixCache2 = MatrixMath.indentityMatrix();
		this.spaceEnterMatrix = MatrixMath.indentityMatrix();
		this.spaceEnterNormalMatrix = MatrixMath.indentityMatrix();
		this.spaceExitMatrix = MatrixMath.indentityMatrix();
		this.spaceExitNormalMatrix = MatrixMath.indentityMatrix();
		recalculateMatrices();
	}
	
	private void recalculateMatrices() {
		TransformationMath.spaceExitMatrix(spaceExitMatrix, this, matrixCache1, matrixCache2);
		TransformationMath.spaceExitNormalMatrix(spaceExitNormalMatrix, this, matrixCache1, matrixCache2);
		TransformationMath.spaceEnterMatrix(spaceEnterMatrix, this, matrixCache1, matrixCache2);
		TransformationMath.spaceEnterNormalMatrix(spaceEnterNormalMatrix, this, matrixCache1, matrixCache2);
	}
	
	public void setLocation(int x, int y, int z) {
		location[VECTOR_X] = x;
		location[VECTOR_Y] = y;
		location[VECTOR_Z] = z;
		recalculateMatrices();
	}

	public void setRotation(int x, int y, int z) {
		rotation[VECTOR_X] = x;
		rotation[VECTOR_Y] = y;
		rotation[VECTOR_Z] = z;
		recalculateMatrices();
	}
	
	public void setScale(int x, int y, int z) {
		scale[VECTOR_X] = x;
		scale[VECTOR_Y] = y;
		scale[VECTOR_Z] = z;
		recalculateMatrices();
	}
	
	public void translate(int x, int y, int z) {
		setLocation(location[VECTOR_X] + x, location[VECTOR_Y] + y, location[VECTOR_Z] + z);
	}

	public void rotate(int x, int y, int z) {
		setRotation(rotation[VECTOR_X] + x, rotation[VECTOR_Y] + y, rotation[VECTOR_Z] + z);
	}
	
	public void scale(int x, int y, int z) {
		setScale(scale[VECTOR_X] + x, scale[VECTOR_Y] + y, scale[VECTOR_Z] + z);
	}
	
	public void translate(int[] vector) {
		translate(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}

	public void rotate(int[] angles) {
		rotate(angles[VECTOR_X], angles[VECTOR_Y], angles[VECTOR_Z]);
	}
	
	public void scale(int[] vector) {
		scale(vector[VECTOR_X], vector[VECTOR_Y], vector[VECTOR_Z]);
	}

	public int[] getLocation() {
		return location;
	}

	public int[] getRotation() {
		return rotation;
	}

	public int[] getScale() {
		return scale;
	}

	public int[][] getSpaceEnterMatrix() {
		return spaceEnterMatrix;
	}

	public int[][] getSpaceEnterNormalMatrix() {
		return spaceEnterNormalMatrix;
	}

	public int[][] getSpaceExitMatrix() {
		return spaceExitMatrix;
	}

	public int[][] getSpaceExitNormalMatrix() {
		return spaceExitNormalMatrix;
	}
}
