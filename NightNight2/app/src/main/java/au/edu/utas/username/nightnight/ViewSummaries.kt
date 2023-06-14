package au.edu.utas.username.nightnight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.setPadding
import androidx.viewpager.widget.ViewPager
import au.edu.utas.username.nightnight.adapter.SummaryAdapter
import au.edu.utas.username.nightnight.databinding.ActivityViewSummariesBinding

class ViewSummaries : AppCompatActivity() {
    private lateinit var ui: ActivityViewSummariesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ui binding
        ui = ActivityViewSummariesBinding.inflate(layoutInflater)
        setContentView(ui.root)
        //the current tab of the view pager (optionsContainer)
        var selectedTab = 0

        //the last tab chosen
        var lastTab = 0

        //list of category titles
        val titleList = mutableListOf<String>()
        titleList.add(0, ui.listFeedingIcon2.contentDescription.toString())
        titleList.add(1, ui.listNappyIcon2.contentDescription.toString())
        titleList.add(2, ui.listSleepIcon2.contentDescription.toString())
        titleList.add(3, ui.listActivityIcon2.contentDescription.toString())

        //list of icon
        val iconList = mutableListOf<ImageView>()
        iconList.add(0, ui.listFeedingIcon2)
        iconList.add(1, ui.listNappyIcon2)
        iconList.add(2, ui.listSleepIcon2)
        iconList.add(3, ui.listActivityIcon2)

        if(intent.getStringExtra("currentList") == "feeding")
        {
            selectedTab = 0
            lastTab = 0
        }

        if(intent.getStringExtra("currentList") == "nappy")
        {
            selectedTab = 1
            lastTab = 1

        }

        if(intent.getStringExtra("currentList") == "sleep")
        {
            selectedTab = 2
            lastTab = 2
        }

        val viewPagerAdapter = SummaryAdapter(this, supportFragmentManager, TOTAL_ICON)
        ui.summaryContainer.adapter = viewPagerAdapter

        ui.listBackButton2.setOnClickListener {
            finish()
        }

        //events happening when users click on an icon
        ui.listFeedingIcon2.setOnClickListener{
            if (selectedTab != 0)
            {
                selectedTab = 0
                ui.summaryContainer.setCurrentItem(selectedTab,true)
            }
        }

        ui.listNappyIcon2.setOnClickListener{
            if (selectedTab != 1)
            {
                selectedTab = 1
                ui.summaryContainer.setCurrentItem(selectedTab,true)
            }
        }

        ui.listSleepIcon2.setOnClickListener{
            if (selectedTab != 2)
            {
                selectedTab = 2
                ui.summaryContainer.setCurrentItem(selectedTab, true)
            }
        }

        ui.listActivityIcon2.setOnClickListener{
            if (selectedTab != 3)
            {
                selectedTab = 3
                ui.summaryContainer.setCurrentItem(selectedTab, true)
            }
        }

        iconList[selectedTab].setBackgroundResource(R.drawable.rectangle_white_border)
        iconList[selectedTab].setPadding(10)
        ui.listTitle2.text = titleList[selectedTab]
        ui.summaryContainer.setCurrentItem(selectedTab, true)

        //manage icons when users scroll tabs
        ui.summaryContainer.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}
            //set title for each tab
            override fun onPageSelected(position: Int) {
                //set the new title and appearance when an icon/tab is selected
                selectedTab = position
                ui.listTitle2.text = titleList[position]
                iconList[position].setBackgroundResource(R.drawable.rectangle_white_border)
                iconList[position].setPadding(10)

                //undo the UI settings of the last selected icon
                iconList[lastTab].setBackgroundColor(getColor(R.color.blue_primary))
                iconList[lastTab].setPadding(0)
                lastTab = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
}