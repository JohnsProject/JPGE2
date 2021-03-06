package com.johnsproject.jgameengine.model;

import com.johnsproject.jgameengine.util.ColorUtils;
import com.johnsproject.jgameengine.util.FixedPointUtils;
import com.johnsproject.jgameengine.util.TransformationUtils;
import com.johnsproject.jgameengine.util.VectorUtils;

public class Light extends SceneObject {
	
	public static final String LIGHT_TAG = "Light";
	
	private static final int DIRECTIONAL_BIAS = FixedPointUtils.toFixedPoint(0.05f);
	private static final int SPOT_BIAS = FixedPointUtils.toFixedPoint(2f);
	
	private LightType type;
	private int intensity;
	private int color;
	private int ambientColor;
	private final int[] directionRotation;
	private final int[] direction;
	private int spotSize;
	private int spotSizeCos;
	private int innerSpotSize;
	private int innerSpotSizeCos;
	private int spotSoftness;
	private int constantAttenuation;
	private int linearAttenuation;
	private int quadraticAttenuation;
	private int shadowBias;
	private boolean hasShadow;
	private boolean isMain;
	
	public Light(String name, Transform transform) {
		super(name, transform);
		super.tag = LIGHT_TAG;
		super.rigidBody.setKinematic(true);
		this.type = LightType.DIRECTIONAL;
		this.intensity = FixedPointUtils.FP_ONE;
		this.color = ColorUtils.WHITE;
		this.ambientColor = ColorUtils.toColor(30, 30, 30);
		this.directionRotation = VectorUtils.emptyVector();
		this.direction = VectorUtils.VECTOR_FORWARD.clone();
		this.constantAttenuation = FixedPointUtils.toFixedPoint(1);
		this.linearAttenuation = FixedPointUtils.toFixedPoint(0.09);
		this.quadraticAttenuation = FixedPointUtils.toFixedPoint(0.032);
		this.shadowBias = DIRECTIONAL_BIAS;
		this.hasShadow = true;
		this.isMain = false;
		setSpotSize(FixedPointUtils.toFixedPoint(45));
		setInnerSpotSize(FixedPointUtils.toFixedPoint(35));
	}
	
	public LightType getType() {
		return type;
	}

	public void setType(LightType type) {
		this.type = type;
		if(hasShadowBiasDefaultValue()) {
			switch (type) {
			case DIRECTIONAL:
				shadowBias = DIRECTIONAL_BIAS;
				break;
			case SPOT:
				shadowBias = SPOT_BIAS;
				break;
			default:
				break;
			}
		}
	}
	
	private boolean hasShadowBiasDefaultValue() {
		return (shadowBias == DIRECTIONAL_BIAS)
				|| (shadowBias == SPOT_BIAS);
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int strength) {
		this.intensity = strength;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(int ambientColor) {
		this.ambientColor = ambientColor;
	}

	/**
	 * Returns the direction of this {@link Light}.
	 * The direction is calculated based on the light's rotation.
	 * 
	 * @return The direction of this Light.
	 */
	public int[] getDirection() {
		if(!VectorUtils.equals(directionRotation, transform.getRotation())) {
			synchronized (directionRotation) {
				VectorUtils.copy(directionRotation, transform.getRotation());
				VectorUtils.copy(direction, VectorUtils.VECTOR_FORWARD);
				TransformationUtils.rotateX(direction, directionRotation[VectorUtils.VECTOR_X]);
				TransformationUtils.rotateY(direction, directionRotation[VectorUtils.VECTOR_Y]);
				TransformationUtils.rotateZ(direction, directionRotation[VectorUtils.VECTOR_Z]);
			}
		}
		return direction;
	}
	
	/**
	 * Returns the cosine of the spot size of this {@link Light}.
	 * This is needed for lighting calculations.
	 * 
	 * @return The cosine of the spot size.
	 */
	public int getSpotSizeCosine() {
		return spotSizeCos;
	}

	public int getSpotSize() {
		return spotSize;
	}

	public void setSpotSize(int degrees) {
		this.spotSize = degrees;
		// divide by 2 so the size is the size of the whole spot
		this.spotSizeCos = FixedPointUtils.cos(degrees >> 1);
		calculateSpotSoftness();
	}
	
	/**
	 * Returns the cosine of the inner spot size of this {@link Light}.
	 * This is needed for lighting calculations.
	 * 
	 * @return The cosine of the inner spot size.
	 */
	public int getInnerSpotSizeCosine() {
		return innerSpotSizeCos;
	}
	
	public int getInnerSpotSize() {
		return innerSpotSize;
	}

	public void setInnerSpotSize(int degrees) {
		this.innerSpotSize = degrees;
		this.innerSpotSizeCos = FixedPointUtils.cos(degrees >> 1);
		calculateSpotSoftness();
	}
	
	/**
	 * Returns the difference between {@link #getInnerSpotSizeCosine()} and {@link #getSpotSizeCosine()}.
	 * This is needed for lighting calculations.
	 * 
	 * @return The difference between innerSpotSize and spotSize.
	 */
	public int getSpotSoftness() {
		return spotSoftness;
	}
	
	private void calculateSpotSoftness() {
		// + 1 because it can't be 0
		spotSoftness = (innerSpotSizeCos - spotSizeCos) + 1;
	}

	public int getConstantAttenuation() {
		return constantAttenuation;
	}

	public void setConstantAttenuation(int constantAttenuation) {
		this.constantAttenuation = constantAttenuation;
	}

	public int getLinearAttenuation() {
		return linearAttenuation;
	}

	public void setLinearAttenuation(int linearAttenuation) {
		this.linearAttenuation = linearAttenuation;
	}

	public int getQuadraticAttenuation() {
		return quadraticAttenuation;
	}

	public void setQuadraticAttenuation(int quadraticAttenuation) {
		this.quadraticAttenuation = quadraticAttenuation;
	}

	public int getShadowBias() {
		return shadowBias;
	}

	public void setShadowBias(int shadowBias) {
		this.shadowBias = shadowBias;
	}

	public boolean hasShadow() {
		return hasShadow;
	}

	public void setShadow(boolean hasShadow) {
		this.hasShadow = hasShadow;
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean isMain) {
		this.isMain = isMain;
	}
}
