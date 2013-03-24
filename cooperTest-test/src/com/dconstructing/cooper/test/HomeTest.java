/**
 * 
 */
package com.dconstructing.cooper.test;

import android.app.Fragment;
import android.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.dconstructing.cooper.MainActivity;

/**
 * @author dcox
 *
 */
public class HomeTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mActivity;
	//private View mAddButton;
	
	/**
	 * 
	 */
	public HomeTest() {
		super(MainActivity.class);
		// TODO Auto-generated constructor stub
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		setActivityInitialTouchMode(false); // In order for the test to use touch events
		
		mActivity = (MainActivity)getActivity();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * To test that the application is initialized correctly
	 */
	public void testPreconditions() {
		assertEquals("MainActivity", mActivity.TAG);
	}
	
	public void testSomething() {
		FragmentManager fragmentManager = mActivity.getFragmentManager();
		//fragmentManager.executePendingTransactions();
		
		Fragment connectionFragment = fragmentManager.findFragmentByTag("connections");
		assertNotNull(connectionFragment);
		
		Fragment addConnectionFragment = fragmentManager.findFragmentByTag("addConnection");
		assertNull(addConnectionFragment);

		final View addButton = mActivity.findViewById(com.dconstructing.cooper.R.id.add_connection);

		mActivity.runOnUiThread(
			new Runnable() {
				public void run() {
					addButton.requestFocus();
					addButton.callOnClick();
				}
			}
		);
		
		getInstrumentation().waitForIdleSync();
		
		addConnectionFragment = fragmentManager.findFragmentByTag("addConnection");
		assertNotNull(addConnectionFragment);
	}
	
}
