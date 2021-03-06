package com.johnsproject.jgameengine;

import java.awt.Color;
import java.awt.TextArea;
import java.util.List;

import com.johnsproject.jgameengine.event.EngineEvent;
import com.johnsproject.jgameengine.event.EngineListener;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Model;

public class EngineStatistics implements EngineListener {

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_WIDTH = 180;
	private static final int STATISTICS_HEIGHT = 130;
	private static final Color STATISTICS_BACKROUND = Color.WHITE;
	
	private static final long BYTE_TO_MEGABYTE = 1024L * 1024L;
	
	private final TextArea textArea;
	private GraphicsEngine graphicsEngine;
	private long averageUpdates;
	private long loops;
	
	public EngineStatistics(EngineWindow window) {
		this.textArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_NONE);
		window.add(textArea, 0);
	}
	
	public void initialize(EngineEvent e) { 
		textArea.setLocation(STATISTICS_X, STATISTICS_Y);
		textArea.setSize(STATISTICS_WIDTH, STATISTICS_HEIGHT);
		textArea.setEditable(false);
		textArea.setBackground(STATISTICS_BACKROUND);
		graphicsEngine = getGraphicsEngine();
	}
	
	private GraphicsEngine getGraphicsEngine() {
		GraphicsEngine graphicsEngine = null;
		final List<EngineListener> engineListeners = Engine.getInstance().getEngineListeners(); 
		for (int i = 0; i < engineListeners.size(); i++) {
			final EngineListener engineListener = engineListeners.get(i);
			if(engineListener instanceof GraphicsEngine) {
				graphicsEngine = (GraphicsEngine) engineListener;
			}
		}
		return graphicsEngine;				
	}
	
	public void fixedUpdate(EngineEvent e) {
		final String output = getOutput(e);
		textArea.setText(output);
	}

	public void dynamicUpdate(EngineEvent e) { }

	public int getLayer() {
		return GRAPHICS_ENGINE_LAYER - 1;
	}
	
	private String getOutput(EngineEvent e) {
		final List<Model> models = e.getScene().getModels();		
		
		String output = "== ENGINE STATISTICS ==\n";
		output += getRAMUsage();
		output += getCPUTime(e.getElapsedUpdateTime() + 1);
		output += getFrameBufferSize();
		output += getVertexCount(models);
		output += getTriangleCount(models);
		return output;
	}
	
	private String getRAMUsage() {
		final Runtime runtime = Runtime.getRuntime();
		final long totalRAM = runtime.totalMemory() / BYTE_TO_MEGABYTE;
		final long usedRAM = (runtime.totalMemory() - runtime.freeMemory()) / BYTE_TO_MEGABYTE;
		return "RAM usage\t" + usedRAM + " / " + totalRAM + " MB\n";
	}
	
	private String getCPUTime(long elapsedTime) {
		final long updates = 1000 / elapsedTime;
		averageUpdates += updates;
		loops++;
		if(loops >= 100) {
			averageUpdates = averageUpdates / loops;
			loops = 1;
		}
		String cpuTime = "CPU time\t" + elapsedTime + " ms\n";
		cpuTime += "Updates / s\t" + updates + "\n";
		cpuTime += "Average U / s\t" + (averageUpdates / loops) + "\n";
		return cpuTime;
	}
	
	private String getFrameBufferSize() {
		final FrameBuffer frameBuffer = graphicsEngine.getFrameBuffer();
		return "Framebuffer\t" + frameBuffer.getWidth() + "x" + frameBuffer.getHeight() + "\n";
	}
	
	private String getVertexCount(List<Model> models) {
		int vertexCount = 0;
		for (int i = 0; i < models.size(); i++)
			vertexCount += models.get(i).getMesh().getVertices().length;

		return "Vertices\t\t" + vertexCount + "\n";
	}
	
	private String getTriangleCount(List<Model> models) {
		int triangleCount = 0;
		for (int i = 0; i < models.size(); i++)
			triangleCount += models.get(i).getMesh().getFaces().length;
		
		return "Triangles\t" + triangleCount + "\n";
	}
}
