/*
 * Copyright 2013 Matthew Precious
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms.view;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class MarginAnimation extends Animation {
  private View view;

  private int fromMarginBottom;
  private int fromMarginLeft;
  private int fromMarginRight;
  private int fromMarginTop;

  private int toMarginBottom;
  private int toMarginLeft;
  private int toMarginRight;
  private int toMarginTop;

  public MarginAnimation(View view) {
    this.view = view;

    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    fromMarginBottom = params.bottomMargin;
    fromMarginLeft = params.leftMargin;
    fromMarginRight = params.rightMargin;
    fromMarginTop = params.topMargin;
  }

  public void setMarginBottom(int toMarginBottom) {
    this.toMarginBottom = toMarginBottom;
  }

  public void setMarginBottom(int fromMarginBottom, int toMarginBottom) {
    this.fromMarginBottom = fromMarginBottom;
    this.toMarginBottom = toMarginBottom;
  }

  public void setMarginLeft(int toMarginLeft) {
    this.toMarginLeft = toMarginLeft;
  }

  public void setMarginLeft(int fromMarginLeft, int toMarginLeft) {
    this.fromMarginLeft = fromMarginLeft;
    this.toMarginLeft = toMarginLeft;
  }

  public void setMarginRight(int toMarginRight) {
    this.toMarginRight = toMarginRight;
  }

  public void setMarginRight(int fromMarginRight, int toMarginRight) {
    this.fromMarginRight = fromMarginRight;
    this.toMarginRight = toMarginRight;
  }

  public void setMarginTop(int toMarginTop) {
    this.toMarginTop = toMarginTop;
  }

  public void setMarginTop(int fromMarginTop, int toMarginTop) {
    this.fromMarginTop = fromMarginTop;
    this.toMarginTop = toMarginTop;
  }

  @Override protected void applyTransformation(float interpolatedTime, Transformation t) {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    params.bottomMargin =
        (int) (fromMarginBottom + ((toMarginBottom - fromMarginBottom) * interpolatedTime));
    params.leftMargin =
        (int) (fromMarginLeft + ((toMarginLeft - fromMarginLeft) * interpolatedTime));
    params.rightMargin =
        (int) (fromMarginRight + ((toMarginRight - fromMarginRight) * interpolatedTime));
    params.topMargin =
        (int) (fromMarginTop + ((toMarginTop - fromMarginTop) * interpolatedTime));
    view.requestLayout();
  }
}
