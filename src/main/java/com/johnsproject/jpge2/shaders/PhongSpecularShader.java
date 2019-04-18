package com.johnsproject.jpge2.shaders;

import java.util.List;

import com.johnsproject.jpge2.dto.Camera;
import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.FrameBuffer;
import com.johnsproject.jpge2.dto.Light;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Texture;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.CentralProcessor;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor;
import com.johnsproject.jpge2.processors.GraphicsProcessor.Shader;
import com.johnsproject.jpge2.processors.GraphicsProcessor.ShaderDataBuffer;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.MatrixProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class PhongSpecularShader extends Shader {

	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private static final byte FP_BITS = MathProcessor.FP_BITS;
	private static final int FP_ONE = MathProcessor.FP_ONE;
	
	private final int[] uvX;
	private final int[] uvY;

	private final int[][] modelMatrix;
	private final int[][] normalMatrix;
	private final int[][] viewMatrix;
	private final int[][] projectionMatrix;

	private final int[] fragmentLocation;
	private final int[] normalizedNormal;
	private final int[] lightDirection;
	private final int[] viewDirection;

	private final int[] viewDirectionX;
	private final int[] viewDirectionY;
	private final int[] viewDirectionZ;
	private final int[] locationX;
	private final int[] locationY;
	private final int[] locationZ;
	private final int[] normalX;
	private final int[] normalY;
	private final int[] normalZ;

	private final MathProcessor mathProcessor;
	private final MatrixProcessor matrixProcessor;
	private final VectorProcessor vectorProcessor;
	private final ColorProcessor colorProcessor;
	private final GraphicsProcessor graphicsProcessor;
	
	private ShaderDataBuffer dataBuffer;
	
	private Material material;
	private int modelColor;
	private Texture texture;

	private Camera camera;
	private List<Light> lights;
	private FrameBuffer frameBuffer;

	public PhongSpecularShader(CentralProcessor centralProcessor) {
		super(centralProcessor);
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.matrixProcessor = centralProcessor.getMatrixProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
		this.graphicsProcessor = centralProcessor.getGraphicsProcessor();
		
		dataBuffer = new ShaderDataBuffer();
		
		uvX = vectorProcessor.generate();
		uvY = vectorProcessor.generate();

		fragmentLocation = vectorProcessor.generate();
		normalizedNormal = vectorProcessor.generate();
		lightDirection = vectorProcessor.generate();
		viewDirection = vectorProcessor.generate();

		viewDirectionX = vectorProcessor.generate();
		viewDirectionY = vectorProcessor.generate();
		viewDirectionZ = vectorProcessor.generate();
		locationX = vectorProcessor.generate();
		locationY = vectorProcessor.generate();
		locationZ = vectorProcessor.generate();
		normalX = vectorProcessor.generate();
		normalY = vectorProcessor.generate();
		normalZ = vectorProcessor.generate();
		
		modelMatrix = matrixProcessor.generate();
		normalMatrix = matrixProcessor.generate();
		viewMatrix = matrixProcessor.generate();
		projectionMatrix = matrixProcessor.generate();
	}
	
	public void setDataBuffer(ShaderDataBuffer shaderDataBuffer) {
		this.dataBuffer = shaderDataBuffer;
		this.lights = shaderDataBuffer.getLights();
		this.frameBuffer = shaderDataBuffer.getFrameBuffer();
		frameBuffer.clearColorBuffer();
		frameBuffer.clearDepthBuffer();
	}

	public ShaderDataBuffer getDataBuffer() {
		return dataBuffer;
	}

	public void setup(Model model, Camera camera) {
		this.camera = camera;

		graphicsProcessor.setup(frameBuffer.getSize(), camera.getCanvas(), this);
		
		matrixProcessor.copy(modelMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(normalMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(viewMatrix, MatrixProcessor.MATRIX_IDENTITY);
		matrixProcessor.copy(projectionMatrix, MatrixProcessor.MATRIX_IDENTITY);

		graphicsProcessor.getModelMatrix(model.getTransform(), modelMatrix);
		graphicsProcessor.getNormalMatrix(model.getTransform(), normalMatrix);
		graphicsProcessor.getViewMatrix(camera.getTransform(), viewMatrix);

		switch (camera.getType()) {
		case ORTHOGRAPHIC:
			graphicsProcessor.getOrthographicMatrix(camera.getFrustum(), projectionMatrix);
			break;

		case PERSPECTIVE:
			graphicsProcessor.getPerspectiveMatrix(camera.getFrustum(), projectionMatrix);
			break;
		}
	}

	public void vertex(int index, Vertex vertex) {
		int[] location = vectorProcessor.copy(vertex.getLocation(), vertex.getStartLocation());
		int[] normal = vectorProcessor.copy(vertex.getNormal(), vertex.getStartNormal());
		
		vectorProcessor.multiply(location, modelMatrix, location);
		locationX[index] = location[VECTOR_X];
		locationY[index] = location[VECTOR_Y];
		locationZ[index] = location[VECTOR_Z];
		
		vectorProcessor.subtract(camera.getTransform().getLocation(), location, viewDirection);
		vectorProcessor.normalize(viewDirection, viewDirection);
		viewDirectionX[index] = viewDirection[VECTOR_X];
		viewDirectionY[index] = viewDirection[VECTOR_Y];
		viewDirectionZ[index] = viewDirection[VECTOR_Z];
		
		vectorProcessor.multiply(location, viewMatrix, location);
		vectorProcessor.multiply(location, projectionMatrix, location);
		graphicsProcessor.viewport(location, location);

		vectorProcessor.multiply(normal, normalMatrix, normal);
		vectorProcessor.normalize(normal, normalizedNormal);
		normalX[index] = normalizedNormal[VECTOR_X];
		normalY[index] = normalizedNormal[VECTOR_Y];
		normalZ[index] = normalizedNormal[VECTOR_Z];
	}

	public void geometry(Face face) {
		int[] location1 = face.getVertex(0).getLocation();
		int[] location2 = face.getVertex(1).getLocation();
		int[] location3 = face.getVertex(2).getLocation();

		material = face.getMaterial();

		if (!graphicsProcessor.isBackface(location1, location2, location3)
				&& graphicsProcessor.isInsideFrustum(location1, location2, location3, camera.getFrustum())) {
			texture = face.getMaterial().getTexture();
			// set uv values that will be interpolated and fit uv into texture resolution
			if (texture != null) {
				int width = texture.getWidth() - 1;
				int height = texture.getHeight() - 1;
				uvX[0] = mathProcessor.multiply(face.getUV1()[VECTOR_X], width);
				uvX[1] = mathProcessor.multiply(face.getUV2()[VECTOR_X], width);
				uvX[2] = mathProcessor.multiply(face.getUV3()[VECTOR_X], width);
				uvY[0] = mathProcessor.multiply(face.getUV1()[VECTOR_Y], height);
				uvY[1] = mathProcessor.multiply(face.getUV2()[VECTOR_Y], height);
				uvY[2] = mathProcessor.multiply(face.getUV3()[VECTOR_Y], height);
			}
			graphicsProcessor.drawTriangle(location1, location2, location3);
		}
	}

	public void fragment(int[] location, int[] barycentric) {

		viewDirection[VECTOR_X] = graphicsProcessor.interpolate(viewDirectionX, barycentric);
		viewDirection[VECTOR_Y] = graphicsProcessor.interpolate(viewDirectionY, barycentric);
		viewDirection[VECTOR_Z] = graphicsProcessor.interpolate(viewDirectionZ, barycentric);
		
		fragmentLocation[VECTOR_X] = graphicsProcessor.interpolate(locationX, barycentric);
		fragmentLocation[VECTOR_Y] = graphicsProcessor.interpolate(locationY, barycentric);
		fragmentLocation[VECTOR_Z] = graphicsProcessor.interpolate(locationZ, barycentric);

		normalizedNormal[VECTOR_X] = graphicsProcessor.interpolate(normalX, barycentric);
		normalizedNormal[VECTOR_Y] = graphicsProcessor.interpolate(normalY, barycentric);
		normalizedNormal[VECTOR_Z] = graphicsProcessor.interpolate(normalZ, barycentric);

		int lightColor = ColorProcessor.WHITE;
		int lightFactor = 0;

		for (int i = 0; i < lights.size(); i++) {
			Light light = lights.get(i);
			int[] lightLocation = light.getTransform().getLocation();
			int currentFactor = 0;
			vectorProcessor.subtract(lightLocation, fragmentLocation, lightDirection);
			switch (light.getType()) {
			case DIRECTIONAL:
				vectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				break;
			case POINT:
				// attenuation
				long distance = vectorProcessor.magnitude(lightDirection);
				int attenuation = FP_ONE;
				attenuation += mathProcessor.multiply(distance, 3000);
				attenuation += mathProcessor.multiply(mathProcessor.multiply(distance, distance), 20);
				attenuation = attenuation >> FP_BITS;
				// other light values
				vectorProcessor.normalize(lightDirection, lightDirection);
				currentFactor = getLightFactor(light, normalizedNormal, lightDirection, viewDirection, material);
				currentFactor = (currentFactor * 100) / attenuation;
				break;
			}
			lightColor = colorProcessor.lerp(lightColor, light.getDiffuseColor(), currentFactor);
			lightFactor += currentFactor;
		}

		if (texture != null) {
			int u = graphicsProcessor.interpolate(uvX, barycentric);
			int v = graphicsProcessor.interpolate(uvY, barycentric);
			int texel = texture.getPixel(u, v);
			if (colorProcessor.getAlpha(texel) == 0) // discard pixel if alpha = 0
				return;
			modelColor = colorProcessor.lerp(ColorProcessor.BLACK, texel, lightFactor);
			modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
		} else {
			modelColor = colorProcessor.lerp(ColorProcessor.BLACK, material.getColor(), lightFactor);
			modelColor = colorProcessor.multiplyColor(modelColor, lightColor);
		}
		frameBuffer.setPixel(location[VECTOR_X], location[VECTOR_Y], location[VECTOR_Z], (byte) 0, modelColor);
	}

	private int getLightFactor(Light light, int[] normal, int[] lightDirection, int[] viewDirection,
			Material material) {
		// diffuse
		int dotProduct = vectorProcessor.dotProduct(normal, lightDirection);
		int diffuseFactor = Math.max(dotProduct, 0);
		diffuseFactor = mathProcessor.multiply(diffuseFactor, material.getDiffuseIntensity());
		// specular
		vectorProcessor.invert(lightDirection, lightDirection);
		vectorProcessor.reflect(lightDirection, normal, lightDirection);
		dotProduct = vectorProcessor.dotProduct(viewDirection, lightDirection);
		int specularFactor = Math.max(dotProduct, 0);
		specularFactor = mathProcessor.pow(specularFactor, material.getShininess());
		specularFactor = mathProcessor.multiply(specularFactor, material.getSpecularIntensity());
		// putting it all together...
		return ((diffuseFactor + specularFactor + light.getStrength()) * 100) >> FP_BITS;
	}
}
