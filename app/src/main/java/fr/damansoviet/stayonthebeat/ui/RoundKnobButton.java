/*
This file has been adapted by HugoS for StayOnTheBeat project.

Modification Date:  April, 2020

****************************************************************************

File:              RoundKnobButton
Version:           1.0.0
Release Date:      November, 2013
License:           GPL v2
Description:	   A round knob button to control volume and toggle between two states


****************************************************************************
Copyright (C) 2013 Radu Motisan  <radu.motisan@gmail.com>

http://www.pocketmagic.net

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
****************************************************************************/
package fr.damansoviet.stayonthebeat.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class RoundKnobButton extends RelativeLayout implements OnGestureListener {

	//*** members ***//
	private static final String TAG = "RoundKnobButton";
	private GestureDetector 	gestureDetector;
	private float 				mAngleDown , mAngleUp, percentage;
	private ImageView			ivRotor;
	private Bitmap 				bmpRotorOn , bmpRotorOff;
	private boolean 			mState = false;
	
	interface RoundKnobButtonListener {
		public void onStateChange(boolean newState) ;
		public void onRotate(float percentage);
	}
	private RoundKnobButtonListener m_listener;

	//*** Constructors ***//
	public RoundKnobButton(Context context, int back, int rotorOn, int rotorOff, final int w, final int h) {
		super(context);
		// create stator
		ImageView ivBack = new ImageView(context);
		ivBack.setImageResource(back);
		RelativeLayout.LayoutParams lp_ivBack = new RelativeLayout.LayoutParams(
				w,h);
		lp_ivBack.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(ivBack, lp_ivBack);
		// load rotor images
		Bitmap srcOn = BitmapFactory.decodeResource(context.getResources(), rotorOn);
		Bitmap srcOff = BitmapFactory.decodeResource(context.getResources(), rotorOff);
	    float scaleWidth = ((float) w) / srcOn.getWidth();
	    float scaleHeight = ((float) h) / srcOn.getHeight();
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
		    
		bmpRotorOn = Bitmap.createBitmap(
				srcOn, 0, 0,
				srcOn.getWidth(),srcOn.getHeight() , matrix , true);
		bmpRotorOff = Bitmap.createBitmap(
				srcOff, 0, 0,
				srcOff.getWidth(),srcOff.getHeight() , matrix , true);
		// create rotor
		ivRotor = new ImageView(context);
		ivRotor.setImageBitmap(bmpRotorOn);
		RelativeLayout.LayoutParams lp_ivKnob = new RelativeLayout.LayoutParams(w,h);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp_ivKnob.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(ivRotor, lp_ivKnob);
		// set initial state
		setState(mState);
		// enable gesture detector
		gestureDetector = new GestureDetector(getContext(), this);
	}

	//*** private methods ***//
	private float cartesianToPolar(float x, float y) {
		return (float) -Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
	}

	//*** Getters and setters ***//

	public void setListener(RoundKnobButtonListener l) {
		m_listener = l;
	}

	public boolean getState() { return mState; }

	public void setState(boolean state) {
		mState = state;
		ivRotor.setImageBitmap(state?bmpRotorOn:bmpRotorOff);
	}

	private void setRotorPosAngle(float deg) {

		if (deg >= 210 || deg <= 150) {
			if (deg > 180) deg = deg - 360;
			Matrix matrix=new Matrix();
			ivRotor.setScaleType(ScaleType.MATRIX);
			matrix.postRotate(deg, (int)(getWidth()/2), (int)(getHeight()/2));//;
			ivRotor.setImageMatrix(matrix);
		}
	}

	public float getRotorPercentage() {
		return percentage;
	}

	public void setRotorPercentage(float percentage) {
		// check if in bounds
		this.percentage = (percentage<0f?0f:(percentage>100?100:percentage));
		// compute degrees
		float posDegree = this.percentage * 3f - 150f;
		if (posDegree < 0) posDegree = 360f + posDegree;
		setRotorPosAngle(posDegree);
	}

	//*** RelativeLayout part ***//

	@Override public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) return true;
		else return super.onTouchEvent(event);
	}

	//*** OnGestureListener implementation ***//

	public boolean onDown(MotionEvent e) {
		float x = e.getX() / ((float) getWidth());
		float y = e.getY() / ((float) getHeight());
		mAngleDown = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
		return true;
	}
	
	public boolean onSingleTapUp(MotionEvent e) {
		float x = e.getX() / ((float) getWidth());
		float y = e.getY() / ((float) getHeight());
		mAngleUp = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
		
		// if we click up the same place where we clicked down, it's just a button press
		if (! Float.isNaN(mAngleDown) && ! Float.isNaN(mAngleUp) && Math.abs(mAngleUp-mAngleDown) < 10) {
			setState(!mState);
			if (m_listener != null) m_listener.onStateChange(mState);
		}
		return true;
	}
	
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		float x = e2.getX() / ((float) getWidth());
		float y = e2.getY() / ((float) getHeight());
		float rotDegrees = cartesianToPolar(1 - x, 1 - y);// 1- to correct our custom axis direction
		
		if (! Float.isNaN(rotDegrees)) {
			// instead of getting 0-> 180, -180 0 , we go for 0 -> 360
			float posDegrees = rotDegrees;
			if (rotDegrees < 0) posDegrees = 360 + rotDegrees;
			
			// deny full rotation, start start and stop point, and get a linear scale
			if (posDegrees > 210 || posDegrees < 150) {
				// rotate our imageView
				setRotorPosAngle(posDegrees);
				// get a linear scale
				float scaleDegrees = rotDegrees + 150; // given the current parameters, we go from 0 to 300
				// get position percent
				percentage = scaleDegrees / 3;
				if (m_listener != null) m_listener.onRotate(percentage);
				return true; //consumed
			} else
				return false;
		} else
			return false; // not consumed
	}

	public void onShowPress(MotionEvent e) { 	}
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) { return false; }
	public void onLongPress(MotionEvent e) {	}

}

