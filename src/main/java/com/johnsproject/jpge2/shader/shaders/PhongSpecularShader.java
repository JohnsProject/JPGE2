package com.johnsproject.jpge2.shader.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.library.ColorLibrary;
import com.johnsproject.jpge2.library.GraphicsLibrary;
import com.johnsproject.jpge2.library.MathLibrary;
import com.johnsproject.jpge2.library.MatrixLibrary;
import com.johnsproject.jpge2.library.VectorLibrary;
import com.johnsproject.jpge2.shader.Shader;
import com.johnsproject.jpge2.shader.ShaderDataBuffer;
import com.johnsproject.jpge2.shader.databuffers.ForwardDataBuffer;
import com.johnsproject.jpge2.shader.properties.SpecularShaderProperties;

public class PhongSpecularShader extends Shader {

	private final GraphicsLibrary graphicsLibrary;
	private final MathLibrary mathLibrary;
	private final MatrixLibrary matrixLibrary;
	private final VectorLibrary vectorLibrary;
	private final ColorLibrary colorLibrary;

	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;

	private final int[] uvX;
	private final int[] uvY;

	private final int[] fragmentLocation;
	private final int[] normalizedNormal;
	private final int[] lightLocation;
	private final int[] lightDirection;
	private final int[] viewDirection;
	private final int[] portedCanvas;

	private final int[] viewDirectionX;
	private final int[] viewDirectionY;
	private final int[] viewDirectionZ;
	private final int[] locationX;
	private final int[] locationY;
	private final int[] locationZ;
	private final int[] normalX;
	private final int[] normalY;
	private final int[] normalZ;

	private final int[] directionalLocation;
	private final int[] directionalLocationX;
	private final int[] directionalLocationY;
	private final int[] directionalLocationZ;

	private final int[] spotLocation;
	private final int[] spotLocationX;
	private final int[] spotLocationY;
	private final int[] spotLocationZ;

	private int modelColor;
	private Texture texture;

	private Camera camera;
	private List<Light> lights;
	private FrameBuffer frameBuffer;
	private ForwardDataBuffer shaderData;
	private SpecularShaderProperties shaderProperties;

	public PhongSpecularShader() {
		super(19);
		this.graphicsLibrary = new GraphicsLibrary();
		this.mathLibrary = new MathLibrary();
		this.matrixLibrary = new MatrixLibrary();
		this.vectorLibrary = new VectorLibrary();
		this.colorLibrary = new ColorLibrary();

		this.viewMatrix = matrixLibrary.generate();
		this.projectionMatrix = matrixLibrary.generate();

		this.uvX = getVariable(0);
		this.uvY = getVariable(1);
		
		this.viewDirectionX = getVariable(2);
		this.viewDirectionY = getVariable(3);
		this.viewDirectionZ = getVariable(4);
		this.locationX = getVariable(5);
		this.locationY = getVariable(6);
		this.locationZ = getVariable(7);
		this.normalX = getVariable(8);
		this.normalY = getVariable(9);
		this.normalZ = getVariable(10);

		this.directionalLocation = getVariable(11);
		this.directionalLocationX = getVariable(12);
		this.directionalLocationY = getVariable(13);
		this.directionalLocationZ = getVariable(14);

		this.spotLocation = getVariable(15);
		this.spotLocationX = getVariable(16);
		this.spotLocationY = getVariable(17);
		this.spotLocationZ = getVariable(18);

		this.fragmentLocation = vectorLibrary.generate();
		this.normalizedNormal = vectorLibrary.generate();
		this.lightLocation = vectorLibrary.generate();
		this.lightDirection = vectorLibrary.generate();
		this.viewDirection = vectorLibrary.generate();
		this.portedCanvas = vectorLibrary.generate();
	}

	@Override
	public void update(ShaderDataBuffer shaderDataBuffer) {
		this.shaderData = (ForwardDataBuffer) shaderDataBuffer;
		this.lights = shaderData.getLights();
		this.frameBuffer = shaderData.getFrameBuffer();
		frameBuffer.getColorBuffer().fill(0);
		frameBuffer.getDepthBuffer().fill(Integer.MAX_VALUE);
	}

	@Override
	public void setup(Camera camera) {
		this.camera = camera;
		graphicsLibrary.viewMatrix(viewMatrix, camera.getTransform());
		graphicsLibrary.portCanvas(camera.getCanvas(), frameBuffer.getWidth(), frameBuffer.getHeight(), portedCanvas);
		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsLibrary.orthographicMatrix(projectionMatrix, camera.getFrustum());
			break;

		case PERSPECTIVE:
			graphicsLibrary.perspectiveMatrix(projectionMatrix, camera.getFrustum());
			break;
		}
	}

	@Override
	public void vertex(int index, Vertex vertex) {
		int[] location = vertex.getLocation();
		int[] normal = vertex.getNormal();

		locationX[index] = location[VECTOR_X];
		locationY[index] = location[VECTOR_Y];
		locationZ[index] = location[VECTOR_Z];

		vectorLibrary.normalize(normal, normalizedNormal);
		normalX[index] = normalizedNormal[VECTOR_X];
		normalY[index] = normalizedNormal[VECTOR_Y];
		normalZ[index] = normalizedNormal[VECTOR_Z];

		if (shaderData.getDirectionalLightMatrix() != null) {
			vectorLibrary.multiply(location, shaderData.getDirectionalLightMatrix(), directionalLocation);
			graphicsLibrary.viewport(directionalLocation, shaderData.getDirectionalLightCanvas(), directionalLocation);
			directionalLocationX[index] = directionalLocation[VECTOR_X];
			directionalLocationY[index] = directionalLocation[VECTOR_Y];
			directionalLocationZ[index] = directionalLocation[VECTOR_Z];
		}

		if (shaderData.getSpotLightMatrix() != null) {
			vectorLibrary.multiply(location, shaderData.getSpotLightMatrix(), spotLocation);
			graphicsLibrary.viewport(spotLocation, shaderData.getSpotLightCanvas(), spotLocation);
			spotLocationX[index] = spotLocation[VECTOR_X];
			spotLocationY[index] = spotLocation[VECTOR_Y];
			spotLocationZ[index] = spotLocation[VECTOR_Z];
		}

		vectorLibrary.subtract(camera.getTransform().getLocation(), location, viewDirection);
		vectorLibrary.normalize(viewDirection, viewDirection);
		viewDirectionX[index] = viewDirection[VECTOR_X];
		viewDirectionY[index] = viewDirection[VECTOR_Y];
		viewDirectionZ[index] = viewDirection[VECTOR_Z];

		vectorLibrary.multiply(location, viewMatrix, location);
		vectorLibrary.multiply(location, projectionMatrix, location);
		graphicsLibrary.viewport(location, portedCanvas, location);
	}

	@Override
	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		this.shaderProperties = (SpecularShaderProperties)face.getMaterial().getProperties();

		texture = shaderProperties.getTexture();
		// set uv values that will be interpolated and fit uv into texture resolution
		if (texture != null) {
			int width = texture.getWidth() - 1;
			int height = texture.getHeight() - 1;
			uvX[0] = mathLibrary.multiply(face.getUV1()[VECTOR_X], width);
			uvX[1] = mathLibrary.multiply(face.getUV2()[VECTOR_X], width);
			uvX[2] = mathLibrary.multiply(face.getUV3()[VECTOR_X], width);
			uvY[0] = mathLibrary.multiply(face.getUV1()[VECTOR_Y], height);
			uvY[1] = mathLibrary.multiply(face.getUV2()[VECTOR_Y], height);
			uvY[2] = mathLibrary.multiply(face.getUV3()[VECTOR_Y], height);
		}
		graphicsLibrary.drawTriangle(location1, location2, location3, portedCanvas, camera.getFrustum(), this);
	}

	@Override
	public void fragment(int[] location) {		
		directionalLocation[VECTOR_X] = directionalLocationX[3];
		directionalLocation[VECTOR_Y] = directionalLocationY[3];
		directionalLocation[VECTOR_Z] = directionalLocationZ[3];

		spotLocation[VECTOR_X] = spotLocationX[3];
		spotLocation[VECTOR_Y] = spotLocationY[3];
		spotLocation[VECTOR_Z] = spotLocationZ[3];

		viewDirection[VECTOR_X] = viewDirectionX[3];
		viewDirection[VECTOR_Y] = viewDirectionY[3];
		viewDirection[VECTOR_Z] = viewDirectionZ[3];
		
		fragmentLocation[VECTOR_X] = locationX[3];
		fragmentLocation[VECTOR_Y] = locationY[3];
		fragmentLocation[VECTOR_Z] = locationZ[3];

		normalizedNormal[VECTOR_X] = normalX[3];
		normalizedNormal[VECTOR_Y] = normalY[3];
		normalizedNormal[VECTOR_Z] = normalZ[3];

		int lightColor = ColorLibrary.WHITE;
		int lightFactor = 0;

		int[] cameraLocation = camera.getTransform().getLocation();

		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int currentFactor = 0;
			int attenuation = 0;
			int[] lightPosition = light.getTransform().getLocation();
			switch (light.getType()) {
			case DIRECTIONAL:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorLibrary.invert(light.getDirection(), lightDirection);
				currentFactor = getLightFactor(normalizedNormal, lightDirection, viewDirection, shaderProperties);
				break;
			case POINT:
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorLibrary.subtract(lightPosition, fragmentLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				// other light values
				currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
				currentFactor = (currentFactor << 8) / attenuation;
				break;
			case SPOT:
				vectorLibrary.invert(light.getDirection(), lightDirection);
				if (vectorLibrary.distance(cameraLocation, lightPosition) > shaderData.getLightRange())
					continue;
				vectorLibrary.subtract(lightPosition, fragmentLocation, lightLocation);
				// attenuation
				attenuation = getAttenuation(lightLocation);
				vectorLibrary.normalize(lightLocation, lightLocation);
				int theta = vectorLibrary.dotProduct(lightLocation, lightDirection);
				int phi = mathLibrary.cos(light.getSpotSize() >> 1);
				if (theta > phi) {
					int intensity = -mathLibrary.divide(phi - theta, light.getSpotSoftness() + 1);
					intensity = mathLibrary.clamp(intensity, 1, FP_ONE);
					currentFactor = getLightFactor(normalizedNormal, lightLocation, viewDirection, shaderProperties);
					currentFactor = (currentFactor * intensity) / attenuation;
				}
				break;
			}
			currentFactor = mathLibrary.multiply(currentFactor, light.getStrength());
			boolean inShadow = false;
			if (i == shaderData.getDirectionalLightIndex()) {
				if (shaderData.getDirectionalLightMatrix() != null) {
					inShadow = inShadow(directionalLocation, shaderData.getDirectionalShadowMap());
					lightFactor += currentFactor;
				}
			} else if ((i == shaderData.getSpotLightIndex()) && (currentFactor > 10)) {
				if (shaderData.getSpotLightMatrix() != null) {
					inShadow = inShadow(spotLocation, shaderData.getSpotShadowMap());
				}
			}
			if (inShadow) {
				lightColor = colorLibrary.lerp(lightColor, light.getShadowColor(), 128);
			} else {
				lightColor = colorLibrary.lerp(lightColor, light.getColor(), currentFactor);
				lightFactor += currentFactor;
			}
		}
		if (texture != null) {
			modelColor = texture.getPixel(uvX[3], uvY[3]);
			if (colorLibrary.getAlpha(modelColor) == 0) // discard pixel if alpha = 0
				return;
		} else {
			modelColor = shaderProperties.getDiffuseColor();
		}
		modelColor = colorLibrary.lerp(ColorLibrary.BLACK, modelColor, lightFactor);
		modelColor = colorLibrary.multiplyColor(modelColor, lightColor);
		Texture colorBuffer = frameBuffer.getColorBuffer();
		Texture depthBuffer = frameBuffer.getDepthBuffer();
		if (depthBuffer.getPixel(location[VECTOR_X], location[VECTOR_Y]) > location[VECTOR_Z]) {
			depthBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z]);
			colorBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], modelColor);
		}
	}

	private int getLightFactor(int[] normal, int[] lightDirection, int[] viewDirection, SpecularShaderProperties properties) {
		// diffuse
		int dotProduct = vectorLibrary.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathLibrary.multiply(diffuseFactor, properties.getDiffuseIntensity());
		// specular
		vectorLibrary.invert(lightDirection, lightDirection);
		vectorLibrary.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorLibrary.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathLibrary.pow(specularFactor, properties.getShininess() >> FP_BITS);
		specularFactor = mathLibrary.multiply(specularFactor, properties.getSpecularIntensity());
		// putting it all together...
		return (diffuseFactor + specularFactor << 8) >> FP_BITS;
	}

	private int getAttenuation(int[] lightLocation) {
		// attenuation
		long distance = vectorLibrary.magnitude(lightLocation);
		int attenuation = shaderData.getConstantAttenuation();
		attenuation += mathLibrary.multiply(distance, shaderData.getLinearAttenuation());
		attenuation += mathLibrary.multiply(mathLibrary.multiply(distance, distance),
				shaderData.getQuadraticAttenuation());
		attenuation >>= FP_BITS;
		return ((attenuation << 8) >> FP_BITS) + 1;
	}

	private boolean inShadow(int[] lightSpaceLocation, Texture shadowMap) {
		int x = lightSpaceLocation[VECTOR_X];
		int y = lightSpaceLocation[VECTOR_Y];
		x = mathLibrary.clamp(x, 0, shadowMap.getWidth() - 1);
		y = mathLibrary.clamp(y, 0, shadowMap.getHeight() - 1);
		int depth = shadowMap.getPixel(x, y);
		// int color = (depth + 100) >> 5;
		// color = colorLibrary.generate(color, color, color);
		// frameBuffer.setPixel(x, y, depth - 1000, (byte) 0, color);
		return depth < lightSpaceLocation[VECTOR_Z];
	}
}
