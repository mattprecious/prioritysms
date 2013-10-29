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
    private View mView;

    private int mFromMarginBottom;
    private int mFromMarginLeft;
    private int mFromMarginRight;
    private int mFromMarginTop;

    private int mToMarginBottom;
    private int mToMarginLeft;
    private int mToMarginRight;
    private int mToMarginTop;

    public MarginAnimation(View view) {
        mView = view;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        mFromMarginBottom = params.bottomMargin;
        mFromMarginLeft = params.leftMargin;
        mFromMarginRight = params.rightMargin;
        mFromMarginTop = params.topMargin;
    }

    public void setMarginBottom(int toMarginBottom) {
        mToMarginBottom = toMarginBottom;
    }

    public void setMarginBottom(int fromMarginBottom, int toMarginBottom) {
        mFromMarginBottom = fromMarginBottom;
        mToMarginBottom = toMarginBottom;
    }

    public void setMarginLeft(int toMarginLeft) {
        mToMarginLeft = toMarginLeft;
    }

    public void setMarginLeft(int fromMarginLeft, int toMarginLeft) {
        mFromMarginLeft = fromMarginLeft;
        mToMarginLeft = toMarginLeft;
    }

    public void setMarginRight(int toMarginRight) {
        mToMarginRight = toMarginRight;
    }

    public void setMarginRight(int fromMarginRight, int toMarginRight) {
        mFromMarginRight = fromMarginRight;
        mToMarginRight = toMarginRight;
    }

    public void setMarginTop(int toMarginTop) {
        mToMarginTop = toMarginTop;
    }

    public void setMarginTop(int fromMarginTop, int toMarginTop) {
        mFromMarginTop = fromMarginTop;
        mToMarginTop = toMarginTop;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        params.bottomMargin = (int) (mFromMarginBottom + ((mToMarginBottom - mFromMarginBottom) * interpolatedTime));
        params.leftMargin = (int) (mFromMarginLeft + ((mToMarginLeft - mFromMarginLeft) * interpolatedTime));
        params.rightMargin = (int) (mFromMarginRight + ((mToMarginRight - mFromMarginRight) * interpolatedTime));
        params.topMargin = (int) (mFromMarginTop + ((mToMarginTop - mFromMarginTop) * interpolatedTime));
        mView.requestLayout();
    }
}
