package com.martinmarinkovic.myapplication.lockscreen;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.martinmarinkovic.myapplication.R;

public class PinLockView extends RecyclerView {

    private static final int DEFAULT_PIN_LENGTH = 4;
    private static final int[] DEFAULT_KEY_SET = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    private String mPin = "";
    private int mPinLength;
    private int mHorizontalSpacing, mVerticalSpacing;
    private int mTextColor, mDeleteButtonPressedColor;
    private int mTextSize, mButtonSize, mDeleteButtonSize;
    private Drawable mButtonBackgroundDrawable;
    private Drawable mDeleteButtonDrawable;
    private boolean mShowDeleteButton;
    private IndicatorDots mIndicatorDots;
    private PinLockAdapter mAdapter;
    private PinLockListener mPinLockListener;
    private CustomizationOptionsBundle mCustomizationOptionsBundle;
    private int[] mCustomKeySet;

    private PinLockAdapter.OnNumberClickListener mOnNumberClickListener
            = new PinLockAdapter.OnNumberClickListener() {
        @Override
        public void onNumberClicked(int keyValue) {
            if (mPin.length() < getPinLength()) {
                mPin = mPin.concat(String.valueOf(keyValue));

                if (isIndicatorDotsAttached()) {
                    mIndicatorDots.updateDot(mPin.length());
                }

                if (mPin.length() == 1) {
                    mAdapter.setPinLength(mPin.length());
                    mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
                }

                if (mPinLockListener != null) {
                    if (mPin.length() == mPinLength) {
                        mPinLockListener.onComplete(mPin);
                    } else {
                        mPinLockListener.onPinChange(mPin.length(), mPin);
                    }
                }
            } else {
                if (!isShowDeleteButton()) {
                    resetPinLockView();
                    mPin = mPin.concat(String.valueOf(keyValue));

                    if (isIndicatorDotsAttached()) {
                        mIndicatorDots.updateDot(mPin.length());
                    }

                    if (mPinLockListener != null) {
                        mPinLockListener.onPinChange(mPin.length(), mPin);
                    }

                } else {
                    if (mPinLockListener != null) {
                        mPinLockListener.onComplete(mPin);
                    }
                }
            }
        }
    };

    private PinLockAdapter.OnDeleteClickListener mOnDeleteClickListener
            = new PinLockAdapter.OnDeleteClickListener() {
        @Override
        public void onDeleteClicked() {
            if (mPin.length() > 0) {
                mPin = mPin.substring(0, mPin.length() - 1);

                if (isIndicatorDotsAttached()) {
                    mIndicatorDots.updateDot(mPin.length());
                }

                if (mPin.length() == 0) {
                    mAdapter.setPinLength(mPin.length());
                    mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
                }

                if (mPinLockListener != null) {
                    if (mPin.length() == 0) {
                        mPinLockListener.onEmpty();
                        clearInternalPin();
                    } else {
                        mPinLockListener.onPinChange(mPin.length(), mPin);
                    }
                }
            } else {
                if (mPinLockListener != null) {
                    mPinLockListener.onEmpty();
                }
            }
        }

        @Override
        public void onDeleteLongClicked() {
            resetPinLockView();
            if (mPinLockListener != null) {
                mPinLockListener.onEmpty();
            }
        }
    };

    public PinLockView(Context context) {
        super(context);
        init(null, 0);
    }

    public PinLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PinLockView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attributeSet, int defStyle) {

        TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PinLockView);

        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH);
            mHorizontalSpacing = (int) typedArray.getDimension(R.styleable.PinLockView_keypadHorizontalSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_horizontal_spacing));
            mVerticalSpacing = (int) typedArray.getDimension(R.styleable.PinLockView_keypadVerticalSpacing, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_vertical_spacing));
            mTextColor = typedArray.getColor(R.styleable.PinLockView_keypadTextColor, ResourceUtils.getColor(getContext(), R.color.white));
            mTextSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadTextSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_text_size));
            mButtonSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_button_size));
            mDeleteButtonSize = (int) typedArray.getDimension(R.styleable.PinLockView_keypadDeleteButtonSize, ResourceUtils.getDimensionInPx(getContext(), R.dimen.default_delete_button_size));
            mButtonBackgroundDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadButtonBackgroundDrawable);
            mDeleteButtonDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadDeleteButtonDrawable);
            mShowDeleteButton = typedArray.getBoolean(R.styleable.PinLockView_keypadShowDeleteButton, true);
            mDeleteButtonPressedColor = typedArray.getColor(R.styleable.PinLockView_keypadDeleteButtonPressedColor, ResourceUtils.getColor(getContext(), R.color.greyish));
        } finally {
            typedArray.recycle();
        }

        mCustomizationOptionsBundle = new CustomizationOptionsBundle();
        mCustomizationOptionsBundle.setTextColor(mTextColor);
        mCustomizationOptionsBundle.setTextSize(mTextSize);
        mCustomizationOptionsBundle.setButtonSize(mButtonSize);
        mCustomizationOptionsBundle.setButtonBackgroundDrawable(mButtonBackgroundDrawable);
        mCustomizationOptionsBundle.setDeleteButtonDrawable(mDeleteButtonDrawable);
        mCustomizationOptionsBundle.setDeleteButtonSize(mDeleteButtonSize);
        mCustomizationOptionsBundle.setShowDeleteButton(mShowDeleteButton);
        mCustomizationOptionsBundle.setDeleteButtonPressesColor(mDeleteButtonPressedColor);

        initView();
    }

    private void initView() {
        setLayoutManager(new LTRGridLayoutManager(getContext(), 3));

        mAdapter = new PinLockAdapter(getContext());
        mAdapter.setOnItemClickListener(mOnNumberClickListener);
        mAdapter.setOnDeleteClickListener(mOnDeleteClickListener);
        mAdapter.setCustomizationOptions(mCustomizationOptionsBundle);
        setAdapter(mAdapter);

        addItemDecoration(new ItemSpaceDecoration(mHorizontalSpacing, mVerticalSpacing, 3, false));
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setPinLockListener(PinLockListener pinLockListener) {
        this.mPinLockListener = pinLockListener;
    }

    public int getPinLength() {
        return mPinLength;
    }

    public void setPinLength(int pinLength) {
        this.mPinLength = pinLength;

        if (isIndicatorDotsAttached()) {
            mIndicatorDots.setPinLength(pinLength);
        }
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        mCustomizationOptionsBundle.setTextColor(textColor);
        mAdapter.notifyDataSetChanged();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        mCustomizationOptionsBundle.setTextSize(textSize);
        mAdapter.notifyDataSetChanged();
    }

    public int getButtonSize() {
        return mButtonSize;
    }

    public void setButtonSize(int buttonSize) {
        this.mButtonSize = buttonSize;
        mCustomizationOptionsBundle.setButtonSize(buttonSize);
        mAdapter.notifyDataSetChanged();
    }

    public Drawable getButtonBackgroundDrawable() {
        return mButtonBackgroundDrawable;
    }

    public void setButtonBackgroundDrawable(Drawable buttonBackgroundDrawable) {
        this.mButtonBackgroundDrawable = buttonBackgroundDrawable;
        mCustomizationOptionsBundle.setButtonBackgroundDrawable(buttonBackgroundDrawable);
        mAdapter.notifyDataSetChanged();
    }

    public Drawable getDeleteButtonDrawable() {
        return mDeleteButtonDrawable;
    }

    public void setDeleteButtonDrawable(Drawable deleteBackgroundDrawable) {
        this.mDeleteButtonDrawable = deleteBackgroundDrawable;
        mCustomizationOptionsBundle.setDeleteButtonDrawable(deleteBackgroundDrawable);
        mAdapter.notifyDataSetChanged();
    }

    public int getDeleteButtonSize() {
        return mDeleteButtonSize;
    }

    public void setDeleteButtonSize(int deleteButtonSize) {
        this.mDeleteButtonSize = deleteButtonSize;
        mCustomizationOptionsBundle.setDeleteButtonSize(deleteButtonSize);
        mAdapter.notifyDataSetChanged();
    }

    public boolean isShowDeleteButton() {
        return mShowDeleteButton;
    }

    public void setShowDeleteButton(boolean showDeleteButton) {
        this.mShowDeleteButton = showDeleteButton;
        mCustomizationOptionsBundle.setShowDeleteButton(showDeleteButton);
        mAdapter.notifyDataSetChanged();
    }

    public int getDeleteButtonPressedColor() {
        return mDeleteButtonPressedColor;
    }

    public void setDeleteButtonPressedColor(int deleteButtonPressedColor) {
        this.mDeleteButtonPressedColor = deleteButtonPressedColor;
        mCustomizationOptionsBundle.setDeleteButtonPressesColor(deleteButtonPressedColor);
        mAdapter.notifyDataSetChanged();
    }

    public int[] getCustomKeySet() {
        return mCustomKeySet;
    }

    public void setCustomKeySet(int[] customKeySet) {
        this.mCustomKeySet = customKeySet;

        if (mAdapter != null) {
            mAdapter.setKeyValues(customKeySet);
        }
    }

    public void enableLayoutShuffling() {
        this.mCustomKeySet = ShuffleArrayUtils.shuffle(DEFAULT_KEY_SET);

        if (mAdapter != null) {
            mAdapter.setKeyValues(mCustomKeySet);
        }
    }

    private void clearInternalPin() {
        mPin = "";
    }

    public void resetPinLockView() {

        clearInternalPin();

        mAdapter.setPinLength(mPin.length());
        mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);

        if (mIndicatorDots != null) {
            mIndicatorDots.updateDot(mPin.length());
        }
    }

    public boolean isIndicatorDotsAttached() {
        return mIndicatorDots != null;
    }

    public void attachIndicatorDots(IndicatorDots mIndicatorDots) {
        this.mIndicatorDots = mIndicatorDots;
    }
}
