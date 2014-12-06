package com.norscan.levlertapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;


public class MainActivity extends FragmentActivity
{
    public Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab firstTab = actionBar.newTab().setText("Devices");
        ActionBar.Tab secondTab = actionBar.newTab().setText("Wifi Config");

        Fragment f_deviceList = new deviceList();
        Fragment f_smartConfig = new smartConfig();

        firstTab.setTabListener(new TabListener(f_deviceList));
        secondTab.setTabListener(new TabListener(f_smartConfig));

        actionBar.addTab(firstTab);
        actionBar.addTab(secondTab);
    }

    class TabListener implements ActionBar.TabListener
    {
        public Fragment fragment;

        public TabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.replace(R.id.fragment_area, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.action_refresh:
                MenuItem refreshItem = optionsMenu.findItem(R.id.action_refresh);
                refreshItem.setActionView(R.layout.actionbar_refresh);
                deviceList fragment = (deviceList) getFragmentManager().findFragmentById(R.id.fragment_area);
                fragment.retrieve = false;
                fragment.getDevices();
                break;
        }
        return true;
    }

    public Menu getMenu(){
        return this.optionsMenu;
    }
}
