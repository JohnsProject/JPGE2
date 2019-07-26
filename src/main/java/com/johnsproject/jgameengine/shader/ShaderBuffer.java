package com.johnsproject.jgameengine.shader;

import java.util.List;

import com.johnsproject.jgameengine.model.Camera;
import com.johnsproject.jgameengine.model.FrameBuffer;
import com.johnsproject.jgameengine.model.Light;

public interface ShaderBuffer {

	public void setup(Camera camera, List<Light> lights, FrameBuffer frameBuffer);
	
}