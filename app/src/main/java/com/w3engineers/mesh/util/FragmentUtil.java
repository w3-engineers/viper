package com.w3engineers.mesh.util;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.w3engineers.mesh.ui.base.BaseFragment;


/*
*  ****************************************************************************
*  * Created by : Md. Azizul Islam on 12/13/2017 at 6:33 PM.
*  * Email : azizul@w3engineers.com
*  * 
*  * Last edited by : Md. Azizul Islam on 12/13/2017.
*  * 
*  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>  
*  ****************************************************************************
*/
public class FragmentUtil {
    private FragmentUtil() {
    }

    private static <T extends BaseFragment> T newFragment(Class<T> tClass) {
        try {
            return tClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T extends BaseFragment> T getFragment(BaseFragment parentFragment, Class<T> tClass) {
        T tFragment = (T) parentFragment.getChildFragmentManager().findFragmentByTag(tClass.getName());

        if (tFragment == null) {
            tFragment = newFragment(tClass);
        }

        return tFragment;
    }

    public static <T extends BaseFragment> void commitChildFragment(final BaseFragment parentFragment, final Class<T> tClass, final int parentResourceId) {
        commitChildFragment(parentFragment, tClass, parentResourceId, 0, 0);
    }

    public void commitFragment(int parentId, BaseFragment baseFragment) {
        AppCompatActivity activity = null;

        if (baseFragment != null) {
            activity = (AppCompatActivity) baseFragment.getActivity();
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(parentId, baseFragment, baseFragment.getClass().getName())
                .commit();
    }

    public static <T extends BaseFragment> void commitChildFragment(final BaseFragment parentFragment, final Class<T> tClass, final int parentResourceId, final int enter, final int exit) {

        final Runnable commitRunnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {

                Activity activity = null;

                if (parentFragment != null) {
                    activity = parentFragment.getActivity();
                }

                if (activity != null && !activity.isDestroyed()) {

                    BaseFragment currentChildFragment = getFragment(parentFragment, tClass);

                    FragmentTransaction transaction = parentFragment.getChildFragmentManager().beginTransaction();

                    transaction.setCustomAnimations(enter, exit);


                    transaction.replace(parentResourceId, currentChildFragment, tClass.getName())
                            .commitAllowingStateLoss();
                }
            }
        };

        HandlerUtil.postForeground(commitRunnable);
    }


    @Deprecated
    public static void add(Fragment fragment, FragmentManager fragmentManager, int containerViewId,
                           String tag) {
        fragmentManager.beginTransaction()
                .add(containerViewId, fragment, tag)
                .commit();
    }

    @Deprecated
    public static void add(Fragment fragment, FragmentManager fragmentManager,
                           int containerViewId) {
        //noinspection deprecation
        add(fragment, fragmentManager, containerViewId, null);
    }

    public static void add(Fragment fragment, FragmentActivity activity, int containerViewId) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), containerViewId);
    }

    public static void add(Fragment fragment, Fragment parentFragment, int containerViewId) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), containerViewId);
    }

    @Deprecated
    public static void add(Fragment fragment, FragmentManager fragmentManager, String tag) {
        // Pass 0 as in {@link android.support.v4.app.BackStackRecord#add(Fragment, String)}.
        //noinspection deprecation
        add(fragment, fragmentManager, 0, tag);
    }

    public static void add(Fragment fragment, FragmentActivity activity, String tag) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), tag);
    }

    public static void add(Fragment fragment, Fragment parentFragment, String tag) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), tag);
    }

    public static void add(Fragment fragment, FragmentActivity activity) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), null);
    }

    public static void add(Fragment fragment, Fragment parentFragment) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), null);
    }
}
