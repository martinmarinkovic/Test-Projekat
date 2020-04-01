package com.martinmarinkovic.myapplication

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.martinmarinkovic.myapplication.lockscreen.*
import com.martinmarinkovic.myapplication.lockscreen.ResourceUtils.Companion.getColor
import com.martinmarinkovic.myapplication.lockscreen.ResourceUtils.Companion.getDimensionInPx

class PinLockView : RecyclerView {

    companion object {
        private val DEFAULT_PIN_LENGTH = 4
        private val DEFAULT_KEY_SET = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
    }

    private var mPin = ""
    private var mPinLength = 0
    private var mHorizontalSpacing = 0
    private var mVerticalSpacing:Int = 0
    private var mTextColor = 0
    private var mDeleteButtonPressedColor:Int = 0
    private var mTextSize = 0
    private var mButtonSize:Int = 0
    private var mDeleteButtonSize:Int = 0
    private var mButtonBackgroundDrawable: Drawable? = null
    private var mDeleteButtonDrawable: Drawable? = null
    private var mShowDeleteButton = false
    private var mIndicatorDots: IndicatorDots? = null
    private var mAdapter: PinLockAdapter? = null
    private var mPinLockListener: PinLockListener? = null
    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private var mCustomKeySet: IntArray? = null

    private val mOnNumberClickListener: PinLockAdapter.OnNumberClickListener = object :
        PinLockAdapter.OnNumberClickListener {
        override fun onNumberClicked(keyValue: Int) {
            if (mPin.length < getPinLength()) {
                mPin += keyValue.toString()
                if (isIndicatorDotsAttached()) {
                    mIndicatorDots!!.updateDot(mPin.length)
                }
                if (mPin.length == 1) {
                    mAdapter!!.setPinLength(mPin.length)
                    mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
                }
                if (mPinLockListener != null) {
                    if (mPin.length == mPinLength) {
                        mPinLockListener!!.onComplete(mPin)
                    } else {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
                    }
                }
            } else {
                if (!isShowDeleteButton()) {
                    resetPinLockView()
                    mPin += keyValue.toString()
                    if (isIndicatorDotsAttached()) {
                        mIndicatorDots!!.updateDot(mPin.length)
                    }
                    if (mPinLockListener != null) {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
                    }
                } else {
                    if (mPinLockListener != null) {
                        mPinLockListener!!.onComplete(mPin)
                    }
                }
            }
        }
    }

    private val mOnDeleteClickListener: PinLockAdapter.OnDeleteClickListener = object :
        PinLockAdapter.OnDeleteClickListener {
        override fun onDeleteClicked() {
            if (mPin.length > 0) {
                mPin = mPin.substring(0, mPin.length - 1)
                if (isIndicatorDotsAttached()) {
                    mIndicatorDots!!.updateDot(mPin.length)
                }
                if (mPin.length == 0) {
                    mAdapter!!.setPinLength(mPin.length)
                    mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
                }
                if (mPinLockListener != null) {
                    if (mPin.length == 0) {
                        mPinLockListener!!.onEmpty()
                        clearInternalPin()
                    } else {
                        mPinLockListener!!.onPinChange(mPin.length, mPin)
                    }
                }
            } else {
                if (mPinLockListener != null) {
                    mPinLockListener!!.onEmpty()
                }
            }
        }

        override fun onDeleteLongClicked() {
            resetPinLockView()
            if (mPinLockListener != null) {
                mPinLockListener!!.onEmpty()
            }
        }
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attributeSet: AttributeSet?, defStyle: Int) {

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PinLockView)

        try {
            mPinLength = typedArray.getInt(R.styleable.PinLockView_pinLength, DEFAULT_PIN_LENGTH)
            mHorizontalSpacing = typedArray.getDimension(R.styleable.PinLockView_keypadHorizontalSpacing,
                getDimensionInPx(
                    context,
                    R.dimen.default_horizontal_spacing
                )
            ).toInt()
            mVerticalSpacing = typedArray.getDimension(R.styleable.PinLockView_keypadVerticalSpacing,
                getDimensionInPx(
                    context,
                    R.dimen.default_vertical_spacing
                )
            ).toInt()
            mTextColor = typedArray.getColor(R.styleable.PinLockView_keypadTextColor,
                getColor(
                    context,
                    R.color.white
                )
            )
            mTextSize = typedArray.getDimension(R.styleable.PinLockView_keypadTextSize,
                getDimensionInPx(
                    context,
                    R.dimen.default_text_size
                )
            ).toInt()
            mButtonSize = typedArray.getDimension(R.styleable.PinLockView_keypadButtonSize,
                getDimensionInPx(
                    context,
                    R.dimen.default_button_size
                )
            ).toInt()
            mDeleteButtonSize = typedArray.getDimension(R.styleable.PinLockView_keypadDeleteButtonSize,
                getDimensionInPx(
                    context,
                    R.dimen.default_delete_button_size
                )
            ).toInt()
            mButtonBackgroundDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadButtonBackgroundDrawable)
            mDeleteButtonDrawable = typedArray.getDrawable(R.styleable.PinLockView_keypadDeleteButtonDrawable)
            mShowDeleteButton = typedArray.getBoolean(R.styleable.PinLockView_keypadShowDeleteButton, true)
            mDeleteButtonPressedColor = typedArray.getColor(R.styleable.PinLockView_keypadDeleteButtonPressedColor,
                getColor(
                    context,
                    R.color.greyish
                )
            )
        } finally {
            typedArray.recycle()
        }

        mCustomizationOptionsBundle = CustomizationOptionsBundle()
        mCustomizationOptionsBundle!!.textColor = mTextColor
        mCustomizationOptionsBundle!!.textSize = mTextSize
        mCustomizationOptionsBundle!!.buttonSize = mButtonSize
        mCustomizationOptionsBundle!!.buttonBackgroundDrawable = mButtonBackgroundDrawable
        mCustomizationOptionsBundle!!.deleteButtonDrawable = mDeleteButtonDrawable
        mCustomizationOptionsBundle!!.deleteButtonSize = mDeleteButtonSize
        mCustomizationOptionsBundle!!.isShowDeleteButton = mShowDeleteButton
        mCustomizationOptionsBundle!!.deleteButtonPressesColor = mDeleteButtonPressedColor

        initView()
    }

    private fun initView() {
        layoutManager = LTRGridLayoutManager(context, 3)
        mAdapter = PinLockAdapter(context)
        mAdapter!!.setOnItemClickListener(mOnNumberClickListener)
        mAdapter!!.setOnDeleteClickListener(mOnDeleteClickListener)
        mAdapter!!.setCustomizationOptions(mCustomizationOptionsBundle)
        adapter = mAdapter
        addItemDecoration(ItemSpaceDecoration(mHorizontalSpacing, mVerticalSpacing, 3, false))
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    fun setPinLockListener(pinLockListener: PinLockListener?) {
        mPinLockListener = pinLockListener
    }

    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
        if (isIndicatorDotsAttached()) {
            mIndicatorDots!!.pinLength = pinLength
        }
    }

    fun getTextColor(): Int {
        return mTextColor
    }

    fun setTextColor(textColor: Int) {
        mTextColor = textColor
        mCustomizationOptionsBundle!!.textColor = textColor
        mAdapter!!.notifyDataSetChanged()
    }

    fun getTextSize(): Int {
        return mTextSize
    }

    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        mCustomizationOptionsBundle!!.textSize = textSize
        mAdapter!!.notifyDataSetChanged()
    }

    fun getButtonSize(): Int {
        return mButtonSize
    }

    fun setButtonSize(buttonSize: Int) {
        mButtonSize = buttonSize
        mCustomizationOptionsBundle!!.buttonSize = buttonSize
        mAdapter!!.notifyDataSetChanged()
    }

    fun getButtonBackgroundDrawable(): Drawable? {
        return mButtonBackgroundDrawable
    }

    fun setButtonBackgroundDrawable(buttonBackgroundDrawable: Drawable?) {
        mButtonBackgroundDrawable = buttonBackgroundDrawable
        mCustomizationOptionsBundle!!.buttonBackgroundDrawable = buttonBackgroundDrawable
        mAdapter!!.notifyDataSetChanged()
    }

    fun getDeleteButtonDrawable(): Drawable? {
        return mDeleteButtonDrawable
    }

    fun setDeleteButtonDrawable(deleteBackgroundDrawable: Drawable?) {
        mDeleteButtonDrawable = deleteBackgroundDrawable
        mCustomizationOptionsBundle!!.deleteButtonDrawable = deleteBackgroundDrawable
        mAdapter!!.notifyDataSetChanged()
    }

    fun getDeleteButtonSize(): Int {
        return mDeleteButtonSize
    }

    fun setDeleteButtonSize(deleteButtonSize: Int) {
        mDeleteButtonSize = deleteButtonSize
        mCustomizationOptionsBundle!!.deleteButtonSize = deleteButtonSize
        mAdapter!!.notifyDataSetChanged()
    }

    fun isShowDeleteButton(): Boolean {
        return mShowDeleteButton
    }

    fun setShowDeleteButton(showDeleteButton: Boolean) {
        mShowDeleteButton = showDeleteButton
        mCustomizationOptionsBundle!!.isShowDeleteButton = showDeleteButton
        mAdapter!!.notifyDataSetChanged()
    }

    fun getDeleteButtonPressedColor(): Int {
        return mDeleteButtonPressedColor
    }

    fun setDeleteButtonPressedColor(deleteButtonPressedColor: Int) {
        mDeleteButtonPressedColor = deleteButtonPressedColor
        mCustomizationOptionsBundle!!.deleteButtonPressesColor = deleteButtonPressedColor
        mAdapter!!.notifyDataSetChanged()
    }

    fun getCustomKeySet(): IntArray? {
        return mCustomKeySet
    }

    fun setCustomKeySet(customKeySet: IntArray?) {
        mCustomKeySet = customKeySet
        if (mAdapter != null) {
            mAdapter!!.setKeyValues(customKeySet!!)
        }
    }

    fun enableLayoutShuffling() {
        mCustomKeySet =
            ShuffleArrayUtils.shuffle(DEFAULT_KEY_SET)
        if (mAdapter != null) {
            mAdapter!!.setKeyValues(mCustomKeySet!!)
        }
    }

    private fun clearInternalPin() {
        mPin = ""
    }

    fun resetPinLockView() {
        clearInternalPin()
        mAdapter!!.setPinLength(mPin.length)
        mAdapter!!.notifyItemChanged(mAdapter!!.itemCount - 1)
        if (mIndicatorDots != null) {
            mIndicatorDots!!.updateDot(mPin.length)
        }
    }

    fun isIndicatorDotsAttached(): Boolean {
        return mIndicatorDots != null
    }

    fun attachIndicatorDots(mIndicatorDots: IndicatorDots?) {
        this.mIndicatorDots = mIndicatorDots
    }

}