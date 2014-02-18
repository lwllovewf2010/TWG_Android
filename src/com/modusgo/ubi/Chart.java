package com.modusgo.ubi;

public class Chart {
	
	public String name;
	public float[] chartsHeight;
	public ChartFragment fragment;
	
	public Chart(String name, float[] chartsHeight) {
		this.name = name;
		this.chartsHeight = chartsHeight;
		
		float averageSum = 0;
		for (float f : chartsHeight) {
			averageSum+=f;
		}

		chartsHeight[chartsHeight.length-1] = averageSum/(chartsHeight.length-1);
		
		fragment = new ChartFragment();
		fragment.setColumnsHeight(chartsHeight);
	}

}
