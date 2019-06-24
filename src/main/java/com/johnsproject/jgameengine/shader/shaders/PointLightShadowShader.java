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
package com.johnsproject.jgameengine.shader.shaders;

import java.util.List;

import com.johnsproject.jgameengine.dto.Camera;
import com.johnsproject.jgameengine.dto.GeometryBuffer;
import com.johnsproject.jgameengine.dto.Light;
import com.johnsproject.jgameengine.dto.LightType;
import com.johnsproject.jgameengine.dto.Model;
import com.johnsproject.jgameengine.dto.ShaderBuffer;
import com.johnsproject.jgameengine.dto.Texture;
import com.johnsproject.jgameengine.dto.Transform;
import com.johnsproject.jgameengine.dto.VertexBuffer;
import com.johnsproject.jgameengine.library.GraphicsLibrary;
import com.johnsproject.jgameengine.library.MathLibrary;
import com.johnsproject.jgameengine.library.MatrixLibrary;
import com.johnsproject.jgameengine.library.VectorLibrary;
import com.johnsproject.jgameengine.shader.FlatTriangle;
import com.johnsproject.jgameengine.shader.Shader;

public class PointLightShadowShader implements Shader {

	private static final int LIGHT_RANGE = MathLibrary.FP_ONE * 300;
	
	private static final byte VECTOR_X = VectorLibrary.VECTOR_X;
	private static final byte VECTOR_Y = VectorLibrary.VECTOR_Y;
	private static final byte VECTOR_Z = VectorLibrary.VECTOR_Z;

	private static final int FP_ONE = MathLibrary.FP_ONE;
	
	private static final short SHADOW_BIAS = 50;
	
	private final GraphicsLibrary graphicsLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;

	private final FlatTriangle triangle;
	
	private int[] modelMatrix;
	private int[] projectionMatrix;
	private final int[][] lightMatrices;
	
	private int[] lightFrustum;
	private final int[] portedFrustum;

	private final int[] location0Cache;
	private final int[] location1Cache;
	private final int[] location2Cache;
	
	private final Texture[] shadowMaps;
	private Texture currentShadowMap;
	private Transform lightTransform;

	private List<Light> lights;
	private ShaderBuffer shaderBuffer;

	public PointLightShadowShader() {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.modelMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrices = new int[6][16];
		
		this.location0Cache = vectorLibrary.generate();
		this.location1Cache = vectorLibrary.generate();
		this.location2Cache = vectorLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMaps = new Texture[6];
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i] = new Texture(64, 64);
		}
	}
	
	public PointLightShadowShader(int width, int height) {
		this.graphicsLibrary = new GraphicsLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.triangle = new FlatTriangle(this);

		this.modelMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();
		this.lightMatrices = new int[6][16];
		
		this.location0Cache = vectorLibrary.generate();
		this.location1Cache = vectorLibrary.generate();
		this.location2Cache = vectorLibrary.generate();
		
		this.lightFrustum = new int[Camera.FRUSTUM_SIZE];
		lightFrustum[Camera.FRUSTUM_LEFT] = 0;
		lightFrustum[Camera.FRUSTUM_RIGHT] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_TOP] = 0;
		lightFrustum[Camera.FRUSTUM_BOTTOM] = FP_ONE;
		lightFrustum[Camera.FRUSTUM_NEAR] = FP_ONE / 10;
		lightFrustum[Camera.FRUSTUM_FAR] = FP_ONE * 10000;
		this.portedFrustum = new int[Camera.FRUSTUM_SIZE];
		this.shadowMaps = new Texture[6];
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i] = new Texture(width, height);
		}
	}
	
	public void update(ShaderBuffer shaderBuffer) {
		this.shaderBuffer = shaderBuffer;
		this.lights = shaderBuffer.getLights();
		if (shaderBuffer.getPointLightIndex() == -1) {
			shaderBuffer.setPointLightFrustum(portedFrustum);
			shaderBuffer.setPointLightMatrices(lightMatrices);
			shaderBuffer.setPointShadowMaps(shadowMaps);
		}
		graphicsLibrary.screenportFrustum(lightFrustum, shadowMaps[0].getWidth(), shadowMaps[0].getHeight(), portedFrustum);
		for (int i = 0; i < shadowMaps.length; i++) {
			shadowMaps[i].fill(Integer.MAX_VALUE);
		}
	}

	public void setup(Camera camera) {
		shaderBuffer.setPointLightIndex(-1);
		if(lights.size() > 0) {
			int[] cameraLocation = camera.getTransform().getLocation();		
			int distance = Integer.MAX_VALUE;
			for (int i = 0; i < lights.size(); i++) {
				Light light = lights.get(i);
				lightTransform = light.getTransform();
				int[] lightPosition = lightTransform.getLocation();
				int dist = vectorLibrary.averagedDistance(cameraLocation, lightPosition);
				if ((light.getType() == LightType.POINT) & (dist < distance) & (dist < LIGHT_RANGE)) {
					distance = dist;
					shaderBuffer.setPointLightIndex(i);
				}
			}
			if (shaderBuffer.getPointLightIndex() == -1)
				return;		
			int[] lightMatrix = lightMatrices[0];
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
			lightTransform.rotate(0, 0, 90);
			lightMatrix = lightMatrices[1];
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
			lightTransform.rotate(0, 0, 90);
			lightMatrix = lightMatrices[2];
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
			lightTransform.rotate(0, 0, 90);
			lightMatrix = lightMatrices[3];
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
			lightTransform.rotate(0, 0, -270);
			lightTransform.rotate(90, 0, 0);
			lightMatrix = lightMatrices[4];
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
			lightTransform.rotate(-180, 0, 0);
			lightMatrix = lightMatrices[5];
			graphicsLibrary.viewMatrix(modelMatrix, lightTransform);
			graphicsLibrary.perspectiveMatrix(projectionMatrix, portedFrustum);
			matrixLibrary.multiply(projectionMatrix, modelMatrix, lightMatrix);
			lightTransform.rotate(90, 0, 0);
		}
	}
	
	public void setup(Model model) {
		graphicsLibrary.modelMatrix(modelMatrix, model.getTransform());
	}
	
	public void vertex(VertexBuffer vertexBuffer) {
	}

	public void geometry(GeometryBuffer geometryBuffer) {
		if (shaderBuffer.getPointLightIndex() == -1)
			return;	
		backup(geometryBuffer);
		for (int i = 0; i < lightMatrices.length; i++) {
			currentShadowMap = shadowMaps[i];
			for (int j = 0; j < geometryBuffer.getVertexDataBuffers().length; j++) {
				int[] vertexLocation = geometryBuffer.getVertexDataBuffer(j).getLocation();
				vectorLibrary.matrixMultiply(vertexLocation, modelMatrix, vertexLocation);
				vectorLibrary.matrixMultiply(vertexLocation, lightMatrices[i], vertexLocation);
				graphicsLibrary.screenportVector(vertexLocation, portedFrustum, vertexLocation);
			}
			triangle.setLocation0(geometryBuffer.getVertexDataBuffer(0).getLocation());
			triangle.setLocation1(geometryBuffer.getVertexDataBuffer(1).getLocation());
			triangle.setLocation2(geometryBuffer.getVertexDataBuffer(2).getLocation());
			if(graphicsLibrary.shoelace(triangle) > 0)
				graphicsLibrary.drawFlatTriangle(triangle, portedFrustum);
			restore(geometryBuffer);
		}
	}

	public void fragment(int[] location) {
		int x = location[VECTOR_X];
		int y = location[VECTOR_Y];
		int z = location[VECTOR_Z];
		if (currentShadowMap.getPixel(x, y) > z) {
			currentShadowMap.setPixel(x, y, z + SHADOW_BIAS);
		}
	}
	
	private void backup(GeometryBuffer geometryBuffer) {
		vectorLibrary.copy(location0Cache, geometryBuffer.getVertexDataBuffer(0).getLocation());
		vectorLibrary.copy(location1Cache, geometryBuffer.getVertexDataBuffer(1).getLocation());
		vectorLibrary.copy(location2Cache, geometryBuffer.getVertexDataBuffer(2).getLocation());
	}
	
	private void restore(GeometryBuffer geometryBuffer) {
		vectorLibrary.copy(geometryBuffer.getVertexDataBuffer(0).getLocation(), location0Cache);
		vectorLibrary.copy(geometryBuffer.getVertexDataBuffer(1).getLocation(), location1Cache);
		vectorLibrary.copy(geometryBuffer.getVertexDataBuffer(2).getLocation(), location2Cache);
	}

	public void terminate(ShaderBuffer shaderBuffer) {
		shaderBuffer.setPointLightIndex(-1);
		shaderBuffer.setPointLightFrustum(null);
		shaderBuffer.setPointLightMatrices(null);
	}	
}