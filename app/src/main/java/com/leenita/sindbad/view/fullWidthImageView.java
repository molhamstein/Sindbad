package com.leenita.sindbad.view;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.leenita.sindbad.R;

public class fullWidthImageView extends ImageView {

    public static final String TAG = "fullWidthRoundedImageView";

    public static final int DEFAULT_RADIUS = 0;
    public static final int DEFAULT_BORDER = 0;

    private int mCornerRadius;
    private int mBorderWidth;
    private ColorStateList mBorderColor;

    private boolean mRoundBackground = false;
    private boolean mOval = false;

    private Drawable mDrawable;
    private Drawable mBackgroundDrawable;

    private ScaleType mScaleType;

    private static final ScaleType[] sScaleTypeArray = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };

    public fullWidthImageView(Context context) {
        super(context);
        mCornerRadius = DEFAULT_RADIUS;
        mBorderWidth = DEFAULT_BORDER;
        mBorderColor = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
    }

    public fullWidthImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public fullWidthImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        try {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);

			int index = a.getInt(R.styleable.RoundedImageView_android_scaleType, -1);
			if (index >= 0) {
			    setScaleType(sScaleTypeArray[index]);
			}

			mCornerRadius = a.getDimensionPixelSize(R.styleable.RoundedImageView_corner_radius, -1);
			mBorderWidth = a.getDimensionPixelSize(R.styleable.RoundedImageView_border_width, -1);

			// don't allow negative values for radius and border
			if (mCornerRadius < 0) {
			    mCornerRadius = DEFAULT_RADIUS;
			}
			if (mBorderWidth < 0) {
			    mBorderWidth = DEFAULT_BORDER;
			}

			mBorderColor = a.getColorStateList(R.styleable.RoundedImageView_border_color);
			if (mBorderColor == null) {
			    mBorderColor = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
			}

			mRoundBackground = a.getBoolean(R.styleable.RoundedImageView_round_background, false);
			mOval = a.getBoolean(R.styleable.RoundedImageView_is_oval, false);

			if (mDrawable instanceof RoundedDrawable) {
			    updateDrawableAttrs((RoundedDrawable) mDrawable);
			}

			if (mRoundBackground) {
			    if (!(mBackgroundDrawable instanceof RoundedDrawable)) {
			        // try setting background drawable now that we got the mRoundBackground param
			        setBackgroundDrawable(mBackgroundDrawable);
			    }
			    if (mBackgroundDrawable instanceof RoundedDrawable) {
			        updateDrawableAttrs((RoundedDrawable) mBackgroundDrawable);
			    }
			}

			a.recycle();
		}
        catch (Exception ignored) {}
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            Drawable drawable = getDrawable();

            if (drawable == null) {
                setMeasuredDimension(0, 0);
            } else {
                float imageSideRatio = (float)drawable.getIntrinsicWidth() / (float)drawable.getIntrinsicHeight();


                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = (int)(width / imageSideRatio);
                setMeasuredDimension(width, height);

    //** replace the couple previous lines with the following block to make the image fit either width or height **//


//                float viewSideRatio = (float)MeasureSpec.getSize(widthMeasureSpec) / (float)MeasureSpec.getSize(heightMeasureSpec);
//                if (imageSideRatio >= viewSideRatio) {
//                    // Image is wider than the display (ratio)
//                    int width = MeasureSpec.getSize(widthMeasureSpec);
//                    int height = (int)(width / imageSideRatio);
//                    setMeasuredDimension(width, height);
//                } else {
//                    // Image is taller than the display (ratio)
//                    int height = MeasureSpec.getSize(heightMeasureSpec);
//                    int width = (int)(height * imageSideRatio);
//                    setMeasuredDimension(width, height);
//                }
            }
        } catch (Exception e) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void drawableStateChanged() {
        try {
			super.drawableStateChanged();
			invalidate();
		} 
        catch (Exception ignored) {}
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    @Override
    public void setScaleType(ScaleType scaleType) {
        try {
			if (scaleType == null) {
			    throw new NullPointerException();
			}

			if (mScaleType != scaleType) {
			    mScaleType = scaleType;

			    switch (scaleType) {
			        case CENTER:
			        case CENTER_CROP:
			        case CENTER_INSIDE:
			        case FIT_CENTER:
			        case FIT_START:
			        case FIT_END:
			        case FIT_XY:
			            super.setScaleType(ScaleType.FIT_XY);
			            break;
			        default:
			            super.setScaleType(scaleType);
			            break;
			    }

			    if (mDrawable instanceof RoundedDrawable
			            && ((RoundedDrawable) mDrawable).getScaleType() != scaleType) {
			        ((RoundedDrawable) mDrawable).setScaleType(scaleType);
			    }

			    if (mBackgroundDrawable instanceof RoundedDrawable
			            && ((RoundedDrawable) mBackgroundDrawable).getScaleType() != scaleType) {
			        ((RoundedDrawable) mBackgroundDrawable).setScaleType(scaleType);
			    }
			    setWillNotCacheDrawing(true);
			    requestLayout();
			    invalidate();
			}
		}
        catch (Exception ignored) {}
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see android.widget.ImageView.ScaleType
     */
    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }


    @Override
    public void setImageDrawable(Drawable drawable) {
        try {
			if (drawable != null) {
			    mDrawable = RoundedDrawable.fromDrawable(drawable, mCornerRadius, mBorderWidth, mBorderColor, mOval);
			    updateDrawableAttrs((RoundedDrawable) mDrawable);
			} else {
			    mDrawable = null;
			}
			super.setImageDrawable(mDrawable);
		} 
        catch (Exception ignored) {}
    }

    public void setImageBitmap(Bitmap bm) {
        try {
			if (bm != null) {
			    mDrawable = new RoundedDrawable(bm, mCornerRadius, mBorderWidth, mBorderColor, mOval);
			    updateDrawableAttrs((RoundedDrawable) mDrawable);
			} else {
			    mDrawable = null;
			}
			super.setImageDrawable(mDrawable);
		} 
        catch (Exception ignored) {}
    }

    /*@Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }*/

    private void updateDrawableAttrs(RoundedDrawable drawable) {
        try {
			drawable.setScaleType(mScaleType);
			drawable.setCornerRadius(mCornerRadius);
			drawable.setBorderWidth(mBorderWidth);
			drawable.setBorderColors(mBorderColor);
			drawable.setOval(mOval);
		} 
        catch (Exception ignored) {}
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        try {
			if (mRoundBackground && background != null) {
			    mBackgroundDrawable = RoundedDrawable.fromDrawable(background, mCornerRadius, mBorderWidth, mBorderColor, mOval);
			    updateDrawableAttrs((RoundedDrawable) mBackgroundDrawable);
			} else {
			    mBackgroundDrawable = background;
			}
			super.setBackgroundDrawable(mBackgroundDrawable);
		} 
        catch (Exception ignored) {}
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public int getBorder() {
        return mBorderWidth;
    }

    public int getBorderColor() {
        return mBorderColor.getDefaultColor();
    }

    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    public void setCornerRadius(int radius) {
        try {
			if (mCornerRadius == radius) {
			    return;
			}

			mCornerRadius = radius;
			if (mDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mDrawable).setCornerRadius(radius);
			}
			if (mRoundBackground && mBackgroundDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mBackgroundDrawable).setCornerRadius(radius);
			}
		}
        catch (Exception ignored) {}
    }

    public void setBorderWidth(int width) {
        try {
			if (mBorderWidth == width) {
			    return;
			}

			mBorderWidth = width;
			if (mDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mDrawable).setBorderWidth(width);
			}
			if (mRoundBackground && mBackgroundDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mBackgroundDrawable).setBorderWidth(width);
			}
			invalidate();
		} 
        catch (Exception ignored) {}
    }

    public void setBorderColor(int color) {
        setBorderColors(ColorStateList.valueOf(color));
    }

    public void setBorderColors(ColorStateList colors) {
        try {
			if (mBorderColor.equals(colors)) {
			    return;
			}

			mBorderColor = colors != null ? colors : ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
			if (mDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mDrawable).setBorderColors(colors);
			}
			if (mRoundBackground && mBackgroundDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mBackgroundDrawable).setBorderColors(colors);
			}
			if (mBorderWidth > 0) {
			    invalidate();
			}
		}
        catch (Exception ignored) {}
    }

    public void setOval(boolean oval) {
        try {
			mOval = oval;
			if (mDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mDrawable).setOval(oval);
			}
			if (mRoundBackground && mBackgroundDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mBackgroundDrawable).setOval(oval);
			}
			invalidate();
		}
        catch (Exception ignored) {}
    }

    public boolean isOval() {
        return mOval;
    }

    @Override
    public void setImageResource(int resId) {
        try {
			super.setImageResource(resId);
			setImageDrawable(getDrawable());
		} 
        catch (Exception ignored) {}
    }

    public boolean isRoundBackground() {
        return mRoundBackground;
    }

    public void setRoundBackground(boolean roundBackground) {
        try {
			if (mRoundBackground == roundBackground) {
			    return;
			}

			mRoundBackground = roundBackground;
			if (roundBackground) {
			    if (mBackgroundDrawable instanceof RoundedDrawable) {
			        updateDrawableAttrs((RoundedDrawable) mBackgroundDrawable);
			    } else {
			        setBackgroundDrawable(mBackgroundDrawable);
			    }
			} else if (mBackgroundDrawable instanceof RoundedDrawable) {
			    ((RoundedDrawable) mBackgroundDrawable).setBorderWidth(0);
			    ((RoundedDrawable) mBackgroundDrawable).setCornerRadius(0);
			}

			invalidate();
		}
        catch (Exception ignored) {}
    }
}