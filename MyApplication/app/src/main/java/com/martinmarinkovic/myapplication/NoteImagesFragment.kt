package com.martinmarinkovic.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_note_images.*

class NoteImagesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_images, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sglm = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rv.layoutManager = sglm

        val imageList = ArrayList<String>()
        imageList.add("https://farm5.staticflickr.com/4403/36538794702_83fd8b63b7_c.jpg")
        imageList.add("https://farm5.staticflickr.com/4354/35684440714_434610d1d6_c.jpg")
        imageList.add("https://farm5.staticflickr.com/4301/35690634410_f5d0e312cb_c.jpg")
        imageList.add("https://farm4.staticflickr.com/3854/32890526884_7dc068fedd_c.jpg")
        imageList.add("https://farm8.staticflickr.com/7787/18143831616_a239c78056_c.jpg")
        imageList.add("https://farm9.staticflickr.com/8745/16657401480_57653ac8b0_c.jpg")
        imageList.add("https://farm3.staticflickr.com/2917/14144166232_44613c53c7_c.jpg")
        imageList.add("https://farm8.staticflickr.com/7453/13960410788_3dd02b7a02_c.jpg")
        imageList.add("https://farm1.staticflickr.com/920/29297133218_de38a7e4c8_c.jpg")
        imageList.add("https://farm2.staticflickr.com/1788/42989123072_6720c9608d_c.jpg")
        imageList.add("https://farm1.staticflickr.com/888/29062858008_89851766c9_c.jpg")
        imageList.add("https://farm2.staticflickr.com/1731/27940806257_8067196b41_c.jpg")
        imageList.add("https://farm1.staticflickr.com/884/42745897912_ff65398e38_c.jpg")
        imageList.add("https://farm2.staticflickr.com/1829/27971893037_1858467f9a_c.jpg")
        imageList.add("https://farm2.staticflickr.com/1822/41996470025_414452d7a0_c.jpg")
        imageList.add("https://farm2.staticflickr.com/1793/42937679651_3094ebb2b9_c.jpg")
        imageList.add("https://farm1.staticflickr.com/892/42078661914_b940d96992_c.jpg")
        val igka = NoteImageAdapter(activity!!, imageList)
        rv.adapter = igka
    }

}
