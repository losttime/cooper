package com.dconstructing.cooper.test;

import android.test.ActivityInstrumentationTestCase2;

import com.dconstructing.cooper.MainActivity;

/**
 * Created by dcox on 10/10/13.
 */
public class BasicTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public BasicTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testStartFirstActivity() {
        assertNotNull(getActivity());
    }

//    public void testStartSecondActivity() throws Exception {
//
//        // add monitor to check for the second activity
//        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(MainActivity.class.getName(), null, false);
//
//        // find button and click it
//        Button view = (Button) activity.findViewById(R.id.button1);
//        TouchUtils.clickView(this, view);
//
//        // To click on a click, e.g. in a listview
//        // listView.getChildAt(0);
//
//        // Wait 2 seconds for the start of the activity
//        MainActivity startedActivity = (MainActivity) monitor
//                .waitForActivityWithTimeout(2000);
//        assertNotNull(startedActivity);
//
//        // Search for the textView
//        TextView textView = (TextView) startedActivity.findViewById(R.id.resultText);
//
//        // check that the TextView is on the screen
//        ViewAsserts.assertOnScreen(startedActivity.getWindow().getDecorView(),
//                textView);
//        // Validate the text on the TextView
//        assertEquals("Text incorrect", "Started", textView.getText().toString());
//
//        // Press back and click again
//        this.sendKeys(KeyEvent.KEYCODE_BACK);
//        TouchUtils.clickView(this, view);
//
//    }
}
