package com.johnsproject.jpge2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.johnsproject.jpge2.controllers.GraphicsController;
import com.johnsproject.jpge2.dto.Model;

public class EngineStatistics implements EngineListener{

	private static final int STATISTICS_X = 10;
	private static final int STATISTICS_Y = 30;
	private static final int STATISTICS_LINE = 13;
	private static final int STATISTICS_WIDTH = 160;
	private static final int STATISTICS_HEIGHT = 115;
	private static final Color STATISTICS_BACKROUND = new Color(230, 230, 230, 200);
	
	private long lastUpdateTime; 
	
	public EngineStatistics() {
		Engine.getInstance().addEngineListener(this);
	}
	
	public void start() {
		
	}

	public void update() {
		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - lastUpdateTime;
		lastUpdateTime = currentTime;
		long fps = 1000 / elapsed;
		long ramUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20;
		GraphicsController graphicsController = Engine.getInstance().getController().getGraphicsController();
		int[] frameBufferSize = graphicsController.getFrameBuffer().getSize();
		int maxFPS = Engine.getInstance().getUpdateRate();
		int shadersCount = graphicsController.getShaders().size();
		List<Model> models = Engine.getInstance().getScene().getModels();
		int verticesCount = 0;
		int trianglesCount = 0;
		for (int i = 0; i < models.size(); i++) {
			Model model = models.get(i);
			verticesCount += model.getVertices().length;
			trianglesCount += model.getFaces().length;
		}
		Graphics2D g = graphicsController.getFrameBuffer().getImage().createGraphics();
		g.setColor(STATISTICS_BACKROUND);
		g.fillRect(STATISTICS_X, STATISTICS_Y, STATISTICS_WIDTH, STATISTICS_HEIGHT);
		g.setColor(Color.black);
		int currentLine = 1;
		g.drawString("====== Statistics ======", STATISTICS_X * 2 , STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("CPU time", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(elapsed + " ms", STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("RAM usage", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(ramUsage + " MB", STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("FPS", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(fps + " / " + maxFPS, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Framebuffer", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString(frameBufferSize[0] + "x" + frameBufferSize[1], STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Shaders", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + shadersCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Vertices", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + verticesCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		currentLine++;
		g.drawString("Triangles", STATISTICS_X * 2, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.drawString("" + trianglesCount, STATISTICS_X * 11, STATISTICS_Y + STATISTICS_LINE * currentLine + 3);
		g.dispose();
	}

	public void fixedUpdate() {
		
	}

	public int getPriority() {
		return 10000;
	}

}
