package com.martinmarinkovic.myapplication.lockscreen

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.martinmarinkovic.myapplication.R


class PinLockAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mContext: Context = context
    private var mKeyValues: IntArray = getAdjustKeyValues(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))

    companion object {
        private val VIEW_TYPE_NUMBER = 0
        private val VIEW_TYPE_DELETE = 1
        private var mOnNumberClickListener: OnNumberClickListener? = null
        private var mOnDeleteClickListener: OnDeleteClickListener? = null
        private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
        private var mPinLength = 0
        private var mBackgrounds: IntArray  = intArrayOf(
            R.drawable.btn_1,
            R.drawable.btn_2,
            R.drawable.btn_3,
            R.drawable.btn_4,
            R.drawable.btn_5,
            R.drawable.btn_6,
            R.drawable.btn_7,
            R.drawable.btn_8,
            R.drawable.btn_9,
            R.drawable.btn_0,
            R.drawable.btn_0
        )
        private var mSelBackgrounds: IntArray  = intArrayOf(
            R.drawable.btn_1_sel,
            R.drawable.btn_2_sel,
            R.drawable.btn_3_sel,
            R.drawable.btn_4_sel,
            R.drawable.btn_5_sel,
            R.drawable.btn_6_sel,
            R.drawable.btn_7_sel,
            R.drawable.btn_8_sel,
            R.drawable.btn_9_sel,
            R.drawable.btn_0_sel,
            R.drawable.btn_0_sel
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == VIEW_TYPE_NUMBER) {
            val view = inflater.inflate(R.layout.layout_number_item, parent, false)
            viewHolder = NumberViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.layout_delete_item, parent, false)
            viewHolder = DeleteViewHolder(view)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_NUMBER) {
            val vh1: NumberViewHolder = holder as NumberViewHolder
            configureNumberButtonHolder(vh1, position)
        } else if (holder.itemViewType == VIEW_TYPE_DELETE) {
            val vh2: DeleteViewHolder = holder as DeleteViewHolder
            configureDeleteButtonHolder(vh2)
        }
    }

    private fun configureNumberButtonHolder(holder: NumberViewHolder, position: Int) {
        if (holder != null) {
            if (position == 9) {
                holder.mNumberButton.setVisibility(View.GONE)
                holder.mNumberButton.setTag(mKeyValues!![position])
            } else {
                holder.mNumberButton.setBackgroundResource(mBackgrounds!![position])
                //holder.mNumberButton.setText(String.valueOf(mKeyValues[position]));
                holder.mNumberButton.setVisibility(View.VISIBLE)
                holder.mNumberButton.setTag(mKeyValues!![position])
            }
            if (mCustomizationOptionsBundle != null) {
                holder.mNumberButton.setTextColor(mCustomizationOptionsBundle!!.textColor)
                if (mCustomizationOptionsBundle!!.buttonBackgroundDrawable != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        holder.mNumberButton.setBackgroundDrawable(
                            mCustomizationOptionsBundle!!.buttonBackgroundDrawable
                        )
                    } else {
                        holder.mNumberButton.setBackground(
                            mCustomizationOptionsBundle!!.buttonBackgroundDrawable
                        )
                    }
                }
                holder.mNumberButton.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    mCustomizationOptionsBundle!!.textSize.toFloat()
                )
                val params = LinearLayout.LayoutParams(
                    mCustomizationOptionsBundle!!.buttonSize,
                    mCustomizationOptionsBundle!!.buttonSize
                )
                holder.mNumberButton.setLayoutParams(params)
            }
        }
    }

    private fun configureDeleteButtonHolder(holder: DeleteViewHolder?) {
        if (holder != null) {
            if (mCustomizationOptionsBundle!!.isShowDeleteButton && mPinLength > 0) {
                holder.mDeleteButton.setVisibility(View.VISIBLE)

                /*if (mCustomizationOptionsBundle!!.deleteButtonDrawable != null) {
                    holder.mButtonImage.setImageDrawable(mCustomizationOptionsBundle!!.deleteButtonDrawable)
                }
                holder.mButtonImage.setColorFilter(
                    mCustomizationOptionsBundle!!.textColor,
                    PorterDuff.Mode.SRC_ATOP
                )
                val params = LinearLayout.LayoutParams(
                    mCustomizationOptionsBundle!!.deleteButtonSize,
                    mCustomizationOptionsBundle!!.deleteButtonSize
                )
                holder.mButtonImage.setLayoutParams(params)*/

            } else {
                holder.mDeleteButton.setVisibility(View.GONE)
            }
        }
    }

    override fun getItemCount(): Int {
        return 12
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            VIEW_TYPE_DELETE
        } else VIEW_TYPE_NUMBER
    }

    fun getPinLength(): Int {
        return mPinLength
    }

    fun setPinLength(pinLength: Int) {
        mPinLength = pinLength
    }

    fun getKeyValues(): IntArray? {
        return mKeyValues
    }

    fun setKeyValues(keyValues: IntArray) {
        mKeyValues = getAdjustKeyValues(keyValues)
        notifyDataSetChanged()
    }

    private fun getAdjustKeyValues(keyValues: IntArray): IntArray {
        val adjustedKeyValues = IntArray(keyValues.size + 1)
        for (i in keyValues.indices) {
            if (i < 9) {
                adjustedKeyValues[i] = keyValues[i]
            } else {
                adjustedKeyValues[i] = -1
                adjustedKeyValues[i + 1] = keyValues[i]
            }
        }
        return adjustedKeyValues
    }

    fun getOnItemClickListener(): OnNumberClickListener? {
        return mOnNumberClickListener
    }

    fun setOnItemClickListener(onNumberClickListener: OnNumberClickListener) {
        mOnNumberClickListener = onNumberClickListener
    }

    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener?) {
        mOnDeleteClickListener = onDeleteClickListener
    }

    fun getCustomizationOptions(): CustomizationOptionsBundle? {
        return mCustomizationOptionsBundle
    }

    fun setCustomizationOptions(customizationOptionsBundle: CustomizationOptionsBundle?) {
        mCustomizationOptionsBundle = customizationOptionsBundle
    }

    class NumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mNumberButton: Button = itemView.findViewById<View>(R.id.button) as Button
        init {
            mNumberButton.setOnClickListener { v ->
                mOnNumberClickListener?.onNumberClicked(v.tag as Int)
            }

            mNumberButton.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (v.tag as Int == 0) mNumberButton.setBackgroundResource(R.drawable.btn_0_sel) else mNumberButton.setBackgroundResource(
                        mSelBackgrounds!![v.tag as Int - 1]
                    )
                }
                if (event.action == MotionEvent.ACTION_UP) {
                    if (v.tag as Int == 0) mNumberButton.setBackgroundResource(R.drawable.btn_0) else mNumberButton.setBackgroundResource(
                        mBackgrounds!![v.tag as Int - 1]
                    )
                }
                false
            }
        }
    }

    class DeleteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //LinearLayout mDeleteButton;
        //ImageView mButtonImage;
        var mDeleteButton: Button = itemView.findViewById<View>(R.id.button) as Button

        init {
            //mButtonImage = (ImageView) itemView.findViewById(R.id.buttonImage);
            mDeleteButton.setBackgroundResource(R.drawable.btn_backspace)
            if (mCustomizationOptionsBundle!!.isShowDeleteButton && mPinLength > 0) {
                mDeleteButton.setOnClickListener {
                    mOnDeleteClickListener?.onDeleteClicked()
                }
                mDeleteButton.setOnLongClickListener {
                    mOnDeleteClickListener?.onDeleteLongClicked()
                    true
                }
                mDeleteButton.setOnTouchListener(object : OnTouchListener {
                    private var rect: Rect? = null
                    override fun onTouch(
                        v: View,
                        event: MotionEvent
                    ): Boolean {
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            /*mButtonImage.setColorFilter(mCustomizationOptionsBundle
                                    .getDeleteButtonPressesColor());*/
                            mDeleteButton.setBackgroundResource(R.drawable.btn_backspace_sel)
                            rect = Rect(
                                v.left,
                                v.top,
                                v.right,
                                v.bottom
                            )
                        }
                        if (event.action == MotionEvent.ACTION_UP) {
                            //mButtonImage.clearColorFilter();
                            mDeleteButton.setBackgroundResource(R.drawable.btn_backspace)
                        }
                        if (event.action == MotionEvent.ACTION_MOVE) {
                            if (!rect!!.contains(
                                    v.left + event.x.toInt(),
                                    v.top + event.y.toInt()
                                )
                            ) {
                                //mButtonImage.clearColorFilter();
                                mDeleteButton.setBackgroundResource(R.drawable.btn_backspace_sel)
                            }
                        }
                        return false
                    }
                })
            }
        }
    }

    interface OnNumberClickListener {
        fun onNumberClicked(keyValue: Int)
    }

    interface OnDeleteClickListener {
        fun onDeleteClicked()
        fun onDeleteLongClicked()
    }
}