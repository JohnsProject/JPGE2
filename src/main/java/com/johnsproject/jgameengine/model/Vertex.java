package com.johnsproject.jgameengine.model;

public class Vertex {
	
	private final int index;
	private final int[] localLocation;
	private final int[] localNormal;
	private final int[] worldLocation;
	private final int[] worldNormal;
	private final int[] location;
	private final Material material;
	private int lightColor;
	
	public Vertex(int index, int[] location, int[] normal, Material material) {
		this.index = index;
		this.localLocation = location;
		this.localNormal = normal;
		this.worldLocation = location.clone();
		this.worldNormal = normal.clone();
		this.location = location.clone();
		this.material = material;
	}

	public int getIndex() {
		return index;
	}
	
	public int[] getLocalLocation() {
		return localLocation;
	}

	public int[] getLocalNormal() {
		return localNormal;
	}

	public int[] getWorldLocation() {
		return worldLocation;
	}

	public int[] getWorldNormal() {
		return worldNormal;
	}

	public int[] getLocation() {
		return location;
	}

	public Material getMaterial() {
		return material;
	}

	/**
	 * Returns the light color of this {@link Vertex}.
	 * The light color is the color of all lights that reach and affect the illumination of
	 * this Vertex put together.
	 * 
	 * @return The light color of this Vertex.
	 */
	public int getLightColor() {
		return lightColor;
	}

	/**
	 * Sets the light color of this {@link Vertex}.
	 * The light color is the color of all lights that reach and affect the illumination of 
	 * this Vertex put together.
	 * 
	 * @param lightColor to set.
	 */
	public void setLightColor(int lightColor) {
		this.lightColor = lightColor;
	}
}
