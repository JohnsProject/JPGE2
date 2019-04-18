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
package com.johnsproject.jpge2.importers;

import java.io.IOException;
import java.io.InputStream;

import com.johnsproject.jpge2.dto.Face;
import com.johnsproject.jpge2.dto.Material;
import com.johnsproject.jpge2.dto.Model;
import com.johnsproject.jpge2.dto.Transform;
import com.johnsproject.jpge2.dto.Vertex;
import com.johnsproject.jpge2.processors.CentralProcessor;
import com.johnsproject.jpge2.processors.ColorProcessor;
import com.johnsproject.jpge2.processors.FileProcessor;
import com.johnsproject.jpge2.processors.MathProcessor;
import com.johnsproject.jpge2.processors.VectorProcessor;

public class SOMImporter {
	
	private static final byte VECTOR_X = VectorProcessor.VECTOR_X;
	private static final byte VECTOR_Y = VectorProcessor.VECTOR_Y;
	private static final byte VECTOR_Z = VectorProcessor.VECTOR_Z;
	
	private final FileProcessor fileProcessor;
	private final MathProcessor mathProcessor;
	private final VectorProcessor vectorProcessor;
	private final ColorProcessor colorProcessor;
	
	public SOMImporter(CentralProcessor centralProcessor) {
		this.fileProcessor = centralProcessor.getFileProcessor();
		this.mathProcessor = centralProcessor.getMathProcessor();
		this.vectorProcessor = centralProcessor.getVectorProcessor();
		this.colorProcessor = centralProcessor.getColorProcessor();
	}
	
	public Model load(String path) throws IOException {
		String content = fileProcessor.readFile(path);
		return loadFromRaw(content);
	}

	public Model load(InputStream stream) throws IOException {
		String content = fileProcessor.readStream(stream);
		return loadFromRaw(content);
	}

	public Model loadFromRaw(String data) throws IOException {
		String rawData = data.replace(" ", "").replace("\n", "");
		Material[] materials = parseMaterials(rawData);
		Vertex[] vertices = parseVertices(rawData, materials);
		Face[] faces = parseFaces(rawData, vertices, materials);
		int[] location = vectorProcessor.generate();
		int[] rotation = vectorProcessor.generate();
		int[] scale = vectorProcessor.generate(10000, 10000, 10000);
		Model result = new Model("Model", new Transform(location, rotation, scale), vertices, faces, materials);
		System.gc();
		return result;
	}
	
	private Vertex[] parseVertices(String rawData, Material[] materials) throws IOException {
		String vCountData = rawData.split("vCount<")[1].split(">vCount", 2)[0];
		Vertex[] vertices = new Vertex[getint(vCountData)];
		String[] vLocationData = rawData.split("vPosition<")[1].split(">vPosition", 2)[0].split(",");
		String[] vNormalData = rawData.split("vNormal<")[1].split(">vNormal", 2)[0].split(",");
		String[] vMaterialData = rawData.split("vMaterial<")[1].split(">vMaterial", 2)[0].split(",");
		for (int i = 0; i < vertices.length * 3; i += 3) {
			int[] location = vectorProcessor.generate();
			location[VECTOR_X] = mathProcessor.generate(getFloat(vLocationData[i + VECTOR_X]));
			location[VECTOR_Y] = mathProcessor.generate(getFloat(vLocationData[i + VECTOR_Y]));
			location[VECTOR_Z] = mathProcessor.generate(getFloat(vLocationData[i + VECTOR_Z]));
			int[] normal = vectorProcessor.generate();
			normal[VECTOR_X] = mathProcessor.generate(getFloat(vNormalData[i + VECTOR_X]));
			normal[VECTOR_Y] = mathProcessor.generate(getFloat(vNormalData[i + VECTOR_Y]));
			normal[VECTOR_Z] = mathProcessor.generate(getFloat(vNormalData[i + VECTOR_Z]));
			int material = getint(vMaterialData[i / 3]);
			vertices[i / 3] = new Vertex(i / 3, location, normal, materials[material]);
		}
		return vertices;
	}
	
	private Face[] parseFaces(String rawData, Vertex[] vertices, Material[] materials) throws IOException {
		String fCountData = rawData.split("fCount<")[1].split(">fCount", 2)[0];
		Face[] faces = new Face[getint(fCountData)];
		String[] fVertex1Data = rawData.split("fVertex1<")[1].split(">fVertex1", 2)[0].split(",");
		String[] fVertex2Data = rawData.split("fVertex2<")[1].split(">fVertex2", 2)[0].split(",");
		String[] fVertex3Data = rawData.split("fVertex3<")[1].split(">fVertex3", 2)[0].split(",");
		String[] fMaterialData = rawData.split("fMaterial<")[1].split(">fMaterial", 2)[0].split(",");
		String[] fNormalData = rawData.split("fNormal<")[1].split(">fNormal", 2)[0].split(",");
		String[] fUV1Data = rawData.split("fUV1<")[1].split(">fUV1", 2)[0].split(",");
		String[] fUV2Data = rawData.split("fUV2<")[1].split(">fUV2", 2)[0].split(",");
		String[] fUV3Data = rawData.split("fUV3<")[1].split(">fUV3", 2)[0].split(",");
		for (int i = 0; i < faces.length * 6; i += 6) {
			int vertex1 = getint(fVertex1Data[i / 6]);
			int vertex2 = getint(fVertex2Data[i / 6]);
			int vertex3 = getint(fVertex3Data[i / 6]);
			int material = getint(fMaterialData[i / 6]);
			int[] normal = vectorProcessor.generate();
			normal[VECTOR_X] = mathProcessor.generate(getFloat(fNormalData[(i / 2) + VECTOR_X]));
			normal[VECTOR_Y] = mathProcessor.generate(getFloat(fNormalData[(i / 2) + VECTOR_Y]));
			normal[VECTOR_Z] = mathProcessor.generate(getFloat(fNormalData[(i / 2) + VECTOR_Z]));
			int[] uv1 = vectorProcessor.generate();
			uv1[VECTOR_X] = mathProcessor.generate(getFloat(fUV1Data[(i / 3) + VECTOR_X]));
			uv1[VECTOR_Y] = mathProcessor.generate(getFloat(fUV1Data[(i / 3) + VECTOR_Y]));
			int[] uv2 = vectorProcessor.generate();
			uv2[VECTOR_X] = mathProcessor.generate(getFloat(fUV2Data[(i / 3) + VECTOR_X]));
			uv2[VECTOR_Y] = mathProcessor.generate(getFloat(fUV2Data[(i / 3) + VECTOR_Y]));
			int[] uv3 = vectorProcessor.generate();
			uv3[VECTOR_X] = mathProcessor.generate(getFloat(fUV3Data[(i / 3) + VECTOR_X]));
			uv3[VECTOR_Y] = mathProcessor.generate(getFloat(fUV3Data[(i / 3) + VECTOR_Y]));
			faces[i / 6] = new Face(i / 6, vertices[vertex1], vertices[vertex2], vertices[vertex3], materials[material], normal, uv1, uv2, uv3);
		}
		return faces;
	}
	
	private Material[] parseMaterials(String rawData){
		String mCountData = rawData.split("mCount<")[1].split(">mCount", 2)[0];
		Material[] materials = new Material[getint(mCountData)];
		String[] mDiffuseColorData = rawData.split("mDiffuseColor<")[1].split(">mDiffuseColor", 2)[0].split(",");
		String[] mDiffuseIntensityData = rawData.split("mDiffuseIntensity<")[1].split(">mDiffuseIntensity", 2)[0].split(",");
		String[] mSpecularIntensityData = rawData.split("mSpecularIntensity<")[1].split(">mSpecularIntensity", 2)[0].split(",");
		for (int i = 0; i < materials.length * 4; i+=4) {
			// * 256 to get int rgb values
			int r = mathProcessor.generate(getFloat(mDiffuseColorData[i]) * 256);
			int	g = mathProcessor.generate(getFloat(mDiffuseColorData[i+1]) * 256);
			int	b = mathProcessor.generate(getFloat(mDiffuseColorData[i+2]) * 256);
			int	a = mathProcessor.generate(getFloat(mDiffuseColorData[i+3]) * 256);
			int diffuseIntensity = mathProcessor.generate(getFloat(mDiffuseIntensityData[i / 4]));
			int specularIntensity = mathProcessor.generate(getFloat(mSpecularIntensityData[i / 4]));
			materials[i/4] = new Material(i/4, "", colorProcessor.generate(a, r, g, b), diffuseIntensity, specularIntensity, 0, null);
		}
		return materials;
	}
	
	private int getint(String string) {
		return Integer.parseInt(string);
	}
	
	private float getFloat(String string) {
		return Float.parseFloat(string);
	}
}
