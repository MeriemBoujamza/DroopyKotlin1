package ma.ensaf.veryempty.activities

import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ma.ensaf.veryempty.R

import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Intent
import android.view.View
import ma.ensaf.veryempty.databinding.ActivityWelcomeScreenBinding

class ActivityWelcomeScreen : BaseActivity() {
    var binding: ActivityWelcomeScreenBinding? = null
    private var parent_view: View? = null
    private var introSliderPagerAdapter: IntroSliderPagerAdapter? = null
    private lateinit var layouts: IntArray
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome_screen)
        parent_view = findViewById(android.R.id.content)

        // layouts of all welcome sliders
        layouts = intArrayOf(
            R.layout.fragment_welcome_slide1,
            R.layout.fragment_welcome_slide2,
            R.layout.fragment_welcome_slide3
        )

        // the viewpager
        introSliderPagerAdapter = IntroSliderPagerAdapter()
        binding?.viewPager?.adapter = introSliderPagerAdapter
        binding?.viewPager?.addOnPageChangeListener(viewPagerPageChangeListener)
        binding?.tabDots?.setupWithViewPager(binding?.viewPager, true)

        // go to the register activity
        binding?.finishTextView?.setOnClickListener {
            ActivityRegister.start(
                activityContext
            )
        }
    }

    //  viewpager change listener
    var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            if (position >= layouts.size - 1) {
                binding!!.finishTextView.visibility = View.VISIBLE
            } else {
                binding!!.finishTextView.visibility = View.GONE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * View pager adapter
     */
    inner class IntroSliderPagerAdapter internal constructor() : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater!!.inflate(layouts[position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ActivityWelcomeScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }
    }
}